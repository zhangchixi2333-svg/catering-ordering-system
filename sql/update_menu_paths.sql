-- =============================================
-- auth_system.sql 菜单路径更新脚本
-- 更新日期: 2026-05-19
-- 说明: 修正菜单路径以匹配前端路由配置
-- =============================================

USE catering_auth;

-- 1. 确保 visible 字段存在（如果不存在则添加）
ALTER TABLE sys_menu 
ADD COLUMN IF NOT EXISTS visible TINYINT DEFAULT 1 COMMENT '是否可见：0-隐藏，1-显示' AFTER permission;

-- 2. 更新菜单路径
UPDATE sys_menu SET path = '/queue' WHERE id = 6;              -- 取号排队
UPDATE sys_menu SET path = '/call-number' WHERE id = 7;        -- 叫号管理
UPDATE sys_menu SET path = '/orders' WHERE id IN (8, 9);       -- 我的订单、全部订单
UPDATE sys_menu SET path = '/shops' WHERE id IN (10, 11);      -- 店铺列表、店铺统计
UPDATE sys_menu SET path = '/settings' WHERE id = 14;          -- 系统设置

-- 3. 设置 visible 字段
UPDATE sys_menu SET visible = 1 WHERE id IN (1, 2, 3, 4, 5);   -- 顶级菜单全部可见
UPDATE sys_menu SET visible = 1 WHERE id IN (6, 7, 8, 9, 10, 11, 14);  -- 大部分二级菜单可见
UPDATE sys_menu SET visible = 0 WHERE id IN (12, 13);          -- 隐藏用户管理和角色管理

-- 4. 验证更新结果
SELECT '菜单路径更新完成！' AS status;

SELECT id, menu_name, path, visible 
FROM sys_menu 
WHERE menu_type = 2 
ORDER BY id;
