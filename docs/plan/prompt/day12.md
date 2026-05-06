# 📋 Day 12 任务清单：权限细化 + 笔记审核 + Web 代码清理

## 任务概览

| 序号 | 任务 | 优先级 | 说明 |
|:--:|:---|:--:|:---|
| 1 | reviewed 字段 + 审核接口 | 🔴 高 | 后端笔记审核标记 + 一键审核接口 + 按钮权限 SQL 补全 |
| 2 | 按钮权限分组修复 | 🔴 高 | getAuthButtonList 按页面路由名分组，适配前端 v-auth |
| 3 | Web 审核 + 权限控制 | 🔴 高 | 笔记管理审核筛选/按钮 + 全业务页面 v-auth 权限 |
| 4 | 首页重定向 + 代码清理 | 🟡 中 | HOME_URL 修改 + Geeker-Admin 示例页面删除 + API 路径检查 |

## 当前已有基础

- `Note` 实体：noteId, userId, categoryId, title, content, isPublic, status, viewCount, likeCount, commentCount, summary, delFlag, createTime, updateTime
- `AdminNoteController`：GET /list、GET /{id}、PUT /{id}/audit、DELETE /{id}
- `AdminNoteQueryDTO`：pageNum, pageSize, status, categoryId, keyword, userId, startTime, endTime
- `AdminNoteVO`：noteId, title, summary, status, isPublic, viewCount, likeCount, commentCount, userId, author, avatar, categoryId, categoryName, createTime, updateTime
- `NoteMapper.xml`：selectAdminNotePage（管理端分页查询）、auditNote（审核上架/下架）
- `v-auth` 指令：已注册（directives/modules/auth.ts），通过 `authStore.authButtonListGet[route.name]` 按页面路由名查找按钮权限
- `useAuthButtons` hooks：通过 `route.name` 获取当前页按钮权限
- `AdminAuthServiceImpl.getAuthButtonList()`：当前返回扁平 `{ authButton: [...], useProTable: [...] }`，**未按页面分组**
- `UserAuthMapper.xml.selectButtonListByUserId`：当前 `p.title AS parentName`（返回菜单标题"笔记列表"，而非路由名"noteList"）
- `SysMenu.java`：⚠️ 当前**无** parentName 属性，需新增 @TableField(exist = false) 字段以接收 SQL JOIN 映射
- `HOME_URL`：当前为 `/home/index`，但 `/home/index` 页面只是一个欢迎图，实际首页应为 `/dashboard/index`
- init_sys_data.sql：已有 note_review 字典（dict_id=10），已有 note:audit 和 note:delete 按钮权限（menu_id 3011、3012）

## 需要实现的内容

| 序号 | 问题 | 当前状态 | 目标 |
|:--:|:---|:---|:---|
| 1 | note 表无 reviewed 字段 | 无审核标记 | 新增 reviewed 字段（0未审核/1已审核） |
| 2 | 无审核筛选接口 | 只能按 status 筛选 | AdminNoteQueryDTO 新增 reviewed 参数 |
| 3 | 无一键审核接口 | 只有上架/下架 | 新增 PUT /{id}/review |
| 4 | ~~user_message type 注释错误~~ | ~~已修正~~ | Day 2 已修复，跳过 |
| 5 | 无 note:review 按钮权限 | 无 | 新增 menu_id=3013 |
| 5b | 角色管理无按钮权限定义 | 无 sys:role:add 等记录 | 新增 menu_id=2021~2024 |
| 5c | 标签管理无删除按钮权限 | 无 note:tag:del 记录 | 新增 menu_id=4011 |
| 5d | 通知管理无按钮权限 | 无 sys:notification:send 记录 | 已有 menu_id=5030（菜单级），新增 F 型按钮 5031 |
| 6 | 按钮权限未按页面分组 | 扁平数组 | 按路由名分组返回 |
| 7 | Web 无审核筛选/按钮 | 只有上架/下架 | 新增审核列 + 标记已审核按钮 |
| 8 | 业务页面无 v-auth | 无按钮权限控制 | 所有页面添加 v-auth |
| 9 | HOME_URL 指向 /home/index | 欢迎页面 | 改为 /dashboard/index |
| 10 | Geeker-Admin 示例页面未清理 | 11 个冗余目录 | 全部删除 |

---

# 📝 Day 12 提示词

## 提示词 1：后端 — reviewed 字段 + 审核接口 + 按钮权限 SQL 补全

```
在 smart-note-system 的 note 模块中，为笔记新增审核标记字段和一键审核接口，同时补全各页面的按钮权限 SQL 记录。

⚠️ 设计决策：
- reviewed 字段：0=未审核（默认），1=已审核
- 用户发布笔记时默认 reviewed=0（先发后审，社区仍可见）
- 管理员可按 reviewed 筛选，逐条标记已审核
- 上架/下架（audit 接口）和标记已审核（review 接口）是两个独立操作

已有代码参考：
- 实体类：note/domain/entity/Note.java
- DTO：note/domain/dto/AdminNoteQueryDTO.java、NoteAuditDTO.java
- VO：note/domain/vo/AdminNoteVO.java
- Mapper：note/mapper/NoteMapper.java + resources/com/littlewin/note/mapper/NoteMapper.xml
- Service：note/service/AdminNoteService.java + impl/AdminNoteServiceImpl.java
- Controller：note/controller/AdminNoteController.java（audit 接口在第 ? 行）
- SQL：docs/sql/init_db.sql（note 表定义在第 108-135 行，user_message 表定义在下文）

### 步骤 1：修改 init_db.sql — note 表新增 reviewed 字段

在 note 表 CREATE TABLE 中，`del_flag` 字段之前添加：
`reviewed TINYINT NOT NULL DEFAULT 0 COMMENT '审核标记：0 未审核, 1 已审核',`

### ~~步骤 2：user_message type 注释~~（Day 2 已修复，跳过）

init_db.sql 中 user_message 的 type 字段注释已在 Day 2 修正为正确的 `1评论, 2回复, 3审核通过, 4审核不通过, 5违规下架, 6系统公告, 7点赞, 8收藏`，无需重复修改。

### 步骤 3：修改 init_sys_data.sql — 补全各页面按钮权限

(1) 笔记列表按钮权限（menu_id 3011、3012 附近），新增：
INSERT INTO sys_menu (menu_id, parent_id, menu_type, title, perms, sort_order) VALUES
(3013, 3010, 'F', '标记已审核', 'note:review', 3);

(2) 角色管理（menu_id=2020）当前无按钮权限记录，在字典管理记录之后新增：
INSERT INTO sys_menu (menu_id, parent_id, menu_type, title, perms, sort_order) VALUES
(2021, 2020, 'F', '新增角色', 'sys:role:add', 1),
(2022, 2020, 'F', '修改角色', 'sys:role:edit', 2),
(2023, 2020, 'F', '删除角色', 'sys:role:delete', 3),
(2024, 2020, 'F', '分配权限', 'sys:role:assign', 4);

(3) 标签管理（menu_id=4010）当前无按钮权限记录，在分类管理按钮权限之后新增：
INSERT INTO sys_menu (menu_id, parent_id, menu_type, title, perms, sort_order) VALUES
(4011, 4010, 'F', '删除标签', 'note:tag:del', 1);

(4) 通知管理（menu_id=5030）当前为 C 型菜单，需新增 F 型按钮权限。在通知管理菜单记录之后新增：
INSERT INTO sys_menu (menu_id, parent_id, menu_type, title, perms, sort_order) VALUES
(5031, 5030, 'F', '发送通知', 'sys:notification:send', 1);
注意：原 5030 的 perms='sys:notification:send' 保留不变（菜单级权限控制页面可见性），5031 为按钮级权限控制表单内"发送"按钮。

### 步骤 4：修改 Note.java

在 `delFlag` 字段之前添加：
private Integer reviewed;

### 步骤 5：修改 AdminNoteQueryDTO.java

在现有字段末尾添加：
private Integer reviewed;

### 步骤 6：修改 AdminNoteVO.java

在 `commentCount` 字段之后添加：
private Integer reviewed;

### 步骤 7：修改 NoteMapper.xml — selectAdminNotePage

在 SELECT 列表中（n.comment_count AS commentCount 之后）添加：
n.reviewed AS reviewed,

在 WHERE 条件块中（endTime 条件之后、ORDER BY 之前）添加：
<if test="query.reviewed != null">
    AND n.reviewed = #{query.reviewed}
</if>

### 步骤 8：修改 NoteMapper.xml — 新增 reviewNote SQL

在 auditNote SQL 之后添加：
<update id="reviewNote">
    UPDATE note SET reviewed = 1, update_time = NOW()
    WHERE note_id = #{noteId} AND del_flag = 0
</update>

### 步骤 9：修改 NoteMapper.java

在 auditNote 方法之后添加：
int reviewNote(@Param("noteId") Long noteId);

### 步骤 10：修改 AdminNoteService.java

在 auditNote 方法之后添加：
void reviewNote(Long noteId);

### 步骤 11：修改 AdminNoteServiceImpl.java

在 auditNote 方法之后添加：
@Override
public void reviewNote(Long noteId) {
    int rows = noteMapper.reviewNote(noteId);
    if (rows == 0) {
        throw new ServiceException("笔记不存在或已删除");
    }
}

### 步骤 12：修改 AdminNoteController.java

在 audit 接口之后添加：
@PutMapping("/{id}/review")
@Log(module = LogModule.NOTE, action = LogAction.UPDATE, desc = "标记笔记已审核")
public Result<Void> review(@PathVariable("id") Long id) {
    adminNoteService.reviewNote(id);
    LogContext.setBusinessId(id);
    LogContext.setDesc("标记笔记已审核: " + id);
    return Result.success(null);
}

验证：
1. init_db.sql 中 note 表有 reviewed 字段
2. init_sys_data.sql 中有 note:review 权限记录（3013）、角色管理按钮权限（2021~2024）、标签管理按钮权限（4011）、通知管理按钮权限（5031）
3. PUT /api/admin/notes/{id}/review → 返回成功，数据库 reviewed 变为 1
4. GET /api/admin/notes/list?reviewed=0 → 只返回未审核笔记
5. Note.java / AdminNoteQueryDTO / AdminNoteVO 都有 reviewed 字段
```

---

## 提示词 2：后端 — 按钮权限分组修复

```
修复 smart-note-system 的按钮权限接口，使其按页面路由名分组返回，适配前端 v-auth 指令。

⚠️ 问题描述：
前端 v-auth 指令通过 authStore.authButtonListGet[route.name] 按页面路由名查找按钮权限。
例如笔记列表页 route.name = "noteList"，需要从 authButtonList["noteList"] 中获取 ["note:audit", "note:delete", "note:review"]。
当前后端返回 { authButton: [...], useProTable: [...] }，是扁平数组，无法按页面区分。

需要将按钮权限改为按页面路由名（即 sys_menu.name）分组：
{
  "noteList": ["note:audit", "note:delete", "note:review"],
  "user": ["sys:user:add", "sys:user:edit", "sys:user:delete"],
  ...
}

已有代码参考：
- Mapper XML：system/resources/com/littlewin/system/mapper/UserAuthMapper.xml（selectButtonListByUserId）
- Mapper 接口：system/mapper/UserAuthMapper.java
- ServiceImpl：system/service/impl/AdminAuthServiceImpl.java（getAuthButtonList 方法）
- SysMenu 实体：system/domain/entity/SysMenu.java（⚠️ 当前无 parentName，需新增 @TableField(exist = false) 字段）
- 按钮权限 SQL 关联关系：F 类型菜单 → LEFT JOIN 父菜单（parent_id → menu_id），取父菜单的 name

### 步骤 1：修改 SysMenu.java — 新增 parentName 字段

当前 SysMenu 实体没有 parentName 属性，SQL 中 `p.name AS parentName` 的映射结果会丢失。
在 `private String perms;` 之后添加：

@TableField(exist = false)
private String parentName;

@TableField(exist = false) 表示该字段不对应 sys_menu 表列，仅用于 SQL JOIN 结果的映射。

### 步骤 2：修改 UserAuthMapper.xml — selectButtonListByUserId

将当前的 selectButtonListByUserId SQL 修改为：

<select id="selectButtonListByUserId" resultType="com.littlewin.system.domain.entity.SysMenu">
    SELECT
        m.menu_id,
        m.parent_id,
        m.perms,
        p.name AS parentName,
        p.title AS parentTitle
    FROM sys_menu m
             LEFT JOIN sys_menu p ON m.parent_id = p.menu_id
             INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
             INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
    WHERE ur.user_id = #{userId}
      AND m.menu_type = 'F'
      AND m.perms IS NOT NULL
      AND m.perms != ''
</select>

关键变化：原来 p.title AS parentName 改为 p.name AS parentName。
现在 parentName 返回路由名（如 "noteList"、"user"），而不是菜单标题（如 "笔记列表"、"账号列表"）。

### 步骤 3：修改 AdminAuthServiceImpl.java — getAuthButtonList()

将 getAuthButtonList() 方法替换为：

@Override
public Map<String, List<String>> getAuthButtonList() {
    LoginDTO user = SecurityUtils.getLoginUser();
    if (user == null) return new HashMap<>();

    List<SysMenu> buttons = userAuthMapper.selectButtonListByUserId(user.getUserId());

    // 按页面路由名（parentName = sys_menu.name）分组
    Map<String, List<String>> pageButtons = buttons.stream()
            .filter(b -> b.getPerms() != null && !b.getPerms().isEmpty())
            .collect(Collectors.groupingBy(
                    b -> b.getParentName() != null ? b.getParentName() : "unknown",
                    Collectors.mapping(SysMenu::getPerms, Collectors.toList())
            ));

    Map<String, List<String>> result = new HashMap<>();
    result.putAll(pageButtons);

    // ProTable 增强权限：遍历所有页面的权限，筛选 ProTable 识别的标识
    List<String> proTableKeys = List.of("add", "batchAdd", "export", "batchDelete", "status");
    List<String> useProTablePerms = buttons.stream()
            .map(SysMenu::getPerms)
            .filter(Objects::nonNull)
            .filter(proTableKeys::contains)
            .distinct()
            .collect(Collectors.toList());
    result.put("useProTable", useProTablePerms);

    return result;
}

返回示例（admin 角色）：
{
  "noteList": ["note:audit", "note:delete", "note:review"],
  "user": ["sys:user:add", "sys:user:edit", "sys:user:delete"],
  "role": ["sys:role:add", "sys:role:edit", "sys:role:delete", "sys:role:assign"],
  "tag": ["note:tag:del"],
  "tree": ["category:add", "category:edit", "category:delete"],
  "notification": ["sys:notification:send"],
  "dict": ["sys:dict:edit"],
  "useProTable": ["add", "export", "batchDelete"]
}

验证：
1. GET /api/admin/auth/getAuthButtonList → 返回按页面路由名分组的 Map
2. Map 的 key 是路由名（noteList、user、role 等），不是菜单标题
3. useProTable 仍然正确返回
4. editor 角色只能看到分配的页面权限（不包含 user、role 等系统管理页面）
```

---

## 提示词 3：Web — 笔记审核 + 按钮权限控制

```
在 smart-note-ui Web 前端中，为笔记管理页增加审核筛选和一键审核功能，并为所有业务页面添加 v-auth 按钮级权限控制。

⚠️ 前置条件：提示词 1、2 已完成（后端 reviewed 字段、review 接口、按钮权限分组均已就绪）。

已有代码参考：
- 笔记管理页：src/views/note/list/index.vue（ProTable + 操作列模板）
- note API：src/api/modules/note.ts（getNoteList、auditNote、deleteNote）
- useAuthButtons：src/hooks/useAuthButtons.ts（通过 route.name 获取按钮权限）
- v-auth 指令：已注册，支持字符串和数组两种用法
- 用户管理页：src/views/system/user/index.vue
- 角色管理页：src/views/system/role/index.vue
- 分类管理页：src/views/category/category/index.vue
- 标签管理页：src/views/category/tag/index.vue
- 通知管理页：src/views/monitor/notification/index.vue

### 步骤 1：修改 note.ts — 新增 reviewed 参数和审核 API

(1) ReqNoteParams 接口中添加：
reviewed?: number;

(2) NoteListVO 接口中添加：
reviewed: number;

(3) 文件末尾添加审核 API：
// 标记笔记已审核
export const reviewNote = (noteId: number) => {
  return http.put(`/admin/notes/${noteId}/review`);
};

### 步骤 2：修改 note/list/index.vue — 审核功能

(1) script 中引入 useAuthButtons 和 reviewNote：
import { useAuthButtons } from "@/hooks/useAuthButtons";
import { reviewNote } from "@/api/modules/note";

在 noteDrawerRef 之后添加：
const { BUTTONS } = useAuthButtons();

(2) 新增 reviewedOptions：
const reviewedOptions = [
  { label: "未审核", value: 0 },
  { label: "已审核", value: 1 }
];

(3) columns 数组中添加 reviewed 列（在 isPublic 列之后）：
{
  prop: "reviewed",
  label: "审核",
  width: 90,
  search: { el: "select", props: { placeholder: "审核状态" } },
  enum: reviewedOptions,
  render: (scope: any) => {
    const reviewed = scope.row.reviewed === 1;
    return <el-tag type={reviewed ? "success" : "warning"}>{reviewed ? "已审核" : "未审核"}</el-tag>;
  }
},

(4) 操作列模板替换为：
<template #operation="scope">
  <el-button type="primary" link :icon="View" @click="viewDetail(scope.row)">详情</el-button>
  <el-button
    v-if="scope.row.status === 1"
    v-auth="'note:audit'"
    type="danger"
    link
    @click="handleAudit(scope.row, 3)"
  >下架</el-button>
  <el-button
    v-if="scope.row.status === 3"
    v-auth="'note:audit'"
    type="success"
    link
    @click="handleAudit(scope.row, 1)"
  >上架</el-button>
  <el-button
    v-if="scope.row.reviewed === 0"
    v-auth="'note:review'"
    type="warning"
    link
    @click="handleReview(scope.row)"
  >标记已审核</el-button>
  <el-button
    v-auth="'note:delete'"
    type="danger"
    link
    :icon="Delete"
    @click="handleDelete(scope.row)"
  >删除</el-button>
</template>

(5) handleAudit 函数之后添加 handleReview：
const handleReview = async (row: NoteListVO) => {
  await useHandleData(() => reviewNote(row.noteId), {}, `标记笔记【${row.title}】已审核`);
  proTable.value?.getTableList();
};

### 步骤 3：为其他业务页面添加 v-auth

读取以下页面文件，在操作列按钮上添加对应的 v-auth 指令。

用户管理页 src/views/system/user/index.vue：
- 新增按钮：v-auth="'sys:user:add'"
- 编辑按钮：v-auth="'sys:user:edit'"
- 删除按钮：v-auth="'sys:user:delete'"

角色管理页 src/views/system/role/index.vue：
- 新增按钮：v-auth="'sys:role:add'"
- 分配权限按钮：v-auth="'sys:role:assign'"
- 编辑按钮：v-auth="'sys:role:edit'"
- 删除按钮：v-auth="'sys:role:delete'"

分类管理页 src/views/category/category/index.vue：
- 新增按钮：v-auth="'category:add'"
- 编辑按钮：v-auth="'category:edit'"
- 删除按钮：v-auth="'category:delete'"

标签管理页 src/views/category/tag/index.vue：
- 删除按钮：v-auth="'note:tag:del'"

通知管理页 src/views/monitor/notification/index.vue：
- 发送按钮（el-form 中的"发送"按钮）：v-auth="'sys:notification:send'"
  ⚠️ 该页面不是 ProTable，是表单页面。在"发送" el-button 上添加 v-auth，无权限用户看不到发送按钮。

⚠️ 注意：
- 如果页面已使用 useAuthButtons hooks，保留 hooks 方式并在按钮上同时使用 v-auth 指令
- 如果页面没有引入 useAuthButtons，只用 v-auth 指令即可
- v-auth 会直接移除无权限的 DOM 元素（el.remove()），用户完全看不到按钮

验证：
1. 笔记管理页 → 列表有"审核"列，可筛选未审核/已审核
2. 笔记管理页 → 未审核笔记操作列有"标记已审核"按钮
3. 笔记管理页 → 点击"标记已审核" → reviewed 变为 1，按钮消失
4. editor 角色登录 → 笔记列表看不到"标记已审核"和"删除"按钮（editor 无 note:review 和 note:delete 权限）
5. editor 角色登录 → 用户管理页看不到新增/编辑/删除按钮（editor 无 sys:user:* 权限）
6. admin 角色登录 → 所有按钮可见
7. 标签管理页 → 只有"删除"按钮有 v-auth 控制
```

---

## 提示词 4：Web — 首页重定向 + 代码清理 + API 路径检查

```
在 smart-note-ui 中，将默认首页路由从 /home/index 改为 /dashboard/index，删除 Geeker-Admin 模板的示例页面，检查并修复 API 双斜杠路径。

### 步骤 1：修改 config/index.ts — HOME_URL

将：
export const HOME_URL: string = "/home/index";
改为：
export const HOME_URL: string = "/dashboard/index";

### 步骤 2：修改 init_sys_data.sql — 注释掉首页菜单

将 menu_id=1000 的首页菜单记录：
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
(1000, 0, 'home', '/home/index', '/home/index', 'C', '首页', 'HomeFilled', 1);

改为（注释掉，因为 dashboard 已在 menu_id=1100 定义）：
-- 首页菜单已由工作台 (menu_id=1100) 替代，不再需要独立的 /home/index 路由
-- INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
-- (1000, 0, 'home', '/home/index', '/home/index', 'C', '首页', 'HomeFilled', 1);

### 步骤 3：修改 src/assets/json/authMenuList.json — 删除首页条目

删除 authMenuList.json 中 "path": "/home/index" 的整个菜单条目（dashboard 条目已存在且设为 isAffix）。

### 步骤 4：删除 Geeker-Admin 示例页面

删除以下目录（整个目录，共 11 个）：
src/views/assembly/
src/views/directives/
src/views/echarts/
src/views/form/
src/views/link/
src/views/menu/
src/views/proTable/
src/views/home/
src/views/auth/
src/views/dataScreen/
src/views/about/

保留以下目录（项目实际使用的）：
src/views/dashboard/      — 工作台
src/views/login/          — 登录页
src/views/note/           — 笔记管理
src/views/category/       — 分类/标签管理
src/views/system/         — 系统管理
src/views/monitor/        — 系统监控
src/views/profile/        — 个人中心

### 步骤 5：清理 src/assets/json/authMenuList.json — 删除对应菜单条目

在 authMenuList.json 中搜索并删除以下顶级菜单节点（含全部子节点）：
- "path": "/dataScreen"
- "path": "/proTable"
- "path": "/assembly"
- "path": "/auth"
- "path": "/form"
- "path": "/echarts"
- "path": "/directives"
- "path": "/menu"
- "path": "/link"
- "path": "/about"

只保留：home/index（如果未删）、dashboard/index、system 系列、note 系列、category 系列、monitor 系列、profile 相关。

### 步骤 6：检查 API 文件双斜杠路径

逐一读取 src/api/modules/ 目录下的所有 .ts 文件，搜索所有 API 路径字符串，确认没有 /xxx//xxx 形式的双斜杠。

已知文件清单：login.ts、note.ts、user.ts、role.ts、dict.ts、category.ts、tag.ts、log.ts、aiLog.ts、notification.ts。

同时检查 smart-note-mp/api/ 目录下的 JS 文件，同样排查双斜杠。

如发现双斜杠路径，修正为单斜杠。

⚠️ 注意：
- 删除 views/home/ 目录前确认静态路由中没有直接引用（静态路由 redirect 到 HOME_URL，不直接 import home/index.vue）
- 删除 views/auth/ 目录后，authMenuList.json 中也必须删除对应条目，否则动态路由注册时找不到组件文件会报错
- authButtonList.json 也需要检查，但该文件是 Mock 数据，后端已用真实接口，可保留不动

验证：
1. 登录后自动跳转 /dashboard/index，而非 /home/index
2. 访问 /home/index → 返回 404（或重定向到 dashboard）
3. 左侧菜单无"常用组件""表单""ECharts""数据大屏""外部链接""菜单嵌套"等条目
4. 所有业务页面（笔记管理、用户管理、角色管理等）正常显示
5. src/api/modules/ 下所有 .ts 文件无双斜杠路径
6. smart-note-mp/api/ 下所有 .js 文件无双斜杠路径
```

---

# ⏱️ Day 12 执行顺序

| 顺序 | 提示词 | 前置依赖 | 说明 |
|:--:|:---|:--:|:---|
| 1️⃣ | 提示词 1：reviewed 字段 + 审核接口 | 无 | 后端基础改动 |
| 2️⃣ | 提示词 2：按钮权限分组修复 | 无 | 后端权限接口改造 |
| 3️⃣ | 提示词 3：Web 审核 + 权限控制 | 提示词 1、2 | 前端依赖后端接口 |
| 4️⃣ | 提示词 4：首页重定向 + 代码清理 | 无 | 独立任务 |

> 提示词 1 和 2 互相独立，可并行执行。提示词 3 依赖 1、2。提示词 4 与其他无依赖。

---

# 🔍 Day 12 涉及的文件清单

| 文件 | 改动类型 | 说明 |
|:---|:---|:---|
| init_db.sql | 修改 | note 表新增 reviewed 字段 |
| init_sys_data.sql | 修改 | 补全 note:review/角色/标签/通知按钮权限、首页菜单注释 |
| Note.java | 修改 | 新增 reviewed 字段 |
| AdminNoteQueryDTO.java | 修改 | 新增 reviewed 字段 |
| AdminNoteVO.java | 修改 | 新增 reviewed 字段 |
| NoteMapper.java | 修改 | 新增 reviewNote 方法 |
| NoteMapper.xml | 修改 | selectAdminNotePage 加 reviewed、新增 reviewNote SQL |
| AdminNoteService.java | 修改 | 新增 reviewNote 方法 |
| AdminNoteServiceImpl.java | 修改 | 新增 reviewNote 实现 |
| AdminNoteController.java | 修改 | 新增 PUT /{id}/review 接口 |
| UserAuthMapper.xml | 修改 | selectButtonListByUserId 返回 p.name AS parentName |
| AdminAuthServiceImpl.java | 修改 | getAuthButtonList 按 parentName 分组 |
| SysMenu.java | 修改 | 新增 @TableField(exist = false) parentName 字段 |
| note.ts | 修改 | 新增 reviewed 参数 + reviewNote API |
| note/list/index.vue | 修改 | 审核列 + 审核按钮 + v-auth 权限 |
| system/user/index.vue | 修改 | 操作按钮添加 v-auth |
| system/role/index.vue | 修改 | 操作按钮添加 v-auth |
| category/category/index.vue | 修改 | 操作按钮添加 v-auth |
| category/tag/index.vue | 修改 | 删除按钮添加 v-auth |
| monitor/notification/index.vue | 修改 | 发送按钮添加 v-auth |
| config/index.ts | 修改 | HOME_URL 改为 /dashboard/index |
| authMenuList.json | 修改 | 删除示例菜单 + 首页条目 |
| src/views/assembly/ | 删除 | Geeker-Admin 示例 |
| src/views/directives/ | 删除 | Geeker-Admin 示例 |
| src/views/echarts/ | 删除 | Geeker-Admin 示例 |
| src/views/form/ | 删除 | Geeker-Admin 示例 |
| src/views/link/ | 删除 | Geeker-Admin 示例 |
| src/views/menu/ | 删除 | Geeker-Admin 示例 |
| src/views/proTable/ | 删除 | Geeker-Admin 示例 |
| src/views/home/ | 删除 | 替换为 dashboard |
| src/views/auth/ | 删除 | Geeker-Admin 示例 |
| src/views/dataScreen/ | 删除 | Geeker-Admin 示例 |
| src/views/about/ | 删除 | Geeker-Admin 示例 |

---

# 🐛 Day 12 Debug 提示词

## 修复：NoteMapper.xml selectAdminNotePage 缺少 reviewed 列和筛选条件

```
修复 smart-note-system 的 NoteMapper.xml，selectAdminNotePage SQL 缺少 reviewed 字段的查询和筛选。

⚠️ 问题描述：
AdminNoteVO 已有 reviewed 字段，AdminNoteQueryDTO 已有 reviewed 参数，但 selectAdminNotePage SQL 中：
1. SELECT 列表缺少 n.reviewed AS reviewed
2. WHERE 条件缺少 reviewed 过滤

导致前端"审核"列永远显示 null，筛选和标记已审核功能均失效。

### 修改文件：NoteMapper.xml（resources/com/littlewin/note/mapper/NoteMapper.xml）

找到 selectAdminNotePage 的 SELECT 列表，在 `n.comment_count AS commentCount,` 之后添加：
n.reviewed AS reviewed,

找到 selectAdminNotePage 的 WHERE 条件块，在 endTime 条件之后、ORDER BY 之前添加：
<if test="query.reviewed != null">
    AND n.reviewed = #{query.reviewed}
</if>

修改后的完整 SQL 应为：
<select id="selectAdminNotePage" resultType="com.littlewin.note.domain.vo.AdminNoteVO">
    SELECT
        n.note_id     AS noteId,
        n.title       AS title,
        IFNULL(n.summary, LEFT(n.content, 200)) AS summary,
        n.status      AS status,
        n.is_public   AS isPublic,
        n.view_count  AS viewCount,
        n.like_count  AS likeCount,
        n.comment_count AS commentCount,
        n.reviewed    AS reviewed,
        n.user_id     AS userId,
        u.nickname    AS author,
        u.avatar      AS avatar,
        n.category_id AS categoryId,
        c.name        AS categoryName,
        n.create_time AS createTime,
        n.update_time AS updateTime
    FROM note n
    INNER JOIN sys_user u ON u.user_id = n.user_id
    LEFT JOIN sys_category c ON c.category_id = n.category_id
    WHERE n.del_flag = 0
    <if test="query.status != null">
        AND n.status = #{query.status}
    </if>
    <if test="query.categoryId != null">
        AND n.category_id = #{query.categoryId}
    </if>
    <if test="query.keyword != null and query.keyword != ''">
        AND (n.title LIKE CONCAT('%', #{query.keyword}, '%') OR n.content LIKE CONCAT('%', #{query.keyword}, '%'))
    </if>
    <if test="query.userId != null">
        AND n.user_id = #{query.userId}
    </if>
    <if test="query.startTime != null">
        AND n.create_time &gt;= #{query.startTime}
    </if>
    <if test="query.endTime != null">
        AND n.create_time &lt;= #{query.endTime}
    </if>
    <if test="query.reviewed != null">
        AND n.reviewed = #{query.reviewed}
    </if>
    ORDER BY n.create_time DESC
</select>

验证：
1. GET /api/admin/notes/list → 返回的笔记对象包含 reviewed 字段（0 或 1）
2. GET /api/admin/notes/list?reviewed=0 → 只返回未审核笔记
3. 前端笔记管理页"审核"列正确显示"未审核"/"已审核"
4. 未审核笔记操作列显示"标记已审核"按钮
```

---

## 补充优化：笔记列表操作列按钮占位对齐

```
笔记管理页操作列按钮因 v-if 条件显隐导致不同行按钮数量不一致，视觉不对齐。
改用固定文字占位方案：每个按钮位置永远存在一个按钮，只是文字和样式随状态变化。

⚠️ 方案：两个固定位置，文字变化占位
位置 1（上架/下架）：status=1 显示"下架"(danger)，status=3 显示"上架"(success)，其他显示"私密"(info, disabled)
位置 2（审核）：reviewed=0 显示"标记已审核"(warning)，reviewed=1 显示"审核已通过"(success, disabled)

每行永远有 4 个按钮：详情 + 位置1 + 位置2 + 删除，宽度完全一致。

### 修改文件：src/views/note/list/index.vue

操作列模板替换为：

<template #operation="scope">
  <el-button type="primary" link :icon="View" @click="viewDetail(scope.row)">详情</el-button>
  <el-button
    v-auth="'note:audit'"
    :type="scope.row.status === 1 ? 'danger' : scope.row.status === 3 ? 'success' : 'info'"
    link
    :disabled="scope.row.status !== 1 && scope.row.status !== 3"
    @click="handleAudit(scope.row, scope.row.status === 1 ? 3 : 1)"
  >
    {{ scope.row.status === 1 ? '下架' : scope.row.status === 3 ? '上架' : '私密' }}
  </el-button>
  <el-button
    v-auth="'note:review'"
    :type="scope.row.reviewed === 0 ? 'warning' : 'success'"
    link
    :disabled="scope.row.reviewed === 1"
    @click="handleReview(scope.row)"
  >
    {{ scope.row.reviewed === 0 ? '标记已审核' : '审核已通过' }}
  </el-button>
  <el-button v-auth="'note:delete'" type="danger" link :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
</template>

⚠️ 注意：
- "私密"按钮 disabled，仅占位，点击无反应
- "审核已通过"按钮 disabled，仅占位，表示该笔记已经审核过了
- handleAudit 逻辑不变，只是从两个按钮合并为一个按钮，通过 status 判断传 1 还是 3
- 可删除原来的 handleAudit 函数中对 status 参数的判断（因为按钮 click 已直接传值）

验证：
1. 正常公开未审核笔记：显示"详情""下架""标记已审核""删除"
2. 下架未审核笔记：显示"详情""上架""标记已审核""删除"
3. 正常公开已审核笔记：显示"详情""下架""审核已通过""删除"
4. 草稿/私密笔记：显示"详情""私密(disabled)""标记已审核""删除"
5. 所有行操作列宽度完全一致，4 个按钮位置固定对齐
```
