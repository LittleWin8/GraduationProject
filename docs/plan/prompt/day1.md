# Day1 提示词

## 提示词 1：后端笔记创建接口

```
在 smart-note-system 的 note 模块中，实现笔记创建接口 POST /api/wx/
notes。

参考已有代码：
- Controller 风格参考 WxTagController.java
- Service 风格参考 NoteTagServiceImpl.java
- 实体类参考 note/domain/entity/Note.java、NoteTagRel.java
- Mapper 风格参考 NoteTagMapper.java 和对应的 XML

接口要求：
1. 请求体字段：title(必填)、content(必填)、categoryId(可选)、
isPublic(默认1)、tagIds(数组，可选)
2. 从 SecurityUtils 获取当前用户ID作为作者
3. 笔记状态默认为1(正常)，del_flag默认0
4. 如果传了 tagIds，批量插入 note_tag_rel 关联表
5. 返回创建的 noteId 和 createTime
6. 添加 @Log 操作日志注解
7. 写好注释

请同时创建：NoteCreateDTO、WxNoteController 中补充 createNote 方法、NoteDetailService 中补充 createNote 方法及实现。
```
## 提示词 2：后端笔记列表接口
```
在 WxNoteController 中，实现笔记列表接口 GET /api/wx/notes。

参考已有代码：
- 分页查询风格参考 WxNoteStatsController 的 getMyNotes 方法
- 查询 DTO 参考 NoteQueryDTO.java
- Mapper XML 参考 NoteMapper.xml 中已有的查询

接口要求：
1. 支持 type 参数：public(公开社区笔记) / my(我的笔记)
2. 公开笔记：只查 is_public=1 且 status=1 且 del_flag=0 的笔记，按创
建时间倒序
3. 我的笔记：查当前用户的笔记，支持 status 筛选(0草稿/1正常/2回收站)
4. 支持 categoryId、tagId 筛选
5. 支持 pageNum、pageSize 分页
6. 返回 IPage 分页结果，每条笔记包含：noteId、title、content(截取前200
字作摘要)、viewCount、likeCount、commentCount、isPublic、
createTime、作者昵称+头像、分类名称、标签列表
7. likeCount 和 commentCount 从 note_reaction 和 note_comment 表
聚合统计
8.写好注释

请在 NoteMapper.xml 中编写 SQL，NoteDetailService 中添加 listNotes 方法。
```
## 提示词 3：后端笔记更新/删除/恢复接口
```
在 WxNoteController 中，实现笔记的更新、删除、恢复三个接口。

参考已有代码风格：WxTagController、WxNoteStatsController

1. PUT /api/wx/notes/{id} — 更新笔记
   - 只能更新自己的笔记
   - 支持修改：title、content、categoryId、isPublic、tagIds
   - 如果传了 tagIds，先删除旧关联再批量插入新关联
   - 添加 @Log 注解

2. DELETE /api/wx/notes/{id} — 删除笔记
   - 只能删除自己的笔记
   - permanent=false(默认)：逻辑删除，将 status 改为 2(回收站)
   - permanent=true：将 del_flag 改为 1(永久删除)
   - 添加 @Log 注解

3. PUT /api/wx/notes/{id}/restore — 恢复笔记
   - 只能恢复自己的笔记
   - 将 status 从 2(回收站) 改回 1(正常)
   - 添加 @Log 注解
4.写好注释

请在 NoteDetailService 中添加 updateNote、deleteNote、restoreNote 
方法及实现，NoteMapper.xml 中添加对应 SQL。
```
## 提示词 4：小程序社区页+发布页对接真实API
```
在 smart-note-mp 小程序中，将社区页和发布页从 mock 数据切换为真实后端 
API。

参考已有代码：
- API 模块：api/modules/note.js（已有 getNotes、createNote 方法定
义）
- API 配置：api/config.js
- 请求封装：api/request.js
- 社区页：pages/community/community.vue（当前使用 mock 数据）
- 发布页：pages/create/create.vue（表单已有但未对接提交）

社区页改造：
1. 移除 import { mockNotes } from '@/common/mock.js'
2. 使用 noteApi.getNotes('public', page, size) 获取公开笔记列表
3. 实现下拉刷新和上拉加载更多（分页）
4. 搜索功能对接（keyword 参数传入筛选）
5. Tab 切换（最新/最热）通过 orderBy 参数实现

发布页改造：
1. onMounted 时加载分类列表 categoryApi.getList() 和我的标签 
tagApi.getMyTags()
2. 提交按钮调用 noteApi.createNote({ title, content, categoryId, 
isPublic, tagIds })
3. 创建成功后跳转到社区页并提示
4. 标签新增弹窗调用 tagApi.createTag，成功后刷新标签列表

注意：保持现有 UI 不变，只替换数据源。
```
## 提示词 5：小程序笔记编辑+删除+回收站
```
在 smart-note-mp 小程序中，实现笔记编辑、删除和回收站功能。

参考已有代码：
- 发布页：pages/create/create.vue（复用为编辑页）
- 笔记列表：pages/note-list/note-list.vue（通用列表组件）
- API：api/modules/note.js（已有 updateNote、deleteNote、
restoreNote）

1. 笔记编辑
   - 发布页支持编辑模式：通过路由参数 id 判断是新建还是编辑
   - 编辑模式：onLoad 时调用 noteApi.getNoteDetail(id) 回填表单数据
   - 提交时调用 noteApi.updateNote(id, data) 而非 createNote
   - 导航栏标题根据模式显示"发布笔记"或"编辑笔记"

2. 笔记删除
   - 我的笔记列表中添加左滑删除或长按删除操作
   - 调用 noteApi.deleteNote(id, false) 移入回收站
   - 删除后刷新列表

3. 回收站页面
   - 在个人中心添加"回收站"入口
   - 新建 pages/recycle-bin/recycle-bin.vue
   - 调用 noteApi.getNotes('my', page, size, { status: 2 }) 获取
   回收站笔记
   - 每条笔记支持"恢复"（noteApi.restoreNote）和"永久删除"（noteApi.
   deleteNote(id, true)）
   - 恢复/删除后刷新列表
   - 在 pages.json 中注册新页面

4. 笔记详情页添加编辑和删除入口
   - 作者本人可见编辑按钮和删除按钮
   - 编辑跳转到发布页（带 id 参数）
   - 删除确认后移入回收站
```
## 💡 使用建议

| 顺序 |                  提示词                  |  预计耗时   |
| :--: | :--------------------------------------: | :---------: |
|  1️⃣   |     先用提示词 1-3 完成全部后端接口      | 约 3-4 小时 |
|  2️⃣   | 启动后端，用 Knife4j 或 Postman 验证接口 | 约 30 分钟  |
|  3️⃣   |    用提示词 4 完成小程序社区+发布对接    |  约 2 小时  |
|  4️⃣   |     用提示词 5 完成编辑+删除+回收站      |  约 2 小时  |
|  5️⃣   |              全流程联调测试              |  约 1 小时  |

> 每个提示词都包含了 参考代码位置 和 具体字段要求 ，这样我可以直接定位到正确的文件和风格，减少反复确认的时间。