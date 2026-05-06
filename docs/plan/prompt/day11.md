# 📋 Day 11 任务清单：消息系统重构

## 任务概览

| 序号 | 任务 | 优先级 | 说明 |
|:--:|:---|:--:|:---|
| 1 | 后端基础层（表+实体+Mapper+Service+Controller） | 🔴 高 | 扩展 user_message 表，改造消息服务 |
| 2 | 后端业务层（互动消息+管理端通知） | 🔴 高 | InteractionServiceImpl 写入消息，AdminNotificationController |
| 3 | 小程序端改造 | 🔴 高 | message.vue 双 Tab + 新 API |
| 4 | Web 端通知管理 | 🟡 中 | 通知管理页面 + 发送公告弹窗 |

## 当前已有基础

- `UserMessage` 实体：id, receiverId, senderId, noteId, commentId, type(1评论/2回复), content, isRead, createTime
- `MessageVO`：id, type, content, isRead, createTime, noteId, noteTitle, senderName, senderAvatar, commentId
- `MessageServiceImpl`：getUnreadCount, listMessages, markAllRead, deleteMessage, sendMessage
- `WxMessageController`：unread-count / list / read-all / delete 四个接口
- `CommentServiceImpl`：评论时调用 `messageService.sendMessage()` 发送 type=1/2 消息
- `InteractionServiceImpl`：点赞/收藏切换，**未接入消息**
- `message.vue`：小程序消息列表页，**无 Tab 切换**
- `message.js`：小程序消息 API，getUnreadCount / getList / readAll / remove

## 需要实现的内容

| 序号 | 问题 | 当前状态 | 目标 |
|:--:|:---|:---|:---|
| 1 | user_message 表缺 title 字段 | 无 title 列 | ALTER TABLE 新增 title |
| 2 | type 只有 1/2 | 仅评论/回复 | 扩展到 1-8，新增点赞/收藏/审核/公告 |
| 3 | 无分类查询 | 查询返回全部消息 | 支持 type=interaction/notice 过滤 |
| 4 | 未读数无分类 | 只有 totalCount | 返回 interactionCount + noticeCount + totalCount |
| 5 | 点赞/收藏无消息通知 | InteractionServiceImpl 未接入 | 点赞/收藏时写入 user_message |
| 6 | 无管理端通知接口 | 无 | 新增 AdminNotificationController |
| 7 | 小程序无 Tab | 只有列表 | 互动消息/系统通知双 Tab |
| 8 | Web 无通知管理页面 | 无 | 新建通知管理页面 + 发送公告 |

---

# 📝 Day 11 提示词

## 提示词 1：后端基础层（表+实体+Mapper+Service+Controller 改造）

```
在 smart-note-system 的 note 模块中，改造消息系统基础层，支持类型扩展和分类查询。

⚠️ 设计决策（已在计划中确认）：
- 点赞/收藏取消后不删消息，历史记录保留（方案 A）
- 自己点赞/收藏自己的笔记不发消息（与评论逻辑一致）
- 系统通知的 sender_id 用操作者的管理员 userId

已有代码参考：
- 实体类：note/domain/entity/UserMessage.java
- VO：note/domain/vo/MessageVO.java
- Service：note/service/MessageService.java
- ServiceImpl：note/service/impl/MessageServiceImpl.java
- Controller：note/controller/WxMessageController.java
- Mapper：note/mapper/UserMessageMapper.java
- Mapper XML：note/src/main/resources/com/littlewin/note/mapper/UserMessageMapper.xml
- 评论调用处：note/service/impl/CommentServiceImpl.java（第 66-67 行调用 sendMessage）
- 后端分页返回格式：{ code:200, data: { records:[], total:0 } }

### 步骤 1：修改 init_db.sql（新增 ALTER 语句）

在 init_db.sql 的 user_message 表定义处添加注释，说明需要执行以下 ALTER：

ALTER TABLE user_message ADD COLUMN title VARCHAR(100) DEFAULT NULL COMMENT '消息标题（系统通知用）';
ALTER TABLE user_message MODIFY COLUMN note_id BIGINT DEFAULT NULL COMMENT '关联笔记ID（系统公告可为空）';

同时在 init_db.sql 中直接更新表结构定义（ALTER 和 CREATE TABLE 都更新，保持一致）：
- CREATE TABLE user_message 中将 `note_id BIGINT NOT NULL` 改为 `note_id BIGINT DEFAULT NULL COMMENT '关联笔记ID（系统公告可为空）'`
- 删除 NOT NULL 约束，添加 DEFAULT NULL

### 步骤 2：修改 UserMessage 实体

在 UserMessage.java 中新增 title 字段：
private String title;

### 步骤 3：修改 MessageVO

在 MessageVO.java 中新增 title 字段：
private String title;

### 步骤 4：修改 MessageService 接口

新增以下方法（保留原有方法不变，不要破坏 CommentServiceImpl 的调用）：

/** 按类型分组查询未读数（互动消息 type∈(1,2,7,8)、系统通知 type∈(3,4,5,6)） */
Map<String, Integer> getUnreadCountGrouped(Long userId);

/** 分页查询消息列表（支持按类型组过滤） */
IPage<MessageVO> listMessages(Long userId, String group, int page, int size);

/** 发送消息（带标题，系统通知用） */
void sendMessage(Long receiverId, Long senderId, Long noteId, Long commentId, int type, String title, String content);

注意：保留原有 sendMessage(receiverId, senderId, noteId, commentId, type, content) 签名，避免破坏 CommentServiceImpl 调用。可在原方法中调用新方法，title 传 null。

### 步骤 5：修改 MessageServiceImpl

实现新增方法：

(1) getUnreadCountGrouped：
SELECT type, COUNT(*) FROM user_message WHERE receiver_id=? AND is_read=0 GROUP BY type
根据 type 值分为 interaction（1,2,7,8）和 notice（3,4,5,6）分别求和
返回 {"interactionCount": 5, "noticeCount": 2, "totalCount": 7}

(2) listMessages 带 group 过滤：
在 selectMessagePage SQL 基础上，根据 group 参数添加 WHERE 条件：
- group="interaction"：AND um.type IN (1,2,7,8)
- group="notice"：AND um.type IN (3,4,5,6)
- group 为空或 null：不加过滤（返回全部）

⚠️ 保持原有的"查询后自动标记已读"行为：新方法 listMessages(userId, group, page, size) 查询后，同样需要将返回的未读消息标记为已读（复用原方法中的 markReadByIds 逻辑）。

(3) sendMessage 带 title（改造原有方法体）：

将原 sendMessage 方法体（当前第 68-78 行，直接 new UserMessage + insert）替换为调用新重载方法：

@Override
public void sendMessage(Long receiverId, Long senderId, Long noteId, Long commentId, int type, String content) {
    sendMessage(receiverId, senderId, noteId, commentId, type, null, content);
}

@Override
public void sendMessage(Long receiverId, Long senderId, Long noteId, Long commentId, int type, String title, String content) {
    UserMessage msg = new UserMessage();
    msg.setReceiverId(receiverId);
    msg.setSenderId(senderId);
    msg.setNoteId(noteId);
    msg.setCommentId(commentId);
    msg.setType(type);
    msg.setTitle(title);
    msg.setContent(content);
    msg.setIsRead(0);
    msg.setCreateTime(LocalDateTime.now());
    userMessageMapper.insert(msg);
}

原方法签名不变（6 参数），内部委托新方法（7 参数，title 传 null）。CommentServiceImpl 调用不受影响。

### 步骤 6：修改 UserMessageMapper

新增方法签名：
int countUnreadGrouped(@Param("receiverId") Long receiverId, @Param("types") List<Integer> types);

IPage<MessageVO> selectMessagePageByGroup(Page<MessageVO> page,
    @Param("receiverId") Long receiverId, @Param("types") List<Integer> types);

### 步骤 7：修改 UserMessageMapper.xml

新增 SQL：

<select id="countUnreadGrouped" resultType="int">
    SELECT COUNT(*)
    FROM user_message
    WHERE receiver_id = #{receiverId}
      AND is_read = 0
      AND type IN
    <foreach collection="types" item="t" open="(" separator="," close=")">#{t}</foreach>
</select>

<select id="selectMessagePageByGroup" resultType="com.littlewin.note.domain.vo.MessageVO">
    SELECT um.id,
           um.type,
           um.title,
           um.content,
           um.is_read     AS isRead,
           um.create_time AS createTime,
           um.note_id     AS noteId,
           um.comment_id  AS commentId,
           CASE
               WHEN LENGTH(n.title) > 30 THEN CONCAT(LEFT(n.title, 30), '...')
               ELSE n.title
           END            AS noteTitle,
           su.nickname    AS senderName,
           su.avatar      AS senderAvatar
    FROM user_message um
             LEFT JOIN sys_user su ON um.sender_id = su.user_id
             LEFT JOIN note n ON um.note_id = n.note_id
    WHERE um.receiver_id = #{receiverId}
      AND um.type IN
    <foreach collection="types" item="t" open="(" separator="," close=")">#{t}</foreach>
    ORDER BY um.create_time DESC
</select>

同时修改原有的 selectMessagePage SQL，在 SELECT 中补充 um.title。

### 步骤 8：修改 WxMessageController

改造现有接口：

(1) GET /api/wx/messages/unread-count — 返回分组未读数
原：返回 { "count": N }
改为：返回 { "interactionCount": 5, "noticeCount": 2, "totalCount": 7 }

(2) GET /api/wx/messages — 新增 group 参数
原：@RequestParam(value = "page") int page, @RequestParam(value = "size") int size
改为：@RequestParam(value = "group", required = false) String group,
     @RequestParam(value = "page", defaultValue = "1") int page,
     @RequestParam(value = "size", defaultValue = "20") int size
调用新的 listMessages(userId, group, page, size)

其余接口（read-all、delete）不变。

验证：
1. GET /api/wx/messages/unread-count → 返回 { interactionCount, noticeCount, totalCount }
2. GET /api/wx/messages?group=interaction → 只返回 type 1,2,7,8 的消息
3. GET /api/wx/messages?group=notice → 只返回 type 3,4,5,6 的消息
4. GET /api/wx/messages?group=&page=1&size=20 → 返回全部消息（兼容）
5. CommentServiceImpl 调用 sendMessage 不受影响（原方法签名保留）
```

---

## 提示词 2：后端业务层（互动消息+管理端通知接口）

```
在 smart-note-system 中，实现两个功能：① 点赞/收藏时写入消息通知 ② 管理端发送系统公告接口。

⚠️ 设计决策：
- 点赞/收藏取消后不删消息（历史保留）
- 自己对自己不发消息
- 系统通知 sender_id = 操作者（管理员）的 userId

已有代码参考：
- 互动服务：note/service/impl/InteractionServiceImpl.java（注入了 NoteReactionMapper + NoteMapper）
- 评论发消息参考：note/service/impl/CommentServiceImpl.java 第 66 行
- 消息服务：note/service/MessageService.java（已有 sendMessage + 新增的带 title 重载）
- 管理端接口风格：note/controller/AdminNoteController.java（@Log、Result 返回）
- 用户查询：system/service/SysUserService.java（getUserPageList 分页查询）
- sys_user 表结构：user_id, auth_type, identifier, nickname, avatar, phone ...

### 功能 1：InteractionServiceImpl 接入消息通知

修改 InteractionServiceImpl.java：

(1) 新增注入 MessageService：
private final MessageService messageService;
（注意：MessageService → UserMessageMapper，InteractionService → NoteReactionMapper + NoteMapper，无循环依赖）

(2) 改造 toggle() 方法，在 toggleLike/toggleCollect 返回结果后判断并写入消息：

把原 toggle() 方法改为：

@Override
public InteractionResultVO toggle(Long userId, Long noteId, String type) {
    NoteReaction existing = noteReactionMapper.selectOne(
            new LambdaQueryWrapper<NoteReaction>()
                    .eq(NoteReaction::getNoteId, noteId)
                    .eq(NoteReaction::getUserId, userId)
    );

    InteractionResultVO result;
    if ("like".equals(type)) {
        result = toggleLike(userId, noteId, existing);
        if (result.getIsLiked()) {
            Note note = noteMapper.selectById(noteId);
            if (note != null && !note.getUserId().equals(userId)) {
                messageService.sendMessage(note.getUserId(), userId, noteId, null, 7, "点赞了你的笔记");
            }
        }
    } else if ("collect".equals(type)) {
        result = toggleCollect(userId, noteId, existing);
        if (result.getIsCollected()) {
            Note note = noteMapper.selectById(noteId);
            if (note != null && !note.getUserId().equals(userId)) {
                messageService.sendMessage(note.getUserId(), userId, noteId, null, 8, "收藏了你的笔记");
            }
        }
    } else {
        throw new ServiceException("不支持的互动类型: " + type);
    }
    return result;
}

⚠️ 为什么放在 toggle() 而不是 toggleLike()/toggleCollect() 内部：
- toggleLike/toggleCollect 是 private 方法，isLiked/isCollected 是局部变量，在 return 之前有多个分支
- 放在 toggle() 中，通过返回的 InteractionResultVO 判断结果，逻辑更清晰，不会遗漏分支

注意：
- type=7 表示点赞，type=8 表示收藏
- content 参数传动作描述（"点赞了你的笔记"/"收藏了你的笔记"），title 传 null（互动消息不需要 title）
- 只在操作成功时发消息，取消点赞/收藏时不发也不删（方案 A）

### 功能 2：AdminNotificationController 管理端通知接口

新建文件：

1. AdminNotificationController.java（com.littlewin.note.controller）

路径：/api/admin/notifications
实现 1 个接口：

POST /api/admin/notifications/send — 发送系统公告
- 请求体 DTO：AdminNotificationDTO
  - title: String（@NotBlank，公告标题）
  - content: String（@NotBlank，公告内容）
  - type: Integer（@NotNull，3=审核通过 / 4=审核不通过 / 5=违规下架 / 6=系统公告）
  - userIds: List<Long>（可选，指定用户ID列表。为空则发送给全部用户）
  - noteId: Long（可选，关联笔记ID）
- 添加 @Log 注解
- 返回 Result<Void>

2. AdminNotificationDTO.java（com.littlewin.note.domain.dto）

@Data
@NotBlank title
@NotBlank content
@NotNull type
List<Long> userIds（可选）
Long noteId（可选）

3. AdminNotificationService.java + AdminNotificationServiceImpl.java（com.littlewin.note.service）

发送逻辑：
- 如果 userIds 不为空，只发送给指定用户
- 如果 userIds 为空，发送给全部用户（查询 sys_user 表获取所有 user_id）
- 使用 MessageService.sendMessage 逐条写入（注意 sender_id = 当前管理员 userId）
- 注意：系统公告的 receiver_id 是用户，sender_id 是管理员

⚠️ 发给全部用户的优化：
- 用户量少（毕业项目级别），直接循环 sendMessage 即可
- 不需要 batch 优化

验证：
1. POST /api/admin/notifications/send { title:"新功能上线", content:"AI摘要功能已开放", type:6 } → 全部用户收到 type=6 消息
2. POST /api/admin/notifications/send { title:"审核通过", content:"你的笔记已通过", type:3, userIds:[1,2] } → 只有用户 1、2 收到
3. 查看 user_message 表，确认 title、content、type、sender_id 正确
```

---

## 提示词 3：小程序端消息页改造

```
改造 smart-note-mp 小程序的消息页面，支持互动消息/系统通知双 Tab 切换。

已有代码参考：
- 当前页面：pages/message/message.vue（完整消息列表，无 Tab）
- API：api/modules/message.js（getUnreadCount / getList / readAll / remove）
- API 配置：api/config.js（APIS.MESSAGE 路径）
- request.js：uni.request 封装，返回 result.data（已解包 code/data）
- 后端返回格式：getUnreadCount 返回 { interactionCount, noticeCount, totalCount }；getList 返回 { records:[], total:0 }
- uView 组件：u-empty、u-icon（已在项目中使用）

需要修改 2 个文件：

### 1. api/modules/message.js

修改 getUnreadCount，返回格式变化（从 {count} 变为 {interactionCount, noticeCount, totalCount}），前端适配即可，API 调用路径不变。
⚠️ 原 message.vue 中使用 unreadRes?.count 获取未读数，改为 unreadRes?.data?.totalCount（具体赋值见下方 message.vue 说明）。

修改 getList，新增 group 参数：
getList(page = 1, size = 20, group = '') {
    const params = { page, size }
    if (group) params.group = group
    return request({
      url: APIS.MESSAGE.LIST,
      method: 'GET',
      params
    })
},

### 2. pages/message/message.vue — 完整改造

改造方案：在现有页面基础上，顶部操作栏下方添加两个 Tab，数据分开展示。

UI 结构：
┌─────────────────────────────────┐
│  消息                    全部已读 │
├────────────────┬────────────────┤
│  互动消息(5)   │   系统通知(2)   │
├────────────────┴────────────────┤
│  当前 Tab 的消息列表             │
│  ...                            │
└─────────────────────────────────┘

核心改造点：

(1) 新增状态：
- activeTab: ref('interaction')  // 'interaction' 或 'notice'
- interactionCount: ref(0)
- noticeCount: ref(0)

(2) Tab 切换逻辑：
- 点击 Tab 时切换 activeTab，重新加载对应列表（reset=true）
- loadMessages 方法根据 activeTab 传 group 参数：
  const res = await messageApi.getList(page.value, 20, activeTab.value)

(3) 未读数更新：
- loadMessages 成功后调用 getUnreadCount，更新 interactionCount 和 noticeCount
- 后端返回格式变化，具体赋值如下：
  const unreadRes = await messageApi.getUnreadCount()
  // unreadRes 结构（request.js 已解包 data）：{ interactionCount: 5, noticeCount: 2, totalCount: 7 }
  interactionCount.value = unreadRes.interactionCount || 0
  noticeCount.value = unreadRes.noticeCount || 0
  uni.setStorageSync('unreadCount', unreadRes.totalCount || 0)

(4) 全部已读（onReadAll）：
- 保持不变，调用 readAll() 后更新两个计数为 0

(5) 消息列表展示逻辑改造：
- 互动消息 Tab（type 1/2/7/8）：
  - type=1：显示 "评论了你的笔记"
  - type=2：显示 "回复了你的笔记"
  - type=7：显示 "点赞了你的笔记"
  - type=8：显示 "收藏了你的笔记"
  - 展示：发送者头像 + 名字 + 动作文本 + 笔记标题 + 内容摘要
  - 点击跳转笔记详情

- 系统通知 Tab（type 3/4/5/6）：
  - type=3：icon="🔔"，标题 "审核通过"
  - type=4：icon="🔔"，标题 "审核不通过"
  - type=5：icon="⚠️"，标题 "违规下架"
  - type=6：icon="📢"，标题 "系统公告"
  - 展示：系统图标（不用头像）+ 标题（用 message.title 或根据 type 自动生成）+ 内容 + 时间
  - 点击跳转笔记详情（noteId 为空时不跳转，提示"系统公告"）

(6) 系统通知的样式：
系统通知不展示发送者头像，改为左侧显示图标 + 消息标题：

┌───────────────────────────┐
│ 🔔 审核通过                │
│    你的笔记「xxx」已通过审核 │
│                    10:30  │
├───────────────────────────┤
│ 📢 系统公告                │
│    AI 摘要功能已开放...     │
│                    昨天    │
└───────────────────────────┘

(7) 删除按钮：
- 互动消息和系统通知都可以删除，保持现有 onDelete 逻辑不变

(8) CSS 样式：
- Tab 栏：flex 布局，两个 Tab 各占 50%，active Tab 底部有蓝色指示条
- 未读数显示在 Tab 文字后面，用圆角小徽章（红底白字），如 "互动消息(5)"
- 系统通知的图标用固定宽高的 view 替代 image
- 保持与现有页面的整体风格一致

⚠️ 注意事项：
- onShow 时加载当前 Tab 的数据
- onPullDownRefresh 刷新当前 Tab
- onReachBottom 加载当前 Tab 的更多数据
- Tab 切换时 page 重置为 1

验证：
1. 打开消息页 → 默认显示互动消息 Tab
2. 点击系统通知 Tab → 切换到系统通知列表
3. Tab 标题旁显示未读数
4. TabBar 消息图标显示总未读数
5. 互动消息展示评论/回复/点赞/收藏通知
6. 系统通知展示审核/公告通知
7. 点击消息跳转笔记详情（系统公告 noteId 为空时不跳转）
8. 全部已读 → 两个 Tab 未读数都清零
```

---

## 提示词 4：Web 端通知管理页面

```
在 smart-note-ui Web 前端中，实现通知管理页面（发送系统公告）。

已有代码参考：
- 页面风格：src/views/note/list/index.vue 或 src/views/system/user/index.vue（ProTable 列表）
- API 风格：src/api/modules/category.ts（直接 export interface + 函数）
- 分页格式：{ records: [], total: 0 }
- init_sys_data.sql 中菜单配置格式（menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order）
- 父菜单 5000（系统监控），已有子菜单 5010(操作审计)、5015(行为日志)、5020(AI监控)

⚠️ 本页面 Day 11 后端已实现 AdminNotificationController，前端直接对接。

需要创建/修改 3 个文件：

### 1. init_sys_data.sql — 新增菜单配置

在 5020(AI监控) 之后添加：
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(5030, 5000, 'notification', '/monitor/notification', '/monitor/notification/index', 'C', '通知管理', 'Bell', 'sys:notification:send', 4);

### 2. src/api/modules/notification.ts — 通知管理 API

import http from "@/api";

export interface ReqNotificationParams {
  pageNum?: number;
  pageSize?: number;
}

export interface NotificationDTO {
  title: string;
  content: string;
  type: number;
  userIds?: number[];
  noteId?: number;
}

export const sendNotification = (data: NotificationDTO) => {
  return http.post(`/admin/notifications/send`, data);
};

注意：当前只需要"发送"功能，不需要"查看已发送列表"接口（后端未实现）。
页面主体是一个发送表单，不是列表。

### 3. src/views/monitor/notification/index.vue — 通知管理页面

⚠️ 重要：这个页面不是 ProTable 列表，而是发送通知的表单页面。

页面结构：
- 顶部标题："发送系统通知"
- 表单区域（el-form）：
  - 通知类型（el-select）：审核通过(3)、审核不通过(4)、违规下架(5)、系统公告(6)
  - 通知标题（el-input）：placeholder="请输入通知标题"
  - 通知内容（el-input type="textarea"）：placeholder="请输入通知内容"，rows=4
  - 接收范围（el-radio-group）：全部用户 / 指定用户
  - 指定用户（el-input）：仅当选择"指定用户"时显示，placeholder="请输入用户ID，多个用英文逗号分隔"（如 "1,2,3"）
- 底部按钮：发送

表单校验：
- title：@blur 校验，必填
- content：@blur 校验，必填
- type：@change 校验，必填

发送逻辑：
- 提交时将 userIds 字符串 "1,2,3" 解析为数组 [1, 2, 3]
- 调用 sendNotification API
- 成功后 ElMessage.success("通知发送成功")，清空表单

⚠️ 注意：
- 选择"全部用户"时，userIds 不传（后端理解为全部）
- 选择"指定用户"时，解析逗号分隔的 ID 字符串为 number[]
- 页面使用 Element Plus 表单组件，不需要 ProTable

验证：
1. 访问 /monitor/notification → 显示发送通知表单
2. 选择类型+输入标题内容+选择全部用户 → 发送成功
3. 选择指定用户+输入 "1,2" → 只有用户 1、2 收到通知
4. 表单校验：标题或内容为空时提示
5. 小程序端查看系统通知 Tab → 能看到刚发送的公告
```

---

# ⏱️ Day 11 执行顺序

| 顺序 | 提示词 | 前置依赖 |
|:--:|:---|:--:|
| 1️⃣ | 提示词 1：后端基础层 | 无 |
| 2️⃣ | 提示词 2：后端业务层 | 提示词 1（依赖新的 MessageService） |
| 3️⃣ | 提示词 3：小程序端改造 | 提示词 1（依赖新的 Controller 接口） |
| 4️⃣ | 提示词 4：Web 通知管理页面 | 提示词 2（依赖 AdminNotificationController） |

> 提示词 1 必须先执行。提示词 2 和 3 依赖提示词 1 完成。提示词 4 依赖提示词 2 完成。
> 提示词 3 和 2 可以并行执行（小程序只依赖 Controller，不依赖 AdminNotification）。

---

# 🔍 Day 11 涉及的文件清单

| 文件 | 改动类型 | 说明 |
|:---|:---|:---|
| init_db.sql | 修改 | ALTER TABLE 新增 title、修改 note_id |
| UserMessage.java | 修改 | 新增 title 字段 |
| MessageVO.java | 修改 | 新增 title 字段 |
| MessageService.java | 修改 | 新增 3 个方法 |
| MessageServiceImpl.java | 修改 | 实现新增方法 |
| UserMessageMapper.java | 修改 | 新增 2 个方法 |
| UserMessageMapper.xml | 修改 | 新增 SQL + 修改已有 SQL |
| WxMessageController.java | 修改 | 改造 unread-count 和 list 接口 |
| InteractionServiceImpl.java | 修改 | 注入 MessageService，点赞/收藏写消息 |
| AdminNotificationController.java | 新建 | 管理端通知接口 |
| AdminNotificationDTO.java | 新建 | 通知发送 DTO |
| AdminNotificationService.java | 新建 | 通知服务接口 |
| AdminNotificationServiceImpl.java | 新建 | 通知服务实现 |
| message.js | 修改 | getList 新增 group 参数 |
| config.js | 不变（检查确认） | APIS.MESSAGE 路径配置，确认 LIST 路径兼容 group 参数 |
| message.vue | 重写 | 双 Tab + 两种消息样式 |
| notification.ts | 新建 | Web 通知 API |
| notification/index.vue | 新建 | 通知管理页面 |
| init_sys_data.sql | 修改 | 新增菜单 5030 |
