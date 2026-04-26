# 基于 Spring Boot 的个人知识管理与智能笔记系统

## 一、项目概述

本项目是一个前后端分离的个人知识管理与智能笔记系统，面向学习记录、笔记沉淀与轻量社交互动场景。

系统由三个核心子项目组成：

- `smart-note-system`：后端服务（Spring Boot 多模块）
- `smart-note-ui`：管理端 Web（Vue 3 + Element Plus）
- `smart-note-mp`：移动端小程序（uni-app + Vue 3，目标平台为微信小程序）

## 二、核心目标

- 高效记录：支持移动端随手创建与管理笔记。
- 结构化沉淀：通过分类与标签组织知识内容，支持 Markdown。
- 智能辅助：预留 AI 能力扩展，用于摘要、关键词提取等场景。
- 轻量互动：支持公开笔记的评论、点赞与收藏。

## 三、技术栈与工具

| 维度 | 分类 | 技术选型 / 工具 | 说明 |
| --- | --- | --- | --- |
| 后端 | 核心框架 | Spring Boot 3.x | 提供业务逻辑与 RESTful API |
| 后端 | 持久层 | MyBatis-Plus | 简化数据库操作 |
| 后端 | 安全认证 | Spring Security + JWT | 登录认证与接口权限控制 |
| 后端 | 接口文档 | Knife4j（OpenAPI 3） | 接口调试与文档展示 |
| 后端 | AI 扩展 | LangChain4j | 智能能力接入预留 |
| 数据库 | 核心数据库 | MySQL 8.0 | 存储用户、笔记、交互与日志数据 |
| 管理端 | 前端框架 | Vue 3 + Vite + Element Plus | 系统管理、审核与统计 |
| 小程序端 | 前端框架 | uni-app（Vue 3）+ uview-plus | 面向微信小程序的业务端应用 |
| 小程序端 | 开发工具 | HBuilderX（DCloud） | uni-app 主要开发与构建工具 |
| 小程序端 | 联调工具 | 微信开发者工具 | 小程序预览、调试与上传 |
| 其他 | 接口调试 | Postman | 接口联调与验证 |
| 其他 | 运行环境 | Java 17 / Node.js（建议 16+） / Maven 3.x | 项目构建与运行依赖 |

> 说明：小程序端当前为 **uni-app 开发模式**，并非微信原生小程序开发。

## 四、项目结构

```text
GraduationProject
├─ smart-note-system   # 后端（Spring Boot 多模块）
├─ smart-note-ui       # 管理端前端（Vue3）
├─ smart-note-mp       # 小程序端（uni-app）
└─ docs                # 设计与数据库文档
```

## 五、相关文档

1. 数据库设计说明：[docs/sql/data_dictionary.md](docs/sql/data_dictionary.md)
2. 数据库初始化脚本：[docs/sql/init_db.sql](docs/sql/init_db.sql)
3. 系统架构设计：[docs/design/architecture.md](docs/design/architecture.md)
4. 后端 API 设计：[docs/design/api-design.md](docs/design/api-design.md)

