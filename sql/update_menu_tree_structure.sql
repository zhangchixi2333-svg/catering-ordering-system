-- =============================================
-- 更新菜单数据以支持树形结构
-- 说明：根据实际数据结构优化菜单层级结构
-- =============================================

USE catering_auth;

-- 清空现有菜单数据
DELETE FROM sys_role_menu;
DELETE FROM sys_menu;

-- 重新插入菜单数据（根据实际数据结构）
-- 字段：id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible, status

-- 一级菜单（根节点，parentId=0，menuType=1表示目录）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible, status) VALUES
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
(18, 3, '我的订单', 'order:my', 2, '/my-orders', 'MyOrdersView.vue', '📦', 2, 'order:my', 1, 1),
(19, 3, '支付订单', 'order:payment', 2, '/payment', 'PaymentView.vue', '💳', 3, 'order:payment', 1, 1),
(20, 3, '全部订单', 'order:all', 2, '/orders', 'OrderView.vue', '📊', 4, 'order:all', 1, 1),

-- 店铺管理子菜单（parentId=4）
(10, 4, '店铺列表', 'shop:list', 2, '/shops', 'ShopView.vue', '🏪', 1, 'shop:list', 1, 1),
(11, 4, '店铺统计', 'shop:stats', 2, '/shops/stats', 'ShopStatsView.vue', '📈', 2, 'shop:stats', 1, 1),
(21, 4, '桌台管理', 'shop:table', 2, '/table-management', 'TableManagementView.vue', '🪑', 3, 'shop:table', 1, 1),

-- 系统管理子菜单（parentId=5）
(12, 5, '用户管理', 'system:user', 2, '/system/users', 'UserManageView.vue', '👥', 1, 'system:user', 0, 1),
(13, 5, '角色管理', 'system:role', 2, '/system/roles', 'RoleManageView.vue', '🔐', 2, 'system:role', 0, 1),
(14, 5, '系统设置', 'system:settings', 2, '/settings', 'SettingsView.vue', '⚙️', 3, 'system:settings', 1, 1);

-- 为各角色分配菜单权限

-- 普通用户 (USER) - 角色ID=1
-- 首页、取号排队、在线点餐、我的订单、支付订单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1),  -- 首页
(1, 6),  -- 取号排队
(1, 8),  -- 在线点餐
(1, 18), -- 我的订单
(1, 19); -- 支付订单

-- 店员 (STAFF) - 角色ID=2
-- 根据实际数据：88,2,1 89,2,2 90,2,3 91,2,4 92,2,6 93,2,7 94,2,10 95,2,18 96,2,19 97,2,20 98,2,21
-- 首页、排队管理（目录）、订单管理（目录）、店铺管理（目录）、取号排队、叫号管理、店铺列表、我的订单、支付订单、全部订单、桌台管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1),  -- 首页
(2, 2),  -- 排队管理（目录）
(2, 3),  -- 订单管理（目录）
(2, 4),  -- 店铺管理（目录）
(2, 6),  -- 取号排队
(2, 7),  -- 叫号管理
(2, 10), -- 店铺列表
(2, 18), -- 我的订单
(2, 19), -- 支付订单
(2, 20), -- 全部订单
(2, 21); -- 桌台管理

-- 店长 (MANAGER) - 角色ID=3
-- 首页、排队管理、订单管理、店铺管理（包含店铺列表和桌台管理）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1),  -- 首页
(3, 2),  -- 排队管理（目录）
(3, 3),  -- 订单管理（目录）
(3, 4),  -- 店铺管理（目录）
(3, 6),  -- 取号排队
(3, 7),  -- 叫号管理
(3, 8),  -- 在线点餐
(3, 10), -- 店铺列表
(3, 11), -- 店铺统计
(3, 18), -- 我的订单
(3, 19), -- 支付订单
(3, 20), -- 全部订单
(3, 21); -- 桌台管理

-- 超级管理员 (ADMIN) - 角色ID=4
-- 所有菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1),  -- 首页
(4, 2),  -- 排队管理（目录）
(4, 3),  -- 订单管理（目录）
(4, 4),  -- 店铺管理（目录）
(4, 5),  -- 系统管理（目录）
(4, 6),  -- 取号排队
(4, 7),  -- 叫号管理
(4, 8),  -- 在线点餐
(4, 10), -- 店铺列表
(4, 11), -- 店铺统计
(4, 12), -- 用户管理
(4, 13), -- 角色管理
(4, 14), -- 系统设置
(4, 18), -- 我的订单
(4, 19), -- 支付订单
(4, 20), -- 全部订单
(4, 21); -- 桌台管理

-- 验证菜单结构
SELECT 
    m.id,
    m.parent_id,
    m.menu_name,
    m.menu_code,
    CASE m.menu_type 
        WHEN 1 THEN '目录'
        WHEN 2 THEN '页面'
        WHEN 3 THEN '按钮'
        ELSE '未知'
    END AS menu_type,
    m.path,
    m.icon,
    m.sort_order,
    CASE m.visible 
        WHEN 0 THEN '隐藏'
        WHEN 1 THEN '显示'
        ELSE '未知'
    END AS visible,
    CASE m.status 
        WHEN 0 THEN '禁用'
        WHEN 1 THEN '启用'
        ELSE '未知'
    END AS status
FROM sys_menu m
ORDER BY m.parent_id, m.sort_order;

-- 验证角色菜单分配（店员角色）
SELECT 
    r.role_code,
    r.role_name,
    m.id AS menu_id,
    m.parent_id,
    m.menu_name,
    m.menu_code,
    CASE m.menu_type 
        WHEN 1 THEN '目录'
        WHEN 2 THEN '页面'
        WHEN 3 THEN '按钮'
        ELSE '未知'
    END AS menu_type
FROM sys_role r
JOIN sys_role_menu rm ON r.id = rm.role_id
JOIN sys_menu m ON rm.menu_id = m.id
WHERE r.id = 2
ORDER BY m.parent_id, m.sort_order;