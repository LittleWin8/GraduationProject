-- =============================================
-- 1. 角色数据
-- =============================================
INSERT INTO sys_role (`role_id`, `role_name`, `role_key`, `sort_order`) VALUES (1, '超级管理员', 'admin', 1);
INSERT INTO sys_role (`role_id`, `role_name`, `role_key`, `sort_order`) VALUES (2, '内容运营', 'editor', 2);

-- =============================================
-- 2. 菜单数据 (M-目录, C-菜单, F-按钮)
-- 所有主键从1开始连续递增
-- =============================================

-- 顶级菜单：首页（直接作为菜单C，无子菜单）
INSERT INTO sys_menu VALUES (1, 0, '首页', '/home/index', 'home/index', NULL, 'HomeFilled', 'C', 1, NOW());

-- 顶级目录：工作台
INSERT INTO sys_menu VALUES (2, 0, '工作台', '/dashboard', NULL, NULL, 'Odometer', 'M', 2, NOW());

-- 顶级目录：用户管理 (仅超管)
INSERT INTO sys_menu VALUES (3, 0, '用户管理', '/system', NULL, NULL, 'User', 'M', 3, NOW());

-- 顶级目录：内容管理 (两者共有)
INSERT INTO sys_menu VALUES (4, 0, '内容管理', '/note', NULL, NULL, 'Document', 'M', 4, NOW());

-- 顶级目录：分类维护 (运营关注)
INSERT INTO sys_menu VALUES (5, 0, '分类维护', '/category', NULL, NULL, 'CollectionTag', 'M', 5, NOW());

-- 顶级目录：系统监控 (仅超管)
INSERT INTO sys_menu VALUES (6, 0, '系统监控', '/monitor', NULL, NULL, 'Monitor', 'M', 6, NOW());

-- =============================================
-- 子菜单
-- =============================================

-- 工作台的子菜单 (parent_id=2)
INSERT INTO sys_menu VALUES (7, 2, '统计仪表盘', 'index', 'dashboard/index', 'sys:stats:view', 'TrendCharts', 'C', 1, NOW());
INSERT INTO sys_menu VALUES (8, 2, '待办提醒', 'todo', 'dashboard/todo', 'editor:todo:view', 'Bell', 'C', 2, NOW());

-- 用户管理的子菜单 (parent_id=3)
INSERT INTO sys_menu VALUES (9, 3, '账号列表', 'user', 'system/user/index', 'sys:user:list', 'Avatar', 'C', 1, NOW());
INSERT INTO sys_menu VALUES (10, 3, '角色权限', 'role', 'system/role/index', 'sys:role:list', 'Key', 'C', 2, NOW());

-- 内容管理的子菜单 (parent_id=4)
INSERT INTO sys_menu VALUES (11, 4, '笔记列表', 'list', 'note/list/index', 'note:list:view', 'List', 'C', 1, NOW());
INSERT INTO sys_menu VALUES (12, 4, '合规审核', 'audit', 'note/audit/index', 'note:audit:view', 'Checked', 'C', 2, NOW());

-- 分类维护的子菜单 (parent_id=5)
INSERT INTO sys_menu VALUES (13, 5, '标签管理', 'tag', 'category/tag/index', 'note:tag:list', 'PriceTag', 'C', 1, NOW());
INSERT INTO sys_menu VALUES (14, 5, '分类树', 'tree', 'category/tree/index', 'note:cat:list', 'Operation', 'C', 2, NOW());

-- 系统监控的子菜单 (parent_id=6)
INSERT INTO sys_menu VALUES (15, 6, '操作审计', 'log', 'monitor/log/index', 'sys:log:view', 'Memo', 'C', 1, NOW());

-- =============================================
-- 按钮权限
-- =============================================

-- 账号列表下的按钮 (parent_id=9)
INSERT INTO sys_menu VALUES (16, 9, '重置密码', NULL, NULL, 'sys:user:reset', NULL, 'F', 1, NOW());

-- 笔记列表下的按钮 (parent_id=11)
INSERT INTO sys_menu VALUES (17, 11, '删除笔记', NULL, NULL, 'note:delete', NULL, 'F', 1, NOW());

-- 合规审核下的按钮 (parent_id=12)
INSERT INTO sys_menu VALUES (18, 12, '审核通过', NULL, NULL, 'note:audit:pass', NULL, 'F', 1, NOW());
INSERT INTO sys_menu VALUES (19, 12, '审核驳回', NULL, NULL, 'note:audit:reject', NULL, 'F', 2, NOW());

-- =============================================
-- 3. 角色菜单关联
-- =============================================

-- 超级管理员（角色ID=1）：拥有所有菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 顶级菜单/目录
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),
-- 工作台子菜单
(1, 7), (1, 8),
-- 用户管理及其按钮
(1, 9), (1, 10), (1, 16),
-- 内容管理及其按钮
(1, 11), (1, 12), (1, 17), (1, 18), (1, 19),
-- 分类维护
(1, 13), (1, 14),
-- 系统监控
(1, 15);

-- 内容运营（角色ID=2）：只拥有业务菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 顶级菜单/目录（排除用户管理3和系统监控6）
(2, 1), (2, 2), (2, 4), (2, 5),
-- 工作台（只保留待办提醒）
(2, 8),
-- 内容管理及其按钮（保留审核功能，不保留删除笔记按钮）
(2, 11), (2, 12), (2, 18), (2, 19),
-- 分类维护
(2, 13), (2, 14);

-- =============================================
-- 4. 用户数据
-- =============================================

-- 用户1：超级管理员
INSERT INTO sys_user (user_id, nickname, status) VALUES (1, '超级管理员', 1);
INSERT INTO user_auth (user_id, auth_type, identifier, credential) VALUES (1, 'password', 'admin', '$2a$10$eZO0AnhhyuJPH6Emmd9.Guzfh4XDqItX0dl89I55lTzOaBv7oVK7m');
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 用户2：内容运营
INSERT INTO sys_user (user_id, nickname, status) VALUES (2, '内容运营', 1);
INSERT INTO user_auth (user_id, auth_type, identifier, credential) VALUES (2, 'password', 'editor', '$2a$10$eZO0AnhhyuJPH6Emmd9.Guzfh4XDqItX0dl89I55lTzOaBv7oVK7m');
INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 2);