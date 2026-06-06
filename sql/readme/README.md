# 📚 SQL 脚本目录说明

## 📁 文件结构

```
sql/
├── auth_system.sql                    # 角色权限系统建表脚本
├── readme/
│   ├── README.md                      # 本文件（目录索引）
│   ├── AUTH_SYSTEM_README.md          # 角色权限系统详细文档
│   └── QUICK_START.md                 # 快速开始指南
├── shop-service.sql                   # 店铺服务建表脚本（已有）
├── queue-service.sql                  # 排队服务建表脚本（已有）
├── order-service.sql                  # 订单服务建表脚本（已有）
└── notification-service.sql           # 通知服务建表脚本（已有）
```

---

## 🗄️ 数据库列表

### 1. catering_auth - 认证授权数据库

**脚本**: `auth_system.sql`

**包含表**:
- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_user_role` - 用户角色关联表
- `sys_menu` - 菜单表
- `sys_role_menu` - 角色菜单关联表
- `sys_operation_log` - 操作日志表

**用途**: RBAC 角色权限管理系统

**文档**: 
- 详细说明: `readme/AUTH_SYSTEM_README.md`
- 快速开始: `readme/QUICK_START.md`

---

### 2. catering_shop - 店铺服务数据库

**脚本**: `shop-service.sql`

**包含表**:
- `shop` - 店铺信息表
- `shop_business_hours` - 店铺营业时间表
- `shop_table` - 店铺桌台表

**用途**: 管理店铺基本信息、营业时间、桌台配置

---

### 3. catering_queue - 排队服务数据库

**脚本**: `queue-service.sql`

**包含表**:
- `queue_number` - 排队号码表
- `queue_config` - 排队配置表

**用途**: 管理排队取号、叫号逻辑（配合 Redis 使用）

---

### 4. catering_order - 订单服务数据库

**脚本**: `order-service.sql`

**包含表**:
- `orders` - 订单主表
- `order_item` - 订单明细表
- `order_payment` - 订单支付记录表

**用途**: 管理订单创建、支付、状态流转

---

### 5. catering_notification - 通知服务数据库

**脚本**: `notification-service.sql`

**包含表**:
- `notification` - 通知消息表
- `user_notification` - 用户通知关联表

**用途**: 管理 WebSocket 推送消息、离线消息存储

---

## 🚀 快速执行所有脚本

### 方法 1: 批量执行

```bash
# Windows PowerShell
Get-ChildItem sql\*.sql | ForEach-Object { mysql -u root -p < $_.FullName }

# Linux/Mac
for file in sql/*.sql; do mysql -u root -p < "$file"; done
```

### 方法 2: 逐个执行

```bash
mysql -u root -p < sql/auth_system.sql
mysql -u root -p < sql/shop-service.sql
mysql -u root -p < sql/queue-service.sql
mysql -u root -p < sql/order-service.sql
mysql -u root -p < sql/notification-service.sql
```

### 方法 3: MySQL Workbench

1. 打开 MySQL Workbench
2. File → Open SQL Script
3. 选择要执行的 `.sql` 文件
4. 点击 ⚡ Execute 按钮

---

## 📊 数据库关系图

```
catering_auth (认证授权)
    ├─ sys_user ──────┐
    ├─ sys_role       │
    └─ sys_menu       │
                      │
catering_shop (店铺)   │
    └─ shop ◄─────────┘ (用户属于店铺)
    
catering_queue (排队)
    └─ queue_number ◄── shop_id
    
catering_order (订单)
    ├─ orders ◄──────── shop_id
    └─ orders ◄──────── user_id
    
catering_notification (通知)
    └─ notification ◄── user_id
```

---

## 🔐 默认测试账号

执行 `auth_system.sql` 后，可使用以下测试账号：

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| user | 123456 | 普通用户 | 取号、查看订单 |
| staff | 123456 | 店员 | + 叫号管理 |
| manager | 123456 | 店长 | + 店铺管理 |
| admin | 123456 | 超级管理员 | 所有权限 |

⚠️ **重要**: 生产环境必须修改默认密码并使用 BCrypt 加密！

---

## 📝 执行顺序建议

### 推荐顺序

1. **auth_system.sql** - 先建立用户体系
2. **shop-service.sql** - 创建店铺数据
3. **queue-service.sql** - 配置排队系统
4. **order-service.sql** - 准备订单表
5. **notification-service.sql** - 最后配置通知

### 原因

- 其他服务可能引用 `sys_user` 表的用户ID
- 店铺是排队和订单的基础数据
- 通知服务依赖用户体系

---

## 🧪 验证安装

执行完所有脚本后，运行以下 SQL 验证：

```sql
-- 查看所有数据库
SHOW DATABASES LIKE 'catering_%';

-- 预期输出:
-- catering_auth
-- catering_shop
-- catering_queue
-- catering_order
-- catering_notification

-- 查看每个数据库的表数量
SELECT 
    TABLE_SCHEMA AS database_name,
    COUNT(*) AS table_count
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA LIKE 'catering_%'
GROUP BY TABLE_SCHEMA;

-- 预期输出:
-- catering_auth: 6 tables
-- catering_shop: 3 tables
-- catering_queue: 2 tables
-- catering_order: 3 tables
-- catering_notification: 2 tables
```

---

## 🔄 重置数据库

如需重新初始化，先删除再执行：

```sql
-- 删除所有数据库
DROP DATABASE IF EXISTS catering_auth;
DROP DATABASE IF EXISTS catering_shop;
DROP DATABASE IF EXISTS catering_queue;
DROP DATABASE IF EXISTS catering_order;
DROP DATABASE IF EXISTS catering_notification;

-- 然后重新执行所有 .sql 脚本
```

---

## 📞 文档导航

- **角色权限系统详解**: [AUTH_SYSTEM_README.md](readme/AUTH_SYSTEM_README.md)
- **快速开始指南**: [QUICK_START.md](readme/QUICK_START.md)
- **前端项目文档**: `frontend/README.md`
- **后端项目文档**: 各服务的 `README.md`

---

## ⚠️ 注意事项

1. **字符集**: 所有表使用 `utf8mb4_unicode_ci`，支持中文和 Emoji
2. **外键**: 部分表有外键约束，删除数据时注意顺序
3. **索引**: 已为常用查询字段添加索引，提升性能
4. **软删除**: `sys_user` 表使用 `deleted_at` 实现软删除
5. **时间戳**: 所有表都有 `created_at` 和 `updated_at` 自动维护

---

## 🛠️ 维护建议

### 定期备份

```bash
# 备份所有数据库
mysqldump -u root -p --databases catering_auth catering_shop catering_queue catering_order catering_notification > backup_$(date +%Y%m%d).sql

# 恢复数据库
mysql -u root -p < backup_20260518.sql
```

### 监控表大小

```sql
SELECT 
    TABLE_SCHEMA,
    TABLE_NAME,
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) AS size_mb
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA LIKE 'catering_%'
ORDER BY size_mb DESC;
```

### 清理日志

```sql
-- 删除 30 天前的操作日志
DELETE FROM sys_operation_log 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

---

**最后更新**: 2026-05-18  
**维护者**: Lingma AI Assistant
