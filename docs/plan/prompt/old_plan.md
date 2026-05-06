# 十天开发计划（接口→前端→联调 模式）

## Day 1 — 笔记 CRUD：后端 + 小程序 + 联调

| 阶段 | 任务 |
|---|---|
| 🔧 后端 | 笔记创建 `POST /api/wx/notes`、列表 `GET /api/wx/notes`、更新 `PUT /api/wx/notes/{id}`、删除 `DELETE /api/wx/notes/{id}`、恢复 `PUT /api/wx/notes/{id}/restore` |
| 📱 小程序 | 社区页移除 mock 对接真实列表 API；发布页对接创建接口；笔记编辑（复用发布页）；笔记删除/恢复；回收站页面 |
| 🔗 联调 | 创建笔记 → 社区页可见 → 编辑修改 → 删除进回收站 → 恢复，全流程走通 |

**交付物**：笔记完整生命周期可用

---

## Day 2 — 互动模块：后端 + 小程序 + 联调

| 阶段 | 任务 |
|---|---|
| 🔧 后端 | 点赞/收藏切换 `POST /api/wx/interactions`、互动状态查询（单条 GET + 批量 POST） |
| 📱 小程序 | 社区页/详情页点赞按钮对接；详情页收藏按钮对接；点赞/收藏数实时更新；个人中心-收藏列表对接 `/favorites`；个人中心-点赞列表对接 `/liked` |
| 🔗 联调 | 点赞→计数+1→再点取消→计数-1；收藏→个人中心可见→取消收藏→消失 |

**交付物**：互动功能完整闭环

---

## Day 3 — 评论模块：后端 + 小程序 + 联调

| 阶段 | 任务 |
|---|---|
| 🔧 后端 | 评论列表 `GET /api/wx/comments`、发表评论 `POST /api/wx/comments`、删除评论 `DELETE /api/wx/comments/{id}` |
| 📱 小程序 | 笔记详情页-评论列表加载；发表评论输入框；删除自己的评论；评论数实时更新 |
| 🔗 联调 | 发评论→列表刷新可见→删除→消失；详情页评论数与列表一致 |

**交付物**：评论社交功能完成

---

## Day 4 — 管理端笔记管理：后端 + Web + 联调

| 阶段 | 任务 |
|---|---|
| 🔧 后端 | 管理端笔记列表 `GET /api/admin/notes/list`、笔记审核 `PUT /api/admin/notes/{id}/audit`、管理端分类 CRUD `/api/admin/categories` |
| 🖥️ Web | 笔记管理页面（ProTable 列表、状态/分类筛选）；审核操作（通过/下架）；笔记详情查看弹窗；分类管理页面（树形表格、增删改排序启禁用） |
| 🔗 联调 | 小程序发布公开笔记 → Web 端可见 → 审核下架 → 小程序端不可见 |

**交付物**：管理端可管理笔记和分类

---

## Day 5 — 管理端仪表盘 + 日志：后端 + Web + 联调

| 阶段 | 任务 |
|---|---|
| 🔧 后端 | 仪表盘统计 `GET /api/admin/dashboard/stats`（用户数/笔记数/今日新增/增长趋势）、行为日志上报 `POST /api/wx/log/behavior`、操作日志查询 `GET /api/admin/log/operation/list`、行为日志查询 `GET /api/admin/log/behavior/list` |
| 🖥️ Web | 首页仪表盘（替换欢迎图片，ECharts 图表展示统计数据）；操作日志页面（ProTable + 筛选）；行为日志页面 |
| 📱 小程序 | 行为日志上报（浏览笔记时自动上报 view、搜索时上报 search） |
| 🔗 联调 | 小程序浏览笔记 → Web 日志页可见行为记录；Web 仪表盘数据与实际一致 |

**交付物**：管理端首页有数据，日志可追溯

---

## Day 6 — 小程序体验优化 + 个人中心完善

| 阶段 | 任务 |
|---|---|
| 🔧 后端 | 个人中心统计接口完善 `/api/wx/user/stats`（确保笔记数/获赞数/收藏数准确） |
| 📱 小程序 | 个人中心统计数据对接；我的笔记列表（编辑/删除操作）；登录态检查（Token 过期跳转登录页）；下拉刷新/上拉加载统一处理；空状态/加载态/骨架屏；页面切换动画优化 |
| 🔗 联调 | 未登录→跳登录→登录后返回；各列表页刷新/加载更多正常 |

**交付物**：小程序体验流畅，基础交互完善

---

## Day 7 — Web 个人中心完善 + 权限细化

| 阶段 | 任务 |
|---|---|
| 🔧 后端 | 安全信息更新接口验证加强；按钮权限接口按角色差异化返回 |
| 🖥️ Web | 个人中心-修改密码对接；个人中心-修改手机号对接；按钮权限控制（不同角色看到不同操作按钮）；菜单权限动态渲染验证 |
| 🔗 联调 | 不同角色登录 → 看到不同菜单和按钮；修改密码后重新登录验证 |

**交付物**：权限体系完整可用

---

## Day 8 — AI 摘要功能：后端 + 小程序 + 联调

| 阶段 | 任务 |
|---|---|
| 🔧 后端 | 集成 LangChain4j，实现 `POST /api/wx/notes/ai/summary`；摘要存入 `note_ai_summary` 表；笔记详情接口返回 AI 摘要字段 |
| 📱 小程序 | 笔记详情页展示 AI 摘要卡片（摘要文本 + 关键词标签）；手动触发生成摘要按钮 |
| 🔗 联调 | 打开笔记详情 → 展示已有摘要 / 点击生成 → 摘要展示正常 |

**交付物**：AI 亮点功能完成（⚠️ 如 LangChain4j 集成困难，先 mock 接口保进度）

---

## Day 9 — 全量联调 + Bug 修复

| 阶段 | 任务 |
|---|---|
| 🔗 后端联调 | 所有接口参数校验、异常处理、边界情况复查 |
| 🔗 小程序联调 | 全部页面流程走通：登录→社区→详情→互动→评论→发布→个人中心→编辑→删除 |
| 🔗 Web 联调 | 全部页面流程走通：登录→仪表盘→用户管理→角色管理→笔记管理→分类管理→字典管理→日志→个人中心 |
| 🐛 Bug 修复 | 联调发现的所有问题集中修复 |
| 🔒 安全检查 | SQL 注入防护、XSS 过滤、接口权限验证、Token 过期处理 |

**交付物**：三端全量功能联调通过

---

## Day 10 — 优化 + 部署 + 收尾

| 阶段 | 任务 |
|---|---|
| ⚡ 性能优化 | 慢 SQL 优化、N+1 排查、索引检查；前端资源压缩、图片懒加载 |
| 📱 小程序优化 | 分包加载、启动速度优化、长列表虚拟滚动 |
| 🚀 部署验证 | 后端 jar 包启动、Web 前端 build 部署、小程序真机预览 |
| 📝 文档更新 | README 更新、开发日志补全、API 文档与代码一致性检查 |
| ✅ 最终验收 | 按需求文档逐项确认功能完整性 |

**交付物**：项目完整可交付

---

## 每日闭环目标

| 天数 | 核心节奏 | 当日闭环目标 |
|---|---|---|
| Day 1 | 后端笔记CRUD → 小程序对接 → 联调 | 笔记能创建、能看、能编辑、能删除 |
| Day 2 | 后端互动 → 小程序对接 → 联调 | 点赞收藏可用，个人中心列表有数据 |
| Day 3 | 后端评论 → 小程序对接 → 联调 | 评论能发能看能删 |
| Day 4 | 后端管理笔记/分类 → Web对接 → 联调 | Web端能管理笔记和分类 |
| Day 5 | 后端仪表盘/日志 → Web+小程序对接 → 联调 | 仪表盘有图表，日志可追溯 |
| Day 6 | 小程序体验优化 + 个人中心完善 | 小程序交互流畅 |
| Day 7 | Web 个人中心 + 权限细化 → 联调 | 权限体系完整 |
| Day 8 | 后端AI → 小程序对接 → 联调 | AI摘要可生成可展示 |
| Day 9 | 三端全量联调 + Bug修复 | 所有功能走通 |
| Day 10 | 优化 + 部署 + 收尾 | 项目可交付 |

> 💡 **核心优势**：每天结束都有一个可演示的功能闭环，而不是堆了一堆接口却看不到效果。发现问题也能当天修复，不会积压到最后。





# 计划调整，day1-6和上面相同，day7开始按下面执行

## 📅 Day 7 — Redis 环境搭建 + JWT 安全优化 + SecurityConfig 修复

<div align="center">

|    阶段    |                          任务                           | 详情                                                         |
| :--------: | :-----------------------------------------------------: | :----------------------------------------------------------- |
|  🐳 Docker  |                   Redis 7.2 容器部署                    | 编写 docker-compose.yml，redis:7.2-alpine + 持久化(AOF) + 密码 + 端口映射；验证 redis-cli ping |
|   🔧 后端   |                 Spring Boot Redis 集成                  | ① 父 pom 添加 spring-boot-starter-data-redis + commons-pool2 依赖管理；② application-dev.yml 添加 spring.data.redis 配置(host/port/password)；③ RedisConfig（Key 用 StringRedisSerializer，Value 用 GenericJackson2JsonRedisSerializer）；④ RedisService 工具类（get/set/delete/incr/setNx/expire/hasKey） |
|   🔧 后端   |                      JWT 安全优化                       | ① 密钥改为 64 字符随机串，放 application-dev.yml；② JwtUtils 改为 Spring Bean 注入方式（去掉 @PostConstruct 静态变量 hack）；③ 退出时 Token 写入 Redis 黑名单 token:blacklist:{jti} TTL=剩余有效期；④ JwtAuthenticationFilter 每次校验时检查黑名单 |
|   🔧 后端   |               SecurityConfig 放行路径补全               | 放行 Knife4j：/doc.html、/webjars/**、/v3/api-docs/**、/swagger-resources/**、/favicon.ico；放行行为日志：/api/wx/log/behavior；放行微信回调：/api/wx/callback/** |
| **交付物** | Redis 可用，退出登录 Token 立即失效，Knife4j 文档可访问 |                                                              |

## 📅 Day 8 — Redis 应用实战 + SQL 性能优化

<div align="center">

|    阶段    |                          任务                          | 详情                                                         |
| :--------: | :----------------------------------------------------: | :----------------------------------------------------------- |
|   🔧 后端   |                   浏览量 Redis 优化                    | ① getNoteDetail 时 RedisService.incr("note:views:{id}") 替代直接 UPDATE；② @Scheduled(fixedRate=300000) 定时任务批量同步 Redis 计数到 DB（Lua 脚本 GET+DEL 原子操作）；③ 异常回退：Redis 不可用时走原 DB 更新（try-catch 降级） |
|   🔧 后端   |                    上传限流改 Redis                    | RedisService.incr("upload:limit:{ip}", 1, 3600) 原子操作 + 自动过期，替换 ConcurrentHashMap |
|   🔧 后端   |                       幂等性保护                       | 点赞/收藏 toggle() 前用 RedisService.setNx("interaction:lock:{userId}:{noteId}:{type}", "1", 3s) 防并发重复，操作完成后删除锁 |
|   🔧 后端   |                     SQL 子查询优化                     | ① note 表增加 like_count INT DEFAULT 0、comment_count INT DEFAULT 0、summary VARCHAR(500) 冗余字段；② 点赞/取消点赞时同步更新 like_count；③ 评论增删时同步更新 comment_count；④ 笔记创建/更新时截取前 200 字符写入 summary；⑤ NoteMapper.xml 列表查询去掉子查询，直接读冗余字段 |
|   🔧 后端   |                       数据库索引                       | SQL 脚本添加：note(is_public, status, del_flag, create_time)、note_comment(note_id, del_flag, create_time)、note_reaction(note_id, user_id) UNIQUE、sys_log_operation(user_id, create_time) |
| **交付物** | 浏览量高并发安全，SQL 查询性能大幅提升，数据库索引完善 |                                                              |



## 📅 Day 9 — 后端代码质量提升

<div align="center">

|    阶段    |                        任务                         | 详情                                                         |
| :--------: | :-------------------------------------------------: | :----------------------------------------------------------- |
|   🔧 后端   |                  全局异常处理完善                   | ① 引入 @Slf4j 替换 e.printStackTrace()；② 新增 MethodArgumentNotValidException（@Valid 校验失败返回 400）；③ 新增 ConstraintViolationException（路径参数校验失败返回 400）；④ 新增 AccessDeniedException（权限不足 403）；⑤ 新增 AuthenticationException（未认证 401）；⑥ ServiceException 使用 code 字段，Result.build(e.getCode(), e.getMessage(), null) |
|   🔧 后端   |                 Controller 参数校验                 | ① 创建 DTO 替代 Map：InteractionBatchDTO(noteIds)、NoteAuditDTO(status, reason)；② Controller 方法参数加 @Valid + @RequestBody @Valid DTO；③ DTO 字段加 @NotNull、@NotEmpty、@Size 等注解 |
|   🔧 后端   |                    CORS 配置收紧                    | addAllowedOriginPattern("*") → 从配置文件读取 cors.allowed-origins，dev 环境配 localhost:5173，prod 配实际域名 |
|   🔧 后端   |                 common-log 模块整理                 | ① LogModule、LogAction 枚举从 common 移到 common-log；② common 模块不再依赖日志相关枚举；③ 确保模块依赖方向正确：common-log → common（单向） |
| **交付物** | 异常处理规范、参数校验完善、CORS 安全、模块结构清晰 |                                                              |



## 📅 Day 10 — 微信内容审核接入（文本 + access_token 管理）

<div align="center">

|    阶段    |                         任务                          | 详情                                                         |
| :--------: | :---------------------------------------------------: | :----------------------------------------------------------- |
|   🔧 后端   |                   access_token 管理                   | ① `WechatApiUtils `增加` getAccessToken() `方法，调用` https://api.weixin.qq.com/cgi-bin/token`；② `access_token` 缓存到 Redis（key=`wx:access_token`，TTL=7000s，比 7200s 提前过期防边界）；③ 调用微信 API 前先从 Redis 取，没有则重新获取并缓存 |
|   🔧 后端   |                   文本内容安全检测                    | ①` WechatApiUtils `增加 `msgSecCheck(openid, content, scene, title) `方法；② 调用 `POST /wxa/msg_sec_check?access_token=xxx`；③ 解析返回结果：`suggest=pass `通过、`suggest=risky `拦截、`suggest=review `人工审核；④ 封装` ContentCheckResult` 返回对象 |
|   🔧 后端   |                   业务接入文本审核                    | ① 笔记创建/更新时审核 title + content（scene=3 论坛）；② 评论发布时审核 content（scene=2 评论）；③ 用户昵称修改时审核 nickname（scene=1 资料）；④ 审核不通过抛出 `ServiceException("内容包含违规信息，请修改后重新发布")` |
|  📱 小程序  |                     审核提示对接                      | ① 笔记发布/评论提交时，后端返回审核失败信息，前端展示具体提示；② 添加"内容审核中"状态提示（预留，非必须） |
| **交付物** | 文本内容自动审核，违规内容拦截，access_token 自动管理 |                                                              |



## 📅 Day 11 — 微信内容审核接入（图片审核）+ Web 个人中心

<div align="center">

|    阶段    |              任务              | 详情                                                         |
| :--------: | :----------------------------: | :----------------------------------------------------------- |
|   🔧 后端   |        图片内容安全检测        | ① WechatApiUtils 增加 mediaCheckAsync(openid, mediaUrl, mediaType, scene) 方法；② 调用 POST /wxa/media_check_async?access_token=xxx；③ 异步检测，返回 trace_id |
|   🔧 后端   |        图片审核回调接口        | ① 新增 WxCallbackController，路径 /api/wx/callback/media-check（需在 SecurityConfig 放行）；② 接收微信推送的审核结果（XML/JSON 格式）；③ 解析 trace_id + isrisky/result.suggest；④ 审核不通过时：更新笔记状态为"审核不通过"或标记头像违规 |
|   🔧 后端   |        图片审核状态管理        | ① note 表增加 audit_status TINYINT DEFAULT 0（0 未审核/1 通过/2 不通过/3 审核中）；② 图片上传后先标记"审核中"，回调后更新状态；③ 公开列表查询增加 audit_status=1 条件 |
|   🖥️ Web    |     个人中心-修改密码对接      | 对接 updateSecurityApi，type=1 密码修改                      |
|   🖥️ Web    |    个人中心-修改手机号对接     | 对接 updateSecurityApi，type=2 手机号修改                    |
| **交付物** | 图片审核闭环，Web 个人中心可用 |                                                              |



## 📅 Day 12 — Web 权限细化 + 前端优化

<div align="center">

|    阶段    |                         任务                          | 详情                                                         |
| :--------: | :---------------------------------------------------: | :----------------------------------------------------------- |
|   🖥️ Web    |                     按钮权限控制                      | 不同角色看到不同操作按钮（ProTable tableHeader 插槽 + v-auth 指令） |
|   🖥️ Web    |                 菜单权限动态渲染验证                  | 不同角色登录看到不同菜单                                     |
|   🖥️ Web    |                 Geeker-Admin 冗余清理                 | 删除 src/views 下未使用的示例页面（proTable 示例、detail 示例等），减少打包体积 |
|   🖥️ Web    |                      首页重定向                       | 首页 /home/index 自动重定向到工作台 /dashboard/index         |
|   🖥️ Web    |                     API 路径检查                      | 排查所有 API 路径是否有双斜杠问题，统一修正                  |
|   🔗 联调   | 不同角色登录 → 不同菜单和按钮；修改密码后重新登录验证 |                                                              |
| **交付物** |           权限体系完整可用，Web 端代码整洁            |                                                              |



## 📅 Day 13 — AI 摘要功能

<div align="center">

|    阶段    |                         任务                          | 详情                                                         |
| :--------: | :---------------------------------------------------: | :----------------------------------------------------------- |
|   🔧 后端   |                   LangChain4j 集成                    | 配置 AI 模型（DeepSeek），实现摘要生成服务                   |
|   🔧 后端   |                      AI 摘要接口                      | POST /api/wx/notes/ai/summary，摘要存入 note_ai_summary 表；笔记详情接口返回 AI 摘要字段 |
|  📱 小程序  |                      AI 摘要卡片                      | 笔记详情页展示 AI 摘要卡片（摘要文本 + 关键词标签）；手动触发生成按钮 |
|   🔗 联调   | 打开笔记详情 → 展示已有摘要 / 点击生成 → 摘要展示正常 |                                                              |
| **交付物** |   AI 亮点功能完成（⚠️ 如集成困难先 mock 接口保进度）   |                                                              |



## 📅 Day 14 — 小程序优化

<div align="center">

|    阶段    |           任务           | 详情                                                         |
| :--------: | :----------------------: | :----------------------------------------------------------- |
|  📱 小程序  |   request.js 401 去重    | 提取 handleUnauthorized() 公共函数，HTTP 200 code===401 和 HTTP 401 共用 |
|  📱 小程序  |       环境配置优化       | config.js 用条件编译 #ifdef / process.env.NODE_ENV 区分 dev/prod 环境 baseURL |
|  📱 小程序  |   MarkdownIt 全局复用    | 提取到 utils/markdown.js 单例，详情页 import 复用            |
|  📱 小程序  |     内容审核提示优化     | 发布/评论时审核失败的友好提示；头像审核不通过的引导          |
|  📱 小程序  |       分包加载优化       | 配置 subPackages，将低频页面（回收站、标签管理）放入分包     |
| **交付物** | 小程序性能优化，代码整洁 |                                                              |



## 📅 Day 15 — 全量联调 + Bug 修复

<div align="center">

|    阶段    |                             任务                             | 详情                                                         |
| :--------: | :----------------------------------------------------------: | :----------------------------------------------------------- |
|   🔗 后端   |           所有接口参数校验、异常处理、边界情况复查           | 重点验证 Redis 相关功能（黑名单、浏览量同步、限流、幂等）    |
|  🔗 小程序  |                          全流程走通                          | 登录→社区→详情→互动→评论→发布（含审核）→个人中心→编辑→删除→AI摘要 |
|   🔗 Web    |                          全流程走通                          | 登录→仪表盘→用户管理→角色管理→笔记管理（含审核状态）→分类管理→字典管理→日志→个人中心 |
|   🐛 Bug    |                  联调发现的所有问题集中修复                  | —                                                            |
|   🔒 安全   | SQL 注入防护、XSS 过滤、接口权限验证、Token 过期处理、内容审核覆盖验证 | —                                                            |
| **交付物** |                     三端全量功能联调通过                     |                                                              |



## 📅 Day 16 — 优化 + 部署 + 收尾

<div align="center">

|    阶段    |                             任务                             | 详情 |
| :--------: | :----------------------------------------------------------: | :--- |
|   ⚡ 性能   |         慢 SQL 优化、N+1 排查、Redis 缓存命中率检查          | —    |
|   ⚡ 前端   |                     资源压缩、图片懒加载                     | —    |
|   🚀 部署   | Docker Compose 编排（MySQL + Redis + Spring Boot JAR）；Web 前端 build + Nginx 配置；小程序真机预览 | —    |
|   📝 文档   | README 更新（含 Docker 启动说明）、API 文档与代码一致性检查  | —    |
|   ✅ 验收   |                 按需求文档逐项确认功能完整性                 | —    |
| **交付物** |                        项目完整可交付                        |      |



## 📊 新旧计划对比

<div align="center">

|      对比项       | 原计划 Day 7-10 |                 新计划 Day 7-16                 |
| :---------------: | :-------------: | :---------------------------------------------: |
|      总天数       |      4 天       |                      10 天                      |
|       Redis       |      ❌ 无       | ✅ 完整集成（黑名单/浏览量/限流/幂等/token缓存） |
|     JWT 安全      |    ❌ 未修复     |          ✅ 黑名单 + 密钥加固 + Bean 化          |
|     SQL 性能      |    ❌ 子查询     |                ✅ 冗余字段 + 索引                |
|     异常处理      |    ❌ 不完善     |         ✅ 完整体系（5 种异常 + @Slf4j）         |
|     参数校验      |   ❌ Map 接收    |                 ✅ DTO + @Valid                  |
|      幂等性       |    ❌ 无保护     |                  ✅ Redis SETNX                  |
|    浏览量并发     |  ❌ 直接 UPDATE  |             ✅ Redis INCR + 定时同步             |
|       CORS        |   ❌ 过于宽松    |                 ✅ 配置文件控制                  |
|     模块结构      |   ❌ 日志散落    |                   ✅ 整理清晰                    |
|   微信内容审核    |      ❌ 无       |               ✅ 文本+图片审核闭环               |
| access_token 管理 |      ❌ 无       |             ✅ Redis 缓存 + 自动刷新             |
|   图片审核回调    |      ❌ 无       |              ✅ 回调接口 + 状态管理              |
|     Web 权限      |      简单       |               ✅ 完整按钮+菜单权限               |
|    小程序优化     |      基础       |         ✅ 分包+MarkdownIt复用+环境配置          |



## 📊 每日闭环目标

<div align="center">

|  天数  | 核心节奏                                   | 当日闭环目标                            |
| :----: | :----------------------------------------- | :-------------------------------------- |
| Day 7  | Docker Redis → JWT 黑名单 → SecurityConfig | 退出登录 Token 立即失效，Knife4j 可访问 |
| Day 8  | Redis 应用 → SQL 优化 → 索引               | 浏览量高并发安全，列表查询无子查询      |
| Day 9  | 异常处理 → 参数校验 → CORS → 模块整理      | 全局异常规范，DTO 校验生效              |
| Day 10 | access_token → 文本审核 → 业务接入         | 发布笔记/评论自动审核，违规内容拦截     |
| Day 11 | 图片审核 → 回调接口 → Web 个人中心         | 图片审核闭环，Web 可改密码/手机号       |
| Day 12 | Web 权限 → 冗余清理 → 首页重定向           | 权限体系完整，Web 代码整洁              |
| Day 13 | LangChain4j → AI 摘要 → 小程序卡片         | AI 摘要可生成可展示                     |
| Day 14 | 小程序优化（401/环境/MarkdownIt/分包）     | 小程序性能优化完成                      |
| Day 15 | 三端全量联调 + Bug 修复                    | 所有功能走通                            |
| Day 16 | 性能优化 + Docker 部署 + 收尾              | 项目可交付                              |



## ⚠️ 风险提示

<div align="center">

|                         风险                          | 影响                 | 应对                                                         |
| :---------------------------------------------------: | :------------------- | :----------------------------------------------------------- |
| 微信 media_check_async 回调需要公网可访问的 HTTPS URL | 本地开发无法接收回调 | 开发阶段用 ngrok/natapp 内网穿透；或先只做文本审核，图片审核标记"审核中"默认通过 |
|   微信内容审核需要近 2 小时内访问过小程序的 openid    | 开发环境测试受限     | 测试时确保先触发登录获取新 openid                            |
|   msg_sec_check v2 接口需要小程序已发布上线或体验版   | 开发版可能无法调用   | 在微信后台配置体验版，或开发阶段加开关跳过审核               |
|         LangChain4j + DeepSeek 集成可能不稳定         | AI 功能延期          | 预留 mock 方案，先保证其他功能不受影响                       |



