# 📋 Day 8 任务清单：Redis 应用实战 + SQL 性能优化

## 任务概览

| 序号 | 任务 | 优先级 | 说明 |
|:--:|:---|:--:|:---|
| 1 | RedisService 增强 | 🔴 高 | 添加 incr/decr/setNx 方法 |
| 2 | 浏览量 Redis 优化 | 🔴 高 | Redis INCR 替代直接 UPDATE，定时批量同步 |
| 3 | 上传限流改 Redis | 🟡 中 | 替换 ConcurrentHashMap，用 Redis 原子操作 |
| 4 | SQL 子查询优化（上） | 🔴 高 | note 表增加冗余字段，修改查询 |
| 5 | SQL 子查询优化（下） | 🔴 高 | Service 层改用冗余字段，同步 init_db.sql |
| 6 | 点赞/收藏幂等性保护 | 🔴 高 | DuplicateKeyException 静默处理 |
| 7 | 数据库索引优化 | 🟡 中 | 添加复合索引提升查询性能 |

## 当前已有基础

- `RedisService`：只有基础的 set/get/hasKey/delete/expire 方法，缺少 incr/setNx
- `NoteDetailServiceImpl.getNoteDetail()`：每次访问直接 `UPDATE note SET view_count = view_count + 1`
- `WxUserServiceImpl`：上传限流用 `ConcurrentHashMap<String, Integer>`，有 `clearUploadLimitMap()` 定时清理
- `InteractionServiceImpl.toggle()`：有 `DuplicateKeyException` 兜底，但抛 ServiceException 体验不好
- `NoteMapper.xml`：4 个查询都有 likeCount/commentCount 子查询
- `Note` 实体：没有 like_count、comment_count、summary 冗余字段

## 需要优化的问题清单

| 序号 | 问题 | 当前状态 | 目标 |
|:--:|:---|:---|:---|
| 1 | RedisService 缺少 incr/setNx | 只有基础操作 | 添加原子自增方法 |
| 2 | 浏览量直接写 DB | 每次访问 UPDATE | Redis INCR + 定时同步 |
| 3 | 上传限流用 ConcurrentHashMap | JVM 内存，重启丢失 | Redis INCR + 自动过期 |
| 4 | 列表查询有子查询 | 每条笔记 2 个子查询 | 冗余字段替代 |
| 5 | 点赞/收藏并发处理粗糙 | DuplicateKeyException 抛异常 | 静默处理 |
| 6 | 缺少复合索引 | 只有主键和唯一索引 | 添加业务索引 |

---

# 📝 Day 8 提示词

## 提示词 1：RedisService 增强

```
在 RedisService 中新增 incr/setNx/decr 方法。

文件：common/src/main/java/com/littlewin/common/redis/RedisService.java

在现有方法后添加：

/**
 * 原子自增 — 返回自增后的值
 */
public Long incr(String key, long delta) {
    return stringRedisTemplate.opsForValue().increment(key, delta);
}

/**
 * 原子自增 + 设置过期时间（仅当 key 首次创建时设置 TTL）
 */
public Long incr(String key, long delta, long timeout, TimeUnit unit) {
    Long result = stringRedisTemplate.opsForValue().increment(key, delta);
    if (result != null && result == delta) {
        stringRedisTemplate.expire(key, timeout, unit);
    }
    return result;
}

/**
 * 原子自减
 */
public Long decr(String key, long delta) {
    return stringRedisTemplate.opsForValue().increment(key, -delta);
}

/**
 * SETNX — 仅当 key 不存在时设置值，返回是否设置成功
 */
public boolean setNx(String key, String value, long timeout, TimeUnit unit) {
    return Boolean.TRUE.equals(
        stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit)
    );
}

验证：启动应用，测试 incr/setNx 方法正常工作。
```

---

## 提示词 2：浏览量 Redis 优化

```
浏览量改用 Redis INCR，定时批量同步到 DB。

=== 1. RedisKeyConstants 新增常量 ===

文件：common/src/main/java/com/littlewin/common/constants/RedisKeyConstants.java

添加：
/** 笔记浏览量 Redis key 前缀 */
public static final String NOTE_VIEWS = "note:views:";

=== 2. NoteDetailServiceImpl 改用 Redis ===

文件：note/src/main/java/com/littlewin/note/service/impl/NoteDetailServiceImpl.java

2.1 添加 @Slf4j 注解（用于 Redis 降级日志）

2.2 添加 RedisService 依赖（与现有风格一致，用构造器注入）：
private final RedisService redisService;

2.3 修改 getNoteDetail 方法：

@Override
public NoteDetailVO getNoteDetail(Long noteId, Long userId) {
    NoteDetailVO detail = noteMapper.selectNoteDetailById(noteId, userId);
    if (detail == null) {
        throw new ServiceException("笔记不存在或无权限访问");
    }
    
    // 浏览量写入 Redis，失败降级走 DB
    try {
        redisService.incr(RedisKeyConstants.NOTE_VIEWS + noteId, 1);
    } catch (Exception e) {
        log.warn("Redis 浏览量写入失败，降级走 DB: noteId={}", noteId, e);
        noteMapper.incrementViewCount(noteId);
    }
    
    // 查询标签（原有逻辑保持不变）
    List<NoteTagRel> relList = noteTagRelMapper.selectList(...);
    ...
    return detail;
}

=== 3. 新建定时任务：同步浏览量到 DB ===

新建文件：note/src/main/java/com/littlewin/note/task/NoteViewSyncTask.java

@Slf4j
@Component
@RequiredArgsConstructor
public class NoteViewSyncTask {

    private final StringRedisTemplate stringRedisTemplate;
    private final NoteMapper noteMapper;

    // Lua 脚本：GETSET 原子操作（重置为 0，保留 key，避免 key 重建问题）
    private static final String LUA_GETSET =
        "local val = redis.call('GETSET', KEYS[1], '0') " +
        "return val";

    @Scheduled(fixedRate = 300000) // 每 5 分钟
    public void syncViewCountToDb() {
        // 1. SCAN 遍历所有 note:views:* key（用 RedisCallback 简化代码）
        Set<String> keys = stringRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> result = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions().match("note:views:*").count(100).build();
            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                result.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }
            return result;
        });
        if (keys == null || keys.isEmpty()) return;

        // 2. 逐个取出计数并重置为 0（GETSET 原子操作）
        DefaultRedisScript<String> script = new DefaultRedisScript<>(LUA_GETSET, String.class);
        Map<Long, Long> viewCountMap = new HashMap<>();
        for (String key : keys) {
            String noteIdStr = key.substring("note:views:".length());
            String val = stringRedisTemplate.execute(script, List.of(key));
            if (val != null) {
                Long count = Long.parseLong(val);
                if (count > 0) {
                    viewCountMap.put(Long.parseLong(noteIdStr), count);
                }
            }
        }

        // 3. 批量 UPDATE
        for (Map.Entry<Long, Long> entry : viewCountMap.entrySet()) {
            noteMapper.addViewCount(entry.getKey(), entry.getValue());
        }
    }
}

=== 4. NoteMapper 新增方法 ===

文件：note/src/main/java/com/littlewin/note/mapper/NoteMapper.java

void addViewCount(@Param("noteId") Long noteId, @Param("count") Long count);

文件：note/src/main/resources/com/littlewin/note/mapper/NoteMapper.xml

<update id="addViewCount">
    UPDATE note SET view_count = view_count + #{count} WHERE note_id = #{noteId} AND del_flag = 0
</update>

=== 5. 启动类开启定时任务 ===

文件：admin/src/main/java/com/littlewin/admin/SmartNoteApplication.java

添加 @EnableScheduling 注解（当前已有 @EnableAsync，两者不冲突）。

注意：当前 NoteDetailVO 没有 viewCount 字段，详情页不展示浏览量。如果后续需要展示实时浏览量，需在 NoteDetailVO 添加 viewCount 字段，并在 getNoteDetail 中从 Redis 读取合并（DB 值 + Redis 增量）。

验证：
1. 访问笔记详情 → Redis key note:views:{noteId} 值为 1
2. 再次访问 → Redis 值变为 2
3. 等待 5 分钟（或手动触发定时任务）→ 数据库 view_count 更新，Redis 值重置为 0
```

---

## 提示词 3：上传限流改 Redis

```
上传限流从 ConcurrentHashMap 改为 Redis。

=== 1. RedisKeyConstants 新增常量 ===

文件：common/src/main/java/com/littlewin/common/constants/RedisKeyConstants.java

添加：
/** 上传限流 key 前缀 */
public static final String UPLOAD_LIMIT = "upload:limit:";

=== 2. WxUserServiceImpl 改用 Redis ===

文件：system/src/main/java/com/littlewin/system/service/impl/WxUserServiceImpl.java

改动：
1. 删除 private static final Map<String, Integer> uploadLimitMap = new ConcurrentHashMap<>();
2. 删除 clearUploadLimitMap() 方法及其 @Scheduled 注解（改用 Redis 后无需手动清理）
3. 修改 checkUploadLimit() 方法：

private void checkUploadLimit() {
    Long userId = getCurrentUserId();
    String key = RedisKeyConstants.UPLOAD_LIMIT + userId;
    int maxUploadsPerHour = 10; // 限流阈值，可根据业务需求自行调整

    // Redis INCR + 自动过期（首次自增时设置 1 小时 TTL）
    Long count = redisService.incr(key, 1, 1, TimeUnit.HOURS);
    if (count > maxUploadsPerHour) {
        throw new ServiceException("上传过于频繁，请稍后再试");
    }
}

说明：
- 限流阈值从 20 调整为 10（每小时），可根据业务需求自行调整
- 限流维度从 IP 改为 userId，因为上传接口必须登录，userId 比 IP 更精确且不受 NAT 影响

验证：
1. 连续上传头像 → 第 11 次时提示"上传过于频繁"
2. Redis key upload:limit:{userId} TTL 约 1 小时
```

---

## 提示词 4：SQL 子查询优化（上）— 数据库 + 实体 + Mapper

```
note 表增加冗余字段，修改 Mapper 查询去掉子查询。

=== 1. 数据库增加冗余字段 ===

执行 SQL：
ALTER TABLE note 
ADD COLUMN like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数（冗余）',
ADD COLUMN comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数（冗余）',
ADD COLUMN summary VARCHAR(500) DEFAULT NULL COMMENT '内容摘要（冗余，前200字）';

=== 2. Note 实体增加字段 ===

文件：note/src/main/java/com/littlewin/note/domain/entity/Note.java

添加：
@TableField("like_count")
private Integer likeCount;

@TableField("comment_count")
private Integer commentCount;

private String summary;

=== 3. NoteMapper 新增更新方法 ===

文件：note/src/main/java/com/littlewin/note/mapper/NoteMapper.java

void addLikeCount(@Param("noteId") Long noteId, @Param("count") Long count);
void addCommentCount(@Param("noteId") Long noteId, @Param("count") Long count);

文件：note/src/main/resources/com/littlewin/note/mapper/NoteMapper.xml

<update id="addLikeCount">
    UPDATE note SET like_count = GREATEST(like_count + #{count}, 0) WHERE note_id = #{noteId} AND del_flag = 0
</update>

<update id="addCommentCount">
    UPDATE note SET comment_count = GREATEST(comment_count + #{count}, 0) WHERE note_id = #{noteId} AND del_flag = 0
</update>

=== 4. 修改 selectNoteListPage ===

文件：note/src/main/resources/com/littlewin/note/mapper/NoteMapper.xml

将 likeCount 和 commentCount 子查询改为直接读冗余字段（用 IFNULL 兼容旧数据）：
n.like_count AS likeCount,
n.comment_count AS commentCount,
IFNULL(n.summary, LEFT(n.content, 200)) AS summary,

=== 5. 修改 selectAdminNotePage ===

同上，去掉子查询，改用冗余字段。

=== 6. 修改 selectNoteDetailById ===

注意：
1. NoteDetailVO 字段名为 likes 和 comments，别名需匹配：n.like_count AS likes, n.comment_count AS comments
2. isLiked 字段来自 LEFT JOIN note_reaction cur，这个 JOIN 必须保留，只替换 likes 和 comments 两个子查询为冗余字段

=== 7. 修改 selectAdminNoteDetailById ===

同上。

=== 8. 修改 updateNoteById，增加 summary 更新 ===

<update id="updateNoteById">
    UPDATE note
    <set>
        <if test="dto.title != null and dto.title != ''">title = #{dto.title},</if>
        <if test="dto.content != null and dto.content != ''">
            content = #{dto.content},
            summary = LEFT(#{dto.content}, 200),
        </if>
        category_id = #{dto.categoryId},
        <if test="dto.isPublic != null">is_public = #{dto.isPublic},</if>
        update_time = NOW()
    </set>
    WHERE note_id = #{noteId} AND user_id = #{userId} AND del_flag = 0
</update>

=== 9. 同步更新 init_db.sql ===

文件：docs/sql/init_db.sql

找到 /* 2. 笔记主表 */ 的 CREATE TABLE 语句，新增三个字段：
like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数（冗余）',
comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数（冗余）',
summary VARCHAR(500) DEFAULT NULL COMMENT '内容摘要（冗余，前200字）',

status 注释补充 3 下架。

=== 9. 数据初始化（必须，如有存量数据） ===

⚠️ 如果项目已有测试数据，必须执行以下初始化 SQL，否则冗余字段数据不准确：

UPDATE note n SET
    like_count = (SELECT COUNT(*) FROM note_reaction nr WHERE nr.note_id = n.note_id AND nr.attitude = 1),
    comment_count = (SELECT COUNT(*) FROM note_comment nc WHERE nc.note_id = n.note_id AND nc.del_flag = 0),
    summary = LEFT(n.content, 200);

验证：
1. 创建笔记 → 数据库 summary 字段有值
2. 查询列表 → SQL 无子查询，IFNULL 兼容旧数据
3. 执行数据初始化后，旧数据的 like_count/comment_count/summary 填充正确
```

---

## 提示词 5：SQL 子查询优化（下）— Service 层改用冗余字段

```
Service 层改用冗余字段，更新冗余字段计数。

⚠️ 本提示词必须在提示词 4 之后执行。

=== 1. NoteDetailServiceImpl 创建笔记时维护冗余字段 ===

文件：note/src/main/java/com/littlewin/note/service/impl/NoteDetailServiceImpl.java

修改 createNote 方法，构建 Note 实体时设置：
note.setSummary(dto.getContent().trim().length() > 200 
    ? dto.getContent().trim().substring(0, 200) 
    : dto.getContent().trim());
note.setLikeCount(0);
note.setCommentCount(0);

=== 2. InteractionServiceImpl 改用冗余字段 ===

文件：note/src/main/java/com/littlewin/note/service/impl/InteractionServiceImpl.java

注入 NoteMapper（构造器注入）：
private final NoteMapper noteMapper;

2.1 在 toggleLike 方法中，三种情况都需要更新冗余字段：

if (existing == null) {
    // 新插入记录
    noteReactionMapper.insert(reaction);
    noteMapper.addLikeCount(noteId, 1L);  // 点赞 +1
    isLiked = true;
} else if (existing.getAttitude() != null && existing.getAttitude() == 1) {
    // 已点赞 → 取消
    existing.setAttitude(0);
    noteReactionMapper.updateById(existing);
    noteMapper.addLikeCount(noteId, -1L);  // 点赞 -1
    isLiked = false;
} else {
    // 未点赞 → 点赞
    existing.setAttitude(1);
    noteReactionMapper.updateById(existing);
    noteMapper.addLikeCount(noteId, 1L);  // 点赞 +1
    isLiked = true;
}

2.2 将方法末尾的 COUNT 查询改为读冗余字段：
// 原代码：
Long likeCount = noteReactionMapper.countLikesByNoteId(noteId);

// 改为：
Note note = noteMapper.selectById(noteId);
int likeCount = note != null ? note.getLikeCount() : 0;
int collectCount = noteReactionMapper.countCollectsByNoteId(noteId).intValue(); // 收藏数暂无冗余字段，仍用 COUNT

2.2 将方法末尾的 COUNT 查询改为读冗余字段：
// 原代码：
Long likeCount = noteReactionMapper.countLikesByNoteId(noteId);
Long collectCount = noteReactionMapper.countCollectsByNoteId(noteId);

// 改为：
Note note = noteMapper.selectById(noteId);
int likeCount = note != null ? note.getLikeCount() : 0;
int collectCount = noteReactionMapper.countCollectsByNoteId(noteId).intValue(); // 收藏数暂无冗余字段，仍用 COUNT

说明：note 表只有 like_count 冗余字段，没有 collect_count。收藏数仍需用 COUNT 查询。

=== 3. CommentServiceImpl 发表/删除时更新 comment_count ===

文件：note/src/main/java/com/littlewin/note/service/impl/CommentServiceImpl.java

NoteMapper 已通过构造器注入，直接添加方法调用：

在 createComment 方法中（insert 之后）：
noteMapper.addCommentCount(noteId, 1L);

在 deleteComment 方法中（逻辑删除之后）：
noteMapper.addCommentCount(noteId, -1L);

=== 4. 数据初始化（已在提示词 4 中完成） ===

数据初始化 SQL 已在提示词 4 的步骤 9 中执行，此处无需重复。

验证：
1. 点赞笔记 → like_count + 1
2. 发表评论 → comment_count + 1
3. 取消点赞 → like_count - 1（不小于 0）
```

---

## 提示词 6：点赞/收藏幂等性保护

```
点赞/收藏 DuplicateKeyException 静默处理。

⚠️ 本提示词必须在提示词 5 之后执行，因为需要读取 like_count 冗余字段。

文件：note/src/main/java/com/littlewin/note/service/impl/InteractionServiceImpl.java

=== 1. 添加 buildResultFromExisting 方法 ===

private InteractionResultVO buildResultFromExisting(NoteReaction existing, Long noteId) {
    boolean isLiked = existing != null && existing.getAttitude() != null && existing.getAttitude() == 1;
    boolean isCollected = existing != null && existing.getIsFavorite() != null && existing.getIsFavorite() == 1;
    // 读取冗余字段（提示词 5 已完成）
    Note note = noteMapper.selectById(noteId);
    int likeCount = note != null ? note.getLikeCount() : 0;
    int collectCount = noteReactionMapper.countCollectsByNoteId(noteId).intValue();
    return InteractionResultVO.builder()
            .isLiked(isLiked)
            .isCollected(isCollected)
            .likeCount(likeCount)
            .collectCount(collectCount)
            .build();
}

=== 2. 修改 toggleLike 方法 ===

将 DuplicateKeyException 处理从抛异常改为静默返回：

try {
    noteReactionMapper.insert(reaction);
} catch (DuplicateKeyException e) {
    // 并发重复插入，幂等处理：查一次当前状态返回
    existing = noteReactionMapper.selectOne(
        new LambdaQueryWrapper<NoteReaction>()
            .eq(NoteReaction::getNoteId, noteId)
            .eq(NoteReaction::getUserId, userId)
    );
    return buildResultFromExisting(existing, noteId);
}

=== 3. toggleCollect 方法同理修改 ===

验证：
1. 正常点赞 → 成功，isLiked=true
2. 快速双击 → 第二次请求静默返回当前状态，不报错
```

---

## 提示词 7：数据库索引优化

```
添加复合索引提升查询性能。

=== 1. 执行索引添加 SQL ===

-- 笔记表
CREATE INDEX idx_note_public_status ON note(is_public, status, del_flag, create_time);
CREATE INDEX idx_note_user_status ON note(user_id, status, del_flag);

-- 评论表
CREATE INDEX idx_comment_note ON note_comment(note_id, del_flag, create_time);

-- 互动表
CREATE INDEX idx_reaction_note_attitude ON note_reaction(note_id, attitude);
CREATE INDEX idx_reaction_user_favorite ON note_reaction(user_id, is_favorite);
CREATE INDEX idx_reaction_user_attitude ON note_reaction(user_id, attitude);

-- 日志表
CREATE INDEX idx_log_operation_time ON sys_log_operation(create_time);
CREATE INDEX idx_log_behavior_time ON sys_log_behavior(create_time);

=== 2. 同步更新 init_db.sql ===

文件：docs/sql/init_db.sql

在各表的 CREATE TABLE 中添加对应索引：

note 表：在 INDEX idx_status (status) 后面添加
    INDEX idx_note_public_status (is_public, status, del_flag, create_time),
    INDEX idx_note_user_status (user_id, status, del_flag)

note_comment 表：在 INDEX idx_user_id (user_id) 后面添加
    INDEX idx_comment_note (note_id, del_flag, create_time)

note_reaction 表：在 UNIQUE KEY uk_note_user (note_id, user_id) 后面添加
    INDEX idx_reaction_note_attitude (note_id, attitude),
    INDEX idx_reaction_user_favorite (user_id, is_favorite),
    INDEX idx_reaction_user_attitude (user_id, attitude)

sys_log_operation 表：在 INDEX idx_module (module) 后面添加
    INDEX idx_log_operation_time (create_time)

sys_log_behavior 表：在 INDEX idx_user_id (user_id) 后面添加
    INDEX idx_log_behavior_time (create_time)

注意：添加 like_count/comment_count 冗余字段后，列表查询不再需要 note_reaction 子查询，但 InteractionServiceImpl 中的 countLikesByNoteId/countCollectsByNoteId/batchCountByNoteIds 仍需 idx_reaction_note_attitude 索引，请保留。

验证：
SHOW INDEX FROM note;
EXPLAIN SELECT * FROM note WHERE is_public = 1 AND status = 1 AND del_flag = 0 ORDER BY create_time DESC;
```

---

# ⏱️ Day 8 执行顺序

| 顺序 | 提示词 | 前置依赖 | 预计耗时 |
|:--:|:---|:--:|:--:|
| 1️⃣ | 提示词 1：RedisService 增强 | 无 | 15 分钟 |
| 2️⃣ | 提示词 2：浏览量 Redis 优化 | 提示词 1 | 45 分钟 |
| 3️⃣ | 提示词 3：上传限流改 Redis | 提示词 1 | 20 分钟 |
| 4️⃣ | 提示词 4：SQL 子查询优化（上） | 无 | 45 分钟 |
| 5️⃣ | 提示词 5：SQL 子查询优化（下） | 提示词 4 | 30 分钟 |
| 6️⃣ | 提示词 6：点赞/收藏幂等性保护 | 提示词 5 | 20 分钟 |
| 7️⃣ | 提示词 7：数据库索引优化 | 无 | 15 分钟 |

> 提示词 1 是基础，必须先完成。提示词 2-3 依赖 RedisService 新方法。提示词 4-5 是 SQL 优化的上下两部分，必须按顺序执行。提示词 6 依赖冗余字段，必须在提示词 5 之后。提示词 7 独立可随时执行。

---

# 🐛 Day 8 Debug 提示词

## Debug 0：NoteViewSyncTask 定时任务缺少外层 try-catch

```
定时任务 syncViewCountToDb 没有外层 try-catch，Redis 连接失败时会抛出异常。

文件：note/src/main/java/com/littlewin/note/task/NoteViewSyncTask.java

修复：在方法最外层添加 try-catch，捕获 Redis 连接失败异常，静默处理：

@Scheduled(fixedRate = 300000)
public void syncViewCountToDb() {
    try {
        // ... 原有逻辑 ...
    } catch (Exception e) {
        log.warn("浏览量同步任务执行失败（Redis 可能未连接）: {}", e.getMessage());
    }
}

状态：已修复。
```

---

## Debug 1：NoteViewSyncTask RedisCallback import 路径错误

```
RedisCallback 的 import 路径错误，导致编译失败。

文件：note/src/main/java/com/littlewin/note/task/NoteViewSyncTask.java

错误 import：
import org.springframework.data.redis.connection.RedisCallback;

正确 import：
import org.springframework.data.redis.core.RedisCallback;

说明：RedisCallback 在 Spring Data Redis 3.x 中位于 org.springframework.data.redis.core 包下。
```

---

## Debug 2：NoteDetailServiceImpl 缺少 import 语句（已修复）

```
NoteDetailServiceImpl.java 缺少 3 个 import 语句，会导致编译失败。

文件：note/src/main/java/com/littlewin/note/service/impl/NoteDetailServiceImpl.java

需要添加：
import com.littlewin.common.constants.RedisKeyConstants;
import com.littlewin.common.redis.RedisService;
import lombok.extern.slf4j.Slf4j;

状态：已修复，import 语句已存在。
```
