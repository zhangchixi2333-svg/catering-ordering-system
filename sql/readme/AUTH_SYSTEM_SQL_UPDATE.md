# auth_system.sql 菜单路径更新说明

## 📅 更新日期
2026-05-19

## 🔧 更新内容

### 1. 菜单路径修正

将二级菜单的 `path` 字段更新为与前端路由配置匹配的路径：

| ID | 菜单名称 | 原路径 | 新路径 | 说明 |
|----|---------|--------|--------|------|
| 6 | 取号排队 | `/queue/take` | `/queue` | 简化路径 |
| 7 | 叫号管理 | `/queue/call` | `/call-number` | 匹配前端路由 |
| 8 | 我的订单 | `/orders/my` | `/orders` | 统一订单路径 |
| 9 | 全部订单 | `/orders/all` | `/orders` | 统一订单路径 |
| 10 | 店铺列表 | `/shops/list` | `/shops` | 统一店铺路径 |
| 11 | 店铺统计 | `/shops/stats` | `/shops` | 统一店铺路径 |
| 14 | 系统设置 | `/system/settings` | `/settings` | 简化路径 |

### 2. 添加 visible 字段

在所有菜单 INSERT 语句中添加 `visible` 字段：
- `visible = 1`: 菜单可见（默认）
- `visible = 0`: 菜单隐藏

**隐藏的菜单**：
- ID 12: 用户管理 (`/system/users`) - 前端暂无对应页面
- ID 13: 角色管理 (`/system/roles`) - 前端暂无对应页面

### 3. SQL 语句变更

#### 变更前
```sql
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission) VALUES
(6, 2, '取号排队', 'queue:take', 2, '/queue/take', 'QueueView.vue', '🎫', 1, 'queue:take'),
...
```

#### 变更后
```sql
INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, component, icon, sort_order, permission, visible) VALUES
(6, 2, '取号排队', 'queue:take', 2, '/queue', 'QueueView.vue', '🎫', 1, 'queue:take', 1),
...
```

---

## 📋 使用指南

### 场景 1: 新建数据库

直接执行更新后的 SQL 文件即可：

```bash
mysql -u root -p123456 < sql/auth_system.sql
```

### 场景 2: 更新现有数据库

如果数据库中已有旧数据，需要执行以下更新脚本：

```sql
USE catering_auth;

-- 更新菜单路径
UPDATE sys_menu SET path = '/queue' WHERE id = 6;
UPDATE sys_menu SET path = '/call-number' WHERE id = 7;
UPDATE sys_menu SET path = '/orders' WHERE id IN (8, 9);
UPDATE sys_menu SET path = '/shops' WHERE id IN (10, 11);
UPDATE sys_menu SET path = '/settings' WHERE id = 14;

-- 设置 visible 字段（如果表中还没有该字段，需要先添加）
ALTER TABLE sys_menu ADD COLUMN visible TINYINT DEFAULT 1 COMMENT '是否可见：0-隐藏，1-显示' AFTER permission;

-- 隐藏未实现的菜单
UPDATE sys_menu SET visible = 0 WHERE id IN (12, 13);
```

### 场景 3: 重置数据库

如果需要完全重置数据库：

```bash
# 删除旧数据库
mysql -u root -p123456 -e "DROP DATABASE IF EXISTS catering_auth;"

# 重新创建
mysql -u root -p123456 < sql/auth_system.sql
```

---

## ✅ 验证步骤

执行 SQL 后，验证菜单数据是否正确：

```sql
USE catering_auth;

-- 查看所有菜单及其路径
SELECT id, menu_name, path, visible 
FROM sys_menu 
WHERE menu_type = 2 
ORDER BY id;

-- 预期结果：
-- +----+-----------+--------------+---------+
-- | id | menu_name | path         | visible |
-- +----+-----------+--------------+---------+
-- |  1 | 首页      | /dashboard   |       1 |
-- |  6 | 取号排队  | /queue       |       1 |
-- |  7 | 叫号管理  | /call-number |       1 |
-- |  8 | 我的订单  | /orders      |       1 |
-- |  9 | 全部订单  | /orders      |       1 |
-- | 10 | 店铺列表  | /shops       |       1 |
-- | 11 | 店铺统计  | /shops       |       1 |
-- | 12 | 用户管理  | /system/users|       0 | <- 隐藏
-- | 13 | 角色管理  | /system/roles|       0 | <- 隐藏
-- | 14 | 系统设置  | /settings    |       1 |
-- +----+-----------+--------------+---------+
```

---

## 🔗 相关文件

- **SQL 文件**: [sql/auth_system.sql](file:///C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/auth_system.sql)
- **Mapper 文件**: [user-service/src/main/java/org/example/userservice/mapper/SysMenuMapper.java](file:///C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/user-service/src/main/java/org/example/userservice/mapper/SysMenuMapper.java)
- **前端 Store**: [frontend/src/stores/user.js](file:///C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/frontend/src/stores/user.js)
- **修复报告**: [service_readme/FRONTEND_MENU_FIX_REPORT.md](file:///C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/service_readme/FRONTEND_MENU_FIX_REPORT.md)

---

## ⚠️ 注意事项

1. **路径一致性**: 确保数据库中的 `path` 与前端路由配置（`router/index.js`）保持一致
2. **visible 字段**: 如果数据库表中还没有 `visible` 字段，需要先执行 `ALTER TABLE` 添加
3. **重启服务**: 修改数据库后，需要重启 user-service 以加载新数据
4. **清除缓存**: 前端可能需要清除 localStorage 并重新登录

---

## 📝 更新历史

| 日期 | 版本 | 更新内容 | 更新人 |
|------|------|---------|--------|
| 2026-05-19 | v1.1 | 修正菜单路径，添加 visible 字段 | AI Assistant |
| 2026-05-18 | v1.0 | 初始版本，创建 RBAC 权限系统 | AI Assistant |

---

**最后更新**: 2026-05-19  
**维护人员**: 开发团队
