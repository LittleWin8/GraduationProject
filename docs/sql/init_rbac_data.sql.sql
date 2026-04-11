-- =============================================
-- 0. 清空原始数据（注意顺序，先删关联表）
-- =============================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_role_menu;
TRUNCATE TABLE user_auth;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE sys_menu;
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 1. 角色数据
-- =============================================
INSERT INTO sys_role (role_id, role_name, role_key, sort_order, status, create_time) VALUES
(1, '超级管理员', 'admin', 1, 1, NOW()),
(2, '内容运营', 'editor', 2, 1, NOW());

-- =============================================
-- 2. 菜单数据 (M-目录, C-菜单, F-按钮)
-- =============================================

-- 目录及菜单 (1-15)
INSERT INTO sys_menu VALUES
(1, 0, '首页', '/home/index', 'home/index', NULL, 'C', '首页', 'HomeFilled', '', 0, 0, 0, 1, NULL, NULL, 1, NOW()),
(2, 0, '工作台', '/dashboard', NULL, NULL, 'M', '工作台', 'Odometer', '', 0, 0, 0, 1, NULL, NULL, 2, NOW()),
(3, 0, '用户管理', '/system', NULL, NULL, 'M', '用户管理', 'User', '', 0, 0, 0, 1, NULL, NULL, 3, NOW()),
(4, 0, '内容管理', '/note', NULL, NULL, 'M', '内容管理', 'Document', '', 0, 0, 0, 1, NULL, NULL, 4, NOW()),
(5, 0, '分类维护', '/category', NULL, NULL, 'M', '分类维护', 'CollectionTag', '', 0, 0, 0, 1, NULL, NULL, 5, NOW()),
(6, 0, '系统监控', '/monitor', NULL, NULL, 'M', '系统监控', 'Monitor', '', 0, 0, 0, 1, NULL, NULL, 6, NOW()),
(7, 2, '统计仪表盘', 'index', 'dashboard/index', NULL, 'C', '统计仪表盘', 'TrendCharts', '', 0, 0, 0, 1, NULL, 'sys:stats:view', 1, NOW()),
(8, 2, '待办提醒', 'todo', 'dashboard/todo', NULL, 'C', '待办提醒', 'Bell', '', 0, 0, 0, 1, NULL, 'editor:todo:view', 2, NOW()),
(9, 3, '账号列表', 'user', 'system/user/index', NULL, 'C', '账号列表', 'Avatar', '', 0, 0, 0, 1, NULL, 'sys:user:list', 1, NOW()),
(10, 3, '角色权限', 'role', 'system/role/index', NULL, 'C', '角色权限', 'Key', '', 0, 0, 0, 1, NULL, 'sys:role:list', 2, NOW()),
(11, 4, '笔记列表', 'list', 'note/list/index', NULL, 'C', '笔记列表', 'List', '', 0, 0, 0, 1, NULL, 'note:list:view', 1, NOW()),
(12, 4, '合规审核', 'audit', 'note/audit/index', NULL, 'C', '合规审核', 'Checked', '', 0, 0, 0, 1, NULL, 'note:audit:view', 2, NOW()),
(13, 5, '标签管理', 'tag', 'category/tag/index', NULL, 'C', '标签管理', 'PriceTag', '', 0, 0, 0, 1, NULL, 'note:tag:list', 1, NOW()),
(14, 5, '分类树', 'tree', 'category/tree/index', NULL, 'C', '分类树', 'Operation', '', 0, 0, 0, 1, NULL, 'note:cat:list', 2, NOW()),
(15, 6, '操作审计', 'log', 'monitor/log/index', NULL, 'C', '操作审计', 'Memo', '', 0, 0, 0, 1, NULL, 'sys:log:view', 1, NOW());

-- 按钮权限 (16-35) - 适配 Geeker-Admin authButton 和 useProTable
-- 账号列表按钮 (parent_id=9)
INSERT INTO sys_menu VALUES
(16, 9, NULL, NULL, NULL, NULL, 'F', '新增用户', NULL, '', 0, 0, 0, 1, NULL, 'add', 1, NOW()),
(17, 9, NULL, NULL, NULL, NULL, 'F', '编辑用户', NULL, '', 0, 0, 0, 1, NULL, 'edit', 2, NOW()),
(18, 9, NULL, NULL, NULL, NULL, 'F', '删除用户', NULL, '', 0, 0, 0, 1, NULL, 'delete', 3, NOW()),
(19, 9, NULL, NULL, NULL, NULL, 'F', '重置密码', NULL, '', 0, 0, 0, 1, NULL, 'sys:user:reset', 4, NOW()),
(20, 9, NULL, NULL, NULL, NULL, 'F', '导出数据', NULL, '', 0, 0, 0, 1, NULL, 'export', 5, NOW()),

-- 角色权限按钮 (parent_id=10)
(21, 10, NULL, NULL, NULL, NULL, 'F', '新增角色', NULL, '', 0, 0, 0, 1, NULL, 'add', 1, NOW()),
(22, 10, NULL, NULL, NULL, NULL, 'F', '编辑角色', NULL, '', 0, 0, 0, 1, NULL, 'edit', 2, NOW()),
(23, 10, NULL, NULL, NULL, NULL, 'F', '删除角色', NULL, '', 0, 0, 0, 1, NULL, 'delete', 3, NOW()),

-- 笔记列表按钮 (parent_id=11)
(24, 11, NULL, NULL, NULL, NULL, 'F', '新增笔记', NULL, '', 0, 0, 0, 1, NULL, 'add', 1, NOW()),
(25, 11, NULL, NULL, NULL, NULL, 'F', '删除笔记', NULL, '', 0, 0, 0, 1, NULL, 'delete', 2, NOW()),
(26, 11, NULL, NULL, NULL, NULL, 'F', '批量添加', NULL, '', 0, 0, 0, 1, NULL, 'batchAdd', 3, NOW()),
(27, 11, NULL, NULL, NULL, NULL, 'F', '批量删除', NULL, '', 0, 0, 0, 1, NULL, 'batchDelete', 4, NOW()),
(28, 11, NULL, NULL, NULL, NULL, 'F', '状态切换', NULL, '', 0, 0, 0, 1, NULL, 'status', 5, NOW()),

-- 合规审核按钮 (parent_id=12)
(29, 12, NULL, NULL, NULL, NULL, 'F', '审核通过', NULL, '', 0, 0, 0, 1, NULL, 'note:audit:pass', 1, NOW()),
(30, 12, NULL, NULL, NULL, NULL, 'F', '审核驳回', NULL, '', 0, 0, 0, 1, NULL, 'note:audit:reject', 2, NOW());

-- =============================================
-- 3. 角色菜单关联
-- =============================================

-- 超级管理员 (role_id=1): 关联所有 (1-30)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu;

-- 内容运营 (role_id=2): 仅业务相关
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 2), (2, 4), (2, 5), -- 目录
(2, 8), (2, 11), (2, 12), (2, 13), (2, 14), -- 菜单
(2, 24), (2, 25), (2, 26), (2, 27), (2, 28), -- 笔记按钮
(2, 29), (2, 30); -- 审核按钮

-- =============================================
-- 4. 用户数据
-- =============================================
INSERT INTO sys_user (user_id, nickname, avatar, status, create_time, update_time) VALUES
(1, '超级管理员', NULL, 1, NOW(), NOW()),
(2, '内容运营', NULL, 1, NOW(), NOW());

-- identifier 分别为 admin 和 editor，密码均为 123456
INSERT INTO user_auth (user_id, auth_type, identifier, credential, create_time) VALUES
(1, 'password', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', NOW()),
(2, 'password', 'editor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', NOW());

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2);