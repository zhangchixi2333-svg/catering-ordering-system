# 餐饮点单排队系统 - 数据库文档索引

## 📁 SQL建表语句

| 文件名 | 服务名称 | 说明 |
|--------|---------|------|
| [shop-service.sql](../shop-service.sql) | 店铺服务 | 店铺信息、桌台管理（4张表） |
| [menu-service.sql](../menu-service.sql) | 菜单服务 | 菜单分类、菜品管理（6张表） |
| [order-service.sql](../order-service.sql) | 订单服务 | 订单生命周期管理（6张表） |
| [payment-service.sql](../payment-service.sql) | 支付服务 | 支付记录管理（5张表） |
| [queue-service.sql](../queue-service.sql) | 排队服务 | 排队取号逻辑（6张表） |
| [notification-service.sql](../notification-service.sql) | 通知服务 | 消息推送、通知（5张表） |

**总计**: 6个数据库，32张表

---

## 📖 数据库说明文档

| 文档链接 | 服务名称 | 核心功能 |
|---------|---------|---------|
| [shop-service.md](./shop-service.md) | 店铺服务 | 店铺信息管理、桌台状态管理、扫码点餐 |
| [menu-service.md](./menu-service.md) | 菜单服务 | 菜单分类、菜品CRUD、规格配料、评价管理 |
| [order-service.md](./order-service.md) | 订单服务 | 订单创建、状态流转、订单统计 |
| [payment-service.md](./payment-service.md) | 支付服务 | 多渠道支付、退款管理、支付回调 |
| [queue-service.md](./queue-service.md) | 排队服务 | 排队取号、叫号管理、Redis队列 |
| [notification-service.md](./notification-service.md) | 通知服务 | 消息模板、多渠道通知、WebSocket推送 |

---

## 🚀 快速开始

### 1. 初始化数据库

**数据库连接信息**:
- 主机: localhost
- 端口: 3306
- 用户名: root
- 密码: 123456

**执行SQL文件**:

```bash
# Windows PowerShell
cd C:\Users\lenovo\IdeaProjects\springcloud\CateringOrderingAndQueuingSystem

# 按顺序执行所有SQL文件
Get-Content sql\shop-service.sql | mysql -u root -p123456 -h localhost -P 3306
Get-Content sql\menu-service.sql | mysql -u root -p123456 -h localhost -P 3306
Get-Content sql\order-service.sql | mysql -u root -p123456 -h localhost -P 3306
Get-Content sql\payment-service.sql | mysql -u root -p123456 -h localhost -P 3306
Get-Content sql\queue-service.sql | mysql -u root -p123456 -h localhost -P 3306
Get-Content sql\notification-service.sql | mysql -u root -p123456 -h localhost -P 3306
```

或者使用MySQL命令行：

```bash
# 方式1: 直接执行SQL文件
mysql -u root -p123456 -h localhost -P 3306 < sql/shop-service.sql
mysql -u root -p123456 -h localhost -P 3306 < sql/menu-service.sql
mysql -u root -p123456 -h localhost -P 3306 < sql/order-service.sql
mysql -u root -p123456 -h localhost -P 3306 < sql/payment-service.sql
mysql -u root -p123456 -h localhost -P 3306 < sql/queue-service.sql
mysql -u root -p123456 -h localhost -P 3306 < sql/notification-service.sql

# 方式2: 交互式执行（推荐）
mysql -u root -p123456 -h localhost -P 3306

# 然后在MySQL命令行中依次执行：
source C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/shop-service.sql;
source C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/menu-service.sql;
source C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/order-service.sql;
source C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/payment-service.sql;
source C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/queue-service.sql;
source C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/notification-service.sql;
```

**验证数据库是否创建成功**:

```sql
-- 登录MySQL后执行
SHOW DATABASES LIKE '%service%';

-- 应该看到以下6个数据库：
-- shop_service
-- menu_service
-- order_service
-- payment_service
-- queue_service
-- notification_service

-- 查看某个数据库的表
USE shop_service;
SHOW TABLES;
```

### 2. 配置Redis

确保Redis已启动，用于排队队列和WebSocket会话管理。

### 3. 配置消息队列

启动RabbitMQ或RocketMQ，配置订单推送队列。

### 4. 启动微服务

使用Docker Compose一键启动所有服务：
```bash
docker-compose up -d
```

---

## 📊 数据库概览

### shop_service (店铺服务)
- ✅ shop_info - 店铺信息表
- ✅ table_info - 桌台信息表
- ✅ table_usage_log - 桌台使用记录表
- ✅ shop_config - 店铺配置表

### menu_service (菜单服务)
- ✅ menu_category - 菜单分类表
- ✅ menu_item - 菜品信息表
- ✅ item_specification - 菜品规格表
- ✅ item_topping - 菜品配料表
- ✅ item_review - 菜品评价表
- ✅ menu_change_log - 菜单上下架记录表

### order_service (订单服务)
- ✅ orders - 订单主表
- ✅ order_item - 订单明细表
- ✅ order_status_log - 订单状态流转记录表
- ✅ order_operation_log - 订单操作日志表
- ✅ order_evaluation - 订单评价表
- ✅ order_statistics - 订单统计表

### payment_service (支付服务)
- ✅ payment_order - 支付订单表
- ✅ payment_transaction - 支付流水表
- ✅ refund_record - 退款记录表
- ✅ payment_config - 支付配置表
- ✅ payment_callback_log - 支付回调日志表

### queue_service (排队服务)
- ✅ queue_number - 排队号码表
- ✅ call_record - 叫号记录表
- ✅ queue_config - 排队配置表
- ✅ queue_statistics - 排队统计表
- ✅ queue_operation_log - 排队操作日志表
- ✅ queue_blacklist - 黑名单表

### notification_service (通知服务)
- ✅ message_template - 消息模板表
- ✅ message_send_log - 消息发送记录表
- ✅ websocket_session - WebSocket连接会话表
- ✅ notification_config - 通知配置表
- ✅ message_statistics - 消息统计表

---

## 🎯 核心业务流程

### 1. 扫码点餐流程
```
顾客扫码 → 查看菜单(menu-service) → 选择菜品 → 创建订单(order-service) 
→ 支付(payment-service) → 推送后厨(MQ) → 制作 → 完成
```

### 2. 排队取号流程
```
顾客取号 → Redis队列(queue-service) → 等待 → 叫号 
→ WebSocket推送(notification-service) → 入座 → 关联订单
```

### 3. 消息通知流程
```
业务事件 → 查询模板(notification-service) → 选择渠道 
→ 发送消息 → 记录日志 → 失败重试
```

---

## 🔧 技术栈

- **微服务框架**: Spring Cloud Alibaba
- **服务注册发现**: Nacos
- **网关**: Spring Cloud Gateway
- **熔断限流**: Sentinel
- **消息队列**: RabbitMQ / RocketMQ
- **缓存**: Redis
- **数据库**: MySQL 8.0+
- **实时通信**: WebSocket / SSE
- **容器化**: Docker + Docker Compose
- **负载均衡**: Nginx / Gateway

---

## 📌 注意事项

1. **字符集**: 所有数据库使用utf8mb4字符集
2. **时间字段**: 使用DATETIME类型
3. **金额字段**: 使用DECIMAL类型，避免精度问题
4. **索引优化**: 为常用查询字段建立索引
5. **外键关系**: 未设置物理外键，应用层维护一致性
6. **示例数据**: 每个表都包含示例数据，便于测试

---

## 📞 相关资源

- [项目总览](./README.md)
- [技术架构设计]() (待补充)
- [API接口文档]() (待补充)
- [部署指南]() (待补充)

---

**最后更新**: 2026-05-17  
**版本**: v1.0.0
