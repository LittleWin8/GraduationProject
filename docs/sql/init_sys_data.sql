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
(100, '极简笔记用户', NULL, 1, 0);

-- 认证表：区分登录端属性
INSERT INTO user_auth (user_id, auth_type, identifier, credential) VALUES
(1, 'password', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi'),
(2, 'password', 'editor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi'),
(100, 'wx_openid', 'on_666_open_id_example', NULL);

-- 用户详细资料
INSERT INTO user_info (user_id, gender, phone, email, city, signature) VALUES
(1, 1, '18888888888', 'admin@example.com', '北京', '系统最高管理员，专注架构设计'),
(2, 2, '17777777777', 'editor@example.com', '上海', '资深内容运营，细节控'),
(100, 0, NULL, NULL, '杭州', '随手记，简单活');

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
-- 首页
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
(1000, 0, 'home', '/home/index', '/home/index', 'C', '首页', 'HomeFilled', 1);

-- 工作台
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
(1100, 0, 'dashboard', '/dashboard/index', '/dashboard/index', 'C', '工作台', 'Odometer', 2);

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
(2030, 2000, 'dict', '/system/dict', '/system/dict/index', 'C', '字典管理', 'Book', 'sys:dict:list', 3);

-- 插入字典数据详情页 (作为字典管理的子路由，但设置为隐藏)
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, is_hide,active_menu,sort_order) VALUES
(2031,2030,'dictData','/system/dict/data/:dictType','/system/dict/detail','C','字典数据','Menu','sys:dict:edit',1,'/system/dict',1);


-- ---------------------------------------------
-- [3000系列] 内容管理 (Content)
-- ---------------------------------------------
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, sort_order) VALUES
(3000, 0, 'note', '/note', NULL, 'M', '内容管理', 'Document', 5);

-- 笔记列表
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order) VALUES
(3010, 3000, 'noteList', '/note/list', '/note/list/index', 'C', '笔记列表', 'List', 'note:list:view', 1);

-- 内容审核（审核功能已集成到笔记列表页，隐藏此菜单）
INSERT INTO sys_menu (menu_id, parent_id, name, path, component, menu_type, title, icon, perms, is_hide, sort_order) VALUES
(3020, 3000, 'audit', '/note/audit', '/note/audit/index', 'C', '内容审核', 'Checked', 'note:audit:view', 1, 2);
-- 笔记列表按钮权限
INSERT INTO sys_menu (menu_id, parent_id, menu_type, title, perms, sort_order) VALUES
(3011, 3010, 'F', '审核笔记', 'note:audit', 1),
(3012, 3010, 'F', '删除笔记', 'note:delete', 2);

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
(9, '行为类型', 'behavior_type', 1, 'sys_log_behavior.action_type');


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
('note_status', '草稿', '0', 'info', 1),
('note_status', '正常', '1', 'success', 2),
('note_status', '回收站', '2', 'danger', 3),
('note_status', '下架', '3', 'warning', 4),

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
('behavior_type', '搜索', '2', 'warning', 2);