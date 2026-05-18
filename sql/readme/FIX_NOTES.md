# 🔧 auth_system.sql 修复说明

## ❌ 问题描述

在执行 `auth_system.sql` 时遇到外键约束错误：

```
Cannot add or update a child row: a foreign key constraint fails 
(`catering_auth`.`sys_role_menu`, CONSTRAINT `sys_role_menu_ibfk_2` 
FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`))
```

---

## 🔍 问题原因

在插入二级菜单数据时**没有明确指定 ID**，导致 MySQL 自动生成 ID（从 6 开始递增），但后续的 `sys_role_menu` 插入语句使用了错误的 menu_id 值（7-15）。

### 原始代码（有问题）

```sql
-- 二级菜单（未指定 ID，让数据库自动生成）
INSERT INTO sys_menu (parent_id, menu_name, ...) VALUES
(2, '取号排队', ...),   -- 实际 ID = 6（自动生成）
(2, '叫号管理', ...),   -- 实际 ID = 7
(3, '我的订单', ...),   -- 实际 ID = 8
...

-- 但后续引用时假设 ID 是 7、8、9...
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 7),  -- ❌ 错误！"取号排队" 的实际 ID 是 6，不是 7
(1, 9);  -- ❌ 错误！"我的订单" 的实际 ID 是 8，不是 9
```

---

## ✅ 解决方案

为所有二级菜单**明确指定 ID**，确保与后续 `sys_role_menu` 中的引用一致。

### 修复后的代码

```sql
-- 二级菜单（明确指定 ID 6-14）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission) VALUES
(6, 2, '取号排队', 'queue:take', 2, '/queue/take', 'QueueView.vue', '🎫', 1, 'queue:take'),
(7, 2, '叫号管理', 'queue:call', 2, '/queue/call', 'CallNumberView.vue', '🔔', 2, 'queue:call'),
(8, 3, '我的订单', 'order:my', 2, '/orders/my', 'OrderView.vue', '📋', 1, 'order:my'),
(9, 3, '全部订单', 'order:all', 2, '/orders/all', 'OrderAllView.vue', '📊', 2, 'order:all'),
(10, 4, '店铺列表', 'shop:list', 2, '/shops/list', 'ShopView.vue', '🏪', 1, 'shop:list'),
(11, 4, '店铺统计', 'shop:stats', 2, '/shops/stats', 'ShopStatsView.vue', '📈', 2, 'shop:stats'),
(12, 5, '用户管理', 'system:user', 2, '/system/users', 'UserManageView.vue', '👥', 1, 'system:user'),
(13, 5, '角色管理', 'system:role', 2, '/system/roles', 'RoleManageView.vue', '🔐', 2, 'system:role'),
(14, 5, '系统设置', 'system:settings', 2, '/system/settings', 'SettingsView.vue', '⚙️', 3, 'system:settings');
```

同时更新所有 `sys_role_menu` 引用：

```sql
-- 普通用户 (USER)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1),  -- 首页
(1, 2),  -- 排队管理（目录）
(1, 6),  -- ✅ 取号排队（原来是 7，现在是 6）
(1, 3),  -- 订单管理（目录）
(1, 8);  -- ✅ 我的订单（原来是 9，现在是 8）

-- 其他角色同理...
```

---

## 📊 完整的菜单 ID 映射表

| ID | 父ID | 菜单名称 | 类型 | 路径 |
|----|------|----------|------|------|
| **顶级菜单** | | | | |
| 1 | 0 | 首页 | 菜单 | /dashboard |
| 2 | 0 | 排队管理 | 目录 | /queue |
| 3 | 0 | 订单管理 | 目录 | /orders |
| 4 | 0 | 店铺管理 | 目录 | /shops |
| 5 | 0 | 系统管理 | 目录 | /system |
| **二级菜单** | | | | |
| 6 | 2 | 取号排队 | 菜单 | /queue/take |
| 7 | 2 | 叫号管理 | 菜单 | /queue/call |
| 8 | 3 | 我的订单 | 菜单 | /orders/my |
| 9 | 3 | 全部订单 | 菜单 | /orders/all |
| 10 | 4 | 店铺列表 | 菜单 | /shops/list |
| 11 | 4 | 店铺统计 | 菜单 | /shops/stats |
| 12 | 5 | 用户管理 | 菜单 | /system/users |
| 13 | 5 | 角色管理 | 菜单 | /system/roles |
| 14 | 5 | 系统设置 | 菜单 | /system/settings |

---

## 🧪 验证修复

执行以下 SQL 验证数据是否正确：

```sql
USE catering_auth;

-- 1. 查看所有菜单
SELECT id, parent_id, menu_name, menu_code FROM sys_menu ORDER BY id;

-- 预期输出：14 条记录，ID 从 1 到 14

-- 2. 查看普通用户的菜单
SELECT m.id, m.menu_name, m.parent_id
FROM sys_menu m
JOIN sys_role_menu rm ON m.id = rm.menu_id
WHERE rm.role_id = 1
ORDER BY m.id;

-- 预期输出：
-- id | menu_name | parent_id
-- 1  | 首页      | 0
-- 2  | 排队管理  | 0
-- 3  | 订单管理  | 0
-- 6  | 取号排队  | 2
-- 8  | 我的订单  | 3

-- 3. 查看超级管理员的菜单
SELECT COUNT(*) as menu_count
FROM sys_role_menu
WHERE role_id = 4;

-- 预期输出：14（所有菜单）
```

---

## 📝 经验教训

### ⚠️ 避免类似问题

1. **始终明确指定主键 ID**
   - 即使有 AUTO_INCREMENT，初始化数据时也应手动指定 ID
   - 这样可以确保 ID 的可预测性

2. **分批执行 SQL 并验证**
   - 先执行建表语句
   - 再执行 INSERT 语句
   - 每批执行后验证数据

3. **使用事务保证原子性**
   ```sql
   START TRANSACTION;
   
   -- 执行所有 INSERT
   
   -- 验证数据
   SELECT COUNT(*) FROM sys_menu;
   SELECT COUNT(*) FROM sys_role_menu;
   
   -- 如果没问题则提交
   COMMIT;
   
   -- 如果有问题则回滚
   -- ROLLBACK;
   ```

4. **添加注释说明 ID 分配**
   ```sql
   -- 顶级菜单 ID: 1-5
   -- 二级菜单 ID: 6-14
   ```

---

## 🔄 重新执行脚本

如果需要重新执行修复后的脚本：

```sql
-- 1. 删除旧数据库
DROP DATABASE IF EXISTS catering_auth;

-- 2. 重新创建
CREATE DATABASE catering_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. 执行脚本
USE catering_auth;
SOURCE /path/to/auth_system.sql;

-- 4. 验证
SELECT COUNT(*) AS menu_count FROM sys_menu;
SELECT COUNT(*) AS role_menu_count FROM sys_role_menu;
```

**预期结果**:
- `menu_count`: 14
- `role_menu_count`: 42

---

## ✅ 修复完成

- ✅ 二级菜单 ID 已明确指定（6-14）
- ✅ 所有 `sys_role_menu` 引用已更新
- ✅ 外键约束问题已解决
- ✅ 数据一致性已保证

现在可以正常执行 `auth_system.sql` 脚本了！

---

**修复日期**: 2026-05-18  
**修复者**: Lingma AI Assistant
