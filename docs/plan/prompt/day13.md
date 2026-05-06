# 📋 Day 13 任务清单：AI 摘要 + 用量监控 + 配额管控

## 任务概览

| 序号 | 任务 | 优先级 | 说明 |
|:--:|:---|:--:|:---|
| 1a | 数据库设计 + 实体类 | 🔴 高 | 新建表 + 实体类 |
| 1b | Mapper 接口 + VO + DTO | 🔴 高 | Mapper 接口 + VO + QueryDTO |
| 1c | Mapper XML + 自定义方法 | 🔴 高 | SQL 映射 + 自定义查询方法 |
| 2a | LangChain4j 配置 + 配额服务 | 🔴 高 | 依赖 + 配置 + 配额校验 |
| 2b | 摘要服务 + 接口 + 小程序 | 🔴 高 | 摘要生成 + Controller + note.js |
| 3 | 管理端监控接口 | 🔴 高 | 日志查询 + 配额管理 + 统计 |
| 4 | Web 监控页面改造 | 🟡 中 | AI 监控页对接真实数据 |
| 5 | 小程序摘要卡片 | 🟡 中 | 笔记详情页摘要展示 |

## 当前已有基础

- `note_ai_summary` 表：note_id, summary, keywords, model_name, create_time（只存摘要结果，无 token 追踪）
- `NoteAiSummary.java`：对应实体
- `aiLog.ts`：前端 API 占位，调 `/admin/ai/logs`
- `monitor/aiLog/index.vue`：ProTable 占位页面
- 菜单 5020：AI 监控（/monitor/aiLog）
- 无 LangChain4j 依赖，无 AI 服务实现
- 无 token 追踪、无用户配额管控

## 需要实现的内容

| 序号 | 问题 | 当前状态 | 目标 |
|:--:|:---|:---|:---|
| 1 | 无 AI 调用日志表 | 只有摘要结果表 | 新建 ai_usage_log 记录每次调用的 token 用量 |
| 2 | 无用户配额表 | 无 | 新建 ai_user_quota 管理每个用户的使用上限 |
| 3 | 无 LangChain4j 集成 | 无 | pom 引入依赖，配置 DeepSeek API |
| 4 | 无摘要生成服务 | 无 | 实现 AI 摘要生成 + token 追踪 + 配额校验 |
| 5 | 无管理端 AI 接口 | 只有占位 API | 实现日志查询 + 配额管理 + 统计接口 |
| 6 | Web 监控页是占位 | 无数据 | 对接真实接口，展示日志+统计+配额管理 |
| 7 | 小程序无摘要功能 | 无 | 笔记详情页加摘要卡片 + 生成按钮 |

---

# 📝 Day 13 提示词

## 提示词 1a：数据库设计 + 实体类

```
在 smart-note-system 中，为 AI 摘要功能新建数据库表和实体类。

⚠️ 设计决策：
- 复用 note_ai_summary 表存储摘要结果（已有）
- 新建 ai_usage_log 表记录每次 AI 调用的详细日志（含 token 用量）
- 新建 ai_user_quota 表管理每个用户的使用配额
- token 统计通过 ai_usage_log 聚合查询，不冗余存储

### 步骤 1：修改 init_db.sql — 新增两张表

在 init_db.sql 的"三、增强与日志模块"区块中，note_ai_summary 表定义之后、sys_log_behavior 表定义之前添加：

-- AI 调用日志表（每次请求一条记录）
CREATE TABLE ai_usage_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '调用用户ID',
    note_id BIGINT COMMENT '关联笔记ID',
    action_type VARCHAR(20) NOT NULL DEFAULT 'summary' COMMENT '操作类型：summary(摘要生成)',
    prompt_tokens INT DEFAULT 0 COMMENT '输入 token 数',
    completion_tokens INT DEFAULT 0 COMMENT '输出 token 数',
    total_tokens INT DEFAULT 0 COMMENT '总 token 数',
    model_name VARCHAR(50) COMMENT '使用的AI模型',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1成功, 0失败',
    error_msg VARCHAR(500) COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI调用日志表';

-- 用户 AI 配额表（每个用户一条记录）
CREATE TABLE ai_user_quota (
    user_id BIGINT PRIMARY KEY COMMENT '用户ID',
    monthly_token_limit INT NOT NULL DEFAULT 100000 COMMENT '每月 token 上限',
    monthly_request_limit INT NOT NULL DEFAULT 50 COMMENT '每月请求次数上限',
    used_tokens INT NOT NULL DEFAULT 0 COMMENT '本月已用 token 数',
    used_requests INT NOT NULL DEFAULT 0 COMMENT '本月已用请求次数',
    quota_reset_date DATE COMMENT '配额重置日期（每月1日重置）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户AI配额表';

### 步骤 2：新建实体类

(1) AiUsageLog.java（com.littlewin.note.domain.entity）

@Data
@TableName("ai_usage_log")
public class AiUsageLog implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long noteId;
    private String actionType;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private String modelName;
    private Integer status;
    private String errorMsg;
    private LocalDateTime createTime;
}

(2) AiUserQuota.java（com.littlewin.note.domain.entity）

@Data
@TableName("ai_user_quota")
public class AiUserQuota implements Serializable {
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    private Integer monthlyTokenLimit;
    private Integer monthlyRequestLimit;
    private Integer usedTokens;
    private Integer usedRequests;
    private LocalDate quotaResetDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

验证：
1. init_db.sql 中有 ai_usage_log 和 ai_user_quota 两张表（在"三、增强与日志模块"区块，note_ai_summary 之后）
2. AiUsageLog.java 和 AiUserQuota.java 实体类已创建
3. 项目编译通过
```

---

## 提示词 1b：Mapper 接口 + VO + DTO

```
在 smart-note-system 中，为 AI 功能创建 Mapper 基础接口、VO 和 QueryDTO。

⚠️ 前置条件：提示词 1a 已完成（表结构 + 实体类已就绪）。

### 步骤 1：新建 Mapper 接口（基础 CRUD）

(1) NoteAiSummaryMapper.java（com.littlewin.note.mapper）— NoteAiSummary 实体已有但无 Mapper

public interface NoteAiSummaryMapper extends BaseMapper<NoteAiSummary> {}

(2) AiUsageLogMapper.java（com.littlewin.note.mapper）

public interface AiUsageLogMapper extends BaseMapper<AiUsageLog> {
    // 后续在步骤 4 中添加自定义方法
}

(3) AiUserQuotaMapper.java（com.littlewin.note.mapper）

public interface AiUserQuotaMapper extends BaseMapper<AiUserQuota> {
    // 后续在提示词 1c 中添加自定义方法
}

### 步骤 2：新建 VO + QueryDTO

(1) AiUsageLogVO.java（com.littlewin.note.domain.vo）
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AiUsageLogVO {
    private Long id;
    private Long userId;
    private String userName;
    private Long noteId;
    private String noteTitle;
    private String actionType;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private String modelName;
    private Integer status;
    private String errorMsg;
    private LocalDateTime createTime;
}

(2) AiUserQuotaVO.java（com.littlewin.note.domain.vo）
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AiUserQuotaVO {
    private Long userId;
    private String userName;
    private Integer monthlyTokenLimit;
    private Integer monthlyRequestLimit;
    private Integer usedTokens;
    private Integer usedRequests;
    private LocalDate quotaResetDate;
}

(3) AiUsageLogQueryDTO.java（com.littlewin.note.domain.dto）
@Data
public class AiUsageLogQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long userId;
    private Integer status;
    private String actionType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

(4) AiUserQuotaQueryDTO.java（com.littlewin.note.domain.dto）
@Data
public class AiUserQuotaQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
}

验证：
1. 3 个 Mapper 接口创建完成（基础 CRUD）
2. 2 个 VO + 2 个 QueryDTO 创建完成
3. 项目编译通过
```

---

## 提示词 1c：Mapper XML + 自定义方法

```
在 smart-note-system 中，为 AI 功能创建 Mapper XML 映射和自定义查询方法。

⚠️ 前置条件：提示词 1b 已完成（Mapper 接口、VO、QueryDTO 已就绪）。

### 步骤 1：新建 Mapper XML

(1) AiUsageLogMapper.xml（resources/com/littlewin/note/mapper/）

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.littlewin.note.mapper.AiUsageLogMapper">

    <!-- 用户本月 token 总量 -->
    <select id="sumTokensByUserThisMonth" resultType="int">
        SELECT IFNULL(SUM(total_tokens), 0)
        FROM ai_usage_log
        WHERE user_id = #{userId}
          AND status = 1
          AND YEAR(create_time) = YEAR(CURDATE())
          AND MONTH(create_time) = MONTH(CURDATE())
    </select>

    <!-- 用户本月请求次数 -->
    <select id="countRequestsByUserThisMonth" resultType="int">
        SELECT COUNT(*)
        FROM ai_usage_log
        WHERE user_id = #{userId}
          AND status = 1
          AND YEAR(create_time) = YEAR(CURDATE())
          AND MONTH(create_time) = MONTH(CURDATE())
    </select>

    <!-- 管理端：分页查询日志（关联用户信息） -->
    <select id="selectLogPage" resultType="com.littlewin.note.domain.vo.AiUsageLogVO">
        SELECT
            l.id,
            l.user_id AS userId,
            u.nickname AS userName,
            l.note_id AS noteId,
            n.title AS noteTitle,
            l.action_type AS actionType,
            l.prompt_tokens AS promptTokens,
            l.completion_tokens AS completionTokens,
            l.total_tokens AS totalTokens,
            l.model_name AS modelName,
            l.status,
            l.error_msg AS errorMsg,
            l.create_time AS createTime
        FROM ai_usage_log l
                 LEFT JOIN sys_user u ON l.user_id = u.user_id
                 LEFT JOIN note n ON l.note_id = n.note_id
        <where>
            <if test="query.userId != null">AND l.user_id = #{query.userId}</if>
            <if test="query.status != null">AND l.status = #{query.status}</if>
            <if test="query.actionType != null and query.actionType != ''">AND l.action_type = #{query.actionType}</if>
            <if test="query.startTime != null">AND l.create_time &gt;= #{query.startTime}</if>
            <if test="query.endTime != null">AND l.create_time &lt;= #{query.endTime}</if>
        </where>
        ORDER BY l.create_time DESC
    </select>

    <!-- 管理端：统计面板数据 -->
    <select id="selectGlobalStats" resultType="java.util.Map">
        SELECT
            COUNT(*) AS totalRequests,
            IFNULL(SUM(total_tokens), 0) AS totalTokens,
            IFNULL(SUM(prompt_tokens), 0) AS totalPromptTokens,
            IFNULL(SUM(completion_tokens), 0) AS totalCompletionTokens,
            COUNT(DISTINCT user_id) AS activeUsers
        FROM ai_usage_log
        WHERE status = 1
          AND YEAR(create_time) = YEAR(CURDATE())
          AND MONTH(create_time) = MONTH(CURDATE())
    </select>

    <!-- 管理端：按用户统计排行 -->
    <select id="selectUserRanking" resultType="java.util.Map">
        SELECT
            l.user_id AS userId,
            u.nickname AS userName,
            COUNT(*) AS requestCount,
            IFNULL(SUM(l.total_tokens), 0) AS totalTokens
        FROM ai_usage_log l
                 LEFT JOIN sys_user u ON l.user_id = u.user_id
        WHERE l.status = 1
          AND YEAR(l.create_time) = YEAR(CURDATE())
          AND MONTH(l.create_time) = MONTH(CURDATE())
        GROUP BY l.user_id, u.nickname
        ORDER BY totalTokens DESC
        LIMIT #{limit}
    </select>

</mapper>

(2) AiUserQuotaMapper.xml（resources/com/littlewin/note/mapper/）

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.littlewin.note.mapper.AiUserQuotaMapper">

    <!-- 管理端：分页查询用户配额（关联用户信息） -->
    <select id="selectQuotaPage" resultType="com.littlewin.note.domain.vo.AiUserQuotaVO">
        SELECT
            q.user_id AS userId,
            u.nickname AS userName,
            q.monthly_token_limit AS monthlyTokenLimit,
            q.monthly_request_limit AS monthlyRequestLimit,
            q.used_tokens AS usedTokens,
            q.used_requests AS usedRequests,
            q.quota_reset_date AS quotaResetDate
        FROM ai_user_quota q
                 LEFT JOIN sys_user u ON q.user_id = u.user_id
        <where>
            <if test="query.keyword != null and query.keyword != ''">
                AND u.nickname LIKE CONCAT('%', #{query.keyword}, '%')
            </if>
        </where>
        ORDER BY q.update_time DESC
    </select>

</mapper>

### 步骤 2：补充 Mapper 接口自定义方法

在 AiUsageLogMapper.java 中添加：
IPage<AiUsageLogVO> selectLogPage(Page<AiUsageLogVO> page, @Param("query") AiUsageLogQueryDTO query);
Map<String, Object> selectGlobalStats();
List<Map<String, Object>> selectUserRanking(@Param("limit") int limit);
int sumTokensByUserThisMonth(@Param("userId") Long userId);
int countRequestsByUserThisMonth(@Param("userId") Long userId);

在 AiUserQuotaMapper.java 中添加：
IPage<AiUserQuotaVO> selectQuotaPage(Page<AiUserQuotaVO> page, @Param("query") AiUserQuotaQueryDTO query);

验证：
1. 2 个 Mapper XML 文件创建完成
2. Mapper 接口自定义方法添加完成
3. 项目编译通过
```

---

## 提示词 2a：LangChain4j 配置 + 配额服务

```
在 smart-note-system 中，集成 LangChain4j 依赖和配置，实现 AI 配额校验服务。

⚠️ 设计决策：
- 使用 LangChain4j 集成 DeepSeek API（兼容 OpenAI 接口格式）
- 摘要生成前校验用户配额（token 上限 + 请求次数上限）
- 每次调用记录 ai_usage_log（含 token 用量）
- 配额不足时返回 403 + 提示信息
- 如果 DeepSeek API 不可用，先 mock 实现保进度

### 步骤 1：pom.xml 引入依赖

在 smart-note-system/note/pom.xml 中添加：

<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>0.35.0</version>
</dependency>

注意：DeepSeek 兼容 OpenAI 接口格式，使用 langchain4j-open-ai 即可。

### 步骤 2：application.yml 配置

在 application-dev.yml 中添加：

ai:
  deepseek:
    api-key: ${DEEPSEEK_API_KEY:sk-xxx}
    base-url: https://api.deepseek.com
    model-name: deepseek-chat
    max-tokens: 500

⚠️ api-key 请替换为真实 key，或通过环境变量注入。

### 步骤 3：新建 AiQuotaService（配额校验）

(1) AiQuotaService.java（com.littlewin.note.service）

public interface AiQuotaService {
    /** 校验用户配额，不足则抛出 ServiceException */
    void checkQuota(Long userId);
    /** 记录一次调用用量 */
    void recordUsage(Long userId, int promptTokens, int completionTokens, String modelName, Long noteId, int status, String errorMsg);
    /** 获取用户剩余配额 */
    Map<String, Object> getRemainingQuota(Long userId);
}

(2) AiQuotaServiceImpl.java（com.littlewin.note.service.impl）

@Service
@RequiredArgsConstructor
public class AiQuotaServiceImpl implements AiQuotaService {

    private final AiUserQuotaMapper quotaMapper;
    private final AiUsageLogMapper logMapper;

    @Override
    public void checkQuota(Long userId) {
        AiUserQuota quota = quotaMapper.selectById(userId);
        if (quota == null) {
            // 无配额记录 = 使用默认配额
            quota = new AiUserQuota();
            quota.setUserId(userId);
            quota.setMonthlyTokenLimit(100000);
            quota.setMonthlyRequestLimit(50);
            quota.setUsedTokens(0);
            quota.setUsedRequests(0);
            quotaMapper.insert(quota);
        }

        // 检查是否需要重置（每月1日）
        LocalDate today = LocalDate.now();
        if (quota.getQuotaResetDate() == null || quota.getQuotaResetDate().getMonth() != today.getMonth()) {
            quota.setUsedTokens(0);
            quota.setUsedRequests(0);
            quota.setQuotaResetDate(today.withDayOfMonth(1));
            quotaMapper.updateById(quota);
        }

        // 校验请求次数
        if (quota.getUsedRequests() >= quota.getMonthlyRequestLimit()) {
            throw new ServiceException("本月 AI 请求次数已用完，限额 " + quota.getMonthlyRequestLimit() + " 次");
        }

        // 校验 token 用量（从日志表实时统计更准确）
        int usedTokens = logMapper.sumTokensByUserThisMonth(userId);
        if (usedTokens >= quota.getMonthlyTokenLimit()) {
            throw new ServiceException("本月 AI token 用量已用完，限额 " + quota.getMonthlyTokenLimit() + " tokens");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordUsage(Long userId, int promptTokens, int completionTokens, String modelName, Long noteId, int status, String errorMsg) {
        // 记录日志
        AiUsageLog log = new AiUsageLog();
        log.setUserId(userId);
        log.setNoteId(noteId);
        log.setActionType("summary");
        log.setPromptTokens(promptTokens);
        log.setCompletionTokens(completionTokens);
        log.setTotalTokens(promptTokens + completionTokens);
        log.setModelName(modelName);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);

        // 更新配额表计数
        AiUserQuota quota = quotaMapper.selectById(userId);
        if (quota != null) {
            quota.setUsedTokens(quota.getUsedTokens() + promptTokens + completionTokens);
            quota.setUsedRequests(quota.getUsedRequests() + 1);
            quotaMapper.updateById(quota);
        }
    }

    @Override
    public Map<String, Object> getRemainingQuota(Long userId) {
        AiUserQuota quota = quotaMapper.selectById(userId);
        if (quota == null) {
            return Map.of("tokenLimit", 100000, "requestLimit", 50, "usedTokens", 0, "usedRequests", 0);
        }
        return Map.of(
            "tokenLimit", quota.getMonthlyTokenLimit(),
            "requestLimit", quota.getMonthlyRequestLimit(),
            "usedTokens", quota.getUsedTokens(),
            "usedRequests", quota.getUsedRequests()
        );
    }
}

验证：
1. pom.xml 引入 langchain4j-open-ai 依赖
2. application-dev.yml 配置 DeepSeek API
3. AiQuotaService 接口和实现创建完成
4. 项目编译通过
```

---

## 提示词 2b：摘要服务 + 接口 + 小程序更新

```
在 smart-note-system 中，实现 AI 摘要生成服务和小程序端接口，更新小程序已有 API 方法。

⚠️ 前置条件：提示词 1c + 2a 已完成（Mapper XML、VO、配额服务已就绪）。

### 步骤 1：新建 AiSummaryService（摘要生成）

(1) AiSummaryService.java（com.littlewin.note.service）

public interface AiSummaryService {
    /** 为笔记生成 AI 摘要 */
    Map<String, String> generateSummary(Long noteId, Long userId);
}

(2) AiSummaryServiceImpl.java（com.littlewin.note.service.impl）

@Service
@Slf4j
@RequiredArgsConstructor
public class AiSummaryServiceImpl implements AiSummaryService {

    @Value("${ai.deepseek.api-key:sk-xxx}")
    private String apiKey;
    @Value("${ai.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;
    @Value("${ai.deepseek.model-name:deepseek-chat}")
    private String modelName;
    @Value("${ai.deepseek.max-tokens:500}")
    private Integer maxTokens;

    private final NoteMapper noteMapper;
    private final NoteAiSummaryMapper noteAiSummaryMapper;
    private final AiQuotaService aiQuotaService;

    private OpenAiChatModel chatModel;

    @PostConstruct
    public void init() {
        chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> generateSummary(Long noteId, Long userId) {
        // 1. 校验配额
        aiQuotaService.checkQuota(userId);

        // 2. 查询笔记
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getDelFlag() == 1) {
            throw new ServiceException("笔记不存在");
        }

        // 3. 调用 AI
        int promptTokens = 0;
        int completionTokens = 0;
        String summary = null;
        String keywords = null;
        int status = 1;
        String errorMsg = null;

        try {
            String prompt = "请为以下笔记生成一段简洁的摘要（100字以内），并提取3-5个关键词（逗号分隔）。\n\n"
                    + "标题：" + note.getTitle() + "\n"
                    + "内容：" + note.getContent().substring(0, Math.min(note.getContent().length(), 2000));

            // LangChain4j 标准 API：model.chat(List<ChatMessage>)
            Response<AiMessage> response = chatModel.chat(List.of(UserMessage.from(prompt)));
            String result = response.content().text();

            // 从 API 响应获取真实 token 用量
            TokenUsage usage = response.tokenUsage();
            promptTokens = usage.inputTokenCount();
            completionTokens = usage.outputTokenCount();

            // 解析结果：第一行是摘要，第二行是关键词
            String[] parts = result.split("\n", 2);
            summary = parts[0].trim();
            keywords = parts.length > 1 ? parts[1].trim() : "";

        } catch (ServiceException e) {
            throw e; // 配额不足直接抛出
        } catch (Exception e) {
            status = 0;
            errorMsg = e.getMessage();
            log.error("AI 摘要生成失败: noteId={}", noteId, e);
        }

        // 4. 记录用量
        aiQuotaService.recordUsage(userId, promptTokens, completionTokens, modelName, noteId, status, errorMsg);

        if (status == 0) {
            throw new ServiceException("AI 摘要生成失败：" + errorMsg);
        }

        // 5. 保存摘要
        NoteAiSummary aiSummary = new NoteAiSummary();
        aiSummary.setNoteId(noteId);
        aiSummary.setSummary(summary);
        aiSummary.setKeywords(keywords);
        aiSummary.setModelName(modelName);
        aiSummary.setCreateTime(LocalDateTime.now());
        noteAiSummaryMapper.insertOrUpdate(aiSummary);

        return Map.of("summary", summary, "keywords", keywords);
    }
}

### 步骤 2：新建小程序端 Controller

WxAiController.java（com.littlewin.note.controller）

路径：/api/wx/notes/ai/summary（复用小程序已有的 AI 摘要路径约定）

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wx/notes/ai")
public class WxAiController {

    private final AiSummaryService aiSummaryService;
    private final NoteAiSummaryMapper noteAiSummaryMapper;
    private final AiQuotaService aiQuotaService;

    /** 查询已有摘要 */
    @GetMapping("/summary")
    public Result<NoteAiSummary> getSummary(@RequestParam("noteId") Long noteId) {
        return Result.success(noteAiSummaryMapper.selectById(noteId));
    }

    /** 生成 AI 摘要 */
    @PostMapping("/summary")
    public Result<Map<String, String>> generateSummary(@RequestParam("noteId") Long noteId) {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(aiSummaryService.generateSummary(noteId, userId));
    }

    /** 查询剩余配额 */
    @GetMapping("/quota")
    public Result<Map<String, Object>> getQuota() {
        Long userId = SecurityUtils.getLoginUser().getUserId();
        return Result.success(aiQuotaService.getRemainingQuota(userId));
    }
}

⚠️ 路径说明：
- 复用小程序已有的 /api/wx/notes/ai/summary 路径约定（config.js 中 NOTE.AI_SUMMARY）
- GET 获取已有摘要，POST 生成新摘要
- 小程序端无需新建 ai.js，直接在 note.js 中补充 getAiSummary 方法即可

### 步骤 3：更新小程序已有文件

(1) api/config.js — 在 NOTE 对象中补充 AI_QUOTA：
AI_QUOTA: '/api/wx/notes/ai/quota'

(2) api/modules/note.js — 删除旧的 aiSummary(content) 方法（L68-73），新增以下方法：

⚠️ 注意：request.js 只对 GET/DELETE 处理 params 拼接，POST 的 params 会被忽略。
因此 generateAiSummary 必须手动拼接 URL（与 getNoteDetail 风格一致）。

getAiSummary(noteId) {
    return request({
      url: APIS.NOTE.AI_SUMMARY,
      method: 'GET',
      params: { noteId }
    })
  },
  generateAiSummary(noteId) {
    return request({
      url: `${APIS.NOTE.AI_SUMMARY}?noteId=${noteId}`,
      method: 'POST'
    })
  },
  getAiQuota() {
    return request({
      url: APIS.NOTE.AI_QUOTA,
      method: 'GET'
    })
  }

验证：
1. GET /api/wx/notes/ai/summary?noteId=1 → 返回已有摘要（或 null）
2. POST /api/wx/notes/ai/summary?noteId=1 → 生成并返回 { summary, keywords }
3. ai_usage_log 表新增一条记录，含真实 token 用量
4. ai_user_quota 表 used_tokens 和 used_requests 更新
5. 超过配额时返回 403 "本月 AI 请求次数已用完"
6. GET /api/wx/notes/ai/quota → 返回剩余配额信息
```

---

## 提示词 3：管理端监控接口

```
在 smart-note-system 中，实现管理端 AI 监控接口：日志查询、用户配额管理、统计面板。

### 步骤 1：新建 AdminAiController.java（com.littlewin.note.controller）

路径：/api/admin/ai
实现以下接口：

(1) GET /api/admin/ai/logs — 分页查询 AI 调用日志
- 参数：AiUsageLogQueryDTO
- 返回 IPage<AiUsageLogVO>
- 不加 @Log（只读查询，与现有 GET 接口风格一致）

(2) GET /api/admin/ai/stats — 本月统计面板
- 返回 { totalRequests, totalTokens, totalPromptTokens, totalCompletionTokens, activeUsers }

(3) GET /api/admin/ai/ranking — 用户 token 用量排行
- 参数：@RequestParam(defaultValue = "10") int limit
- 返回 List<Map>（userId, userName, requestCount, totalTokens）

(4) GET /api/admin/ai/quota/list — 分页查询用户配额
- 参数：AiUserQuotaQueryDTO
- 返回 IPage<AiUserQuotaVO>

(5) PUT /api/admin/ai/quota/{userId} — 修改用户配额
- 请求体：{ monthlyTokenLimit, monthlyRequestLimit }
- @Log(module = LogModule.AI, action = LogAction.UPDATE, desc = "修改用户AI配额")

### 步骤 2：新建 AdminAiService.java + AdminAiServiceImpl.java

接口方法：
IPage<AiUsageLogVO> listLogs(AiUsageLogQueryDTO query);
Map<String, Object> getStats();
List<Map<String, Object>> getUserRanking(int limit);
IPage<AiUserQuotaVO> listQuotas(AiUserQuotaQueryDTO query);
void updateQuota(Long userId, Integer tokenLimit, Integer requestLimit);

实现要点：
- listLogs → aiUsageLogMapper.selectLogPage
- getStats → aiUsageLogMapper.selectGlobalStats
- getUserRanking → aiUsageLogMapper.selectUserRanking
- listQuotas → aiUserQuotaMapper.selectQuotaPage
- updateQuota → 直接 updateById

验证：
1. GET /api/admin/ai/logs → 返回分页日志数据
2. GET /api/admin/ai/stats → 返回本月统计
3. GET /api/admin/ai/ranking?limit=10 → 返回用户排行
4. GET /api/admin/ai/quota/list → 返回用户配额列表
5. PUT /api/admin/ai/quota/1 { monthlyTokenLimit: 200000, monthlyRequestLimit: 100 } → 修改成功
```

---

## 提示词 4：Web 监控页面改造

```
在 smart-note-ui 中，将 AI 监控页面从占位改造为完整功能页面。

⚠️ 前置条件：提示词 3 已完成（管理端接口已就绪）。

已有代码参考：
- 当前占位页面：src/views/monitor/aiLog/index.vue
- 当前占位 API：src/api/modules/aiLog.ts
- ProTable 使用模式：参考 src/views/note/list/index.vue

### 步骤 1：改造 aiLog.ts — 完善 API

替换为：

import http from "@/api";

export interface ReqAiLogParams {
  pageNum?: number;
  pageSize?: number;
  userId?: number;
  status?: number;
  actionType?: string;
  startTime?: string;
  endTime?: string;
}

export interface AiLogVO {
  id: number;
  userId: number;
  userName: string;
  noteId: number;
  noteTitle: string;
  actionType: string;
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
  modelName: string;
  status: number;
  errorMsg: string;
  createTime: string;
}

export interface AiUserQuotaVO {
  userId: number;
  userName: string;
  monthlyTokenLimit: number;
  monthlyRequestLimit: number;
  usedTokens: number;
  usedRequests: number;
  quotaResetDate: string;
}

// AI 日志列表
export const getAiLogList = (params: ReqAiLogParams) => {
  return http.get(`/admin/ai/logs`, params, { loading: false });
};

// 本月统计
export const getAiStats = () => {
  return http.get(`/admin/ai/stats`);
};

// 用户排行
export const getAiRanking = (limit = 10) => {
  return http.get(`/admin/ai/ranking`, { limit });
};

// 用户配额列表
export const getAiQuotaList = (params: { pageNum?: number; pageSize?: number; keyword?: string }) => {
  return http.get(`/admin/ai/quota/list`, params, { loading: false });
};

// 修改用户配额
export const updateAiQuota = (userId: number, data: { monthlyTokenLimit: number; monthlyRequestLimit: number }) => {
  return http.put(`/admin/ai/quota/${userId}`, data);
};

### 步骤 2：改造 monitor/aiLog/index.vue — 完整监控页面

页面结构分为三个区域：

区域 1 — 统计卡片（顶部 4 个 el-card）：
┌──────────┬──────────┬──────────┬──────────┐
│ 总请求次数 │ 总Token   │ 输入Token │ 输出Token │
│   125    │  45,600  │  32,100  │  13,500  │
└──────────┴──────────┴──────────┴──────────┘

区域 2 — 用户排行（右侧 el-card，Top 10）：
| 排名 | 用户名 | 请求次数 | Token用量 |
|  1  | 张三   |   30    |  12,000  |
|  2  | 李四   |   20    |   8,500  |

区域 3 — 调用日志（ProTable 列表，占主要空间）：
列：ID、用户名、笔记标题、操作类型、输入Token、输出Token、总Token、模型、状态、时间
搜索：状态筛选、时间范围

页面底部可选区域 — 配额管理（独立 ProTable 或按钮打开弹窗）：
列：用户名、Token限额、请求限额、已用Token、已用请求、重置日期
操作：修改配额（弹窗编辑）

⚠️ 实现方式：
- 统计卡片和排行用 onMounted 调接口，数据存 ref
- 日志和配额用两个 ProTable
- 配额修改用 el-dialog 弹窗 + el-form

验证：
1. 打开 AI 监控页 → 顶部显示本月统计卡片
2. 日志列表正常分页，可按状态和时间筛选
3. 用户排行显示 Top 10
4. 配额列表显示所有用户的配额使用情况
5. 点击"修改配额"→ 弹窗编辑 → 保存成功
```

---

## 提示词 5：小程序 AI 摘要卡片

```
在 smart-note-mp 小程序中，笔记详情页增加 AI 摘要卡片和生成按钮。

⚠️ 前置条件：提示词 2b 步骤 3 已完成（note.js 已补充 getAiSummary / generateAiSummary / getAiQuota 方法）。

已有代码参考：
- 笔记详情页：pages/note-detail/note-detail.vue
- API：api/modules/note.js（已有 getAiSummary / generateAiSummary / getAiQuota）
- API 配置：api/config.js（已有 NOTE.AI_SUMMARY / NOTE.AI_QUOTA）

### 步骤 1：改造 note-detail.vue

在笔记内容区域之后、评论区域之前，添加 AI 摘要卡片：

<view v-if="aiSummary" class="ai-summary-card">
  <view class="ai-header">
    <text class="ai-label">AI 摘要</text>
    <text class="ai-model">{{ aiSummary.modelName || 'DeepSeek' }}</text>
  </view>
  <text class="ai-content">{{ aiSummary.summary }}</text>
  <view v-if="aiSummary.keywords" class="ai-keywords">
    <text class="keyword-tag" v-for="kw in aiSummary.keywords.split(',')" :key="kw">{{ kw.trim() }}</text>
  </view>
</view>

<view v-else class="ai-generate-btn" @click="onGenerateSummary">
  <text>{{ generating ? '生成中...' : '生成 AI 摘要' }}</text>
</view>

script 中新增：
- aiSummary: ref(null) — 已有摘要数据
- generating: ref(false) — 生成中状态
- onLoad 时调 noteApi.getAiSummary(noteId) 查询已有摘要，结果存入 aiSummary
- onGenerateSummary：调 noteApi.generateAiSummary(noteId)，成功后更新 aiSummary
- 配额不足时（catch 中判断状态码）提示"本月 AI 请求次数已用完"

样式：
- ai-summary-card：白色背景、圆角、蓝色左边框
- ai-header：flex 布局，左侧"AI 摘要"标签，右侧模型名
- ai-content：正文灰色
- ai-keywords：flex wrap，keyword-tag 为小圆角标签
- ai-generate-btn：居中按钮，蓝色文字

验证：
1. 无摘要的笔记 → 显示"生成 AI 摘要"按钮
2. 点击按钮 → 显示"生成中..." → 成功后展示摘要卡片
3. 已有摘要的笔记 → 直接展示摘要卡片
4. 配额不足 → 提示"本月 AI 请求次数已用完"
5. ai_usage_log 表有对应记录
```

---

# ⏱️ Day 13 执行顺序

| 顺序 | 提示词 | 前置依赖 | 说明 |
|:--:|:---|:--:|:---|
| 1️⃣ | 提示词 1a：数据库 + 实体类 | 无 | 表结构 + 实体 |
| 2️⃣ | 提示词 1b：Mapper 接口 + VO + DTO | 1a | 类型定义 |
| 3️⃣ | 提示词 1c：Mapper XML + 自定义方法 | 1b | SQL 映射 |
| 4️⃣ | 提示词 2a：LangChain4j 配置 + 配额服务 | 1a | 依赖 + 配置 + 配额 |
| 5️⃣ | 提示词 2b：摘要服务 + 接口 + 小程序 | 1c, 2a | 摘要生成 + Controller |
| 6️⃣ | 提示词 3：管理端接口 | 1c | 日志查询 + 配额管理 |
| 7️⃣ | 提示词 4：Web 监控页面 | 3 | 前端对接后端 |
| 8️⃣ | 提示词 5：小程序摘要卡片 | 2b | 笔记详情页摘要 |

> 1a → 1b → 2a / 1c 可并行 → 2b 依赖 1c + 2a。3 依赖 1c。4 依赖 3。5 依赖 2b。

---

# 🔍 Day 13 涉及的文件清单

| 文件 | 改动类型 | 说明 |
|:---|:---|:---|
| init_db.sql | 修改 | 新增 ai_usage_log + ai_user_quota 表 |
| AiUsageLog.java | 新建 | AI 调用日志实体 |
| AiUserQuota.java | 新建 | 用户配额实体 |
| NoteAiSummaryMapper.java | 新建 | AI 摘要 Mapper（实体已有无 Mapper） |
| AiUsageLogMapper.java | 新建 | 日志 Mapper 接口 |
| AiUsageLogMapper.xml | 新建 | 日志 SQL（分页+统计+排行） |
| AiUserQuotaMapper.java | 新建 | 配额 Mapper 接口 |
| AiUserQuotaMapper.xml | 新建 | 配额 SQL |
| AiUsageLogVO.java | 新建 | 日志 VO |
| AiUserQuotaVO.java | 新建 | 配额 VO |
| AiUsageLogQueryDTO.java | 新建 | 日志查询 DTO |
| AiUserQuotaQueryDTO.java | 新建 | 配额查询 DTO |
| AiQuotaService.java | 新建 | 配额校验服务接口 |
| AiQuotaServiceImpl.java | 新建 | 配额校验实现 |
| AiSummaryService.java | 新建 | 摘要生成服务接口 |
| AiSummaryServiceImpl.java | 新建 | 摘要生成实现（LangChain4j） |
| WxAiController.java | 新建 | 小程序端 AI 接口 |
| AdminAiController.java | 新建 | 管理端 AI 监控接口 |
| AdminAiService.java | 新建 | 管理端 AI 服务接口 |
| AdminAiServiceImpl.java | 新建 | 管理端 AI 服务实现 |
| note/pom.xml | 修改 | 引入 langchain4j-open-ai |
| application-dev.yml | 修改 | DeepSeek API 配置 |
| aiLog.ts | 重写 | 完善 API 接口 |
| monitor/aiLog/index.vue | 重写 | 完整监控页面 |
| api/config.js | 修改 | 补充 NOTE.AI_QUOTA 路径 |
| api/modules/note.js | 修改 | 补充 getAiSummary / generateAiSummary / getAiQuota |

---

# 🐛 Day 13 Debug 提示词

## 修复 1：日期搜索报错（startTime/endTime 类型不匹配）

```
修复 AiUsageLogQueryDTO 的 startTime/endTime 字段类型与前端传参不匹配的问题。

⚠️ 问题描述：
前端 date-picker 使用 valueFormat="YYYY-MM-DD"，发送 "2026-05-01" 格式的字符串。
后端 AiUsageLogQueryDTO 的 startTime/endTime 是 LocalDateTime 类型，无法自动转换，
导致报错：Failed to convert property value of type 'java.lang.String' to required type 'java.time.LocalDateTime'

⚠️ 修复方案：将 startTime/endTime 从 LocalDateTime 改为 String。
MySQL 中 String 与 DATETIME 列的 >= / <= 比较仍然正确（字符串按字典序比较，"YYYY-MM-DD" 格式兼容）。

### 修改文件：AiUsageLogQueryDTO.java

将：
private LocalDateTime startTime;
private LocalDateTime endTime;

改为：
private String startTime;
private String endTime;

同时删除 import java.time.LocalDateTime;（如果没有其他字段使用的话）

验证：
1. GET /api/admin/ai/logs?startTime=2026-05-01&endTime=2026-05-06 → 正常返回数据
2. 前端日期搜索不再报错
```

---

## 修复 2：用户排行布局 + 功能增强

```
修复 AI 监控页面用户排行区域的布局问题，并增强排行功能。

⚠️ 问题描述：
1. 用户排行表格没有占满 el-col 宽度（el-table 默认 width=100%，但 el-card 内可能需要样式调整）
2. 用户排行需要增加：日期筛选、使用次数排序、token 消耗排序

### 修改文件：src/views/monitor/aiLog/index.vue

(1) 修复布局 — 给排行表格添加 style：

将：
<el-table :data="rankingData" stripe size="small" max-height="420">

改为：
<el-table :data="rankingData" stripe size="small" max-height="420" style="width: 100%">

(2) 排行区域增加日期筛选：

在 ranking-card 的 template #header 中，标题右侧添加日期筛选：

<template #header>
  <div style="display: flex; justify-content: space-between; align-items: center;">
    <span>用户 Token 用量排行</span>
    <el-date-picker
      v-model="rankingDateRange"
      type="daterange"
      value-format="YYYY-MM-DD"
      start-placeholder="开始日期"
      end-placeholder="结束日期"
      size="small"
      style="width: 240px"
      @change="loadRanking"
    />
  </div>
</template>

(3) script 中新增 rankingDateRange 和修改 loadRanking：

const rankingDateRange = ref<string[]>([]);

const loadRanking = async () => {
  try {
    const params: any = { limit: 10 };
    if (rankingDateRange.value && rankingDateRange.value.length === 2) {
      params.startTime = rankingDateRange.value[0];
      params.endTime = rankingDateRange.value[1];
    }
    const { data } = await getAiRanking(10, params);
    rankingData.value = data || [];
  } catch {
    // ignore
  }
};

(4) 修改 aiLog.ts 中 getAiRanking 接口，支持日期参数：

将：
export const getAiRanking = (limit = 10) => {
  return http.get(`/admin/ai/ranking`, { limit });
};

改为：
export const getAiRanking = (limit = 10, params?: { startTime?: string; endTime?: string }) => {
  return http.get(`/admin/ai/ranking`, { limit, ...params });
};

(5) 后端 AdminAiController.java — getUserRanking 接口增加日期参数：

将：
@GetMapping("/ranking")
public Result<List<Map<String, Object>>> getUserRanking(
        @RequestParam(defaultValue = "10") int limit) {
    return Result.success(adminAiService.getUserRanking(limit));
}

改为：
@GetMapping("/ranking")
public Result<List<Map<String, Object>>> getUserRanking(
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String startTime,
        @RequestParam(required = false) String endTime) {
    return Result.success(adminAiService.getUserRanking(limit, startTime, endTime));
}

(6) AdminAiService.java + AdminAiServiceImpl.java — 增加日期参数：

接口方法改为：
List<Map<String, Object>> getUserRanking(int limit, String startTime, String endTime);

实现改为：
@Override
public List<Map<String, Object>> getUserRanking(int limit, String startTime, String endTime) {
    return aiUsageLogMapper.selectUserRanking(limit, startTime, endTime);
}

(7) AiUsageLogMapper.java — 增加日期参数：

将：
List<Map<String, Object>> selectUserRanking(@Param("limit") int limit);

改为：
List<Map<String, Object>> selectUserRanking(@Param("limit") int limit,
    @Param("startTime") String startTime, @Param("endTime") String endTime);

(8) AiUsageLogMapper.xml — selectUserRanking SQL 增加日期条件：

在 WHERE 条件中增加：
<if test="startTime != null and startTime != ''">
    AND l.create_time &gt;= #{startTime}
</if>
<if test="endTime != null and endTime != ''">
    AND l.create_time &lt;= CONCAT(#{endTime}, ' 23:59:59')
</if>

验证：
1. 用户排行表格占满右侧列宽度
2. 选择日期范围后排行数据按日期过滤
3. 不选日期时显示全部数据
```

---

## 修复 3：排行卡片高度未撑满

```
AI 监控页面中间区域，左侧排行卡片（span=8）高度没有撑满，右侧日志卡片（span=16）更高，
导致排行卡片下方留白。

⚠️ 原因：el-row 默认不等高，子 el-col 各自按内容撑高。右侧 ProTable 有分页+搜索栏所以更高。

⚠️ 修复方案：让 main-row 的两列等高，排行卡片撑满父容器。

### 修改文件：src/views/monitor/aiLog/index.vue

(1) style 中修改 .main-row：

将：
.main-row {
  .ranking-card {
    height: 100%;
  }

  .log-card {
    min-height: 480px;
  }
}

改为：
.main-row {
  display: flex;
  align-items: stretch;

  .el-col {
    display: flex;
  }

  .ranking-card {
    flex: 1;
    display: flex;
    flex-direction: column;

    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }
  }

  .log-card {
    flex: 1;
  }
}

(2) template 中排行表格去掉 max-height：

将：
<el-table :data="rankingData" stripe size="small" max-height="420" style="width: 100%">

改为：
<el-table :data="rankingData" stripe size="small" style="width: 100%; flex: 1;">

这样排行表格会自动撑满卡片剩余空间，与右侧日志卡片等高。

验证：
1. 排行卡片与日志卡片底部对齐，无留白
2. 排行数据少时表格仍然撑满（表格有 stripe 样式，空行不难看）
3. 数据多时表格内部滚动（不撑爆页面）
```
| note-detail.vue | 修改 | AI 摘要卡片 |
