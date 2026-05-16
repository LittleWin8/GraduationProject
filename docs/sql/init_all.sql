/* ============================= */
/* 创建数据库            */
/* ============================= */

DROP DATABASE IF EXISTS smart_note;

CREATE DATABASE smart_note
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE smart_note;

/* ============================= */
/* 一、用户与权限模块       */
/* ============================= */

/* 1. 用户主表 */
CREATE TABLE sys_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态：1 正常，0 禁用，2 注销',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常, 1 删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户主表';

/* 2. 用户认证表 */
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

/* 3. 用户详细信息表 */
CREATE TABLE user_info (
    user_id BIGINT PRIMARY KEY COMMENT '关联用户ID',
    gender TINYINT DEFAULT 0 COMMENT '性别：0 未知, 1 男, 2 女',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    birthday DATE COMMENT '生日',
    city VARCHAR(50) COMMENT '所在城市',
    signature VARCHAR(255) COMMENT '个性签名',
    last_login_ip VARCHAR(45) COMMENT '最后登录IP',
    last_login_time DATETIME COMMENT '最后登录时间',
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户详细资料表';

/* 4. 角色表 */
CREATE TABLE sys_role (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL COMMENT '角色权限字符',
    sort_order INT DEFAULT 0 COMMENT '显示顺序',
    status TINYINT DEFAULT 1 COMMENT '状态：1 正常, 0 停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色信息表';

/* 5. 菜单权限表 */
CREATE TABLE sys_menu (
    menu_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    name VARCHAR(50) DEFAULT NULL COMMENT '路由名称',
    path VARCHAR(255) DEFAULT NULL COMMENT '路由地址',
    component VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    redirect VARCHAR(255) DEFAULT NULL COMMENT '重定向地址',
    menu_type CHAR(1) NOT NULL COMMENT '类型：M目录, C菜单, F按钮',
    title VARCHAR(50) NOT NULL COMMENT '菜单标题',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
    is_link VARCHAR(255) DEFAULT '' COMMENT '是否外链',
    is_hide TINYINT DEFAULT 0 COMMENT '是否隐藏：0 否, 1 是',
    is_full TINYINT DEFAULT 0 COMMENT '是否全屏：0 否, 1 是',
    is_affix TINYINT DEFAULT 0 COMMENT '是否固定：0 否, 1 是',
    is_keep_alive TINYINT DEFAULT 1 COMMENT '是否缓存：0 否, 1 是',
    active_menu VARCHAR(255) DEFAULT NULL COMMENT '高亮菜单路径',
    perms VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    sort_order INT DEFAULT 0 COMMENT '显示顺序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

/* 6. 关联表 */
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

/* ============================= */
/* 二、笔记业务模块       */
/* ============================= */

/* 1. 系统预设分类表 (由管理员维护) */
CREATE TABLE sys_category (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0 禁用, 1 启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统预设分类表';

/* 2. 笔记主表 */
CREATE TABLE note (
    note_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '作者ID',
    category_id BIGINT COMMENT '关联系统分类ID',
    title VARCHAR(200) NOT NULL COMMENT '笔记标题',
    content LONGTEXT COMMENT 'Markdown内容',
    is_public TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开：0 私密, 1 公开',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1 正常, 2 回收站, 3 下架',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数（冗余）',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数（冗余）',
    summary VARCHAR(500) DEFAULT NULL COMMENT '内容摘要（冗余，前200字）',
    reviewed TINYINT NOT NULL DEFAULT 0 COMMENT '审核标记：0 未审核, 1 已审核',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常, 1 删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_note_public_status (is_public, status, del_flag, create_time),
    INDEX idx_note_user_status (user_id, status, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记主表';

/* 3. 用户标签定义表 (用户自由创建) - 完善态 */
CREATE TABLE note_tag (
    tag_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_note_tag_user_name (user_id, name),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户笔记标签表';

/* 4. 笔记标签关联表 - 建议补查询索引 */
CREATE TABLE note_tag_rel (
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (note_id, tag_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记与标签关联表';

/* 5. 附件表 (增加用户关联与文件元数据) */
CREATE TABLE note_attachment (
    attach_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
    note_id BIGINT NOT NULL COMMENT '所属笔记ID',
    user_id BIGINT NOT NULL COMMENT '上传者ID',
    file_url VARCHAR(255) NOT NULL COMMENT '文件访问路径',
    file_name VARCHAR(255) COMMENT '原始文件名',
    file_suffix VARCHAR(20) COMMENT '文件后缀(如: png, pdf)',
    file_size BIGINT COMMENT '文件大小(字节)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',

    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记附件表';

/* 6. 评论表 */
CREATE TABLE note_comment (
    comment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    note_id BIGINT NOT NULL COMMENT '所属笔记ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT COMMENT '父评论ID',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常, 1 删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_note_id (note_id),
    INDEX idx_user_id (user_id),
    INDEX idx_comment_note (note_id, del_flag, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记评论表';

/* 7. 笔记互动表 */
CREATE TABLE note_reaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '互动记录ID',
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    attitude TINYINT NOT NULL DEFAULT 0 COMMENT '态度：0 无, 1 点赞',
    is_favorite TINYINT NOT NULL DEFAULT 0 COMMENT '收藏状态：0 未收藏, 1 已收藏',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_note_user (note_id, user_id),
    INDEX idx_reaction_note_attitude (note_id, attitude),
    INDEX idx_reaction_user_favorite (user_id, is_favorite),
    INDEX idx_reaction_user_attitude (user_id, attitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记用户互动表';

/* ============================= */
/* 三、增强与日志模块       */
/* ============================= */

/* 1. AI 摘要表 */
CREATE TABLE note_ai_summary (
    note_id BIGINT PRIMARY KEY COMMENT '笔记ID',
    summary TEXT COMMENT 'AI生成摘要内容',
    keywords VARCHAR(255) COMMENT '关键词',
    model_name VARCHAR(50) COMMENT '使用的AI模型',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记AI摘要表';

/* 2. AI 调用日志表（每次请求一条记录） */
CREATE TABLE ai_usage_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NOT NULL COMMENT '调用用户ID',
    note_id BIGINT COMMENT '关联笔记ID',
    action_type VARCHAR(20) NOT NULL DEFAULT 'summary' COMMENT '操作类型：summary(摘要生成)',
    prompt_tokens INT DEFAULT 0 COMMENT '输入 token 数',
    completion_tokens INT DEFAULT 0 COMMENT '输出 token 数',
    total_tokens INT DEFAULT 0 COMMENT '总 token 数',
    model_name VARCHAR(50) COMMENT '使用的AI模型',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1成功, 0失败',
    error_msg VARCHAR(500) COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI调用日志表';

/* 3. 用户 AI 配额表（每个用户一条记录） */
CREATE TABLE ai_user_quota (
    user_id BIGINT PRIMARY KEY COMMENT '用户ID',
    monthly_token_limit INT NOT NULL DEFAULT 100000 COMMENT '每月 token 上限',
    monthly_request_limit INT NOT NULL DEFAULT 50 COMMENT '每月请求次数上限',
    used_tokens INT NOT NULL DEFAULT 0 COMMENT '本月已用 token 数',
    used_requests INT NOT NULL DEFAULT 0 COMMENT '本月已用请求次数',
    quota_reset_date DATE COMMENT '配额重置日期（每月1日重置）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户AI配额表';

/* 4. 用户行为日志表 */
CREATE TABLE sys_log_behavior (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    action_type TINYINT NOT NULL COMMENT '行为类型：1 浏览, 2 搜索',
    content VARCHAR(255) COMMENT '内容(笔记ID或关键词)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',

    INDEX idx_user_id (user_id),
    INDEX idx_log_behavior_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志表';

/* 5. 系统操作审计表 */
CREATE TABLE sys_log_operation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '执行者ID',
    username VARCHAR(50) COMMENT '昵称',
    module VARCHAR(30) NOT NULL COMMENT '模块：AUTH, USER, NOTE, DICT, AI, SYSTEM, ROLE',
    action_type TINYINT NOT NULL COMMENT '类型：1登录, 2退出, 3创建, 4修改, 5删除, 6审核',
    business_id BIGINT COMMENT '业务ID',
    description VARCHAR(255) COMMENT '描述',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_method VARCHAR(10) COMMENT '请求方式',
    ip_address VARCHAR(128) COMMENT 'IP',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '结果：1成功, 0失败',
    error_msg TEXT COMMENT '失败原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',

    INDEX idx_module (module),
    INDEX idx_log_operation_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作审计表';

/* ============================= */
/* 四、数据字典模块 (核心配置)     */
/* ============================= */

CREATE TABLE sys_dict_type (
    dict_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典主键',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常, 0停用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

CREATE TABLE sys_dict_data (
    data_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据主键',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_label VARCHAR(100) NOT NULL COMMENT '标签',
    dict_value VARCHAR(100) NOT NULL COMMENT '键值',
    tag_type VARCHAR(100) DEFAULT 'primary' COMMENT 'UI样式(success/info/warning/danger)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常, 0停用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

/* ============================= */
/* 五、用户消息提示                */
/* ============================= */

CREATE TABLE IF NOT EXISTS user_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者用户ID（笔记作者）',
    sender_id BIGINT NOT NULL COMMENT '触发者用户ID（评论者）',
    title VARCHAR(100) DEFAULT NULL COMMENT '消息标题（系统通知用）',
    note_id BIGINT DEFAULT NULL COMMENT '关联笔记ID（系统公告可为空）',
    comment_id BIGINT COMMENT '关联评论ID',
    type TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型：1评论, 2回复, 3审核通过, 4审核不通过, 5违规下架, 6系统公告, 7点赞, 8收藏',
    content VARCHAR(500) COMMENT '消息内容摘要（评论内容前50字）',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读, 1已读',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_receiver_read (receiver_id, is_read),
    INDEX idx_receiver_time (receiver_id, create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- =============================================
-- 系统初始化数据
-- =============================================

-- =============================================
-- 0. 环境初始化：清空旧数据并重置自增 ID
-- =============================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_role_menu;
TRUNCATE TABLE user_auth;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE sys_menu;
TRUNCATE TABLE user_info;
TRUNCATE TABLE sys_category;
TRUNCATE TABLE sys_dict_type;
TRUNCATE TABLE sys_dict_data;
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 1. 用户基础数据 (管理端与客户端示例)
-- =============================================
-- user_id 1: 超级管理员 (管理端)
INSERT INTO sys_user (user_id, nickname, avatar, status, del_flag) VALUES
(1, '超级管理员', NULL, 1, 0),
(2, '内容运营', NULL, 1, 0);

-- user_id 100: 模拟客户端微信用户
INSERT INTO sys_user (user_id, nickname, avatar, status, del_flag) VALUES
(100, '测试用户', NULL, 1, 0);

-- 认证表：区分登录端属性
INSERT INTO user_auth (user_id, auth_type, identifier, credential) VALUES
(1, 'password', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi'),
(2, 'password', 'editor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi'),
(100, 'wx_openid', 'on_666_open_id_example', NULL);

-- 用户详细资料
INSERT INTO user_info (user_id, gender, phone, email, city, signature) VALUES
(1, 1, '18888888888', 'admin@example.com', '北京', '系统最高管理员，专注架构设计'),
(2, 2, '17777777777', 'editor@example.com', '上海', '资深内容运营，细节控'),
(100, 1, NULL, NULL, '北京', '热爱学习，热爱生活');

-- =============================================
-- 2. 角色定义 (RBAC 核心)
-- =============================================
INSERT INTO sys_role (role_id, role_name, role_key, sort_order, status) VALUES
(1, '超级管理员', 'admin', 1, 1),
(2, '内容运营', 'editor', 2, 1);

-- 用户关联角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 2);

-- =============================================
-- 3. 菜单权限重构 (1000-5000 编码体系)
-- =============================================

-- ---------------------------------------------
-- [1000系列] 门户与个人基础 (Portal)
-- ---------------------------------------------
-- 首页菜单已由工作台 (menu_id=1100) 替代，不再需要独立的 /home/index 路由
-- INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
-- (1000, 0, 'home', '/home/index', '/home/index', 'C', '首页', 'HomeFilled', 1);

-- 工作台（M 型目录，下设仪表盘和数据分析）
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
(1100, 0, 'dashboard', '/dashboard', '', 'M', '工作台', 'Odometer', 2);

-- 仪表盘
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
(1101, 1100, 'dashboardIndex', '/dashboard/index', '/dashboard/index', 'C', '仪表盘', 'DataLine', 1);

-- 数据分析
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
(1102, 1100, 'analyze', '/dashboard/analyze', '/dashboard/analyze/index', 'C', '数据分析', 'ChatLineRound', 2);

-- 个人中心 (顶级目录，扁平化 Path，独立组件路径)
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
(1200, 0, 'profile', '/profile/index', '/profile/index', 'C', '个人中心', 'UserFilled', 3);


-- ---------------------------------------------
-- [2000系列] 系统管理 (System Admin)
-- ---------------------------------------------
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
(2000, 0, 'system', '/system', NULL, 'M', '系统管理', 'Setting', 4);

-- 账号列表
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(2010, 2000, 'user', '/system/user', '/system/user/index', 'C', '账号列表', 'Avatar', 'sys:user:list', 1);
-- 按钮权限
INSERT INTO sys_menu (menu_id, parent_id, menu_type, title, perms, sort_order) VALUES
(2011, 2010, 'F', '新增用户', 'sys:user:add', 1),
(2012, 2010, 'F', '修改用户', 'sys:user:edit', 2),
(2013, 2010, 'F', '删除用户', 'sys:user:delete', 3);

-- 角色权限
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(2020, 2000, 'role', '/system/role', '/system/role/index', 'C', '角色权限', 'Key', 'sys:role:list', 2);

-- 字典管理
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(2030, 2000, 'dict', '/system/dict', '/system/dict/index', 'C', '字典管理', 'Memo', 'sys:dict:list', 3);

-- 插入字典数据详情页 (作为字典管理的子路由，但设置为隐藏)
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, is_hide,active_menu,sort_order) VALUES
(2031,2030,'dictData','/system/dict/data/:dictType','/system/dict/detail','C','字典数据','Menu','sys:dict:edit',1,'/system/dict',1);

-- 角色管理按钮权限
INSERT INTO sys_menu (menu_id, parent_id, menu_type, title, perms, sort_order) VALUES
(2021, 2020, 'F', '新增角色', 'sys:role:add', 1),
(2022, 2020, 'F', '修改角色', 'sys:role:edit', 2),
(2023, 2020, 'F', '删除角色', 'sys:role:delete', 3),
(2024, 2020, 'F', '分配权限', 'sys:role:assign', 4);


-- ---------------------------------------------
-- [3000系列] 内容管理 (Content)
-- ---------------------------------------------
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
(3000, 0, 'note', '/note', NULL, 'M', '内容管理', 'Document', 5);

-- 笔记列表
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(3010, 3000, 'noteList', '/note/list', '/note/list/index', 'C', '笔记列表', 'List', 'note:list:view', 1);

-- 笔记列表按钮权限
INSERT INTO sys_menu (menu_id, parent_id, menu_type, title, perms, sort_order) VALUES
(3011, 3010, 'F', '审核笔记', 'note:audit', 1),
(3012, 3010, 'F', '删除笔记', 'note:delete', 2),
(3013, 3010, 'F', '标记已审核', 'note:review', 3);

-- ---------------------------------------------
-- [4000系列] 分类维护 (Category)
-- ---------------------------------------------
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
(4000, 0, 'category', '/category', NULL, 'M', '分类维护', 'CollectionTag', 6);

-- 标签管理
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(4010, 4000, 'tag', '/category/tag', '/category/tag/index', 'C', '标签管理', 'PriceTag', 'note:tag:list', 1);

-- 分类管理（TreeFilter + ProTable 组合布局）
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(4020, 4000, 'tree', '/category/category', '/category/category/index', 'C', '分类管理', 'Operation', 'note:cat:list', 2);

-- 分类管理按钮权限
INSERT INTO sys_menu (menu_id, parent_id, menu_type, title, perms, sort_order) VALUES
(4021, 4020, 'F', '新增分类', 'category:add', 1),
(4022, 4020, 'F', '编辑分类', 'category:edit', 2),
(4023, 4020, 'F', '删除分类', 'category:delete', 3);

-- 标签管理按钮权限
INSERT INTO sys_menu (menu_id, parent_id, menu_type, title, perms, sort_order) VALUES
(4011, 4010, 'F', '删除标签', 'note:tag:del', 1);

-- ---------------------------------------------
-- [5000系列] 系统监控 (Monitor)
-- ---------------------------------------------
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
(5000, 0, 'monitor', '/monitor', NULL, 'M', '系统监控', 'Monitor', 7);

-- 操作日志
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(5010, 5000, 'log', '/monitor/log', '/monitor/log/index', 'C', '操作审计', 'Memo', 'sys:log:view', 1);

-- 行为日志菜单
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(5015, 5000, 'behavior', '/monitor/behavior', '/monitor/behavior/index', 'C', '行为日志', 'View', 'sys:log:behavior', 2);

-- AI 任务监控
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(5020, 5000, 'aiLog', '/monitor/aiLog', '/monitor/aiLog/index', 'C', 'AI 监控', 'Cpu', 'sys:ai:log', 3);

-- 通知管理
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(5030, 5000, 'notification', '/monitor/notification', '/monitor/notification/index', 'C', '通知管理', 'Bell', 'sys:notification:send', 4);

-- 通知管理按钮权限
INSERT INTO sys_menu (menu_id, parent_id, menu_type, title, perms, sort_order) VALUES
(5031, 5030, 'F', '发送通知', 'sys:notification:send', 1);


-- =============================================
-- 4. 角色菜单权限关联 (RBAC Assign)
-- =============================================
-- admin: 关联全部
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu;

-- editor: 关联门户(1000-1299) + 内容与分类(3000-4999)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu
WHERE (menu_id BETWEEN 1000 AND 1299)
   OR (menu_id BETWEEN 3000 AND 4999);


-- =============================================
-- 5. 系统预设分类
-- =============================================
INSERT INTO sys_category (category_id, name, parent_id, sort_order, status) VALUES
(1, '技术笔记', 0, 1, 1),
(2, '生活随笔', 0, 2, 1),
(3, 'Java', 1, 1, 1),
(4, 'Vue', 1, 2, 1),
(5, 'AI工具', 1, 3, 1);


-- =============================================
-- 6. 数据字典 (全量增强版)
-- =============================================

-- 6.1 字典类型
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, remark) VALUES
(1, '用户性别', 'user_gender', 1, 'user_info.gender'),
(2, '用户状态', 'user_status', 1, 'sys_user.status'),
(3, '笔记状态', 'note_status', 1, 'note.status'),
(4, '认证类型', 'auth_type', 1, '区分管理端(pwd)与客户端(wx)'),
(5, '笔记公开性', 'note_public', 1, 'note.is_public'),
(6, '互动态度', 'reaction_attitude', 1, 'note_reaction.attitude'),
(7, '操作类型', 'operation_type', 1, '审计日志操作行为'),
(8, '角色名称', 'role_name', 1, 'sys_role.role_id'),
(9, '行为类型', 'behavior_type', 1, 'sys_log_behavior.action_type'),
(10, '笔记审核状态', 'note_review', 1, 'note.reviewed');


-- 6.2 字典数据
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, tag_type, sort_order) VALUES
-- 认证类型
('auth_type', '账号密码(管理端)', 'password', 'primary', 1),
('auth_type', '微信登录(客户端)', 'wx_openid', 'success', 2),

-- 性别
('user_gender', '未知', '0', 'info', 1),
('user_gender', '男', '1', 'primary', 2),
('user_gender', '女', '2', 'warning', 3),

-- 用户状态
('user_status', '正常', '1', 'success', 1),
('user_status', '禁用', '0', 'danger', 2),
('user_status', '注销', '2', 'info', 3),

-- 笔记状态
('note_status', '正常', '1', 'success', 1),
('note_status', '回收站', '2', 'danger', 2),
('note_status', '下架', '3', 'warning', 3),

-- 公开性
('note_public', '私密', '0', 'danger', 1),
('note_public', '公开', '1', 'success', 2),

-- 互动态度
('reaction_attitude', '无态度', '0', 'info', 1),
('reaction_attitude', '点赞', '1', 'success', 2),
('reaction_attitude', '踩', '2', 'warning', 3),

-- 审计操作
('operation_type', '登录', '1', 'success', 1),
('operation_type', '退出', '2', 'info', 2),
('operation_type', '创建', '3', 'primary', 3),
('operation_type', '修改', '4', 'warning', 4),
('operation_type', '删除', '5', 'danger', 5),
('operation_type', '审核', '6', 'success', 6),

-- 角色名称
('role_name', '超级管理员', '1', 'danger', 1),
('role_name', '内容运营', '2', 'success', 2),

-- 用户行为
('behavior_type', '浏览', '1', 'primary', 1),
('behavior_type', '搜索', '2', 'warning', 2),

-- 笔记审核状态
('note_review', '未审核', '0', 'warning', 1),
('note_review', '已审核', '1', 'success', 2);

-- =============================================
-- 测试数据：笔记 + 标签 + 评论（属于 user_id=100 测试用户）
-- =============================================

-- 1. 用户标签
INSERT INTO note_tag (tag_id, name, user_id) VALUES
(1001, 'Java', 100),
(1002, 'Spring Boot', 100),
(1003, 'Vue3', 100),
(1004, '前端', 100),
(1005, 'AI', 100),
(1006, '学习笔记', 100);

-- 3. 笔记数据（5 篇，分属不同分类和标签）
-- 笔记 1：技术笔记 > Java（公开，已审核）
INSERT INTO note (note_id, user_id, category_id, title, content, is_public, status, view_count, like_count, comment_count, summary, reviewed, create_time, update_time) VALUES
(1001, 100, 3, 'Java 集合框架深入理解',
'# Java 集合框架深入理解

## List 接口
- ArrayList：基于数组，随机访问快
- LinkedList：基于链表，插入删除快

## Map 接口
- HashMap：基于哈希表，O(1) 查找
- TreeMap：基于红黑树，有序

## Set 接口
- HashSet：基于 HashMap
- TreeSet：基于 TreeMap

```java
List<String> list = new ArrayList<>();
list.add("hello");
list.add("world");
```

> 集合框架是 Java 最重要的基础知识之一。',
1, 1, 42, 5, 1, 'Java 集合框架是 Java 最重要的基础知识之一，包括 List、Map、Set 三大接口。', 1, NOW(), NOW());

-- 笔记 2：技术笔记 > Vue（公开，已审核）
INSERT INTO note (note_id, user_id, category_id, title, content, is_public, status, view_count, like_count, comment_count, summary, reviewed, create_time, update_time) VALUES
(1002, 100, 4, 'Vue3 组合式 API 实战',
'# Vue3 组合式 API 实战

## ref 和 reactive
```javascript
import { ref, reactive } from \'vue\'

const count = ref(0)
const state = reactive({ name: \'张三\', age: 25 })
```

## computed 计算属性
```javascript
const doubleCount = computed(() => count.value * 2)
```

## watch 侦听器
```javascript
watch(count, (newVal, oldVal) => {
  console.log(`count 从 ${oldVal} 变为 ${newVal}`)
})
```

组合式 API 让代码组织更灵活，逻辑复用更方便。',
1, 1, 89, 8, 2, 'Vue3 组合式 API 实战指南，包括 ref、reactive、computed、watch 的使用。', 1, NOW(), NOW());

-- 笔记 3：技术笔记 > AI 工具（公开，未审核）
INSERT INTO note (note_id, user_id, category_id, title, content, is_public, status, view_count, like_count, comment_count, summary, reviewed, create_time, update_time) VALUES
(1003, 100, 5, 'DeepSeek API 接入指南',
'# DeepSeek API 接入指南

## 什么是 DeepSeek
DeepSeek 是国产大语言模型，兼容 OpenAI 接口格式。

## 接入步骤
1. 注册账号获取 API Key
2. 配置 base_url: https://api.deepseek.com
3. 使用 LangChain4j 集成

```java
OpenAiChatModel model = OpenAiChatModel.builder()
    .apiKey(apiKey)
    .baseUrl(baseUrl)
    .modelName("deepseek-chat")
    .build();
```

## 应用场景
- 文本摘要生成
- 智能问答
- 内容创作辅助',
1, 1, 23, 2, 0, 'DeepSeek API 接入指南，兼容 OpenAI 接口格式，可用于摘要、问答等场景。', 0, NOW(), NOW());

-- 笔记 4：生活随笔（私密）
INSERT INTO note (note_id, user_id, category_id, title, content, is_public, status, view_count, like_count, comment_count, summary, reviewed, create_time, update_time) VALUES
(1004, 100, 2, '今天的学习计划',
'# 今天的学习计划

## 上午
- 复习 Java 集合框架
- 完成 LeetCode 每日一题

## 下午
- 学习 Vue3 组合式 API
- 整理笔记

## 晚上
- 阅读技术博客
- 写学习总结

> 坚持每天学习，积少成多！',
0, 1, 5, 0, 0, '今天的学习计划，包括 Java 复习、Vue3 学习和技术博客阅读。', 1, NOW(), NOW());

-- 笔记 5：欢迎使用笔记（公开，已审核）
INSERT INTO note (note_id, user_id, category_id, title, content, is_public, status, view_count, like_count, comment_count, summary, reviewed, create_time, update_time) VALUES
(1005, 100, 1, '欢迎使用智能笔记系统',
'# 欢迎使用智能笔记系统 👋

感谢你使用本系统！这是一款面向个人知识管理的智能笔记工具。

## 主要功能

### 📝 笔记管理
- 支持 Markdown 格式编辑
- 分类和标签组织笔记
- 公开/私密灵活控制

### 🤖 AI 智能助手
- **AI 摘要**：一键生成笔记摘要和关键词
- **AI 扩写**：自动扩展内容到 200-300 字
- **AI 润色**：优化文字表达
- **AI 总结**：50 字精炼总结
- **标签推荐**：智能匹配已有标签

### 💬 社交互动
- 点赞、收藏感兴趣的笔记
- 评论交流，分享观点
- 消息通知，及时获取动态

### 📊 数据统计
- 浏览量、点赞数、评论数实时统计
- 个人中心查看创作数据

## 快速开始

1. 点击底部"+"按钮创建你的第一篇笔记
2. 选择合适的分类和标签
3. 使用 AI 助手优化你的内容
4. 设置为公开，与社区分享

> 知识需要沉淀，学习需要记录。开始你的知识管理之旅吧！',
1, 1, 156, 12, 1, '欢迎使用智能笔记系统！本系统支持 Markdown 编辑、AI 智能助手、社交互动等功能。', 1, NOW(), NOW());

-- 4. 笔记标签关联
INSERT INTO note_tag_rel (note_id, tag_id) VALUES
(1001, 1001),  -- Java 集合 → Java 标签
(1001, 1006),  -- Java 集合 → 学习笔记
(1002, 1003),  -- Vue3 实战 → Vue3 标签
(1002, 1004),  -- Vue3 实战 → 前端标签
(1002, 1006),  -- Vue3 实战 → 学习笔记
(1003, 1005),  -- DeepSeek → AI 标签
(1003, 1006),  -- DeepSeek → 学习笔记
(1005, 1006);  -- 欢迎笔记 → 学习笔记

-- 5. 互动数据（点赞 + 收藏）
INSERT INTO note_reaction (note_id, user_id, attitude, is_favorite) VALUES
(1001, 100, 1, 1),  -- 点赞并收藏
(1002, 100, 1, 1),
(1003, 100, 1, 0),  -- 只点赞
(1005, 100, 1, 1);

-- 6. 欢迎笔记评论
INSERT INTO note_comment (comment_id, note_id, user_id, content, parent_id, del_flag, create_time) VALUES
(1001, 1005, 1, '欢迎使用本系统！如有任何问题，随时联系我们。祝你学习愉快！ 🎉', NULL, 0, NOW());

-- 7. 系统通知消息（欢迎消息）
INSERT INTO user_message (receiver_id, sender_id, title, note_id, type, content, is_read, create_time) VALUES
(100, 1, '欢迎加入', 1005, 6, '欢迎使用智能笔记系统！开始记录你的知识之旅吧。', 0, NOW());
