# 一、 设计概述

本系统（Smart Note）后台数据库采用关系型数据库 MySQL。

- **存储引擎**：InnoDB（支持事务处理、行级锁）。
- **字符编码**：`utf8mb4`（支持完整 Unicode 字符集，包括 Emoji 表情）。
- **排序规则**：`utf8mb4_general_ci`。
- **关联设计**：采用**逻辑外键**设计，表间关联通过业务字段（如 `user_id`、`note_id`）实现，不使用数据库级 `FOREIGN KEY` 约束，以提升系统灵活性和开发效率。
- **数据审计**：根据业务重要程度差异化设计 —— 核心数据表含创建与更新双时间戳，日志表仅记录创建时间，关联表不设时间戳。



# 二、数据库E-R图

![E-R图](./img/erd.png)



# 三、表清单汇总

| 模块           | 物理表名          | 中文名称       | 核心作用                                     |
| :------------- | :---------------- | :------------- | :------------------------------------------- |
| **用户与权限** | sys_user          | 用户主表       | 存储系统用户基础信息                         |
|                | user_auth         | 用户认证表     | 存储登录凭证（密码 / 微信 OpenID）           |
|                | sys_role          | 角色信息表     | 定义系统角色（超管、运营等）                 |
|                | sys_menu          | 菜单权限表     | 管理端菜单结构及权限标识                     |
|                | sys_role_menu     | 角色菜单关联表 | 关联角色与菜单权限                           |
|                | sys_user_role     | 用户角色关联表 | 关联用户与角色                               |
| **笔记业务**   | note              | 笔记主表       | 存储笔记核心内容                             |
|                | note_tag          | 标签定义表     | 笔记标签库                                   |
|                | note_tag_rel      | 笔记标签关联表 | 处理多对多关系                               |
|                | note_attachment   | 笔记附件表     | 存储图片与文件信息                           |
|                | note_comment      | 笔记评论表     | 用户对笔记的评论                             |
|                | note_reaction     | 笔记互动表     | 点赞 / 踩 / 收藏记录                         |
| **增强与日志** | note_ai_summary   | AI 摘要表      | 存储 AI 生成内容                             |
|                | sys_log_behavior  | 用户行为日志表 | 记录浏览、搜索等行为                         |
|                | sys_log_operation | 系统操作审计表 | 记录全部用户关键动作，用于安全追溯和管理审计 |

# 四、各表字段详细设计

## 1. 用户与权限模块（6张表）

### sys_user（用户主表）

| 字段名      | 类型         | 说明                     |
| :---------- | :----------- | :----------------------- |
| user_id     | BIGINT (PK)  | 用户ID，自增             |
| nickname    | VARCHAR(50)  | 昵称                     |
| avatar      | VARCHAR(255) | 头像URL                  |
| status      | TINYINT      | 用户状态：1 正常，0 禁用 |
| create_time | DATETIME     | 创建时间，默认当前时间   |
| update_time | DATETIME     | 更新时间，自动更新       |

### user_auth（用户认证表）

| 字段名      | 类型         | 说明                                                         |
| :---------- | :----------- | :----------------------------------------------------------- |
| auth_id     | BIGINT (PK)  | 认证ID，自增                                                 |
| user_id     | BIGINT       | 关联用户ID                                                   |
| auth_type   | VARCHAR(30)  | 认证类型：password / wx_openid                               |
| identifier  | VARCHAR(100) | 用户名或OpenID                                               |
| credential  | VARCHAR(255) | 加密密码（微信可为空）                                       |
| create_time | DATETIME     | 创建时间                                                     |
| **索引**    |              | uk_identifier_type (identifier, auth_type) 唯一；idx_user_id |

### sys_role（角色信息表）

| 字段名      | 类型        | 说明                     |
| :---------- | :---------- | :----------------------- |
| role_id     | BIGINT (PK) | 角色ID，自增             |
| role_name   | VARCHAR(50) | 角色名称（如超级管理员） |
| role_key    | VARCHAR(50) | 角色权限字符（如 admin） |
| sort_order  | INT         | 显示顺序                 |
| status      | TINYINT     | 状态：1正常，0停用       |
| create_time | DATETIME    | 创建时间                 |



### sys_menu（菜单权限表）

| 字段名        | 类型         | 说明                                              |
| :------------ | :----------- | :------------------------------------------------ |
| menu_id       | BIGINT (PK)  | 菜单ID，自增                                      |
| parent_id     | BIGINT       | 父菜单ID，0为顶级，默认0                          |
| name          | VARCHAR(50)  | 路由名称（对应前端JSON中的name，用于KeepAlive）   |
| path          | VARCHAR(255) | 路由地址（对应前端JSON中的path）                  |
| component     | VARCHAR(255) | 组件路径（对应前端JSON中的component）             |
| redirect      | VARCHAR(255) | 重定向地址                                        |
| menu_type     | CHAR(1)      | 类型：M目录 / C菜单 / F按钮（非空）               |
| title         | VARCHAR(50)  | 菜单标题（对应meta.title，非空）                  |
| icon          | VARCHAR(50)  | 图标（对应meta.icon）                             |
| is_link       | VARCHAR(255) | 是否外链（对应meta.isLink），默认空字符串         |
| is_hide       | TINYINT      | 是否隐藏：0否, 1是（对应meta.isHide），默认0      |
| is_full       | TINYINT      | 是否全屏：0否, 1是（对应meta.isFull），默认0      |
| is_affix      | TINYINT      | 是否固定：0否, 1是（对应meta.isAffix），默认0     |
| is_keep_alive | TINYINT      | 是否缓存：0否, 1是（对应meta.isKeepAlive），默认1 |
| active_menu   | VARCHAR(255) | 高亮菜单路径（对应meta.activeMenu）               |
| perms         | VARCHAR(100) | 权限标识（如：add, edit, delete）                 |
| sort_order    | INT          | 显示顺序，默认0                                   |
| create_time   | DATETIME     | 创建时间，默认当前时间                            |

### sys_role_menu（角色菜单关联表）

| 字段名  | 类型   | 说明               |
| :------ | :----- | :----------------- |
| role_id | BIGINT | 角色ID（联合主键） |
| menu_id | BIGINT | 菜单ID（联合主键） |

### sys_user_role（用户角色关联表）

| 字段名  | 类型   | 说明               |
| :------ | :----- | :----------------- |
| user_id | BIGINT | 用户ID（联合主键） |
| role_id | BIGINT | 角色ID（联合主键） |

## 2. 笔记业务模块（6张表）

### note（笔记主表）

| 字段名        | 类型         | 说明                     |
| :------------ | :----------- | :----------------------- |
| note_id       | BIGINT (PK)  | 笔记ID，自增             |
| user_id       | BIGINT       | 作者ID                   |
| category_name | VARCHAR(50)  | 分类名称                 |
| title         | VARCHAR(200) | 标题                     |
| content       | LONGTEXT     | Markdown 内容            |
| is_public     | TINYINT      | 0 私密，1 公开           |
| status        | TINYINT      | 0 草稿，1 正常，2 回收站 |
| view_count    | INT          | 浏览次数，默认0          |
| create_time   | DATETIME     | 创建时间                 |
| update_time   | DATETIME     | 更新时间                 |
| **索引**      |              | idx_user_id, idx_status  |

### note_tag（标签定义表）

| 字段名   | 类型        | 说明         |
| :------- | :---------- | :----------- |
| tag_id   | BIGINT (PK) | 标签ID，自增 |
| name     | VARCHAR(50) | 标签名称     |
| user_id  | BIGINT      | 所属用户ID   |
| **索引** |             | idx_user_id  |

### note_tag_rel（笔记标签关联表）

| 字段名  | 类型   | 说明               |
| :------ | :----- | :----------------- |
| note_id | BIGINT | 笔记ID（联合主键） |
| tag_id  | BIGINT | 标签ID（联合主键） |

### note_attachment（附件表）

| 字段名    | 类型         | 说明             |
| :-------- | :----------- | :--------------- |
| attach_id | BIGINT (PK)  | 附件ID，自增     |
| note_id   | BIGINT       | 所属笔记ID       |
| file_url  | VARCHAR(255) | 文件访问路径     |
| file_name | VARCHAR(255) | 原始文件名       |
| file_size | BIGINT       | 文件大小（字节） |
| **索引**  |              | idx_note_id      |

### note_comment（评论表）

| 字段名      | 类型        | 说明                                    |
| :---------- | :---------- | :-------------------------------------- |
| comment_id  | BIGINT (PK) | 评论ID，自增                            |
| note_id     | BIGINT      | 所属笔记ID                              |
| user_id     | BIGINT      | 评论用户ID                              |
| content     | TEXT        | 评论内容                                |
| parent_id   | BIGINT      | 父评论ID（用于回复）                    |
| create_time | DATETIME    | 创建时间                                |
| del_flag    | TINYINT     | 逻辑删除：0 正常，1 删除                |
| **索引**    |             | idx_note_id, idx_user_id, idx_parent_id |

### note_reaction（互动表）

| 字段名       | 类型        | 说明                            |
| :----------- | :---------- | :------------------------------ |
| id           | BIGINT (PK) | 记录ID，自增                    |
| note_id      | BIGINT      | 笔记ID                          |
| user_id      | BIGINT      | 用户ID                          |
| attitude     | TINYINT     | 0 无态度，1 点赞，2 踩          |
| is_favorite  | TINYINT     | 0 未收藏，1 已收藏              |
| create_time  | DATETIME    | 创建时间                        |
| update_time  | DATETIME    | 更新时间                        |
| **唯一约束** |             | uk_note_user (note_id, user_id) |
| **索引**     |             | idx_note_id, idx_user_id        |

## 3. 增强与日志模块（3张表）

### note_ai_summary（AI 摘要表）

| 字段名      | 类型         | 说明           |
| :---------- | :----------- | :------------- |
| note_id     | BIGINT (PK)  | 笔记ID         |
| summary     | TEXT         | AI生成摘要内容 |
| keywords    | VARCHAR(255) | 关键词         |
| create_time | DATETIME     | 创建时间       |

### sys_log_behavior（行为日志表）

| 字段名      | 类型         | 说明                               |
| :---------- | :----------- | :--------------------------------- |
| id          | BIGINT (PK)  | 日志ID，自增                       |
| user_id     | BIGINT       | 用户ID                             |
| action_type | TINYINT      | 行为类型：1 浏览，2 搜索           |
| content     | VARCHAR(255) | 行为内容（浏览笔记ID或搜索关键词） |
| create_time | DATETIME     | 发生时间                           |
| **索引**    |              | idx_user_id, idx_action_type       |

### sys_log_operation（系统操作审计表）

| 字段名         | 类型         | 说明                                                     |
| :------------- | :----------- | :------------------------------------------------------- |
| id             | BIGINT (PK)  | 日志ID，自增                                             |
| user_id        | BIGINT       | 执行操作的用户ID                                         |
| username       | VARCHAR(50)  | 操作人账号/昵称                                          |
| module         | VARCHAR(30)  | 操作模块：AUTH, USER, NOTE, CATEGORY, INTERACT, AI       |
| action_type    | TINYINT      | 行为类型：1 登录, 2 退出, 3 创建, 4 修改, 5 删除, 6 审核 |
| business_id    | BIGINT       | 业务主键ID（如笔记ID、用户ID）                           |
| description    | VARCHAR(255) | 详细操作描述                                             |
| request_url    | VARCHAR(255) | 请求接口路径                                             |
| request_method | VARCHAR(10)  | 请求方式（POST/DELETE等）                                |
| ip_address     | VARCHAR(128) | 操作时的IP地址                                           |
| status         | TINYINT      | 操作结果：1 成功，0 失败                                 |
| error_msg      | TEXT         | 失败原因                                                 |
| create_time    | DATETIME     | 触发时间                                                 |
| **索引**       |              | idx_user_id, idx_module, idx_create_time                 |
