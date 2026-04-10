/* ============================= */
/*         创建数据库            */
/* ============================= */

DROP DATABASE IF EXISTS smart_note;

CREATE DATABASE smart_note
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE smart_note;


/* ============================= */
/*      一、用户与权限模块       */
/* ============================= */

/* 用户主表 */
CREATE TABLE sys_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态：1 正常，0 禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户主表';


/* 用户认证表 */
CREATE TABLE user_auth (
    auth_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '认证ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    auth_type VARCHAR(30) NOT NULL COMMENT '认证类型：password / wx_openid',
    identifier VARCHAR(100) NOT NULL COMMENT '用户名或OpenID',
    credential VARCHAR(255) COMMENT '加密密码（微信可为空）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_identifier_type (identifier, auth_type),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录认证表';

/* 角色表 */
CREATE TABLE sys_role (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_key` varchar(50) NOT NULL COMMENT '角色权限字符',
  `sort_order` int DEFAULT '0' COMMENT '显示顺序',
  `status` tinyint DEFAULT '1' COMMENT '状态：1正常, 0停用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色信息表';

/* 菜单权限表 */
CREATE TABLE sys_menu (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `title` varchar(50) NOT NULL COMMENT '菜单标题',
  `path` varchar(100) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(100) DEFAULT NULL COMMENT '组件路径',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `menu_type` char(1) DEFAULT NULL COMMENT 'M目录 C菜单 F按钮',
  `sort_order` int DEFAULT '0' COMMENT '显示顺序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

/* 角色-菜单关联表 */
CREATE TABLE sys_role_menu (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

/* 用户角色关联表 */
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';


/* ============================= */
/*        二、笔记业务模块       */
/* ============================= */

/* 笔记主表 */
CREATE TABLE note (
    note_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '作者ID',
    category_name VARCHAR(50) COMMENT '分类名称',
    title VARCHAR(200) NOT NULL COMMENT '笔记标题',
    content LONGTEXT COMMENT 'Markdown内容',
    is_public TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开：0 私密，1 公开',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0 草稿，1 正常，2 回收站',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记主表';


/* 标签定义表 */
CREATE TABLE note_tag (
    tag_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',

    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记标签定义表';


/* 笔记标签关联表（多对多） */
CREATE TABLE note_tag_rel (
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',

    PRIMARY KEY (note_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记与标签关联表';


/* 附件表 */
CREATE TABLE note_attachment (
    attach_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
    note_id BIGINT NOT NULL COMMENT '所属笔记ID',
    file_url VARCHAR(255) NOT NULL COMMENT '文件访问路径',
    file_name VARCHAR(255) COMMENT '原始文件名',
    file_size BIGINT COMMENT '文件大小（字节）',

    INDEX idx_note_id (note_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记附件表';


/* 评论表 */
CREATE TABLE note_comment (
    comment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    note_id BIGINT NOT NULL COMMENT '所属笔记ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT COMMENT '父评论ID（用于回复）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 删除',

    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记评论表';


/* 笔记互动表 */
CREATE TABLE note_reaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '互动记录ID',
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    attitude TINYINT NOT NULL DEFAULT 0 COMMENT '态度：0 无态度，1 点赞，2 踩',
    is_favorite TINYINT NOT NULL DEFAULT 0 COMMENT '收藏状态：0 未收藏，1 已收藏',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_note_user (note_id, user_id),
    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记用户互动表';


/* ============================= */
/*      三、增强与日志模块       */
/* ============================= */

/* AI 摘要表 */
CREATE TABLE note_ai_summary (
    note_id BIGINT PRIMARY KEY COMMENT '笔记ID',
    summary TEXT COMMENT 'AI生成摘要内容',
    keywords VARCHAR(255) COMMENT '关键词',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记AI摘要表';


/* 用户行为日志表 */
CREATE TABLE sys_log_behavior (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    action_type TINYINT NOT NULL COMMENT '行为类型：1 浏览，2 搜索',
    content VARCHAR(255) COMMENT '行为内容（浏览笔记ID或搜索关键词）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',

    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志表';


/* 系统操作审计表 */
CREATE TABLE sys_log_operation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '执行者ID',
    username VARCHAR(50) COMMENT '执行者账号/昵称',
    module VARCHAR(30) NOT NULL COMMENT '操作模块：AUTH, USER, NOTE, CATEGORY, INTERACT, AI',
    action_type TINYINT NOT NULL COMMENT '行为类型：1 登录, 2 退出, 3 创建, 4 修改, 5 删除, 6 审核',
    business_id BIGINT COMMENT '业务主键ID（如笔记ID、用户ID）',
    description VARCHAR(255) COMMENT '操作描述',
    request_url VARCHAR(255) COMMENT '请求接口路径',
    request_method VARCHAR(10) COMMENT '请求方式（POST/DELETE等）',
    ip_address VARCHAR(128) COMMENT 'IP地址',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '操作结果：1 成功, 0 失败',
    error_msg TEXT COMMENT '失败原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '触发时间',

    INDEX idx_user_id (user_id),
    INDEX idx_module (module),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作审计表';

/* ============================= */
/*       四、权限与菜单模块         */
/* ============================= */
/* 管理端菜单权限表表 */
CREATE TABLE sys_menu (
    menu_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID（0为顶级菜单）',
    title VARCHAR(50) NOT NULL COMMENT '菜单标题',
    path VARCHAR(100) COMMENT '路由路径（前端Vue路由地址）',
    component VARCHAR(100) COMMENT '组件路径（Vue文件路径，如 note/audit/index）',
    icon VARCHAR(50) COMMENT '图标标识',
    sort_order INT DEFAULT 0 COMMENT '显示排序',

    -- 核心字段：控制谁能看
    -- 建议逻辑：0-仅超管可见, 2-仅运营可见, 9-两者均可见
    role_scope TINYINT DEFAULT 9 COMMENT '角色可见范围：0-仅超管, 2-仅运营, 9-通用',

    is_hidden TINYINT DEFAULT 0 COMMENT '隐藏状态：0-显示, 1-隐藏',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理端菜单权限表';