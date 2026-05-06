# 项目剩余工作计划（重新编号）

> 基于当前项目完成情况，去掉微信内容审核和部署上线，重新梳理的开发计划。

---

## 📊 当前完成情况

### ✅ 已完成（Day 1-9）

| 原编号 | 任务 | 状态 |
|:--:|:---|:--:|
| Day 1 | 笔记 CRUD（后端 + 小程序 + 联调） | ✅ |
| Day 2 | 互动模块（点赞/收藏） | ✅ |
| Day 3 | 评论模块 + 社区页分类美化 | ✅ |
| Day 4 | 管理端笔记管理 + 分类管理 | ✅ |
| Day 5 | 仪表盘 + 日志系统 | ✅ |
| Day 6 | 小程序体验优化 | ✅ |
| Day 7 | Redis 环境搭建 + JWT 安全优化 | ✅ |
| Day 8 | Redis 应用实战 + SQL 性能优化 | ✅ |
| Day 9 | 后端代码质量提升 | ✅ |

### ❌ 遗漏项（菜单已配置但页面缺失）

| 菜单 ID | 菜单名称 | 组件路径 | 状态 |
|:--:|:---|:---|:--:|
| 4010 | 标签管理 | /category/tag/index | ❌ 页面缺失 |
| 5020 | AI 监控 | /monitor/aiLog/index | ❌ 页面缺失 |

### 🚫 不做的功能

| 功能 | 原因 |
|:---|:---|
| 微信内容审核（文本+图片） | 小程序有社交功能，个人开发者无法上架，审核接口无意义 |
| Docker 部署上线 | 不考虑上线 |

---

## 📝 新计划（从 Day 1 开始编号）

### Day 1 — 遗漏页面补充

| 阶段 | 任务 | 详情 |
|:--:|:---|:---|
| 🔧 后端 | 标签管理接口 | 新增 AdminTagController，管理端查看所有用户标签（分页、搜索），删除违规标签。注意：note_tag 表无 status 字段，不支持启禁用 |
| 🖥️ Web | 标签管理页面 | 新建 category/tag/index.vue，ProTable 列表（展示用户名+标签名+使用次数）+ 删除操作 |
| 🖥️ Web | AI 监控页面 | 新建 monitor/aiLog/index.vue，展示 AI 摘要生成记录（预留，Day 4 实现后端） |

**注意：** Web 个人中心（修改密码/手机号）已完成，无需开发。

**交付物：** 所有菜单页面齐全

---

### Day 2 — 消息系统重构（后端 + 小程序 + Web）

**⚠️ 方案：复用 user_message 表扩展**

现有代码基础：
- user_message 表：已有 receiver_id, sender_id, note_id, comment_id, type(1评论/2回复), content, is_read, create_time
- MessageServiceImpl：已有未读数、列表、标记已读、删除、发送逻辑
- WxMessageController：已有 /api/wx/messages 四个接口
- message.vue：已有完整消息列表页

**type 扩展方案：**

| type | 含义 | 来源 |
|:--:|:---|:---|
| 1 | 评论 | 已有 |
| 2 | 回复 | 已有 |
| 3 | 审核通过 | 新增 |
| 4 | 审核不通过 | 新增 |
| 5 | 违规下架 | 新增 |
| 6 | 系统公告 | 新增 |
| 7 | 点赞 | 新增 |
| 8 | 收藏 | 新增 |

**设计决策：**
- 点赞/收藏采用方案 A：取消后不删消息，历史记录保留
- 自己点赞/收藏自己的笔记不发消息（与评论逻辑一致）

| 阶段 | 任务 | 详情 |
|:--:|:---|:---|
| 🔧 后端 | 扩展 user_message 表 | ALTER TABLE：① ADD COLUMN title VARCHAR(100) COMMENT '消息标题（系统通知用）'；② MODIFY COLUMN note_id BIGINT DEFAULT NULL COMMENT '关联笔记ID（系统公告可为空）'；③ type 扩展为 1-8 |
| 🔧 后端 | 改造 MessageServiceImpl | 查询时支持按 type 范围过滤：互动消息(1,2,7,8)、系统通知(3,4,5,6)；未读数一次请求返回分类统计 |
| 🔧 后端 | 改造 WxMessageController | GET /api/wx/messages 支持 group=interaction/notice 参数过滤；GET /api/wx/messages/unread-count 返回 {interactionCount, noticeCount, totalCount} |
| 🔧 后端 | 点赞/收藏写入消息 | InteractionServiceImpl 注入 MessageService，点赞/收藏时写入 user_message（type=7/8）。注意：自己对自己不发消息，与评论逻辑一致 |
| 🔧 后端 | 管理端通知接口 | 新增 POST /api/admin/notifications/send（发送系统公告：type=6，可选全部用户/指定用户） |
| 📱 小程序 | message.vue 改造 | 在现有页面基础上，顶部添加两个 Tab：左"互动消息"（调 /api/wx/messages?group=interaction）、右"系统通知"（调 /api/wx/messages?group=notice） |
| 📱 小程序 | 互动消息 | 展示评论/回复/点赞/收藏通知 |
| 📱 小程序 | 系统通知 | 展示审核结果、违规通知、系统公告，点击跳转相关笔记（系统公告 noteId 为空时不跳转） |
| 📱 小程序 | 未读数 | TabBar 用 totalCount，Tab 标题旁显示各自未读数 |
| 🖥️ Web | 添加菜单配置 | init_sys_data.sql 添加：menu_id=5030，通知管理，路径 /monitor/notification |
| 🖥️ Web | 通知管理页面 | 新建 monitor/notification/index.vue，ProTable 展示已发送的系统通知 |
| 🖥️ Web | 发送系统公告 | 新建发送弹窗：输入标题+内容+接收范围（全部用户/指定用户），调用 POST /api/admin/notifications/send |

**init_sys_data.sql 新增菜单：**
```sql
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(5030, 5000, 'notification', '/monitor/notification', '/monitor/notification/index', 'C', '通知管理', 'Bell', 'sys:notification:send', 3);
```

**消息页面 UI 设计（小程序改造）：**
```
┌─────────────────────────────────┐
│  消息                    全部已读 │
├────────────────┬────────────────┤
│  互动消息(5)   │   系统通知(2)   │
├────────────────┴────────────────┤
│                                 │
│  互动消息 Tab：                  │
│  ┌───────────────────────────┐ │
│  │ [头像] 张三 评论了你的笔记  │ │
│  │       「Spring Boot 笔记」 │ │
│  │       写得真好！           │ │
│  ├───────────────────────────┤ │
│  │ [头像] 李四 点赞了你的笔记  │ │
│  │       「Vue3 入门指南」     │ │
│  ├───────────────────────────┤ │
│  │ [头像] 王五 收藏了你的笔记  │ │
│  │       「Java 面试题」      │ │
│  └───────────────────────────┘ │
│                                 │
│  系统通知 Tab：                  │
│  ┌───────────────────────────┐ │
│  │ 🔔 你的笔记「xxx」已通过审核 │ │
│  │    2024-01-15 10:30       │ │
│  ├───────────────────────────┤ │
│  │ ⚠️ 你的笔记「xxx」违规被下架 │ │
│  │    原因：包含敏感内容       │ │
│  ├───────────────────────────┤ │
│  │ 📢 系统公告：新功能上线     │ │
│  │    AI 摘要功能已开放...     │ │
│  └───────────────────────────┘ │
└─────────────────────────────────┘
```

**交付物：** 消息系统完善，Web 端可发送系统公告

---

### Day 3 — 权限细化 + 笔记审核 + Web 代码清理

| 阶段 | 任务 | 详情 |
|:--:|:---|:---|
| 🔧 后端 | 笔记审核标记 | note 表新增 `reviewed` 字段（0未审核/1已审核），用户发布笔记时默认 reviewed=0 |
| 🔧 后端 | 审核筛选接口 | AdminNoteQueryDTO 新增 `reviewed` 参数，管理员可筛选未审核笔记 |
| 🔧 后端 | 标记已审核接口 | 新增 `PUT /api/admin/notes/{id}/review`，将 reviewed 设为 1 |
| 🔧 后端 | 按钮权限分组 | SysMenu.java 新增 parentName 字段、UserAuthMapper.xml 返回 p.name、AdminAuthServiceImpl 按路由名分组 |
| 🔧 后端 | 按钮权限 SQL 补全 | 角色管理（2021~2024）、标签管理（4011）、通知管理（5031）、笔记审核（3013） |
| 🖥️ Web | 笔记管理审核筛选 | 笔记管理页新增"审核"列 + 筛选项，调 reviewed=0 过滤 |
| 🖥️ Web | 一键审核按钮 | 笔记列表操作列新增"标记已审核"按钮 |
| 🖥️ Web | 按钮权限控制 | 不同角色看到不同操作按钮（v-auth 指令），权限标识清单见下表 |
| 🖥️ Web | 菜单权限验证 | 不同角色登录看到不同菜单 |
| 🖥️ Web | 首页重定向 | 修改 HOME_URL 从 /home/index 改为 /dashboard/index，删除 views/home/index.vue |
| 🖥️ Web | Geeker-Admin 冗余清理 | 删除 src/views 下未使用的示例页面（assembly、directives、echarts、form、link、menu、proTable 示例等） |
| 🖥️ Web | API 路径检查 | 排查所有 API 路径是否有双斜杠问题 |

**笔记审核工作流（先发后审）：**

```
用户发布笔记 → reviewed=0（未审核）→ 社区可见
                                        ↓
管理员打开笔记管理 → 筛选 reviewed=0 → 看到所有未审核文章
                                        ↓
                                逐条审核：没问题 → reviewed=1（标记已审核）
                                          有问题 → status=3（下架）+ reviewed=1
```

**数据字典补充（init_sys_data.sql）：**

```sql
-- 字典类型
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark) VALUES
(10, '笔记审核状态', 'note_review', 1, 'note.reviewed');

-- 字典数据
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, tag_type, sort_order) VALUES
('note_review', '未审核', '0', 'warning', 1),
('note_review', '已审核', '1', 'success', 2);
```

**v-auth 权限标识清单：**

| 页面 | 按钮 | 权限标识 |
|:---|:---|:---|
| 用户管理 | 新增/编辑/删除 | sys:user:add / sys:user:edit / sys:user:delete |
| 角色管理 | 新增/编辑/删除/分配权限 | sys:role:add / sys:role:edit / sys:role:delete / sys:role:assign |
| 笔记管理 | 上架/下架/删除/标记已审核 | note:audit / note:delete / note:review |
| 分类管理 | 新增/编辑/删除 | category:add / category:edit / category:delete |
| 标签管理 | 删除 | note:tag:del |
| 通知管理 | 发送通知 | sys:notification:send |

**注意：** 需确认后端 getAuthButtonList 接口已按页面返回这些权限标识。

**交付物：** 笔记审核体系完整，权限体系完整，Web 代码整洁

---

### Day 4 — AI 摘要功能

| 阶段 | 任务 | 详情 |
|:--:|:---|:---|
| 🔧 后端 | LangChain4j 集成 | 配置 AI 模型（DeepSeek），实现摘要生成服务 |
| 🔧 后端 | AI 摘要接口 | POST /api/wx/notes/ai/summary，摘要存入 note_ai_summary 表 |
| 🖥️ Web | AI 监控数据 | monitor/aiLog/index.vue 对接后端接口，展示摘要生成记录 |
| 📱 小程序 | AI 摘要卡片 | 笔记详情页展示摘要文本 + 关键词标签，手动触发生成按钮 |
| 🔗 联调 | 验证 | 打开笔记详情 → 展示已有摘要 / 点击生成 → 摘要展示正常 |

**交付物：** AI 亮点功能完成（⚠️ 如集成困难先 mock 接口保进度）

---

### Day 5 — 小程序优化

| 阶段 | 任务 | 详情 |
|:--:|:---|:---|
| 📱 小程序 | request.js 401 去重 | 提取 handleUnauthorized() 公共函数 |
| 📱 小程序 | 环境配置优化 | config.js 区分 dev/prod 环境 baseURL |
| 📱 小程序 | MarkdownIt 全局复用 | 提取到 utils/markdown.js 单例 |
| 📱 小程序 | 分包加载优化 | 低频页面（回收站、标签管理）放入分包 |

**交付物：** 小程序性能优化，代码整洁

---

### Day 6 — AI 笔记助手 + 标签推荐

| 阶段 | 任务 | 详情 |
|:--:|:---|:---|
| 🔧 后端 | AI 笔记助手接口 | POST /api/wx/ai/assist（扩写/润色/总结），复用 AiQuotaService 配额校验 |
| 🔧 后端 | 标签推荐接口 | POST /api/wx/ai/recommend-tags，LLM 从已有标签中匹配推荐 |
| 📱 小程序 | 笔记助手 UI | create.vue 编辑区加"AI 助手"按钮组（扩写/润色/总结） |
| 📱 小程序 | 标签推荐 UI | create.vue 底部加"推荐标签"按钮，展示推荐结果一键添加 |

**技术方案：**
- 复用已有 LangChain4j + DeepSeek 基础设施
- 复用 AiQuotaService 配额校验 + AiUsageLog 日志记录
- prompt 模板：扩写="请将以下内容扩写到200字"，润色="请润色以下文字"，总结="请用50字总结"
- 标签推荐：把用户已有标签列表 + 笔记内容喂给 LLM，返回匹配的标签名数组

**交付物：** AI 辅助创作功能完成

---

### Day 7 — 管理端数据分析问答

| 阶段 | 任务 | 详情 |
|:--:|:---|:---|
| 🔧 后端 | 数据分析接口 | POST /api/admin/ai/analyze，自然语言→SQL→执行→LLM 总结 |
| 🔧 后端 | SQL 安全校验 | 正则拦截非 SELECT 语句，限制行数+超时 |
| 🖥️ Web | 分析问答 UI | 仪表盘页加"数据分析"聊天卡片（输入框+结果区） |

**技术方案：**
- @PostConstruct 加载所有表结构 DDL 到 prompt 模板
- LLM 生成 SQL → 正则校验（只允许 SELECT）→ JdbcTemplate 执行 → LLM 总结结果
- 安全限制：只读、最多 100 行、3 秒超时、禁止 DELETE/UPDATE/DROP
- 结果展示：AI 文字回答 + 数据表格（如有结构化数据）

**交付物：** 管理端数据问答功能完成

---

### Day 8 — 全量联调 + Bug 修复

| 阶段 | 任务 | 详情 |
|:--:|:---|:---|
| 🔗 小程序 | 全流程走通 | 登录→社区→详情→互动→评论→发布→AI助手→标签推荐→消息 |
| 🔗 Web | 全流程走通 | 登录→仪表盘→数据问答→用户管理→角色管理→笔记管理→分类管理→标签管理→日志→通知管理 |
| 🐛 Bug | 集中修复 | 联调发现的所有问题 |
| 🔒 安全 | 安全检查 | SQL 注入防护、XSS 过滤、接口权限验证 |

**交付物：** 三端全量功能联调通过

---

### Day 9 — 优化 + 收尾

| 阶段 | 任务 | 详情 |
|:--:|:---|:---|
| ⚡ 性能 | 慢 SQL 优化 | N+1 排查、Redis 缓存命中率检查 |
| ⚡ 前端 | 资源压缩 | 图片懒加载、代码分割 |
| 📝 文档 | 文档更新 | README 更新、API 文档一致性检查 |
| ✅ 验收 | 功能验收 | 按需求文档逐项确认功能完整性 |

**交付物：** 项目完整可交付

---

## 📊 每日闭环目标

| 天数 | 核心节奏 | 当日闭环目标 |
|:--:|:---|:---|
| Day 1 | 遗漏页面补充 | 所有菜单页面齐全 |
| Day 2 | 消息系统重构 | 互动消息+系统通知双 Tab，Web 可发公告 |
| Day 3 | 权限细化 + 笔记审核 + 代码清理 | 审核体系完整，权限体系完整，Web 代码整洁 |
| Day 4 | AI 摘要 + 用量监控 | AI 摘要可生成，token 追踪+配额管控 |
| Day 5 | 小程序优化 + Web 修复 | 性能优化+头像上传修复 |
| Day 6 | AI 笔记助手 + 标签推荐 | AI 辅助创作功能完成 |
| Day 7 | 管理端数据分析问答 | 自然语言查数据功能完成 |
| Day 8 | 三端全量联调 + Bug 修复 | 所有功能走通 |
| Day 9 | 优化 + 收尾 | 项目可交付 |

---

## ⚠️ 风险提示

| 风险 | 影响 | 应对 |
|:--:|:---|:---|
| LangChain4j + DeepSeek 集成可能不稳定 | AI 功能延期 | 预留 mock 方案，先保证其他功能不受影响 |
| 消息系统改造影响现有功能 | 回归测试不充分 | 改造前备份，改造后全量测试消息流程 |
