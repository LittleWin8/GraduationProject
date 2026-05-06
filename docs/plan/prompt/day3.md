# 📋 Day 3 任务清单：评论模块 + 社区页分类美化
## 任务概览

| 序号 |          任务          |   端   | 优先级 |
| :--: | :--------------------: | :----: | :----: |
|  1   |   后端评论 CRUD 接口   |  后端  |  🔴 高  |
|  2   | 小程序笔记详情页评论区 | 小程序 |  🔴 高  |
|  3   |   社区页分类导航美化   | 小程序 |  🔴 高  |
|  4   |        联调验收        |  全端  |  🔴 高  |

​     

## 🔧 任务 1：后端评论接口
已有基础 ：

- 数据库表 `note_comment` （init_db.sql 已建表）
- API 路径已配置： `/api/wx/comments` （GET/POST/DELETE）
- 小程序 API 模块已定义： `api/modules/comment.js`

需实现 3 个接口 ：

|   接口   |                    路径                     |                  说明                   |
| :------: | :-----------------------------------------: | :-------------------------------------: |
| 评论列表 | ` GET /api/wx/comments?noteId=&page=&size=` |          分页查询，按时间倒序           |
| 发表评论 |           `POST /api/wx/comments`           | 请求体 `{ noteId, content, parentId? }` |
| 删除评论 |       `DELETE /api/wx/comments/{id} `       |               仅本人可删                |

## 📱 任务 2：小程序详情页评论区
在笔记详情页底部互动栏上方，添加评论区：

- 评论列表（分页加载）
- 评论输入框（底部弹出）
- 删除自己的评论
- 评论数联动
## 📱 任务 3：社区页分类导航美化
已有基础 ：

- 后端` GET /api/wx/categories/list` 已返回树形结构
- 小程序 `categoryApi.getList()` 已封装
- 笔记列表 API 已支持 `categoryId` 筛选
- `TreeUtils.java` 已实现树构建

改造目标 ：社区页顶部展示大分类-小分类两级导航，按分类筛选笔记



# 📝 Day 3 提示词
## 提示词 1：后端评论接口
```
在 smart-note-system 的 note 模块中，实现评论模块的三个接口。

数据库表 note_comment 结构（init_db.sql 已建表）：
- comment_id (BIGINT AUTO_INCREMENT PK)
- note_id (BIGINT NOT NULL)
- user_id (BIGINT NOT NULL)
- content (VARCHAR(500) NOT NULL)
- parent_id (BIGINT DEFAULT NULL COMMENT '回复的评论ID，null为顶级评论')
- create_time (DATETIME DEFAULT CURRENT_TIMESTAMP)
- del_flag (TINYINT DEFAULT 0)
- INDEX idx_note_id (note_id)

已有代码参考：
- Controller 风格：WxInteractionController.java（@Log、SecurityUtils、Result 返回）
- Service 风格：InteractionServiceImpl.java（@Service、@RequiredArgsConstructor）
- VO 风格：InteractionResultVO.java（@Builder 模式）
- Mapper 风格：NoteReactionMapper.java（继承 BaseMapper）

需要实现：

1. GET /api/wx/comments — 评论列表
   - 参数：noteId(Long, 必填), page(int, 默认1), size(int, 默认10)
   - 返回 IPage<CommentVO>，按 create_time DESC 排序
   - CommentVO 字段：commentId, noteId, content, parentId, createTime,
     author(昵称), avatar(头像), isOwner(是否当前用户的评论)
   - 只查 del_flag=0 的记录
   - 关联 sys_user 表查 author 和 avatar

2. POST /api/wx/comments — 发表评论
   - 请求体：{ noteId: Long, content: String, parentId: Long(可选) }
   - content 校验：非空，长度 1-500
   - 自动填充 userId（SecurityUtils）、createTime
   - 返回 CommentVO（发表后的完整评论信息）
   - 添加 @Log 注解

3. DELETE /api/wx/comments/{id} — 删除评论
   - 仅本人可删除（校验 userId）
   - 逻辑删除：del_flag 改为 1
   - 添加 @Log 注解
  
4. 写上简洁的注释

请创建：
- WxCommentController.java（com.littlewin.note.controller）
- CommentService.java + CommentServiceImpl.java（com.littlewin.note.service）
- CommentCreateDTO.java（com.littlewin.note.domain.dto）
- CommentVO.java（com.littlewin.note.domain.vo）
- NoteCommentMapper.java + NoteCommentMapper.xml（com.littlewin.note.mapper）
- NoteComment.java（com.littlewin.note.domain.entity，@TableName("note_comment")）
```
## 提示词 2：小程序笔记详情页评论区
```
在 smart-note-mp 小程序的笔记详情页中，添加评论区功能。

当前代码状态：
- 详情页：pages/note-detail/note-detail.vue（已有底部互动栏：点赞/收藏/评论入口）
- 评论 API：api/modules/comment.js（已定义 getList/create/remove 方法）
- API 配置：api/config.js（COMMENT.LIST/CREATE/DELETE 已配置）
- 底部互动栏的评论按钮目前点击显示"评论功能即将上线"

需要实现：

1. 评论区 UI（在 note-body 下方、底部互动栏上方）
   - 评论区标题："评论 (count)"
   - 评论列表：每条显示头像+昵称+时间+内容
   - 如果有 parentId，显示"回复 @xxx"前缀
   - 自己的评论右侧显示删除按钮（红色小字）
   - 空状态："暂无评论，快来抢沙发"
   - 分页加载：上拉加载更多

2. 评论输入
   - 底部互动栏的评论按钮点击后，弹出输入弹窗（u-popup mode="bottom"）
   - 输入框：textarea，placeholder="写下你的评论..."，maxlength=500
   - 发送按钮：点击调用 commentApi.create({ noteId, content })
   - 发送成功后：清空输入、关闭弹窗、刷新评论列表、更新 noteData.comments 计数
   - 回复功能：点击某条评论的"回复"按钮，弹窗 placeholder 变为"回复 @xxx.."，传入 parentId

3. 删除评论
   - 点击自己评论的删除按钮，确认弹窗后调用 commentApi.remove(commentId)
   - 删除成功后从列表移除，noteData.comments 计数 -1

4. 页面加载时
   - loadDetail 成功后，调用 loadComments(noteId) 加载第一页评论
   - 评论数据独立管理（commentList、commentPage、commentHasMore）

5. 样式
   - 评论区背景白色，圆角卡片样式
   - 每条评论间距 20rpx，头像 60rpx 圆形
   - 昵称 26rpx #303133，时间 22rpx #c0c4cc，内容 28rpx #606266
```
## 提示词 3：社区页分类导航美化
```
在 smart-note-mp 小程序的社区页中，添加两级分类导航，并按分类筛选笔记。

当前代码状态：
- 社区页：pages/community/community.vue
  - 顶部：搜索框 + 最新/最热 Tab
  - 主体：note-card 瀑布流列表
  - 已对接 noteApi.getNotes('public', page, size, filters)，filters 
  支持 categoryId
- 分类 API：api/modules/category.js（已有 getList 方法）
- 后端接口：GET /api/wx/categories/list 返回树形结构
  - 返回格式：[{ categoryId, name, parentId, sortOrder, children: [{ categoryId, name, ... }] }]
- 笔记列表 API 已支持 categoryId 筛选参数

需要改造 community.vue：

1. 顶部布局重构（从上到下）：
   ┌─────────────────────────────┐
   │  🔍 搜索框                   │
   ├─────────────────────────────┤
   │  大分类横向滚动 Tab（一级）    │
   │  [全部] [技术] [生活] [读书]  │
   ├─────────────────────────────┤
   │  小分类横向滚动（二级，选中大分类后显示）│
   │  [全部] [前端] [后端] [AI]    │
   ├─────────────────────────────┤
   │  排序 Tab：[最新] [最热]      │
   └─────────────────────────────┘

2. 分类数据加载
   - onShow 时调用 categoryApi.getList() 获取分类树
   - 大分类列表 = 顶层节点，前面加一个"全部"选项（categoryId=null）
   - 小分类列表 = 选中大分类的 children，前面加"全部"选项
   - 选中"全部"大分类时，不显示小分类行

3. 分类筛选逻辑
   - 选中大分类（非"全部"）+ 选中"全部"小分类 → 传大分类 categoryId
   - 选中大分类 + 选中小分类 → 传小分类 categoryId
   - 选中"全部"大分类 → 不传 categoryId
   - 切换分类时重置 page=1，重新 loadNotes(true)
   - loadNotes 的 filters 中加上 categoryId

4. 样式要求
   - 大分类 Tab：u-tabs 组件，lineColor="#1890ff"，选中加粗
   - 小分类行：横向 scroll-view，每个小分类是圆角标签（选中蓝色背景白字，未选
   中灰底灰字）
   - 小分类标签间距 16rpx，padding 12rpx 28rpx，font-size 24rpx，
   border-radius 30rpx
   - 搜索框和分类区域用 u-sticky 固定在顶部
   - 整体背景 #f5f7f9，分类区域背景 #fff

5. 交互细节
   - 切换大分类时，小分类自动重置为"全部"
   - 切换分类后平滑滚动到列表顶部
   - 搜索 + 分类 + 排序三个条件可组合使用
   
6. 写上简洁的注释
```
# ⏱️ Day 3 执行顺序
| 顺序 |          提示词          | 预计耗时 |   前置依赖   |
| :--: | :----------------------: | :------: | :----------: |
|  1️⃣   |  提示词 1：后端评论接口  |    2h    | Day 2 已完成 |
|  2️⃣   |         接口验证         |  30min   |   提示词 1   |
|  3️⃣   |  提示词 2：详情页评论区  |   1.5h   |   提示词 1   |
|  4️⃣   | 提示词 3：社区页分类美化 |    2h    |  无后端依赖  |
|  5️⃣   |        全流程联调        |    1h    |   全部完成   |

​    

# 提示词：站内消息通知（评论提醒）

```
在 smart-note-system 后端 + smart-note-mp 小程序中，实现站内消息通知功能。
当有人评论笔记时，笔记作者收到应用内提示；点赞/收藏不产生消息。

===== 第一部分：数据库 =====

在 init_db.sql 中新增消息表：

CREATE TABLE user_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者用户ID（笔记作者）',
    sender_id BIGINT NOT NULL COMMENT '触发者用户ID（评论者）',
    note_id BIGINT NOT NULL COMMENT '关联笔记ID',
    comment_id BIGINT COMMENT '关联评论ID',
    type TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型：1评论, 2回复(预留)',
    content VARCHAR(500) COMMENT '消息内容摘要（评论内容前50字）',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读, 1已读',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_receiver_read (receiver_id, is_read),
    INDEX idx_receiver_time (receiver_id, create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

===== 第二部分：后端接口 =====

已有代码参考：
- Controller 风格：WxCommentController.java
- Service 风格：CommentServiceImpl.java
- VO 风格：CommentVO.java（@Builder）
- Mapper 风格：NoteCommentMapper.java + NoteCommentMapper.xml

需要实现 4 个接口：

1. GET /api/wx/messages/unread-count — 未读消息数
   - 返回：{ count: int }
   - 用于 TabBar 红点/数字角标

2. GET /api/wx/messages — 消息列表（分页）
   - 参数：page(默认1), size(默认20)
   - 返回 IPage<MessageVO>，按 create_time DESC
   - MessageVO 字段：id, type, content, isRead, createTime,
     noteId, noteTitle(笔记标题，截取前30字),
     senderName(触发者昵称), senderAvatar(触发者头像),
     commentId
   - 关联查询：sys_user 取 senderName/senderAvatar，note 取 noteTitle
   - 查询后自动将该页消息标记为已读（is_read=1）

3. POST /api/wx/messages/read-all — 全部标记已读
   - 将当前用户所有未读消息 is_read 改为 1
   - 返回：成功

4. DELETE /api/wx/messages/{id} — 删除单条消息
   - 仅本人可删（校验 receiver_id）
   - 物理删除

===== 第三部分：评论时写入消息 =====

修改 CommentServiceImpl.createComment 方法：
- 在 noteCommentMapper.insert(comment) 之后，
- 查询笔记作者ID（从 note 表查 user_id）
- 如果评论者不是笔记作者（userId != noteAuthorId），则插入一条 user_message 记录
- content 取评论内容前50字
- type=1（评论），如果是回复评论（parentId 不为空），type=2

注意：CommentServiceImpl 中注入 NoteMapper 和 UserMessageMapper（新增）

===== 第四部分：小程序消息入口 =====

当前代码状态：
- TabBar：components/custom-tab-bar/index.vue（3个Tab：社区/创作/我的）
- 个人中心：pages/profile/profile.vue（有"设置""关于我们"等入口）
- API 配置：api/config.js（需要新增 MESSAGE 路径）

需要改造：

1. api/config.js 新增 MESSAGE 路径配置：
   MESSAGE: {
     LIST: '/api/wx/messages',
     UNREAD_COUNT: '/api/wx/messages/unread-count',
     READ_ALL: '/api/wx/messages/read-all',
     DELETE: '/api/wx/messages'    // /{id}
   }

2. 新建 api/modules/message.js：
   - getUnreadCount()
   - getList(page, size)
   - readAll()
   - remove(id)

3. custom-tab-bar/index.vue 改造：
   - "我的" Tab 图标右上角显示未读消息数红点/数字角标
   - onShow 时调用 messageApi.getUnreadCount() 获取未读数
   - 未读数 > 0 时显示红色角标（数字或红点）
   - 未读数 = 0 时隐藏角标

4. 新建 pages/message/message.vue 消息列表页：
   - 顶部标题"消息" + 右上角"全部已读"按钮
   - 消息列表：每条显示发送者头像+昵称+"评论了你的笔记"+笔记标题+评论摘要+时间
   - 未读消息左侧加蓝色小圆点标识
   - 点击消息跳转到笔记详情页 /pages/note-detail/note-detail?id=noteId
   - 左滑删除单条消息
   - 空状态："暂无消息"
   - 分页加载

5. pages.json 注册新页面：
   pages/message/message → navigationBarTitleText: "消息"

6. profile.vue 消息入口：
   - 在"我的标签"上方添加一行"消息通知"（带图标 bell）
   - 右侧显示未读数角标（红色数字，无未读则不显示）
   - 点击跳转 /pages/message/message
   - onShow 时刷新未读数

7. 角标数据全局共享：
   - 在 custom-tab-bar 和 profile.vue 中都需要未读数
   - 建议使用 uni.setStorageSync('unreadCount') 缓存
   - 进入消息页后清除角标（readAll）
   - 从消息页返回时刷新角标
   
8.写上简洁的注释
```

