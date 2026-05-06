# 系统架构设计说明

> 本文档详细描述了“智能笔记系统”的整体技术架构、模块职责划分、核心业务流程以及项目代码的组织结构。



## 一、 整体技术架构

本项目采用**前后端分离架构**，基于明确的角色划分，分为三个独立运行的端：

* **服务端 (Back-end)**：基于 Spring Boot 3.x 构建的多模块 Maven 工程，提供统一的 RESTful API 服务。
* **管理端 (Web Front-end)**：基于 Vue 3 + Element Plus 构建，专供**系统管理员**进行全局运维与内容管理。
* **用户端 (Mobile)**：基于 uni-app + Vue 3 开发，编译为微信小程序，面向**普通终端用户**，提供知识记录与互动的核心体验。



---

## 二、 技术栈

| 维度 | 技术选型 | 说明 |
|:---|:---|:---|
| 后端框架 | Spring Boot 3.x | RESTful API |
| 持久层 | MyBatis-Plus | ORM + XML 自定义 SQL |
| 安全 | Spring Security + JWT + Redis | 认证鉴权 + Token 黑名单 |
| AI | LangChain4j + DeepSeek | 摘要/助手/标签推荐/数据分析 |
| 缓存 | Redis 7.2（Docker） | 浏览量计数、分布式锁、限流 |
| 数据库 | MySQL 8.0 | 22 张业务表 |
| 接口文档 | Knife4j（OpenAPI 3） | 接口调试 |
| Web 前端 | Vue 3 + Vite + Element Plus + ECharts | 管理端 |
| 小程序 | uni-app（Vue 3）+ uview-plus | 微信小程序 |
| 构建 | Java 17 / Node.js 16+ / Maven 3.x | 运行环境 |

---

## 三、 后端模块职责划分

为提升代码的复用性与可维护性，后端采用多模块拆分方案，遵循“高内聚、低耦合”原则：

* **`smart-note-admin` (启动模块)**：系统的入口，仅包含 `SmartNoteApplication` 类，负责 Spring Boot 容器启动与组件扫描。
* **`smart-note-framework` (核心配置层)**：全局基础设施配置，包含 Spring Security 权限校验、JWT 拦截过滤器、跨域处理以及 MyBatis 等中间件配置。
* **`smart-note-system` (系统管理模块)**：处理底层 RBAC 模型业务，如管理员登录认证、角色与菜单权限分配、系统行为审计等。
* **`smart-note-note` (核心业务模块)**：系统主体业务层，负责笔记的增删改查、分类/标签的多维度管理、评论/点赞互动逻辑及 AI 摘要的扩展接口。
* **`smart-note-common` (公共工具包)**：基础支撑模块，存放全局统一返回对象 (`Result`)、全局异常拦截器、分页组件及各类 Utils（如时间、加密、工具类）。
* **`smart-note-common-log` (日志模块)**：AOP 操作审计与行为日志的注解、枚举及切面实现，被 `system` 和 `note` 模块依赖。

---

## 四、 核心业务流程与数据流转

### 4.1 身份认证流程
1.  **用户登录**：客户端（小程序/Web）发起登录请求。
2.  **凭证校验**：后端通过 `user_auth` 表验证凭证（小程序校验 OpenID，Web 端校验账号密码）。
3.  **Token 签发**：验证成功后，服务端通过 `jjwt` 生成包含用户身份信息的 Token 并返回给客户端。
4.  **无状态鉴权**：客户端后续所有请求均需在 Header 中携带 Token，由 `framework` 模块中的过滤器拦截校验有效性并提取用户信息。

### 4.2 笔记存储与结构化逻辑
* **内容存储**：笔记核心内容采用 Markdown 格式，以 `LONGTEXT` 类型存储在 `note` 表的 `content` 字段中。
* **解耦设计**：笔记与标签（Tag）采用**多对多**关系，为保证查询性能与扩展性，通过中间表 `note_tag_rel` 建立映射关联，实现物理层面的解耦。

---

## 五、 数据库概览

共 22 张表，按业务模块划分：

| 模块 | 表名 | 说明 |
|:---|:---|:---|
| 用户与权限 | sys_user, user_auth, user_info | 用户基础信息 |
| | sys_role, sys_menu, sys_role_menu, sys_user_role | RBAC 权限 |
| 笔记业务 | note, note_tag, note_tag_rel, note_comment, note_reaction, note_attachment | 笔记核心业务 |
| | sys_category | 系统分类 |
| AI | note_ai_summary, ai_usage_log, ai_user_quota | AI 摘要与配额 |
| 消息 | user_message | 站内消息（8 种类型） |
| 日志 | sys_log_behavior, sys_log_operation | 行为 + 操作日志 |
| 数据字典 | sys_dict_type, sys_dict_data | 数据字典 |

> 字段详细设计参见：[data_dictionary.md](../../docs/sql/data_dictionary.md)

---

