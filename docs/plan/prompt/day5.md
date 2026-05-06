# 📋 Day 5 任务清单：管理端仪表盘 + 日志系统
## 任务概览

| 序号 |              任务               |   端   | 优先级 |
| :--: | :-----------------------------: | :----: | :----: |
|  1   |       后端仪表盘统计接口        |  后端  |  🔴 高  |
|  2   | 后端行为日志上报 + 日志查询接口 |  后端  |  🔴 高  |
|  3   | Web 首页仪表盘（ECharts 图表）  |  Web   |  🔴 高  |
|  4   |   Web 操作日志 + 行为日志页面   |  Web   |  🔴高   |
|  5   |     小程序行为日志上报集成      | 小程序 |  🟡 中  |
|  6   |            联调验收             |  全端  |  🔴 高  |

​    

## 🔧 任务 1：后端仪表盘统计接口
已有基础：

- 数据库表： sys_user 、 note 、 note_reaction 、 sys_log_behavior （同一数据库，可跨表查询）

- 仪表盘需要跨模块聚合数据（sys_user + note），放在 system 模块

- system 和 note 是同一数据库，DashboardMapper 可直接查询 note 表

- 分页配置：MybatisPlusConfig.java 已配置分页插件

需实现 1 个接口：

|    接口    |               路径               |               说明                |
| :--------: | :------------------------------: | :-------------------------------: |
| 仪表盘统计 | `GET /api/admin/dashboard/stats` | 返回核心指标 + 趋势 + 分布 + TOP5 |

 

## 🔧 任务 2：后端行为日志上报 + 日志查询接口
已有基础：

- 操作日志实体： `common-log/entity/SysLogOperation.java `（id/userId/username/module/actionType/businessId/description/requestUrl/requestMethod/ipAddress/status/errorMsg/createTime）
- 操作日志 Mapper：` common-log/mapper/SysLogOperationMapper.java` （目前只有 insertOperationLog）
- 行为日志实体： `system/domain/entity/SysLogBehavior.java` （id/userId/actionType/content/createTime）
- 行为日志表： `sys_log_behavior` （action_type: 1浏览, 2搜索; content: 笔记ID或关键词）
- 日志 AOP： `common-log/aspect/LogAspect.java` （操作日志自动记录，已完善）

需实现 3 个接口：

|     接口     |                路径                 |   端   |                  说明                   |
| :----------: | :---------------------------------: | :----: | :-------------------------------------: |
| 行为日志上报 |     `POST /api/wx/log/behavior`     | 小程序 | type=view/search，content=笔记ID/关键词 |
| 操作日志查询 | `GET /api/admin/log/operation/list` | 管理端 | 分页+筛选（模块/类型/操作人/状态/日期） |
| 行为日志查询 | ` GET /api/admin/log/behavior/list` | 管理端 |       分页+筛选（类型/用户/日期）       |

   

## 🖥️ 任务 3：Web 工作台仪表盘
⚠️ 关键区分 ：

- 首页 (menu_id=1000) → /home/index → 欢迎页， 保持不动
- 工作台 (menu_id=1100) → /dashboard/index → 这里放 ECharts 仪表盘
  已有基础：

- ECharts 组件： `src/components/ECharts/index.vue` （传入 option 即可，已配置 resize、主题、按需引入）
- ECharts 配置： `src/components/ECharts/config/index.ts` （已引入 Bar/Line/Pie/Gauge/Radar 图表类型）
- 菜单数据已配置：menu_id=1100，component=/dashboard/index
## 🖥️ 任务 4：Web 操作日志 + 行为日志页面
已有基础：

- 菜单数据已配置：5010 操作审计、5015 行为日志
- ProTable 组件已熟练使用
- 日志页面只读，无增删改操作
## 📱 任务 5：小程序行为日志上报
已有基础：

- API 模块： `api/modules/log.js` （已有` logApi.report(type, content)` 方法）
- API 配置：` api/config.js `（已有 `LOG.BEHAVIOR = '/api/wx/log/behavior`）
## 🔗 联调验收标准
- 小程序浏览笔记 → Web 行为日志页可见浏览记录
- 小程序搜索关键词 → Web 行为日志页可见搜索记录
- Web 端执行审核/删除等操作 → Web 操作日志页可见操作记录
- Web 工作台仪表盘数据与实际数据库一致



# 📝 Day 5 提示词
## 提示词 1：后端仪表盘统计接口

```
在 smart-note-system 的 system 模块中，实现管理端仪表盘统计接口。

⚠️ 架构说明：
- 仪表盘需要跨模块聚合数据（sys_user + note 表），放在 system 模块
- system 和 note 是同一数据库，DashboardMapper 可直接查询 note 表
- 接口路径 /api/admin/dashboard/，Controller 命名 AdminDashboardController

已有代码参考：
- Controller 风格：system/controller/AdminAuthController.java
- Service 风格：system/service/impl/SysUserServiceImpl.java
- Mapper 风格：system/mapper/SysUserMapper.java + SysUserMapper.xml
- VO 风格：system/domain/vo/UserListVO.java

数据库表（同一数据库，可跨表查询）：
- sys_user：user_id, status, del_flag, create_time
- note：note_id, status, del_flag, is_public, create_time, view_count, title
- note_reaction：reaction_id, note_id, user_id, attitude, create_time
- sys_log_behavior：id, user_id, action_type, content, create_time

需要实现：

1. GET /api/admin/dashboard/stats — 仪表盘统计数据

   返回 DashboardStatsVO，包含：

   核心指标（4个数字卡片）：
   - totalUsers：总用户数（SELECT COUNT(*) FROM sys_user WHERE del_flag=0）
   - totalNotes：总笔记数（SELECT COUNT(*) FROM note WHERE del_flag=0 AND status=1）
   - todayNewUsers：今日新增用户（WHERE del_flag=0 AND DATE(create_time)=CURDATE()）
   - todayNewNotes：今日新增笔记（WHERE del_flag=0 AND status=1 AND DATE(create_time)=CURDATE()）

   增长趋势（最近7天，ECharts 折线图数据）：
   - dateList：日期列表，["04-23","04-24",...,"04-29"]，最近7天
   - newUserList：每天新增用户数
   - newNoteList：每天新增笔记数

   笔记状态分布（ECharts 饼图数据）：
   - statusDistribution：[{name:"草稿",value:x},{name:"正常",value:x},{name:"回收站",value:x},{name:"下架",value:x}]

   热门笔记 TOP5（ECharts 横向柱状图数据）：
   - hotNotes：[{title:"xxx",viewCount:100},...]，按 view_count DESC LIMIT 5

请创建：
- AdminDashboardController.java（com.littlewin.system.controller）
- AdminDashboardService.java + AdminDashboardServiceImpl.java（com.littlewin.system.service）
- DashboardStatsVO.java（com.littlewin.system.domain.vo，用 @Builder 模式）
- DashboardMapper.java（com.littlewin.system.mapper）
- DashboardMapper.xml（com.littlewin.system.mapper 资源目录，与 SysUserMapper.xml 同级）

DashboardMapper.xml 中的 SQL 提示：
- 增长趋势用 DATE(create_time) 分组，BETWEEN DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND CURDATE()
- 热门笔记：SELECT title, view_count FROM note WHERE del_flag=0 AND status=1 ORDER BY view_count DESC LIMIT 5
- 注意：即使某天没有数据，dateList 也要补齐7天（在 Java 层用 LocalDate 循环补齐，Mapper 只查有数据的）
```



## 提示词 2：后端行为日志上报 + 日志查询接口

````
在 smart-note-system 中，实现行为日志上报和日志查询接口。

⚠️ 架构说明：
- 行为日志上报是小程序端接口 → WxLogController 放在 system 模块（SysLogBehavior 实体在 system 模块）
- 日志查询是管理端接口 → AdminLogController 放在 system 模块
- 操作日志的 Mapper（SysLogOperationMapper）在 common-log 模块，需新增分页查询方法

已有代码参考：
- 操作日志实体：common-log/entity/SysLogOperation.java（id/userId/username/module/actionType/businessId/description/requestUrl/requestMethod/ipAddress/status/errorMsg/createTime）
- 操作日志 Mapper：common-log/mapper/SysLogOperationMapper.java（目前只有 insertOperationLog）
- 操作日志 Mapper XML：common-log/resources/com/littlewin/common/log/mapper/SysLogOperationMapper.xml
- 行为日志实体：system/domain/entity/SysLogBehavior.java（id/userId/actionType/content/createTime）
- 行为日志表：sys_log_behavior（action_type: 1浏览, 2搜索; content: 笔记ID或关键词）
- Controller 风格：system/controller/AdminAuthController.java
- 分页查询参考：note/mapper/NoteMapper.xml 中的 selectAdminNotePage

需要实现：

一、行为日志上报（小程序端）

1. POST /api/wx/log/behavior — 上报行为日志
   - 请求体 BehaviorLogDTO：{ type: String("view"/"search"), content: String }
   - type 映射 actionType：view→1, search→2
   - content：浏览时传笔记ID，搜索时传关键词
   - userId 从 SecurityUtils.getLoginUser() 获取
   - 无需 @Log 注解（行为日志本身不需要被操作日志记录）
   - 返回 Result.success(null)

二、操作日志查询（管理端）

2. GET /api/admin/log/operation/list — 操作日志分页列表
   - 参数 OperationLogQueryDTO（pageNum/pageSize/module/actionType/startTime/endTime/username/status）
   - 返回 IPage<OperationLogVO>
   - OperationLogVO 字段：id, userId, username, module, actionType, businessId, description, requestUrl, requestMethod, ipAddress, status, errorMsg, createTime
   - module 筛选：AUTH/USER/NOTE/DICT/AI/ROLE
   - actionType 筛选：1登录/2退出/3创建/4修改/5删除
   - 按 create_time DESC

三、行为日志查询（管理端）

3. GET /api/admin/log/behavior/list — 行为日志分页列表
   - 参数 BehaviorLogQueryDTO（pageNum/pageSize/actionType/startTime/endTime/userId）
   - 返回 IPage<BehaviorLogVO>
   - BehaviorLogVO 字段：id, userId, nickname(关联sys_user), actionType, content, createTime
   - actionType 筛选：1浏览/2搜索
   - 按 create_time DESC

请创建/修改：

1. system/controller/WxLogController.java
2. system/controller/AdminLogController.java
3. system/service/LogService.java + LogServiceImpl.java
4. system/domain/dto/BehaviorLogDTO.java
5. system/domain/dto/OperationLogQueryDTO.java
6. system/domain/dto/BehaviorLogQueryDTO.java
7. system/domain/vo/OperationLogVO.java
8. system/domain/vo/BehaviorLogVO.java
9. system/mapper/SysLogBehaviorMapper.java + SysLogBehaviorMapper.xml（insert + 分页查询）
10. common-log/mapper/SysLogOperationMapper.java — 新增 selectOperationLogPage 方法
11. common-log/resources/com/littlewin/common/log/mapper/SysLogOperationMapper.xml — 新增分页查询 SQL

SysLogOperationMapper.xml 新增 SQL 参考：
```xml
<select id="selectOperationLogPage" resultType="com.littlewin.common.log.entity.SysLogOperation">
    SELECT id, user_id, username, module, action_type, business_id,
           description, request_url, request_method, ip_address,
           status, error_msg, create_time
    FROM sys_log_operation
    <where>
        <if test="query.module != null and query.module != ''">
            AND module = #{query.module}
        </if>
        <if test="query.actionType != null">
            AND action_type = #{query.actionType}
        </if>
        <if test="query.username != null and query.username != ''">
            AND username LIKE CONCAT('%', #{query.username}, '%')
        </if>
        <if test="query.status != null">
            AND status = #{query.status}
        </if>
        <if test="query.startTime != null">
            AND create_time &gt;= #{query.startTime}
        </if>
        <if test="query.endTime != null">
            AND create_time &lt;= #{query.endTime}
        </if>
    </where>
    ORDER BY create_time DESC
</select>
```
````

## 提示词 3：Web 工作台仪表盘

```
在 smart-note-ui Web 前端中，实现工作台仪表盘页面（ECharts 图表展示统计数据）。

⚠️ 关键区分：
- 首页 (/home/index) 是欢迎页，保持不动
- 工作台 (/dashboard/index) 才是仪表盘，在这里实现 ECharts 图表

已有代码参考：
- ECharts 组件：src/components/ECharts/index.vue（传入 option 即可，已配置 resize、主题、按需引入）
- ECharts 配置：src/components/ECharts/config/index.ts（已引入 Bar/Line/Pie/Gauge/Radar 图表类型）
- ECOption 类型：从 "@/components/ECharts/config" 导入
- API 风格：src/api/modules/note.ts
- 后端返回格式：Result<DashboardStatsVO>
- 菜单数据：menu_id=1100，component=/dashboard/index

需要创建：

1. src/api/modules/dashboard.ts — 仪表盘 API
   ```typescript
   import http from "@/api";

   export interface DashboardStats {
     totalUsers: number;
     totalNotes: number;
     todayNewUsers: number;
     todayNewNotes: number;
     dateList: string[];
     newUserList: number[];
     newNoteList: number[];
     statusDistribution: { name: string; value: number }[];
     hotNotes: { title: string; viewCount: number }[];
   }

   export const getDashboardStats = () => {
     return http.get<DashboardStats>("/admin/dashboard/stats");
   };
   
2. src/views/dashboard/index.vue — 工作台仪表盘页面（新建文件）

布局设计（el-row + el-col 栅格）：

第一行：4个统计卡片（el-col :span="6"）

- 总用户数（图标 UserFilled，蓝色 #409eff）
- 总笔记数（图标 Document，绿色 #67c23a）
- 今日新增用户（图标 User，橙色 #e6a23c）
- 今日新增笔记（图标 EditPen，红色 #f56c6c）
- 每个卡片：大号数字 + 标题 + 图标，用 el-card 包裹
第二行：增长趋势折线图（el-col :span="16"）+ 笔记状态饼图（el-col :span="8"）

- 折线图：双线，新用户（蓝色线）+ 新笔记（绿色线），X 轴为日期
- 饼图：草稿/正常/回收站/下架 分布，颜色 info/success/warning/danger
第三行：热门笔记 TOP5 横向柱状图（el-col :span="24"）

- 横向柱状图，Y 轴为笔记标题（过长截断），X 轴为浏览量
ECharts option 编写参考：

```typescript
// 折线图
const lineOption = reactive<ECOption>({
  title: { text: "增长趋势（近7天）" },
  tooltip: { trigger: "axis" },
  legend: { data: ["新增用户", "新增笔记"] },
  xAxis: { type: "category", data: stats.dateList },
  yAxis: { type: "value" },
  series: [
    { name: "新增用户", type: "line", data: stats.newUserList, smooth: true, itemStyle: { color: "#409eff" } },
    { name: "新增笔记", type: "line", data: stats.newNoteList, smooth: true, itemStyle: { color: "#67c23a" } }
  ]
});

// 饼图
const pieOption = reactive<ECOption>({
  title: { text: "笔记状态分布" },
  tooltip: { trigger: "item" },
  series: [{
    type: "pie", radius: ["40%", "70%"],
    data: stats.statusDistribution,
    emphasis: { itemStyle: { shadowBlur: 10 } }
  }]
});

// 横向柱状图
const barOption = reactive<ECOption>({
  title: { text: "热门笔记 TOP5" },
  tooltip: { trigger: "axis" },
  xAxis: { type: "value" },
  yAxis: { type: "category", data: stats.hotNotes.map(n => n.title.length > 10 ? n.title.substring(0,10)+"..." : n.title) },
  series: [{ type: "bar", data: stats.hotNotes.map(n => n.viewCount), itemStyle: { color: "#409eff" } }]
});


页面逻辑：
   
   - onMounted 调用 getDashboardStats()，loading 状态
   - 数据返回后赋值给响应式变量，ECharts 组件通过 :option 绑定自动渲染
   - ECharts 组件用法： <echarts :option="lineOption" height="350px"/>
注意：

- ECharts 组件已封装好，直接传 option 即可，不需要手动 init
- ECOption 类型从 "@/components/ECharts/config" 导入
- API 路径不加 /api 前缀，axios baseURL 已配置
- 卡片样式参考 Geeker-Admin 的 dashboard 风格，简洁大方

```



## 提示词 4：Web 操作日志 + 行为日志页面

```
在 smart-note-ui Web 前端中，实现操作日志和行为日志两个页面。

已有代码参考：

- 页面风格：src/views/note/list/index.vue（ProTable 列表 + 搜索 + 操作）
- API 风格：src/api/modules/note.ts
- 后端分页格式：{ records: [], total: 0 }
- 菜单数据：init_sys_data.sql 中已有 5010 操作审计、5015 行为日志
需要创建：

1. src/api/modules/log.ts — 日志管理 API
```typescript
import http from "@/api";

export interface ReqOperationLogParams {
  pageNum?: number;
  pageSize?: number;
  module?: string;
  actionType?: number;
  username?: string;
  status?: number;
  startTime?: string;
  endTime?: string;
}

export interface OperationLogVO {
  id: number;
  userId: number;
  username: string;
  module: string;
  actionType: number;
  businessId: number;
  description: string;
  requestUrl: string;
  requestMethod: string;
  ipAddress: string;
  status: number;
  errorMsg: string;
  createTime: string;
}

export interface ReqBehaviorLogParams {
  pageNum?: number;
  pageSize?: number;
  actionType?: number;
  userId?: number;
  startTime?: string;
  endTime?: string;
}

export interface BehaviorLogVO {
  id: number;
  userId: number;
  nickname: string;
  actionType: number;
  content: string;
  createTime: string;
}

export const getOperationLogList = (params: ReqOperationLogParams) => {
  return http.get(`/admin/log/operation/list`, params, { loading: false });
};

export const getBehaviorLogList = (params: ReqBehaviorLogParams) => {
  return http.get(`/admin/log/behavior/list`, params, { loading: false });
};

2. src/views/monitor/log/index.vue — 操作日志页面
   
   使用 ProTable 组件：
   
   搜索条件：
   
   - 模块：el-select，选项：AUTH/USER/NOTE/DICT/AI/ROLE
   - 操作类型：el-select，选项：登录(1)/退出(2)/创建(3)/修改(4)/删除(5)
   - 操作人：el-input，placeholder="搜索操作人"
   - 状态：el-select，选项：成功(1)/失败(0)
   - 日期范围：el-date-picker type="daterange"
   
   表格列：
   
   - id：label="ID"，width=80
   - username：label="操作人"，width=100
   - module：label="模块"，width=80，用 el-tag 渲染（不同模块不同颜色）
   - actionType：label="操作类型"，width=90，用 el-tag 渲染
     - 1→success 登录 / 2→info 退出 / 3→primary 创建 / 4→warning 修改 / 5→danger 删除
   - description：label="描述"，showOverflowTooltip
   - requestUrl：label="请求URL"，width=200，showOverflowTooltip
   - requestMethod：label="方法"，width=80，用 el-tag（GET→success/POST→primary/PUT→warning/DELETE→danger）
   - ipAddress：label="IP"，width=130
   - status：label="状态"，width=80，el-tag（1→success 成功 / 0→danger 失败）
   - errorMsg：label="错误信息"，width=150，showOverflowTooltip，仅失败时显示
   - createTime：label="操作时间"，width=170，sortable
   dataCallback：return { list: data.records, total: data.total }
   
   ⚠️ 操作日志只读，无增删改操作，不需要操作列和 tableHeader
3. src/views/monitor/behavior/index.vue — 行为日志页面
   
   使用 ProTable 组件：
   
   搜索条件：
   
   - 行为类型：el-select，选项：浏览(1)/搜索(2)
   - 日期范围：el-date-picker type="daterange"
   表格列：
   
   - id：label="ID"，width=80
   - nickname：label="用户"，width=120
   - actionType：label="行为类型"，width=100，el-tag（1→primary 浏览 / 2→warning 搜索）
   - content：label="内容"，showOverflowTooltip（浏览时显示笔记ID，搜索时显示关键词）
   - createTime：label="发生时间"，width=170，sortable
   dataCallback：return { list: data.records, total: data.total }
   
   ⚠️ 行为日志只读，无增删改操作
注意：

- API 路径不加 /api 前缀
- 日志页面都是只读的，ProTable 不需要 operation 列和 tableHeader 插槽
- 日期范围搜索需要拆分为 startTime/endTime（参考 note/list/index.vue 的 getTableList 拦截写法）
```



## 提示词 5：小程序行为日志上报集成

```
在 smart-note-mp 小程序中，集成行为日志上报功能。
已有代码：

- API 模块：api/modules/log.js（已有 logApi.report(type, content) 方法）
- API 配置：api/config.js（已有 LOG.BEHAVIOR = '/api/wx/log/behavior'）
- 笔记详情页：pages/note-detail/note-detail.vue（需集成浏览上报）
- 社区页：pages/community/community.vue（需集成搜索上报）
需要修改：

1. pages/note-detail/note-detail.vue — 浏览笔记时上报
   
   在文件顶部 import`import { logApi } from '@/api/modules/log.js'`
   
   在 loadDetail 方法中，笔记加载成功后上报浏览行为（在 loading.value = false 之前）：`logApi.report('view', String(id)).catch(() => {})`
   
说明：
	type='view'，content=笔记ID
	.catch(() => {}) 静默处理，上报失败不影响用户体验
	只在加载成功时上报，加载失败不上报
	pages/community/community.vue — 搜索时上报

2. 在文件顶部 import：`import { logApi } from '@/api/modules/log.js'`
修改 onSearch 方法：
```JavaScript
const onSearch = () => {
  if (keyword.value.trim()) {
    logApi.report('search', keyword.value.trim()).catch(() => {})
  }
  loadNotes(true)
}

说明：
   
   - type='search'，content=搜索关键词
   - 只有关键词非空时才上报
   - .catch(() => {}) 静默处理
注意：

- 不需要修改 api/modules/log.js，它已经实现了 report 方法
- 上报是异步静默操作，不阻塞页面交互
- 不需要在上报成功/失败时给用户提示
```

## 提示词 6：联调验证

```
Day 5 全部开发完成后，按以下步骤联调验证：

后端验证：

1. 启动 smart-note-system，用 admin 账号登录获取 Token
2. 测试仪表盘接口：
   - GET /api/admin/dashboard/stats → 返回 totalUsers/totalNotes/todayNew*/趋势/分布/热门
3. 测试行为日志上报：
   - POST /api/wx/log/behavior → 传 {type:"view", content:"1"} → 插入成功
4. 测试日志查询：
   - GET /api/admin/log/operation/list → 返回操作日志分页数据
   - GET /api/admin/log/behavior/list → 返回行为日志分页数据
   - 筛选条件（module/actionType/日期范围）正常过滤
Web 端验证：

5. 用 admin 登录 Web 管理端
6. 左侧菜单"工作台"→ 仪表盘页面：4个统计卡片 + 折线图 + 饼图 + 柱状图，数据与数据库一致
7. 左侧菜单"首页"→ 欢迎页，保持不变
8. 系统监控 > 操作审计：列表数据正常、模块/类型/状态筛选正常
9. 系统监控 > 行为日志：列表数据正常、行为类型筛选正常
小程序端验证：

10. 小程序浏览一篇笔记 → 后端 sys_log_behavior 新增一条 action_type=1 记录
11. 小程序搜索关键词 → 后端 sys_log_behavior 新增一条 action_type=2 记录
跨端联调：

12. 小程序浏览笔记 → Web 行为日志页可见浏览记录
13. 小程序搜索关键词 → Web 行为日志页可见搜索记录
14. Web 端执行审核/删除等操作 → Web 操作日志页可见操作记录
15. Web 工作台仪表盘数据与实际数据库数据一致（新增用户/笔记后刷新仪表盘，数字更新）
```

---

# ⏱️ Day 5 执行顺序

| 顺序 | 提示词                                    | 前置依赖      |
| :--: | :---------------------------------------- | :------------ |
|  1️⃣   | 提示词 1：后端仪表盘统计接口              | Day 4 已完成  |
|  2️⃣   | 提示词 2：后端行为日志上报 + 日志查询接口 | Day 4 已完成  |
|  3️⃣   | Knife4j/Postman 验证接口                  | 提示词 1+2    |
|  4️⃣   | 提示词 3：Web 工作台仪表盘                | 提示词 1 验证 |
|  5️⃣   | 提示词 4：Web 操作日志 + 行为日志页面     | 提示词 2 验证 |
|  6️⃣   | 提示词 5：小程序行为日志上报集成          | 提示词 2 验证 |
|  7️⃣   | 提示词 6：联调验证                        | 全部完成      |

> **SQL 状态**：init_sys_data.sql 已包含行为日志菜单 5015 和行为类型字典，无需额外执行 SQL。



## 补充提示词：笔记浏览次数自增

```
在 smart-note-system 的 note 模块中，实现笔记浏览次数自增功能。

⚠️ 背景：
- note 表已有 view_count INT NOT NULL DEFAULT 0 字段
- 目前没有任何代码在用户浏览笔记时自增该字段
- 浏览次数自增应在小程序端获取笔记详情时触发

已有代码参考：
- 小程序端笔记 Controller：note/controller/WxNoteController.java（获取笔记详情接口）
- 小程序端笔记 Service：note/service/WxNoteService.java + WxNoteServiceImpl.java
- 笔记 Mapper：note/mapper/NoteMapper.java + NoteMapper.xml

需要修改：

1. NoteMapper.java — 新增方法：
   void incrementViewCount(@Param("noteId") Long noteId);

2. NoteMapper.xml — 新增 SQL：
   <update id="incrementViewCount">
       UPDATE note SET view_count = view_count + 1 WHERE note_id = #{noteId} AND del_flag = 0
   </update>

3. WxNoteServiceImpl.java — 在获取笔记详情方法中，查询笔记成功后调用：
   noteMapper.incrementViewCount(noteId);

   ⚠️ 注意：
   - 先查询笔记详情，确认笔记存在且状态正常后再自增
   - 笔记不存在或已删除时不要自增
   - 自增操作不需要 @Log 注解（浏览不是管理操作）
   - 不需要事务，单条 UPDATE 原子操作即可

验证：
- 调用小程序端笔记详情接口 GET /api/wx/notes/{id}
- 查询数据库 SELECT note_id, view_count FROM note WHERE note_id = ?
- 每次调用详情接口后 view_count 应 +1
```

