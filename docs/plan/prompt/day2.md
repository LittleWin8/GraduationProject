# 📋 Day 2 任务清单：互动模块
## 🔧 后端（2个接口组）

|       接口       |                  路径                   |                           核心逻辑                           |
| :--------------: | :-------------------------------------: | :----------------------------------------------------------: |
|  点赞/收藏切换   |      `POST /api/wx/interactions `       | `type=like 切换 attitude 0↔1；type=collect 切换 is_favorite 0↔1；返回切换后状态+计数` |
| 单条互动状态 GET | `/api/wx/interactions/status/{noteId} ` |     `返回 isLiked、isCollected、likeCount、collectCount`     |
|   批量互动状态   |   `POST /api/wx/interactions/status `   | `请求 {noteIds:[]}，返回 Map<noteId, {isLiked, isCollected}>` |

   

## 📱 小程序（3个页面改造）

|    页面    |                     改造内容                      |
| :--------: | :-----------------------------------------------: |
|   社区页   | 列表加载后批量查询 isLiked 状态；点赞对接真实 API |
| 笔记详情页 | 底部固定互动栏（点赞+收藏+评论入口）；对接互动API |
|  个人中心  |   收藏/点赞列表项增加取消操作；统计数据实时同步   |

## 🔗 联调验收标准
- 点赞→计数+1→再点取消→计数-1
- 收藏→个人中心可见→取消收藏→消失
- 详情页点赞/收藏状态与社区页一致



# 📝 Day 2 提示词

## 提示词 1：后端互动接口（点赞/收藏切换 + 状态查询）
```
在 smart-note-system 的 note 模块中，实现互动模块的三个接口。

数据库表 note_reaction 结构：
- id (BIGINT AUTO_INCREMENT PK)
- note_id (BIGINT)
- user_id (BIGINT)
- attitude (TINYINT, 0无/1点赞/2踩)
- is_favorite (TINYINT, 0未收藏/1已收藏)
- create_time, update_time
- UNIQUE KEY uk_note_user (note_id, user_id)

已有代码：
- 实体类：note/domain/entity/NoteReaction.java（字段：id, noteId, userId, attitude, isFavorite, createTime, updateTime）
- Mapper：note/mapper/NoteReactionMapper.java（继承 BaseMapper，已有 countLikesByUserNotes、selectFavoriteNotePage、selectLikedNotePage）
- Mapper XML：note/mapper/NoteReactionMapper.xml（已有上述 SQL）
- Controller 风格参考：WxNoteController.java（@Log 注解、SecurityUtils 取用户、Result 返回）
- VO 风格参考：NoteStatsVO.java（@Builder 模式）

需要实现：

1. POST /api/wx/interactions — 点赞/收藏切换
   - 请求体：{ noteId: Long, type: String }，type 取值 "like" 或 "collect"
   - 逻辑：先查 note_reaction 表是否存在该用户对该笔记的记录
     - type=like：不存在则插入 attitude=1；存在且 attitude=1 则改为0（取消）；存在且 attitude=0 则改为1
     - type=collect：同理切换 is_favorite 字段 0↔1
   - 返回切换后状态：
     - type=like 返回 { isLiked: boolean, likeCount: int }
     - type=collect 返回 { isCollected: boolean, collectCount: int }
   - likeCount/collectCount 聚合统计该笔记总数
   - 添加 @Log 注解

2. GET /api/wx/interactions/status/{noteId} — 单条互动状态
   - 返回：{ isLiked: boolean, isCollected: boolean, likeCount: int, collectCount: int }

3. POST /api/wx/interactions/status — 批量互动状态
   - 请求体：{ noteIds: [100, 101, 102] }
   - 返回：Map<String, InteractionStatusVO>，key 为 noteId 字符串

请创建：
- WxInteractionController.java（com.littlewin.note.controller）
- InteractionService.java + InteractionServiceImpl.java（com.littlewin.note.service）
- InteractionResultVO.java（点赞/收藏切换结果）
- InteractionStatusVO.java（单条状态 VO）
- NoteReactionMapper.xml 中补充 countLikesByNoteId、countCollectsByNoteId 两个统计 SQL
```
## 提示词 2：小程序社区页点赞对接
```
在 smart-note-mp 小程序中，将社区页点赞功能对接真实后端 API。

当前代码状态：
- 社区页：pages/community/community.vue（已对接笔记列表 API，mapNoteItem 中 isLiked 硬编码 false，onLike 方法为空）
- 笔记卡片：components/notecard/index.vue（有点赞 UI 和 @like 事件）
- 互动 API：api/modules/interaction.js（已有 interact 和 getStatus 方法）
- API 配置：api/config.js（INTERACTION 已配置）

需要改造：

1. 社区页 loadNotes 成功后，批量查询互动状态
   - 收集当前页所有 noteId，调用 interactionApi.getStatus(noteIds)
   - 将返回的 isLiked 回填到 noteList 对应项

2. onLike 方法对接真实 API
   - 调用 interactionApi.interact(noteId, 'like')
   - 用返回的 isLiked 和 likeCount 更新对应笔记
   - 添加防抖（500ms 内不可重复点击同一笔记）

3. mapNoteItem 中增加 isCollected 字段预留

4. 笔记卡片 notecard/index.vue 的 note props 新增 isCollected
```
## 提示词 3：小程序笔记详情页互动功能（点赞+收藏）
```
在 smart-note-mp 小程序的笔记详情页中，添加底部互动操作栏。

当前代码状态：
- 详情页：pages/note-detail/note-detail.vue（已有 Markdown 渲染、作者操作栏，但无互动按钮）
- 后端 NoteDetailVO 返回字段：isLiked(Integer, 0/1)、likes(Integer)、comments(Integer)
- 互动 API：api/modules/interaction.js（已有 interact 和 getStatus）

需要实现：

1. 底部固定互动栏（position: fixed）
   - 三个按钮等分：点赞（心形）、收藏（星形）、评论（chat 图标+评论数）
   - 点赞：isLiked 时红色实心 heart-fill，未点赞灰色空心 heart，显示 likeCount
   - 收藏：isCollected 时黄色实心 star-fill，未收藏灰色空心 star
   - 评论：chat 图标 + noteData.comments 数量（点击功能 Day 3 实现）

2. 互动逻辑
   - 页面加载时从 noteData 获取 isLiked/likes
   - 调用 interactionApi.getStatus(noteId) 获取 isCollected 和 collectCount
   - 点赞：调用 interactionApi.interact(noteId, 'like')，切换状态，更新计数
   - 收藏：调用 interactionApi.interact(noteId, 'collect')，切换状态，更新计数
   - 操作后 toast 提示
   - 防抖处理

3. 样式
   - 底部栏高度 100rpx，白色背景，顶部 1rpx solid #eee
   - 图标大小 24，文字 22rpx
   - 笔记内容区域底部加 padding-bottom: 120rpx 避免遮挡
```
## 提示词 4：小程序个人中心收藏/点赞列表完善
```
在 smart-note-mp 小程序的个人中心页中，完善收藏和点赞列表的交互。

当前代码状态：
- 个人中心：pages/profile/profile.vue（三个 Tab：我的笔记/我的收藏/赞过，已对接 API）
- 互动 API：api/modules/interaction.js（已有 interact 方法）
- 后端接口：GET /api/wx/note/favorites、GET /api/wx/note/liked 已实现

需要改造：

1. 收藏列表项增加取消收藏操作
   - 每条右侧添加黄色实心星星图标，点击取消收藏
   - 调用 interactionApi.interact(noteId, 'collect')
   - 取消后从列表移除，收藏统计数 -1

2. 赞过列表项增加取消点赞操作
   - 每条右侧添加红色实心心形图标，点击取消点赞
   - 调用 interactionApi.interact(noteId, 'like')
   - 取消后从列表移除，获赞统计数 -1

3. 从详情页返回时数据同步
   - onShow 已有 fetchStats() 和列表加载，确认返回后刷新正常

4. "查看全部"跳转完善
   - goToFullList('favorites') → note-list?type=favorites
   - goToFullList('liked') → note-list?type=liked
   - note-list 页面根据 type 调用对应 API
```
# 💡 使用顺序与时间建议

| 顺序 |           提示词           | 预计耗时 |   前置依赖   |
| :--: | :------------------------: | :------: | :----------: |
|  1️⃣   |   提示词 1：后端互动接口   |   2-3h   | Day 1 已完成 |
|  2️⃣   |  Knife4j/Postman 验证接口  |  30min   |   提示词 1   |
|  3️⃣   |  提示词 2：社区页点赞对接  |    1h    |   提示词 1   |
|  4️⃣   |  提示词 3：详情页互动功能  |   1.5h   |   提示词 1   |
|  5️⃣   | 提示词 4：个人中心列表完善 |    1h    |   提示词 1   |
|  6️⃣   |         全流程联调         |    1h    |   全部完成   |

> 每个提示词都包含了 当前已有代码的精确位置和字段 ，可以直接定位开发，无需反复确认数据结构。