-- =============================================
-- 0. 清空数据
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
-- 1. 用户
-- =============================================
INSERT INTO sys_user (user_id, nickname, avatar, status) VALUES
(1, '超级管理员', NULL, 1),
(2, '内容运营', NULL, 1);

-- =============================================
-- 2. 用户认证
-- =============================================
INSERT INTO user_auth (user_id, auth_type, identifier, credential) VALUES
(1, 'password', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi'),
(2, 'password', 'editor', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi');

-- =============================================
-- 3. 角色
-- =============================================
INSERT INTO sys_role (role_id, role_name, role_key, sort_order, status) VALUES
(1, '超级管理员', 'admin', 1, 1),
(2, '内容运营', 'editor', 2, 1);

-- =============================================
-- 4. 菜单（结构+顺序版）
-- =============================================

-- 顶级
INSERT INTO sys_menu
(menu_id, parent_id, name, path, component, redirect, menu_type, title, icon, sort_order)
VALUES
(1, 0, 'home', '/home/index', 'home/index', NULL, 'C', '首页', 'HomeFilled', 1),
(2, 0, 'dashboard', '/dashboard', NULL, NULL, 'M', '工作台', 'Odometer', 2),
(3, 0, 'system', '/system', NULL, NULL, 'M', '用户管理', 'User', 3),
(4, 0, 'note', '/note', NULL, NULL, 'M', '内容管理', 'Document', 4),
(5, 0, 'category', '/category', NULL, NULL, 'M', '分类维护', 'CollectionTag', 5),
(6, 0, 'monitor', '/monitor', NULL, NULL, 'M', '系统监控', 'Monitor', 6);

-- 工作台
INSERT INTO sys_menu
(menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order)
VALUES
(10, 2, 'dashboardIndex', 'index', 'dashboard/index', 'C', '统计仪表盘', 'TrendCharts', 'sys:stats:view', 1),
(11, 2, 'todo', 'todo', 'dashboard/todo', 'C', '待办提醒', 'Bell', 'editor:todo:view', 2);

-- 用户管理
INSERT INTO sys_menu
(menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order)
VALUES
(20, 3, 'user', 'user', 'system/user/index', 'C', '账号列表', 'Avatar', 'sys:user:list', 1),
(21, 3, 'role', 'role', 'system/role/index', 'C', '角色权限', 'Key', 'sys:role:list', 2),
(22, 3, 'profile', 'profile', 'system/profile/index', 'C', '个人信息', 'UserFilled', NULL, 3);

-- 内容管理
INSERT INTO sys_menu
(menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order)
VALUES
(30, 4, 'noteList', 'list', 'note/list/index', 'C', '笔记列表', 'List', 'note:list:view', 1),
(31, 4, 'audit', 'audit', 'note/audit/index', 'C', '合规审核', 'Checked', 'note:audit:view', 2);

-- 分类
INSERT INTO sys_menu
(menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order)
VALUES
(40, 5, 'tag', 'tag', 'category/tag/index', 'C', '标签管理', 'PriceTag', 'note:tag:list', 1),
(41, 5, 'tree', 'tree', 'category/tree/index', 'C', '分类树', 'Operation', 'note:cat:list', 2);

-- 系统监控
INSERT INTO sys_menu
(menu_id, parent_id, name, path, component, menu_type, title, icon, perms, sort_order)
VALUES
(50, 6, 'log', 'log', 'monitor/log/index', 'C', '操作审计', 'Memo', 'sys:log:view', 1);

-- =============================================
-- 5. 按钮权限（100+）
-- =============================================
INSERT INTO sys_menu
(menu_id, parent_id, menu_type, title, perms, sort_order)
VALUES
(100, 20, 'F', '新增用户', 'add', 1),
(101, 20, 'F', '编辑用户', 'edit', 2),
(102, 20, 'F', '删除用户', 'delete', 3),
(103, 20, 'F', '重置密码', 'sys:user:reset', 4),

(110, 21, 'F', '新增角色', 'add', 1),
(111, 21, 'F', '编辑角色', 'edit', 2),
(112, 21, 'F', '删除角色', 'delete', 3),

(120, 30, 'F', '新增笔记', 'add', 1),
(121, 30, 'F', '删除笔记', 'delete', 2),
(122, 30, 'F', '批量添加', 'batchAdd', 3),
(123, 30, 'F', '批量删除', 'batchDelete', 4),

(130, 31, 'F', '审核通过', 'note:audit:pass', 1),
(131, 31, 'F', '审核驳回', 'note:audit:reject', 2);

-- =============================================
-- 6. 角色菜单（修改后）
-- =============================================

-- admin：全部菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu;

-- editor（内容运营）：只能看到用户管理下的个人信息，看不到账号列表和角色权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1),   -- 首页
(2, 2),   -- 工作台（父菜单）
(2, 3),   -- 用户管理（父菜单，用于显示个人信息）
(2, 4),   -- 内容管理（父菜单）
(2, 5),   -- 分类维护（父菜单）
(2, 10),  -- 统计仪表盘
(2, 11),  -- 待办提醒
(2, 22),  -- 个人信息（用户管理下的子菜单）
(2, 30),  -- 笔记列表
(2, 31),  -- 合规审核
(2, 40),  -- 标签管理
(2, 41),  -- 分类树
(2, 120), -- 新增笔记按钮
(2, 121), -- 删除笔记按钮
(2, 122), -- 批量添加按钮
(2, 123), -- 批量删除按钮
(2, 130), -- 审核通过按钮
(2, 131); -- 审核驳回按钮

-- =============================================
-- 7. 用户角色
-- =============================================
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2);


-- =============================================
-- 8. 用户详细信息
-- =============================================
INSERT INTO user_info (
    user_id,
    gender,
    phone,
    email,
    birthday,
    city,
    signature,
    last_login_ip,
    last_login_time
) VALUES
(
    1,
    1,
    '18888888888',
    'admin@yourdomain.com',
    '1990-01-01',
    '北京市',
    '向上生长，向下扎根。系统最高管理员。',
    '127.0.0.1',
    '2026-04-11 10:00:00'
),
(
    2,
    2,
    '17777777777',
    'editor@yourdomain.com',
    '1995-05-20',
    '上海市',
    '内容为王，细节至上。负责全平台内容审核。',
    '127.0.0.1',
    '2026-04-11 10:30:00'
);