# 系统架构设计说明

> 本文档详细描述了“智能笔记系统”的整体技术架构、模块职责划分、核心业务流程以及项目代码的组织结构。



## 一、 整体技术架构

本项目采用**前后端分离架构**，基于明确的角色划分，分为三个独立运行的端：

* **服务端 (Back-end)**：基于 Spring Boot 3.x 构建的多模块 Maven 工程，提供统一的 RESTful API 服务。
* **管理端 (Web Front-end)**：基于 Vue 3 + Element Plus 构建，专供**系统管理员**进行全局运维与内容管理。
* **用户端 (Mobile)**：基于原生微信小程序开发，面向**普通终端用户**，提供知识记录与互动的核心体验。



---

## 二、 后端模块职责划分

为提升代码的复用性与可维护性，后端采用多模块拆分方案，遵循“高内聚、低耦合”原则：

* **`smart-note-admin` (启动模块)**：系统的入口，仅包含 `SmartNoteApplication` 类，负责 Spring Boot 容器启动与组件扫描。
* **`smart-note-framework` (核心配置层)**：全局基础设施配置，包含 Spring Security 权限校验、JWT 拦截过滤器、跨域处理以及 MyBatis 等中间件配置。
* **`smart-note-system` (系统管理模块)**：处理底层 RBAC 模型业务，如管理员登录认证、角色与菜单权限分配、系统行为审计等。
* **`smart-note-note` (核心业务模块)**：系统主体业务层，负责笔记的增删改查、分类/标签的多维度管理、评论/点赞互动逻辑及 AI 摘要的扩展接口。
* **`smart-note-common` (公共工具包)**：基础支撑模块，存放全局统一返回对象 (`Result`)、全局异常拦截器、分页组件及各类 Utils（如时间、加密、工具类）。

---

## 三、 核心业务流程与数据流转

### 3.1 身份认证流程
1.  **用户登录**：客户端（小程序/Web）发起登录请求。
2.  **凭证校验**：后端通过 `user_auth` 表验证凭证（小程序校验 OpenID，Web 端校验账号密码）。
3.  **Token 签发**：验证成功后，服务端通过 `jjwt` 生成包含用户身份信息的 Token 并返回给客户端。
4.  **无状态鉴权**：客户端后续所有请求均需在 Header 中携带 Token，由 `framework` 模块中的过滤器拦截校验有效性并提取用户信息。

### 3.2 笔记存储与结构化逻辑
* **内容存储**：笔记核心内容采用 Markdown 格式，以 `LONGTEXT` 类型存储在 `note` 表的 `content` 字段中。
* **解耦设计**：笔记与标签（Tag）采用**多对多**关系，为保证查询性能与扩展性，通过中间表 `note_tag_rel` 建立映射关联，实现物理层面的解耦。

---

## 四、项目结构

### 1、后端项目结构

```
smart-note-system
│
├─ admin
│   └─ src/main/java
│       └─ com.littlewin.admin
│           └─ SmartNoteApplication.java
│              ↑ 系统唯一启动类
│              ↑ 负责组件扫描、Mapper 扫描
│              ↑ 不写任何业务代码
│
├─ framework
│   └─ src/main/java
│       └─ com.littlewin.framework
│           ├─ config
│           │   ↑ Spring / MyBatis / Swagger / 跨域等配置
│           │
│           ├─ security
│           │   ↑ Spring Security 配置
│           │   ↑ JWT 登录认证、权限校验
│           │
│           ├─ filter
│           │   ↑ JWT 过滤器
│           │   ↑ 请求进入 Controller 前的安全处理
│           │
│           └─ interceptor
│               ↑ 登录拦截、权限拦截
│               ↑ 主要用于接口访问控制
│
├─ common
│   └─ src/main/java
│       └─ com.littlewin.common
│           ├─ core
│           │   ↑ 统一返回结果（AjaxResult）
│           │
│           ├─ constants
│           │   ↑ 系统常量（状态码、用户类型等）
│           │
│           ├─ utils
│           │   ↑ 工具类（时间、加密、JWT、Security 工具）
│           │
│           └─ exception
│               ↑ 全局异常处理
│               ↑ 自定义业务异常
│
├─ system
│   └─ src/main/java
│       └─ com.littlewin.system
│           ├─ controller
│           │   ↑ 用户、角色、菜单、登录接口
│           │
│           ├─ service
│           │   ↑ 系统业务接口定义
│           │
│           ├─ service/impl
│           │   ↑ 系统业务实现类
│           │
│           ├─ mapper
│           │   ↑ MyBatis Mapper 接口
│           │
│           └─ domain
│               ↑ 系统相关实体类
│               ↑ 用户、角色、权限等
│
├─ note
│   └─ src/main/java
│       └─ com.littlewin.note
│           ├─ controller
│           │   ↑ 笔记相关接口
│           │   ↑ 新增 / 编辑 / 查询 / 公开笔记
│           │
│           ├─ service
│           │   ↑ 笔记业务接口
│           │
│           ├─ service/impl
│           │   ↑ 笔记业务实现
│           │
│           ├─ mapper
│           │   ↑ 笔记、分类、标签、评论 Mapper
│           │
│           └─ domain
│               ↑ 笔记实体类
│               ↑ Note / Category / Tag / Comment 等
│
└─ pom.xml
    ↑ 父 pom
    ↑ 统一管理依赖版本
    ↑ 子模块无需写版本号
```

### 2、Web 前端项目结构

> Web 端采用基于中后台集成方案的二次开发模式，复用了 Geeker-Admin 成熟的路由守卫与权限动态过滤机制，重点开发了符合本系统业务逻辑的笔记审核与用户分析模块。详细结构参考：[Geeker-Admin](https://docs.spicyboy.cn/guide/catalogue.html#geeker-admin-%E7%9B%AE%E5%BD%95%E8%AF%B4%E6%98%8E-%F0%9F%93%9A)



### 3、微信小程序项目结构

> 基于微信原生小程序开发，面向终端用户，提供核心笔记业务。

```
smart-note-mp
│
├─ components
│   ├─ note-card
│   │   ↑ 笔记卡片组件（首页流、收藏列表复用）
│   │
│   └─ search-bar
│       ↑ 搜索组件
│
├─ images
│   ├─ tabbar                ↑ 底部导航栏图标
│   └─ empty                 ↑ 缺省页图片
│
├─ pages
│   ├─ auth
│   │   └─ login             ↑ 授权登录页（获取 OpenID）
│   │
│   ├─ index
│   │   └─ index             ↑ 首页（推荐流 / 关注流）
│   │
│   ├─ note
│   │   ├─ detail            ↑ 笔记详情页（Markdown 渲染、互动）
│   │   └─ edit              ↑ 笔记发布 / 编辑页
│   │
│   ├─ folder
│   │   └─ index             ↑ 知识库页（分类与标签索引）
│   │
│   └─ mine
│       ├─ index             ↑ 个人中心主页
│       ├─ profile           ↑ 个人信息修改
│       └─ history           ↑ 浏览历史 / 收藏列表
│
├─ utils
│   ├─ request.js
│   │   ↑ wx.request 封装（统一 Header Token、错误处理）
│   │
│   └─ format.js
│       ↑ 时间格式化等工具
│
├─ app.js
│   ↑ 小程序逻辑入口（生命周期、全局数据 globalData）
│
├─ app.json
│   ↑ 全局配置（页面路由、Tabbar、窗口表现）
│
├─ app.wxss
│   ↑ 全局样式（主题色、字体定义）
│
└─ project.config.json
    ↑ 项目配置文件（AppID、编译设置）
```

