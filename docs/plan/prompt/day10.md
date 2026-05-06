# 📋 Day 10 任务清单：遗漏页面补充

## 任务概览

| 序号 | 任务 | 优先级 | 说明 |
|:--:|:---|:--:|:---|
| 1 | AdminTagController 标签管理接口 | 🔴 高 | 管理端查看/删除所有用户标签 |
| 2 | 标签管理页面 | 🔴 高 | 新建 category/tag/index.vue |
| 3 | AI 监控页面 | 🟡 中 | 新建 monitor/aiLog/index.vue（预留） |

## 当前已有基础

- `NoteTag` 实体：tagId, name, userId, createTime, noteCount（非数据库字段）
- `NoteTagMapper`：已有 selectTagListWithCount(userId) 查询用户标签+笔记数
- `WxTagController`：小程序端标签 CRUD（/api/wx/tags）
- `NoteTagService`：listMyTags, saveTag, removeTag, listNotesByTag

## 需要实现的内容

| 序号 | 问题 | 当前状态 | 目标 |
|:--:|:---|:---|:---|
| 1 | 无管理端标签接口 | 只有小程序端 WxTagController | 新增 AdminTagController |
| 2 | 标签管理页面缺失 | 菜单 4010 已配置，无页面 | 新建 category/tag/index.vue |
| 3 | AI 监控页面缺失 | 菜单 5020 已配置，无页面 | 新建 monitor/aiLog/index.vue |

---

# 📝 Day 10 提示词

## 提示词 1：AdminTagController 标签管理接口

```
在 smart-note-system 的 note 模块中，实现管理端标签管理接口。

⚠️ 架构约束：
1. 管理端接口路径 /api/admin/，Controller 命名 Admin*Controller
2. 参考 system 模块隔离模式：AdminAuthController（管理端）vs WxAuthController（小程序端）

已有代码参考：
- 实体类：note/domain/entity/NoteTag.java（tagId, name, userId, createTime, noteCount）
- Mapper：note/mapper/NoteTagMapper.java（已有 selectTagListWithCount）
- Mapper XML：note/resources/com/littlewin/note/mapper/NoteTagMapper.xml
- Controller 风格：note/controller/AdminNoteController.java（@Log、Result 返回）
- Service 风格：note/service/impl/AdminNoteServiceImpl.java

需要实现 2 个接口：

1. GET /api/admin/tags/list — 管理端标签列表（分页）
   - 参数：pageNum(默认1)、pageSize(默认20)、keyword(可选，按标签名模糊搜索)
   - 返回 IPage<AdminTagVO>
   - AdminTagVO 字段：tagId、name、userId、userName(关联sys_user.nickname)、noteCount(标签关联笔记数)、createTime
   - 查询逻辑：LEFT JOIN sys_user 查用户名，LEFT JOIN note_tag_rel 统计笔记数
   - 按 create_time DESC 排序

2. DELETE /api/admin/tags/{id} — 管理员删除标签
   - ⚠️ 删除逻辑：先删 note_tag_rel（关联关系），再删 note_tag（标签本身），顺序不能反
   - AdminTagServiceImpl 删除方法加 @Transactional 保证原子性
   - 添加 @Log 注解

请创建：
- AdminTagController.java（com.littlewin.note.controller）
- AdminTagService.java + AdminTagServiceImpl.java（com.littlewin.note.service）
- AdminTagVO.java（com.littlewin.note.domain.vo）
- AdminTagQueryDTO.java（com.littlewin.note.domain.dto，字段：keyword String 可选）
- NoteTagMapper.java 中新增 selectAdminTagPage 方法
- NoteTagMapper.xml 中新增对应 SQL

Mapper 方法签名：
IPage<AdminTagVO> selectAdminTagPage(Page<AdminTagVO> page, @Param("query") AdminTagQueryDTO query);

AdminTagMapper.xml SQL 参考：
<select id="selectAdminTagPage" resultType="com.littlewin.note.domain.vo.AdminTagVO">
    SELECT
        t.tag_id AS tagId,
        t.name AS name,
        t.user_id AS userId,
        u.nickname AS userName,
        (SELECT COUNT(*) FROM note_tag_rel r WHERE r.tag_id = t.tag_id) AS noteCount,
        t.create_time AS createTime
    FROM note_tag t
    LEFT JOIN sys_user u ON u.user_id = t.user_id
    <where>
        <if test="query.keyword != null and query.keyword != ''">
            AND t.name LIKE CONCAT('%', #{query.keyword}, '%')
        </if>
    </where>
    ORDER BY t.create_time DESC
</select>

删除 SQL（先删关联关系，再删标签，顺序不能反）：
<delete id="deleteTagRelByTagId">
    DELETE FROM note_tag_rel WHERE tag_id = #{tagId}
</delete>

<delete id="deleteTagById">
    DELETE FROM note_tag WHERE tag_id = #{tagId}
</delete>

AdminTagServiceImpl 删除方法示例：
@Override
@Transactional(rollbackFor = Exception.class)
public void deleteTag(Long tagId) {
    noteTagMapper.deleteTagRelByTagId(tagId);  // 先删关联
    noteTagMapper.deleteTagById(tagId);         // 再删标签
}

验证：
1. GET /api/admin/tags/list → 返回分页数据
2. GET /api/admin/tags/list?keyword=Java → 返回标签名包含 Java 的标签
3. DELETE /api/admin/tags/{id} → 删除成功，关联关系也删除
```

---

## 提示词 2：Web 标签管理页面

```
在 smart-note-ui Web 前端中，实现标签管理页面。

已有代码参考：
- 页面风格：src/views/system/user/index.vue（ProTable 列表 + 搜索 + 操作）
- API 风格：src/api/modules/category.ts
- 后端分页格式：{ records: [], total: 0 }

需要创建：

1. src/api/modules/tag.ts — 标签管理 API（与 category.ts 风格一致）

import http from "@/api";

export interface ReqTagParams {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
}

export interface TagVO {
  tagId: number;
  name: string;
  userId: number;
  userName: string;
  noteCount: number;
  createTime: string;
}

export const getTagList = (params: ReqTagParams) => {
  return http.get(`/admin/tags/list`, params, { loading: false });
};

export const deleteTag = (id: number) => {
  return http.delete(`/admin/tags/${id}`);
};

2. src/views/category/tag/index.vue — 标签管理页面

使用 ProTable 组件，参考 user/index.vue：

搜索条件：
- 标签名：el-input，placeholder="搜索标签名"

表格列（columns）：
- tagId：label="ID"，width=80，sortable
- name：label="标签名称"
- userName：label="所属用户"，width=120
- noteCount：label="笔记数"，width=100，sortable
- createTime：label="创建时间"，width=170，sortable
- 操作列：删除按钮

操作逻辑：
- 删除：确认弹窗（提示"删除后该标签下的笔记关联关系也将被删除"）→ deleteTag(id)

dataCallback：
const dataCallback = (data: any) => {
  return { list: data.records, total: data.total };
};

requestApi 使用 getTagList，initParam 为空 reactive({})。

注意：
- API 路径不需要加 /api 前缀，axios baseURL 已配置
- 标签管理不需要新增/编辑功能（标签由用户在小程序端创建）
- 只需要查看列表和删除违规标签
```

---

## 提示词 3：Web AI 监控页面（预留）

```
在 smart-note-ui Web 前端中，实现 AI 监控页面（预留，Day 4 实现后端）。

⚠️ 本页面为预留页面，后端接口在 Day 4 实现。当前先创建页面框架和 API 定义。

需要创建：

1. src/api/modules/aiLog.ts — AI 监控 API（预留，与 category.ts 风格一致）

import http from "@/api";

export interface ReqAiLogParams {
  pageNum?: number;
  pageSize?: number;
  status?: number;
  startTime?: string;
  endTime?: string;
}

export interface AiLogVO {
  id: number;
  noteId: number;
  noteTitle: string;
  summary: string;
  keywords: string;
  status: number; // 1成功 0失败
  errorMsg: string;
  createTime: string;
}

export const getAiLogList = (params: ReqAiLogParams) => {
  return http.get(`/admin/ai/logs`, params, { loading: false });
};

2. src/views/monitor/aiLog/index.vue — AI 监控页面

使用 ProTable 组件：

搜索条件：
- 状态：el-select，选项：全部(空)/成功(1)/失败(0)
- 日期范围：el-date-picker type="daterange"

表格列（columns）：
- id：label="ID"，width=80
- noteTitle：label="笔记标题"，showOverflowTooltip
- summary：label="摘要内容"，showOverflowTooltip
- keywords：label="关键词"，width=150
- status：label="状态"，width=90，el-tag（1→success 成功 / 0→danger 失败）
- errorMsg：label="错误信息"，showOverflowTooltip，仅失败时显示
- createTime：label="生成时间"，width=170，sortable

dataCallback：
const dataCallback = (data: any) => {
  return { list: data.records, total: data.total };
};

⚠️ 注意：
- 后端接口 /admin/ai/logs 在 Day 4 实现，当前页面可正常显示但列表为空
- 页面框架先搭好，Day 4 后端实现后直接对接
- AI 监控页面只读，不需要增删改操作
```

---

# ⏱️ Day 10 执行顺序

| 顺序 | 提示词 | 前置依赖 | 预计耗时 |
|:--:|:---|:--:|:--:|
| 1️⃣ | 提示词 1：AdminTagController | 无 | 1.5 小时 |
| 2️⃣ | 提示词 2：标签管理页面 | 提示词 1 | 1 小时 |
| 3️⃣ | 提示词 3：AI 监控页面 | 无 | 30 分钟 |

> 提示词 1 和 3 互相独立，可并行执行。提示词 2 依赖提示词 1 的后端接口。

---

# 🔍 Day 10 涉及的文件清单

| 文件 | 改动类型 | 说明 |
|:---|:---|:---|
| AdminTagController.java | 新建 | 管理端标签接口 |
| AdminTagService.java | 新建 | 标签管理服务接口 |
| AdminTagServiceImpl.java | 新建 | 标签管理服务实现（@Transactional） |
| AdminTagVO.java | 新建 | 管理端标签 VO |
| AdminTagQueryDTO.java | 新建 | 查询参数 DTO（keyword） |
| NoteTagMapper.java | 修改 | 新增 selectAdminTagPage、deleteTagRelByTagId、deleteTagById |
| NoteTagMapper.xml | 修改 | 新增 SQL |
| tag.ts | 新建 | Web 端标签 API |
| category/tag/index.vue | 新建 | 标签管理页面 |
| aiLog.ts | 新建 | Web 端 AI 监控 API |
| monitor/aiLog/index.vue | 新建 | AI 监控页面 |





## 追加：提示词 4：标签管理排序修复 + 用户搜索

```
修复 smart-note-ui 标签管理页面的排序功能，并新增按用户名搜索标签。

当前问题：
1. category/tag/index.vue 中 tagId、noteCount、createTime 三列设了 sortable: "custom"，
   但 ProTable 上没有绑定 @sort-change，点击排序图标无响应
2. 缺少按用户名搜索标签的功能

需要修改的文件：

### 1. 前端：src/views/category/tag/index.vue

参考 src/views/system/user/index.vue 的排序实现：

(1) ProTable 标签添加 @sort-change 事件：
<ProTable
  ref="proTable"
  :columns="columns"
  :request-api="getTableList"
  :init-param="initParam"
  :data-callback="dataCallback"
  row-key="tagId"
  @sort-change="handleSortChange"
>

(2) 新增排序状态和处理函数：
const sortParam = reactive({
  orderColumn: "",
  orderRule: ""
});

const handleSortChange = ({ prop, order }: any) => {
  const columnMap: Record<string, string> = {
    tagId: "t.tag_id",
    noteCount: "noteCount",
    createTime: "t.create_time"
  };
  if (!order) {
    sortParam.orderColumn = "";
    sortParam.orderRule = "";
  } else {
    sortParam.orderColumn = columnMap[prop] || "";
    sortParam.orderRule = order;
  }
  proTable.value?.getTableList();
};

(3) getTableList 中把排序参数传给后端：
const getTableList = (params: any) => {
  const newParams = JSON.parse(JSON.stringify(params));
  if (newParams.name) {
    newParams.keyword = newParams.name;
    delete newParams.name;
  }
  if (sortParam.orderColumn) {
    newParams.orderColumn = sortParam.orderColumn;
    newParams.orderRule = sortParam.orderRule;
  }
  return getTagList(newParams);
};

(4) columns 中 userName 列添加搜索：
{ prop: "userName", label: "所属用户", width: 120, search: { el: "input", props: { placeholder: "搜索用户名" } } }

### 2. 前端：src/api/modules/tag.ts

ReqTagParams 中补充排序字段：
export interface ReqTagParams {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  orderColumn?: string;
  orderRule?: string;
}

### 3. 后端：AdminTagQueryDTO.java

新增排序字段：
private String orderColumn;
private String orderRule;

### 4. 后端：NoteTagMapper.xml（selectAdminTagPage）

在 ORDER BY 处改为动态排序：
原：<if false>ORDER BY t.create_time DESC</if>

改为：
<choose>
    <when test="query.orderColumn != null and query.orderColumn != ''">
        ORDER BY ${query.orderColumn}
        <choose>
            <when test="query.orderRule == 'descending'">DESC</when>
            <otherwise>ASC</otherwise>
        </choose>
    </when>
    <otherwise>
        ORDER BY t.create_time DESC
    </otherwise>
</choose>

### 5. 后端：NoteTagMapper.xml（selectAdminTagPage 的 WHERE）

现有 keyword 只搜标签名，需要补充用户名搜索。将 <where> 块改为：
<where>
    <if test="query.keyword != null and query.keyword != ''">
        AND t.name LIKE CONCAT('%', #{query.keyword}, '%')
    </if>
</where>

用户名搜索的处理方式：前端把 userName 搜索值作为 keyword 传入，后端 keyword 同时搜标签名和用户名：
<where>
    <if test="query.keyword != null and query.keyword != ''">
        AND (t.name LIKE CONCAT('%', #{query.keyword}, '%')
             OR u.nickname LIKE CONCAT('%', #{query.keyword}, '%'))
    </if>
</where>

### 6. 前端：category/tag/index.vue 的 getTableList 调整

userName 搜索值也需要映射到 keyword（与 name 一样）：
const getTableList = (params: any) => {
  const newParams = JSON.parse(JSON.stringify(params));
  if (newParams.name || newParams.userName) {
    newParams.keyword = newParams.name || newParams.userName || "";
    delete newParams.name;
    delete newParams.userName;
  }
  if (sortParam.orderColumn) {
    newParams.orderColumn = sortParam.orderColumn;
    newParams.orderRule = sortParam.orderRule;
  }
  return getTagList(newParams);
};

注意：当同时输入标签名和用户名时，keyword 只取其中一个（优先标签名），这是简单处理。
如果希望精确分离，可以加 username 字段单独传后端，但当前场景不需要这么复杂。

⚠️ SQL 注入提醒：${query.orderColumn} 使用了 ${} 拼接，orderColumn 的白名单校验在前端 columnMap 中限制，
后端也可以在 Service 层加校验（可选，当前场景前端已限制，风险可控）。

验证：
1. 点击 ID 列排序 → 数据按 tag_id 排序
2. 点击笔记数列排序 → 数据按关联笔记数排序
3. 点击创建时间列排序 → 数据按时间排序
4. 再次点击同一列 → 取消排序，恢复默认
5. 输入用户名搜索 → 显示该用户名下的标签
6. 输入标签名搜索 → 显示匹配的标签
```
