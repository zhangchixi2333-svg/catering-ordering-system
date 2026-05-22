-- ============================================
-- 修复菜单数据和权限配置
-- ============================================

USE catering_auth;

-- 1. 清理现有菜单数据
DELETE FROM sys_role_menu;
DELETE FROM sys_menu WHERE id > 0;

-- 2. 重新插入菜单数据
-- 一级菜单（根节点，parentId=0，menuType=1表示目录，menuType=2表示页面）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible, status) VALUES
-- 根菜单
(1, 0, '首页', 'dashboard', 2, '/dashboard', 'DashboardView.vue', '🏠', 1, 'dashboard:view', 1, 1),
(2, 0, '排队管理', 'queue', 1, '', '', '🎫', 2, NULL, 1, 1),
(3, 0, '订单管理', 'order', 1, '', '', '📦', 3, NULL, 1, 1),
(4, 0, '店铺管理', 'shop', 1, '', '', '🏪', 4, NULL, 1, 1),
(5, 0, '系统管理', 'system', 1, '', '', '⚙️', 5, NULL, 1, 1);

-- 二级菜单（parentId指向一级菜单ID，menuType=2表示页面）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible, status) VALUES
-- 排队管理子菜单（parentId=2）
(6, 2, '取号排队', 'queue:take', 2, '/queue', 'QueueView.vue', '🎫', 1, 'queue:take', 1, 1),
(7, 2, '叫号管理', 'queue:call', 2, '/call-number', 'CallNumberView.vue', '🔔', 2, 'queue:call', 1, 1),

-- 订单管理子菜单（parentId=3）
(8, 3, '在线点餐', 'order:menu', 2, '/ordering', 'OrderingView.vue', '🍽️', 1, 'order:menu', 1, 1),
(9, 3, '我的订单', 'order:my', 2, '/my-orders', 'MyOrdersView.vue', '📦', 2, 'order:my', 1, 1),
(10, 3, '支付订单', 'order:payment', 2, '/payment', 'PaymentView.vue', '💳', 3, 'order:payment', 1, 1),
(11, 3, '全部订单', 'order:all', 2, '/orders', 'OrderView.vue', '📊', 4, 'order:all', 1, 1),

-- 店铺管理子菜单（parentId=4）
(12, 4, '店铺列表', 'shop:list', 2, '/shops', 'ShopView.vue', '🏪', 1, 'shop:list', 1, 1),
(13, 4, '店铺统计', 'shop:stats', 2, '/shop-stats', 'ShopStatsView.vue', '📈', 2, 'shop:stats', 1, 1),
(14, 4, '桌台管理', 'shop:table', 2, '/table-management', 'TableManagementView.vue', '🪑', 3, 'shop:table', 1, 1),

-- 系统管理子菜单（parentId=5）
(15, 5, '用户管理', 'system:user', 2, '/system/users', 'UserManageView.vue', '👥', 1, 'system:user', 1, 1),
(16, 5, '角色管理', 'system:role', 2, '/system/roles', 'RoleManageView.vue', '🔐', 2, 'system:role', 1, 1),
(17, 5, '系统设置', 'system:settings', 2, '/settings', 'SettingsView.vue', '⚙️', 3, 'system:settings', 1, 1);

-- 3. 重新分配角色菜单权限

-- 普通用户 (USER) 权限：首页、在线点餐、取号排队、我的订单、支付订单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1),   -- 首页
(1, 2),   -- 排队管理（目录）
(1, 6),   -- 取号排队
(1, 3),   -- 订单管理（目录）
(1, 8),   -- 在线点餐
(1, 9),   -- 我的订单
(1, 10);  -- 支付订单

-- 店员 (STAFF) 权限：普通用户 + 叫号管理、全部订单、桌台管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1),   -- 首页
(2, 2),   -- 排队管理（目录）
(2, 6),   -- 取号排队
(2, 7),   -- 叫号管理
(2, 3),   -- 订单管理（目录）
(2, 8),   -- 在线点餐
(2, 9),   -- 我的订单
(2, 10),  -- 支付订单
(2, 11),  -- 全部订单
(2, 4),   -- 店铺管理（目录）
(2, 14);  -- 桌台管理

-- 店长 (MANAGER) 权限：店员 + 店铺列表、店铺统计
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1),   -- 首页
(3, 2),   -- 排队管理（目录）
(3, 6),   -- 取号排队
(3, 7),   -- 叫号管理
(3, 3),   -- 订单管理（目录）
(3, 8),   -- 在线点餐
(3, 9),   -- 我的订单
(3, 10),  -- 支付订单
(3, 11),  -- 全部订单
(3, 4),   -- 店铺管理（目录）
(3, 12),  -- 店铺列表
(3, 13),  -- 店铺统计
(3, 14);  -- 桌台管理

-- 超级管理员 (ADMIN) 权限：所有菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1),   -- 首页
(4, 2),   -- 排队管理（目录）
(4, 6),   -- 取号排队
(4, 7),   -- 叫号管理
(4, 3),   -- 订单管理（目录）
(4, 8),   -- 在线点餐
(4, 9),   -- 我的订单
(4, 10),  -- 支付订单
(4, 11),  -- 全部订单
(4, 4),   -- 店铺管理（目录）
(4, 12),  -- 店铺列表
(4, 13),  -- 店铺统计
(4, 14),  -- 桌台管理
(4, 5),   -- 系统管理（目录）
(4, 15),  -- 用户管理
(4, 16),  -- 角色管理
(4, 17);  -- 系统设置

-- 4. 验证数据
SELECT '=== 菜单数据 ===' AS info;
SELECT id, parent_id, menu_name, menu_type, path, visible, status FROM sys_menu ORDER BY id;

SELECT '=== 角色菜单权限 ===' AS info;
SELECT rm.role_id, r.role_code, rm.menu_id, m.menu_name, m.parent_id 
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.id
JOIN sys_menu m ON rm.menu_id = m.id
ORDER BY rm.role_id, rm.menu_id;

SELECT '=== 用户角色 ===' AS info;
SELECT u.id, u.username, u.nickname, r.role_code 
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id;

SELECT '=== 修复完成 ===' AS info;