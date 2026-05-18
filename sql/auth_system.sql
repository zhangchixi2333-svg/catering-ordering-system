-- =============================================
-- 餐饮点餐排队系统 - 角色权限系统数据库脚本
-- 版本: 1.0
-- 日期: 2026-05-18
-- 说明: RBAC 角色权限管理系统
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS catering_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE catering_auth;

-- =============================================
-- 1. 用户表 (sys_user)
-- 说明: 存储系统所有用户信息
-- =============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（登录账号）',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    nickname VARCHAR(50) COMMENT '昵称/显示名称',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号码',
    email VARCHAR(100) COMMENT '电子邮箱',
    avatar VARCHAR(255) COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    is_online TINYINT DEFAULT 0 COMMENT '是否在线：0-离线，1-在线',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME COMMENT '删除时间（软删除）',
    
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_status (status),
    INDEX idx_is_online (is_online)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- =============================================
-- 2. 角色表 (sys_role)
-- 说明: 定义系统中的角色类型
-- =============================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码（唯一标识）',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    sort_order INT DEFAULT 0 COMMENT '排序号（数字越小越靠前）',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_role_code (role_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- =============================================
-- 3. 用户角色关联表 (sys_user_role)
-- 说明: 用户与角色的多对多关系
-- =============================================
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =============================================
-- 4. 菜单表 (sys_menu)
-- 说明: 定义系统菜单和权限点
-- =============================================
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID（0表示顶级菜单）',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(50) COMMENT '菜单编码（唯一标识）',
    menu_type TINYINT NOT NULL COMMENT '菜单类型：1-目录，2-菜单，3-按钮/权限',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    permission VARCHAR(100) COMMENT '权限标识（如：queue:call）',
    visible TINYINT DEFAULT 1 COMMENT '是否可见：0-隐藏，1-显示',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_parent_id (parent_id),
    INDEX idx_menu_code (menu_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表';

-- =============================================
-- 5. 角色菜单关联表 (sys_role_menu)
-- 说明: 角色与菜单的多对多关系
-- =============================================
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id),
    
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- =============================================
-- 6. 操作日志表 (sys_operation_log)
-- 说明: 记录用户操作日志
-- =============================================
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(100) COMMENT '操作描述',
    module VARCHAR(50) COMMENT '操作模块',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT '操作IP',
    location VARCHAR(100) COMMENT '操作地点',
    result TINYINT COMMENT '操作结果：0-失败，1-成功',
    error_msg TEXT COMMENT '错误信息',
    duration BIGINT COMMENT '执行时长（毫秒）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 初始化数据
-- =============================================

-- 1. 初始化角色数据
INSERT INTO sys_role (role_code, role_name, description, sort_order) VALUES
('USER', '普通用户', '可以取号排队、查看自己的订单', 4),
('STAFF', '店员', '可以管理叫号、处理订单', 3),
('MANAGER', '店长', '可以管理店铺、查看所有数据', 2),
('ADMIN', '超级管理员', '拥有系统所有权限，包括系统设置', 1);

-- 2. 初始化菜单数据
-- 顶级菜单（menu_type: 1-目录，2-菜单）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible) VALUES
(1, 0, '首页', 'dashboard', 2, '/dashboard', 'DashboardView.vue', '🏠', 1, 'dashboard:view', 1),
(2, 0, '排队管理', 'queue', 1, '/queue', '', '🎫', 2, NULL, 1),
(3, 0, '订单管理', 'order', 1, '/orders', '', '📦', 3, NULL, 1),
(4, 0, '店铺管理', 'shop', 1, '/shops', '', '🏪', 4, NULL, 1),
(5, 0, '系统管理', 'system', 1, '/system', '', '⚙️', 5, NULL, 1);

-- 二级菜单（明确指定 ID 6-14）
-- 注意：path 已更新为与前端路由匹配的路径
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible) VALUES
(6, 2, '取号排队', 'queue:take', 2, '/queue', 'QueueView.vue', '🎫', 1, 'queue:take', 1),
(7, 2, '叫号管理', 'queue:call', 2, '/call-number', 'CallNumberView.vue', '🔔', 2, 'queue:call', 1),
(8, 3, '我的订单', 'order:my', 2, '/orders', 'OrderView.vue', '📋', 1, 'order:my', 1),
(9, 3, '全部订单', 'order:all', 2, '/orders', 'OrderAllView.vue', '📊', 2, 'order:all', 1),
(10, 4, '店铺列表', 'shop:list', 2, '/shops', 'ShopView.vue', '🏪', 1, 'shop:list', 1),
(11, 4, '店铺统计', 'shop:stats', 2, '/shops', 'ShopStatsView.vue', '📈', 2, 'shop:stats', 1),
(12, 5, '用户管理', 'system:user', 2, '/system/users', 'UserManageView.vue', '👥', 1, 'system:user', 0),
(13, 5, '角色管理', 'system:role', 2, '/system/roles', 'RoleManageView.vue', '🔐', 2, 'system:role', 0),
(14, 5, '系统设置', 'system:settings', 2, '/settings', 'SettingsView.vue', '⚙️', 3, 'system:settings', 1);

-- 3. 初始化用户数据（密码统一为 123456，实际应该使用 BCrypt 加密）
-- 注意：这里使用明文密码仅用于测试，生产环境必须加密！
INSERT INTO sys_user (username, password, nickname, real_name, phone, email, status) VALUES
('user', '123456', '普通用户', '张三', '13800138001', 'user@example.com', 1),
('staff', '123456', '店员', '李四', '13800138002', 'staff@example.com', 1),
('manager', '123456', '店长', '王五', '13800138003', 'manager@example.com', 1),
('admin', '123456', '管理员', '赵六', '13800138004', 'admin@example.com', 1);

-- 4. 分配用户角色
-- user -> USER
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
-- staff -> STAFF
INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 2);
-- manager -> MANAGER
INSERT INTO sys_user_role (user_id, role_id) VALUES (3, 3);
-- admin -> ADMIN
INSERT INTO sys_user_role (user_id, role_id) VALUES (4, 4);

-- 5. 分配角色菜单权限

-- 普通用户 (USER) 权限：首页、取号排队、我的订单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1),  -- 首页
(1, 2),  -- 排队管理（目录）
(1, 6),  -- 取号排队
(1, 3),  -- 订单管理（目录）
(1, 8);  -- 我的订单

-- 店员 (STAFF) 权限：普通用户 + 叫号管理、全部订单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1),  -- 首页
(2, 2),  -- 排队管理（目录）
(2, 6),  -- 取号排队
(2, 7),  -- 叫号管理
(2, 3),  -- 订单管理（目录）
(2, 8),  -- 我的订单
(2, 9);  -- 全部订单

-- 店长 (MANAGER) 权限：店员 + 店铺管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 1),  -- 首页
(3, 2),  -- 排队管理（目录）
(3, 6),  -- 取号排队
(3, 7),  -- 叫号管理
(3, 3),  -- 订单管理（目录）
(3, 8),  -- 我的订单
(3, 9),  -- 全部订单
(3, 4),  -- 店铺管理（目录）
(3, 10), -- 店铺列表
(3, 11); -- 店铺统计

-- 超级管理员 (ADMIN) 权限：所有菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 1), (4, 2), (4, 6), (4, 7),  -- 首页、排队管理
(4, 3), (4, 8), (4, 9),          -- 订单管理
(4, 4), (4, 10), (4, 11),        -- 店铺管理
(4, 5), (4, 12), (4, 13), (4, 14); -- 系统管理

-- =============================================
-- 常用查询示例
-- =============================================

-- 查询某个用户的所有角色
-- SELECT r.* FROM sys_role r
-- JOIN sys_user_role ur ON r.id = ur.role_id
-- WHERE ur.user_id = 1;

-- 查询某个角色的所有菜单
-- SELECT m.* FROM sys_menu m
-- JOIN sys_role_menu rm ON m.id = rm.menu_id
-- WHERE rm.role_id = 1
-- ORDER BY m.sort_order;

-- 查询用户的完整权限（角色 + 菜单）
-- SELECT DISTINCT m.* FROM sys_menu m
-- JOIN sys_role_menu rm ON m.id = rm.menu_id
-- JOIN sys_user_role ur ON rm.role_id = ur.role_id
-- WHERE ur.user_id = 1 AND m.status = 1
-- ORDER BY m.sort_order;
