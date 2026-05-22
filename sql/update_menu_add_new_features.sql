-- =============================================
-- 更新菜单数据 - 添加新功能页面
-- 版本: 1.1
-- 日期: 2026-05-21
-- 说明: 添加我的订单、支付订单、桌台管理等新菜单
-- =============================================

USE catering_auth;

-- =============================================
-- 1. 更新现有菜单路径
-- =============================================

-- 更新"我的订单"路径（从 /orders 改为 /my-orders）
UPDATE sys_menu SET path = '/my-orders', component = 'MyOrdersView.vue' WHERE id = 9 AND menu_code = 'order:my';

-- =============================================
-- 2. 添加新菜单项
-- =============================================

-- 添加"支付订单"菜单（ID: 10）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible) 
VALUES (10, 3, '支付订单', 'order:payment', 2, '/payment', 'PaymentView.vue', '💳', 3, 'order:payment', 1)
ON DUPLICATE KEY UPDATE 
    menu_name = VALUES(menu_name),
    path = VALUES(path),
    component = VALUES(component);

-- 添加"全部订单"菜单（ID: 16）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible) 
VALUES (16, 3, '全部订单', 'order:all', 2, '/orders', 'OrderView.vue', '📊', 4, 'order:all', 1)
ON DUPLICATE KEY UPDATE 
    menu_name = VALUES(menu_name),
    path = VALUES(path),
    component = VALUES(component);

-- 添加"桌台管理"菜单（ID: 17）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible) 
VALUES (17, 4, '桌台管理', 'shop:table', 2, '/table-management', 'TableManagementView.vue', '🪑', 3, 'shop:table', 1)
ON DUPLICATE KEY UPDATE 
    menu_name = VALUES(menu_name),
    path = VALUES(path),
    component = VALUES(component);

-- =============================================
-- 3. 删除旧的重复菜单（如果存在）
-- =============================================

-- 删除旧的"全部订单"菜单（如果之前有错误的ID）
DELETE FROM sys_menu WHERE menu_code = 'order:all' AND id NOT IN (16);

-- =============================================
-- 4. 更新角色菜单权限
-- =============================================

-- 清空现有的角色菜单关联（重新分配）
DELETE FROM sys_role_menu WHERE role_id IN (1, 2, 3, 4);

-- 普通用户 (USER) 权限：首页、在线点餐、取号排队、我的订单、支付订单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1),   -- 首页
(1, 3),   -- 订单管理（目录）
(1, 8),   -- 在线点餐
(1, 9),   -- 我的订单
(1, 10),  -- 支付订单
(1, 2),   -- 排队管理（目录）
(1, 6);   -- 取号排队

-- 店员 (STAFF) 权限：普通用户 + 叫号管理、全部订单、桌台管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1),   -- 首页
(2, 3),   -- 订单管理（目录）
(2, 8),   -- 在线点餐
(2, 9),   -- 我的订单
(2, 10),  -- 支付订单
(2, 16),  -- 全部订单
(2, 2),   -- 排队管理（目录）
(2, 6),   -- 取号排队
(2, 7),   -- 叫号管理
(2, 4),   -- 店铺管理（目录）
(2, 17);  -- 桌台管理

-- 店长 (MANAGER) 权限：店员 + 店铺管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1),   -- 首页
(3, 3),   -- 订单管理（目录）
(3, 8),   -- 在线点餐
(3, 9),   -- 我的订单
(3, 10),  -- 支付订单
(3, 16),  -- 全部订单
(3, 2),   -- 排队管理（目录）
(3, 6),   -- 取号排队
(3, 7),   -- 叫号管理
(3, 4),   -- 店铺管理（目录）
(3, 11),  -- 店铺列表
(3, 12),  -- 店铺统计
(3, 17);  -- 桌台管理

-- 超级管理员 (ADMIN) 权限：所有菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1), (4, 2), (4, 6), (4, 7),    -- 首页、排队管理
(4, 3), (4, 8), (4, 9), (4, 10), (4, 16),  -- 订单管理
(4, 4), (4, 11), (4, 12), (4, 17), -- 店铺管理
(4, 5), (4, 13), (4, 14), (4, 15); -- 系统管理

-- =============================================
-- 5. 验证更新结果
-- =============================================

-- 查看所有菜单
SELECT id, parent_id, menu_name, menu_code, menu_type, path, icon, visible 
FROM sys_menu 
ORDER BY parent_id, sort_order;

-- 查看各角色的菜单数量
SELECT 
    r.role_name,
    COUNT(rm.menu_id) as menu_count
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.id = rm.role_id
GROUP BY r.id, r.role_name
ORDER BY r.sort_order;

-- 查看普通用户的菜单
SELECT m.* FROM sys_menu m
JOIN sys_role_menu rm ON m.id = rm.menu_id
JOIN sys_user_role ur ON rm.role_id = ur.role_id
WHERE ur.user_id = 1 AND m.status = 1
ORDER BY m.sort_order;

-- =============================================
-- 完成提示
-- =============================================
SELECT '✅ 菜单更新完成！请重启前端服务并清除浏览器缓存。' AS message;
