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
    attitude TINYINT NOT NULL DEFAULT 0 COMMENT '态度：0 无, 1 点赞, 2 踩',
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

/* 1. AI 摘要表 (增加模型名称记录) */
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
