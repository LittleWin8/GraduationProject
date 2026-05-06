# 📋 Day 4 任务清单：管理端笔记管理 + 分类管理
## 任务概览

| 序号 |            任务            |  端   | 优先级 |
| :--: | :------------------------: | :---: | :----: |
|  1   |   后端管理端笔记接口(4个)   | 后端  |  🔴 高  |
|  2   |  后端管理端分类接口(5个)   | 后端  |  🔴 高  |
|  3   | Web笔记管理页面(ProTable)  |  Web  |  🔴 高  |
|  4   | Web分类管理页面(TreeFilter+ProTable) |  Web  |  🔴 高  |
|  5   |          联调验收          | 全端  |  🔴 高  |

## 🔧 任务 1：后端管理端笔记接口

已有基础：
- 数据库表 `note`（status: 0草稿/1正常/2回收站，需新增3=下架）
- 小程序端笔记接口已完成：`WxNoteController`、`NoteDetailService`、`NoteMapper.xml`
- 管理端接口隔离规范：`/api/admin/` 前缀，Controller 命名 `Admin*Controller`

需实现 4 个接口：

|     接口     |                   路径                    |              说明              |
| :----------: | :---------------------------------------: | :----------------------------: |
| 管理端笔记列表 |      `GET /api/admin/notes/list`       | 分页+筛选（状态/分类/关键词/日期） |
| 笔记详情(管理端) |     `GET /api/admin/notes/{id}`      | 管理员可看所有笔记，无权限限制   |
|   审核笔记   | `PUT /api/admin/notes/{id}/audit` | 传 status=1上架 / status=3下架  |
| 删除笔记(管理端) |  `DELETE /api/admin/notes/{id}`   | 管理员强制删除（del_flag=1）    |

## 🔧 任务 2：后端管理端分类接口

已有基础：
- 数据库表 `sys_category`（categoryId, name, parentId, sortOrder, status, createTime）
- 实体类 `SysCategory.java`（已实现 TreeNode 接口，支持 TreeUtils.build 构建树）
- 小程序端分类接口：`WxCategoryController`（仅 GET /api/wx/categories/list）
- Mapper：`SysCategoryMapper.java`（继承 BaseMapper，已够用）

需实现 5 个接口：

|    接口    |                 路径                  |            说明            |
| :--------: | :-----------------------------------: | :------------------------: |
| 分类列表(树形) |  `GET /api/admin/categories/list`   | 查所有分类（含禁用），构建树形 |
|  新增分类  |     `POST /api/admin/categories`     |  传 name/parentId/sortOrder  |
|  修改分类  |   `PUT /api/admin/categories/{id}`   | 传 name/parentId/sortOrder/status |
|  删除分类  |  `DELETE /api/admin/categories/{id}` | 有子分类或关联笔记时拒绝删除 |
|  切换状态  | `PUT /api/admin/categories/{id}/status` |       启用/禁用切换        |

## 🖥️ 任务 3：Web 笔记管理页面

- ProTable 列表 + 状态/分类/关键词/日期筛选
- 审核操作（上架/下架）
- 笔记详情弹窗
- 强制删除

## 🖥️ 任务 4：Web 分类管理页面

- TreeFilter（左侧分类树导航）+ ProTable（右侧分类列表）组合布局
- 参考 useSelectFilter/index.vue 的 TreeFilter + ProTable 模式
- 新增/编辑 Drawer
- 删除校验 + 启禁用切换

## 🔗 联调验收标准
- 小程序发布公开笔记 → Web端可见 → 下架 → 小程序不可见 → 上架 → 小程序重新可见
- 分类增删改 + 启禁用 → 小程序分类列表同步变化



# 📝 Day 4 提示词

## 提示词 1：后端管理端笔记接口
```
在 smart-note-system 的 note 模块中，实现管理端笔记管理的 4 个接口。

⚠️ 架构约束：
1. admin 模块只负责启动项目，不做业务逻辑
2. 所有 Controller/Service 放在 note 模块
3. 管理端接口路径 /api/admin/，Controller 命名 Admin*Controller
4. 参考 system 模块隔离模式：AdminAuthController（管理端）vs WxAuthController（小程序端）

已有代码参考：
- Controller 风格：WxNoteController.java（@Log、SecurityUtils、Result 返回）
- Service 风格：NoteDetailServiceImpl.java（@Service、@RequiredArgsConstructor）
- VO 风格：NoteListVO.java（@Builder 模式）
- Mapper 风格：NoteMapper.java + NoteMapper.xml（分页查询、条件拼接）
- 分页查询参考：NoteMapper.xml 中的 selectNoteListPage

数据库变更：
- 执行 ALTER TABLE note MODIFY COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0 草稿, 1 正常, 2 回收站, 3 下架';，我已经自己执行了,你了解就行
- 新增 status=3（下架），小程序端已有 AND n.status = 1 条件，下架笔记自动不可见，无需改动

需要实现：

1. GET /api/admin/notes/list — 管理端笔记列表
   - 参数：AdminNoteQueryDTO（pageNum/pageSize/status/categoryId/keyword/userId/startTime/endTime）
   - 返回 IPage<AdminNoteVO>，按 create_time DESC
   - AdminNoteVO 字段：noteId, title, summary(content截取200字), status, isPublic,
     viewCount, likeCount(子查询), commentCount(子查询),
     userId, author(sys_user.nickname), avatar,
     categoryId, categoryName(sys_category.name),
     createTime, updateTime
   - 只查 del_flag=0，支持全部状态筛选（0/1/2/3）

2. GET /api/admin/notes/{id} — 管理端笔记详情
   - 管理员可看所有笔记（包括私密、下架、回收站）
   - ⚠️ 现有 selectNoteDetailById 有 AND (n.is_public=1 OR n.user_id=?) 限制
   - 需新增 selectAdminNoteDetailById SQL，去掉 is_public 和 userId 限制，只保留 del_flag=0

3. PUT /api/admin/notes/{id}/audit — 审核笔记
   - 请求体：{ status: Integer }，只接受 1(上架) 或 3(下架)
   - 直接修改 note 表 status 字段
   - 添加 @Log 注解

4. DELETE /api/admin/notes/{id} — 管理员强制删除
   - 将 del_flag 改为 1（永久删除）
   - 添加 @Log 注解

请创建：
- AdminNoteController.java（com.littlewin.note.controller）
- AdminNoteService.java + AdminNoteServiceImpl.java（com.littlewin.note.service）
- AdminNoteQueryDTO.java（com.littlewin.note.domain.dto）
- AdminNoteVO.java（com.littlewin.note.domain.vo，@Builder 模式）
- NoteMapper.java 中新增 selectAdminNotePage / auditNote / adminForceDelete / selectAdminNoteDetailById
- NoteMapper.xml 中新增对应 4 条 SQL
```

## 提示词 2：后端管理端分类接口
```
在 smart-note-system 的 note 模块中，实现管理端分类管理的 5 个接口。

已有代码参考：
- 实体类：note/domain/entity/SysCategory.java（已实现 TreeNode 接口，有 categoryId/name/parentId/sortOrder/status/createTime/children）
- 小程序端分类接口：WxCategoryController.java（仅 GET /api/wx/categories/list，查 status=1 的分类构建树）
- Mapper：SysCategoryMapper.java（继承 BaseMapper<SysCategory>，已有 insert/selectById/updateById/selectList 等）
- 树构建工具：TreeUtils.build(list, 0L)
- Controller 风格：AdminNoteController（刚创建的，同模块参考）
- 异常处理：throw new ServiceException("提示信息")

需要实现：

1. GET /api/admin/categories/list — 分类列表
   - 支持可选参数 parentId：
     - 无 parentId：查所有分类（不过滤 status，含禁用），用 TreeUtils.build 构建树形结构，返回 Result<List<SysCategory>>（供前端 TreeFilter 组件使用）
     - 有 parentId：查 parent_id = parentId 的直接子分类（扁平列表，不过滤 status），返回 Result<List<SysCategory>>（供前端 ProTable 组件使用，分类数量少无需分页）
   - 前端 TreeFilter + ProTable 组合布局需要两种数据格式：树形（导航）和扁平（列表）

2. POST /api/admin/categories — 新增分类
   - 请求体 CategoryDTO：{ name(必填), parentId(默认0), sortOrder(默认0) }
   - status 默认 1（启用）
   - name 不能为空
   - 添加 @Log 注解

3. PUT /api/admin/categories/{id} — 修改分类
   - 请求体 CategoryDTO：{ name, parentId, sortOrder, status }
   - 不能将自己设为自己的子分类（parentId != categoryId）
   - 添加 @Log 注解

4. DELETE /api/admin/categories/{id} — 删除分类
   - 先查是否有子分类：SELECT COUNT(*) FROM sys_category WHERE parent_id = #{id}
   - 再查是否有笔记关联：SELECT COUNT(*) FROM note WHERE category_id = #{id} AND del_flag = 0
   - 有子分类或关联笔记时抛 ServiceException 拒绝删除
   - 无关联则物理删除
   - 添加 @Log 注解

5. PUT /api/admin/categories/{id}/status — 切换状态
   - 查询当前 status，取反（1→0 或 0→1）后 update
   - 添加 @Log 注解

请创建：
- AdminCategoryController.java（com.littlewin.note.controller）
- AdminCategoryService.java + AdminCategoryServiceImpl.java（com.littlewin.note.service）
- CategoryDTO.java（com.littlewin.note.domain.dto）
```

## 提示词 3：Web 笔记管理页面
```
在 smart-note-ui Web 前端中，实现笔记管理页面。

已有代码参考：
- 页面风格：src/views/system/user/index.vue（ProTable 列表 + 搜索 + 操作）
- API 风格：src/api/modules/user.ts（namespace + http 封装）
- 接口类型：src/api/interface/index.ts（ResPage、ReqPage 等通用类型）
- Drawer 风格：src/views/system/role/components/RoleDrawer.vue
- 后端分页格式：{ records: [], total: 0 }
- dataCallback 适配参考 user/index.vue 中的写法

需要创建：

1. src/api/modules/note.ts — 笔记管理 API
   - getNoteList(params) → GET /admin/notes/list
   - getNoteDetail(noteId) → GET /admin/notes/{noteId}
   - auditNote(noteId, status) → PUT /admin/notes/{noteId}/audit，请求体 { status }
   - deleteNote(noteId) → DELETE /admin/notes/{noteId}
   - namespace Note { ReqNoteParams, NoteListVO } 类型定义

2. src/views/note/list/index.vue — 笔记管理页面

   使用 ProTable 组件，参考 user/index.vue：

   搜索条件：
   - 状态：el-select，选项：全部(空)/草稿(0)/正常(1)/回收站(2)/下架(3)
   - 分类：el-select，选项从 getCategoryTree() 获取（扁平化树结构提取所有分类）
   - 关键词：el-input，placeholder="搜索标题/内容"
   - 日期范围：el-date-picker type="daterange"

   表格列（columns）：
   - noteId：label="ID"，width=80，sortable
   - title：label="标题"，showOverflowTooltip
   - author：label="作者"，width=100
   - categoryName：label="分类"，width=100
   - status：label="状态"，width=90，用 el-tag 渲染
     - 0→type="info" 草稿 / 1→type="success" 正常 / 2→type="warning" 回收站 / 3→type="danger" 下架
   - isPublic：label="公开"，width=80，用 el-tag
     - 0→type="info" 私密 / 1→type="success" 公开
   - viewCount / likeCount / commentCount：label="浏览/点赞/评论"，width=80
   - createTime：label="创建时间"，width=170，sortable
   - 操作列：查看详情、审核、删除

   操作逻辑：
   - 查看详情：el-dialog 弹窗，展示 title + content（纯文本或简单 Markdown），宽 700px
   - 审核：
     - status=1 的笔记显示"下架"按钮（danger），确认后调用 auditNote(id, 3)
     - status=3 的笔记显示"上架"按钮（success），确认后调用 auditNote(id, 1)
     - 其他状态不显示审核按钮
   - 删除：确认弹窗 → deleteNote(id)

   dataCallback：
   typescript
   const dataCallback = (data: any) => {
     return { list: data.records, total: data.total };
   };

   requestApi 使用 getNoteList，initParam 为空 reactive({})。

3. src/api/interface/index.ts 中补充 Note namespace 类型（如果 note.ts 中已内联则跳过）

注意：
- API 路径不需要加 /api 前缀，axios baseURL 已配置
- 参考 user.ts 的写法，http.get/post/put/delete 的参数格式
```

## 提示词 4：Web 分类管理页面
```
在 smart-note-ui Web 前端中，实现分类管理页面。

⚠️ 核心设计：TreeFilter（左侧分类树导航）+ ProTable（右侧分类列表）组合布局

参考文件（必看）：
- 布局模式：src/views/proTable/useSelectFilter/index.vue（照抄这个页面的 TreeFilter + ProTable 组合结构）
- TreeFilter 组件：src/components/TreeFilter/index.vue
- Drawer 风格：src/views/system/role/components/RoleDrawer.vue
- API 风格：src/api/modules/user.ts

需要创建：

1. src/api/modules/category.ts
   - getCategoryTree() → GET /admin/categories/list（无参数，返回树形，供 TreeFilter）
   - getCategoryList(params) → GET /admin/categories/list（带 parentId，返回扁平列表，供 ProTable）
   - addCategory(data) → POST /admin/categories
   - updateCategory(id, data) → PUT /admin/categories/{id}
   - deleteCategory(id) → DELETE /admin/categories/{id}
   - toggleCategoryStatus(id) → PUT /admin/categories/{id}/status

2. src/views/note/category/index.vue

   照抄 useSelectFilter/index.vue 的结构，改为分类场景：

   TreeFilter 配置：
   - title="分类列表"，label="name"，id="categoryId"
   - :request-api="getCategoryTree"（无参数调接口，返回树形数据）
   - 单选模式（默认 multiple=false）
   - @change="changeTreeFilter"：将 val 赋给 initParam.parentId

   ProTable 配置：
   - :request-api="getCategoryList"（带 parentId 参数）
   - :init-param="initParam"（包含 parentId，TreeFilter 变化时自动重请求）
   - :pagination="false"（分类少，不分页）
   - dataCallback：return { list: data, total: data.length }
   - columns：#序号 / ID(categoryId) / 分类名称(name) / 排序(sortOrder) / 状态(status, tag+enum: 1启用success/0禁用danger) / 创建时间 / 操作列
   - 操作列：编辑(EditPen)、启禁用切换(Switch)、删除(Delete)
   - tableHeader：新增分类按钮

3. src/views/note/category/components/CategoryDrawer.vue

   参考 RoleDrawer.vue：
   - 表单：分类名称(el-input,必填) / 父分类(el-tree-select, 数据源getCategoryTree, label=name value=categoryId, parentId=0为顶级) / 排序(el-input-number, min=0)
   - acceptParams 接收 { title, row, api, getTableList }
   - 新增默认 parentId=0, sortOrder=0；编辑回填

4. 菜单数据已在 docs/sql/init_sys_data.sql 中直接修改，无需额外执行 SQL

注意：
- API 路径不加 /api 前缀，axios baseURL 已配置
- TreeFilter 的 id="categoryId" 必须与后端树节点标识一致
- 后端 GET /api/admin/categories/list 支持可选 parentId：无参返回树形，有参返回扁平列表
- 删除/启禁用后需刷新 ProTable 列表
```

## 提示词 5：联调验证

```
Day 4 全部开发完成后，按以下步骤联调验证：

后端验证：

1. 启动 smart-note-system，用 admin 账号登录获取 Token
2. Knife4j 或 Postman 测试 4 个笔记管理接口：
   - GET /api/admin/notes/list → 返回分页数据
   - GET /api/admin/notes/{id} → 返回笔记详情（含私密/下架笔记）
   - PUT /api/admin/notes/{id}/audit → status 切换正常
   - DELETE /api/admin/notes/{id} → 删除成功
3. 测试 5 个分类管理接口：
   - GET /api/admin/categories/list → 返回树形结构
   - POST /api/admin/categories → 新增成功
   - PUT /api/admin/categories/{id} → 修改成功
   - DELETE /api/admin/categories/{id} → 有子分类时拒绝、无关联时删除
   - PUT /api/admin/categories/{id}/status → 状态切换

Web 端验证：

4. 用 admin 登录 Web 管理端
5. 左侧菜单出现"内容管理 > 笔记列表"和"分类维护 > 分类树"
6. 笔记管理页：列表数据正常、搜索筛选正常、审核上下架正常、删除正常
7. 分类管理页：左侧分类树导航正常、右侧列表联动正常、新增/编辑/删除/启禁用正常

跨端联调：

8. 小程序发布一篇公开笔记 → Web 笔记列表可见
9. Web 端下架该笔记 → 小程序社区刷新后不可见
10. Web 端上架该笔记 → 小程序社区刷新后重新可见
11. Web 端禁用某分类 → 小程序分类导航不显示该分类
12. Web 端启用该分类 → 小程序分类导航重新显示

如果以上全部通过，Day 4 验收完成。
```



# ⏱️ Day 4 执行顺序

| 顺序 |            提示词            | 预计耗时 |   前置依赖    |
| :--: | :--------------------------: | :------: | :-----------: |
|  1️⃣   | 提示词 1：后端管理端笔记接口 |   2-3h   | Day 3 已完成  |
|  2️⃣   | 提示词 2：后端管理端分类接口 |  1.5-2h  |   提示词 1    |
|  3️⃣   |     Knife4j/Postman 验证     |  30min   |  提示词 1+2   |
|  4️⃣   |  提示词 3：Web 笔记管理页面  |   2-3h   | 提示词 1 验证 |
|  5️⃣   |  提示词 4：Web 分类管理页面  |   2-3h   | 提示词 2 验证 |
|  6️⃣   |      提示词 5：联调验证      |    1h    |   全部完成    |

> 每个提示词都包含了 当前已有代码的精确位置、字段定义和数据库表结构，可以直接定位开发，无需反复确认数据结构。
