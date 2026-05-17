# 餐饮点单排队系统 - 数据库设计总览

## 项目概述

**项目名称**: 餐饮点单排队系统（店内扫码点餐）  
**架构模式**: 微服务架构  
**技术栈**: Spring Cloud + Nacos + Gateway + Sentinel + RabbitMQ/RocketMQ + WebSocket + Redis + Docker Compose

---

## 微服务划分

本系统共划分为6个微服务，每个服务拥有独立的数据库：

| 序号 | 服务名称 | 数据库名 | 功能描述 | SQL文件 | 说明文档 |
|------|---------|---------|---------|---------|---------|
| 1 | shop-service | shop_service | 店铺信息、桌号管理 | shop-service.sql | shop-service.md |
| 2 | menu-service | menu_service | 菜单分类、菜品管理 | menu-service.sql | menu-service.md |
| 3 | order-service | order_service | 订单生命周期管理 | order-service.sql | order-service.md |
| 4 | payment-service | payment_service | 支付记录管理 | payment-service.sql | payment-service.md |
| 5 | queue-service | queue_service | 排队取号逻辑 | queue-service.sql | queue-service.md |
| 6 | notification-service | notification_service | 消息推送、通知 | notification-service.sql | notification-service.md |

---

## 数据库清单

### 1. shop_service（店铺服务数据库）

**表数量**: 4张表

| 表名 | 中文名 | 主要功能 | 关键字段 |
|------|--------|---------|---------|
| shop_info | 店铺信息表 | 存储店铺基本信息 | shop_name, shop_code, shop_status |
| table_info | 桌台信息表 | 管理店铺桌台 | table_number, seats, table_type, table_status |
| table_usage_log | 桌台使用记录表 | 记录桌台使用历史 | start_time, end_time, party_size |
| shop_config | 店铺配置表 | 店铺动态配置 | config_key, config_value |

**核心业务**:
- 店铺信息管理
- 桌台状态管理（空闲、占用、预订、清洁中）
- 扫码点餐二维码生成
- 桌台使用率统计

---

### 2. menu_service（菜单服务数据库）

**表数量**: 6张表

| 表名 | 中文名 | 主要功能 | 关键字段 |
|------|--------|---------|---------|
| menu_category | 菜单分类表 | 菜品分类管理 | category_name, parent_id, sort_order |
| menu_item | 菜品信息表 | 菜品详细信息 | item_name, price, stock, rating |
| item_specification | 菜品规格表 | 多规格支持 | spec_name, spec_type, price_adjustment |
| item_topping | 菜品配料表 | 可选加料 | topping_name, price, is_required |
| item_review | 菜品评价表 | 顾客评价 | rating, comment, like_count |
| menu_change_log | 菜单上下架记录表 | 变更记录 | change_type, old_value, new_value |

**核心业务**:
- 菜单分类管理（支持多级分类）
- 菜品CRUD操作
- 多规格配置（大小份、辣度、温度等）
- 可选配料（加蛋、加面等）
- 菜品评价管理
- 菜单上下架记录

---

### 3. order_service（订单服务数据库）

**表数量**: 6张表

| 表名 | 中文名 | 主要功能 | 关键字段 |
|------|--------|---------|---------|
| orders | 订单主表 | 订单核心信息 | order_no, order_status, total_amount |
| order_item | 订单明细表 | 订单菜品明细 | item_name, price, quantity, specification |
| order_status_log | 订单状态流转记录表 | 状态变更历史 | old_status, new_status, operator_type |
| order_operation_log | 订单操作日志表 | 操作记录 | operation_type, request_data, response_data |
| order_evaluation | 订单评价表 | 订单整体评价 | overall_rating, taste_rating, service_rating |
| order_statistics | 订单统计表 | 每日统计 | total_orders, total_amount, peak_hour |

**核心业务**:
- 订单创建与管理
- 订单状态流转：待支付→待接单→制作中→待取餐→已完成
- 订单明细管理（含规格、配料）
- 订单状态追踪
- 订单评价
- 订单统计分析

**订单状态机**:
```
待支付(0) → 待接单(1) → 制作中(2) → 待取餐(3) → 已完成(4)
     ↓              ↓
  已取消(5)      已取消(5)
```

---

### 4. payment_service（支付服务数据库）

**表数量**: 5张表

| 表名 | 中文名 | 主要功能 | 关键字段 |
|------|--------|---------|---------|
| payment_order | 支付订单表 | 支付核心信息 | payment_no, payment_status, transaction_id |
| payment_transaction | 支付流水表 | 交易详细记录 | transaction_no, request_data, response_data |
| refund_record | 退款记录表 | 退款管理 | refund_no, refund_status, refund_reason |
| payment_config | 支付配置表 | 支付方式配置 | payment_method, app_id, merchant_id |
| payment_callback_log | 支付回调日志表 | 回调记录 | callback_type, signature_valid, process_status |

**核心业务**:
- 支付订单管理
- 多渠道支付支持（微信、支付宝、现金、会员卡、银行卡）
- 支付流水记录
- 退款流程管理
- 支付回调处理
- 支付配置管理

**支持的支付方式**:
1. 微信支付
2. 支付宝
3. 现金
4. 会员卡
5. 银行卡

---

### 5. queue_service（排队服务数据库）

**表数量**: 6张表

| 表名 | 中文名 | 主要功能 | 关键字段 |
|------|--------|---------|---------|
| queue_number | 排队号码表 | 排队核心信息 | queue_no, queue_status, current_position |
| call_record | 叫号记录表 | 叫号历史 | call_type, call_method, is_answered |
| queue_config | 排队配置表 | 排队规则配置 | config_key, config_value |
| queue_statistics | 排队统计表 | 每日统计 | total_queue_count, avg_wait_time, peak_hour |
| queue_operation_log | 排队操作日志表 | 操作记录 | operation_type, old_status, new_status |
| queue_blacklist | 黑名单表 | 防作弊 | blacklist_type, reason, expire_time |

**核心业务**:
- 排队取号
- 实时排队位置查询
- 叫号管理（手动/自动）
- 过号处理
- 排队统计分析
- 黑名单管理（防恶意取号）

**排队状态机**:
```
等待中(0) → 已叫号(1) → 已入座(2)
     ↓           ↓
  已取消(3)   已过号(4)
```

**Redis数据结构** (核心):
- Sorted Set: `queue:{shop_id}:waiting` - 等待队列
- Sorted Set: `queue:{shop_id}:called` - 已叫号队列
- String: `queue:{shop_id}:daily_count:{date}` - 今日号码计数
- Hash: `queue:{shop_id}:position` - 排队位置缓存

---

### 6. notification_service（通知服务数据库）

**表数量**: 5张表

| 表名 | 中文名 | 主要功能 | 关键字段 |
|------|--------|---------|---------|
| message_template | 消息模板表 | 消息模板管理 | template_code, content, variables |
| message_send_log | 消息发送记录表 | 发送历史 | message_id, send_status, retry_count |
| websocket_session | WebSocket连接会话表 | 长连接管理 | session_id, user_id, status |
| notification_config | 通知配置表 | 渠道配置 | config_key, config_value |
| message_statistics | 消息统计表 | 发送统计 | total_sent, success_rate, total_cost |

**核心业务**:
- 消息模板管理
- 多渠道消息发送（短信、微信、APP推送、邮件、语音）
- WebSocket实时推送
- 消息重试机制
- 通知统计分析

**WebSocket频道设计**:
- `order:{order_no}` - 订单状态频道
- `queue:{shop_id}` - 排队叫号频道
- `shop:{shop_id}:notice` - 店铺公告频道
- `kitchen:{shop_id}` - 后厨订单频道

---

## 数据库关系图

```
┌─────────────────┐
│  shop_service   │
│                 │
│  shop_info      │◄──────┐
│  table_info     │       │
└────────┬────────┘       │
         │                │
         ├────────────────┼────────┐
         │                │        │
         ▼                ▼        ▼
┌─────────────────┐ ┌──────────────┐ ┌─────────────────┐
│  menu_service   │ │order_service │ │ payment_service │
│                 │ │              │ │                 │
│  menu_category  │ │  orders      │ │ payment_order   │
│  menu_item      │ │  order_item  │ │ payment_trans.  │
└────────┬────────┘ └──────┬───────┘ └────────┬────────┘
         │                  │                   │
         │                  │                   │
         └──────────────────┼───────────────────┘
                            │
                            ▼
                  ┌─────────────────┐
                  │  queue_service  │
                  │                 │
                  │  queue_number   │
                  │  call_record    │
                  └────────┬────────┘
                           │
                           │
                           ▼
                  ┌──────────────────────┐
                  │notification_service  │
                  │                      │
                  │  message_send_log    │
                  │  websocket_session   │
                  └──────────────────────┘
```

---

## 核心技术亮点

### 1. 异步处理场景
- **下单后推送后厨**: 通过消息队列（RabbitMQ/RocketMQ）解耦订单服务和后厨系统
- **消息通知**: 异步发送短信、微信、推送通知
- **数据统计**: 异步聚合统计数据到统计表

### 2. 实时排队和叫号
- **Redis Sorted Set**: 实现高效排队队列
- **WebSocket/SSE**: 前后端实时通信，推送叫号通知
- **自动叫号**: 基于配置的定时任务自动叫号

### 3. 服务熔断降级
- **Sentinel**: 实现服务限流、熔断、降级
- **支付服务故障**: 订单服务降级提示"稍后支付"
- **超时控制**: 设置合理的超时时间，避免长时间等待

### 4. 容器化部署
- **Docker Compose**: 一键部署所有微服务
- **多实例**: order-service模拟多实例并发处理
- **负载均衡**: Nginx或Gateway实现负载均衡

---

## 数据一致性保障

### 1. 最终一致性
- 使用本地消息表 + 定时任务
- 或使用事务性消息（RocketMQ）
- 保证跨服务数据最终一致

### 2. 分布式事务
- Seata AT模式（可选）
- TCC模式（复杂场景）
- Saga模式（长事务）

### 3. 幂等性设计
- 唯一索引防止重复插入
- Token机制防止重复提交
- 业务ID作为幂等键

---

## 性能优化建议

### 1. 索引优化
- 为常用查询字段建立索引
- 避免过多索引影响写入性能
- 定期分析慢查询

### 2. 读写分离
- 主库负责写操作
- 从库负责读操作
- MySQL主从复制

### 3. 缓存策略
- Redis缓存热点数据
- 本地缓存（Caffeine）减少Redis访问
- 缓存穿透、击穿、雪崩防护

### 4. 分库分表
- 订单表按月份分表
- 日志表按日期分表
- ShardingSphere实现

---

## 安全建议

### 1. 数据安全
- 敏感字段加密存储（API密钥、私钥等）
- SQL注入防护（使用预编译语句）
- XSS防护

### 2. 访问控制
- API鉴权（JWT）
- 接口限流
- IP白名单

### 3. 审计日志
- 记录关键操作日志
- 保留足够的历史数据
- 定期备份数据库

---

## 数据库初始化步骤

### 1. 数据库连接信息

- **主机**: localhost
- **端口**: 3306
- **用户名**: root
- **密码**: 123456

### 2. 执行SQL文件

**方式一：Windows PowerShell（推荐）**

```powershell
# 进入项目目录
cd C:\Users\lenovo\IdeaProjects\springcloud\CateringOrderingAndQueuingSystem

# 按顺序执行所有SQL文件
Get-Content sql\shop-service.sql | mysql -u root -p123456 -h localhost -P 3306
Get-Content sql\menu-service.sql | mysql -u root -p123456 -h localhost -P 3306
Get-Content sql\order-service.sql | mysql -u root -p123456 -h localhost -P 3306
Get-Content sql\payment-service.sql | mysql -u root -p123456 -h localhost -P 3306
Get-Content sql\queue-service.sql | mysql -u root -p123456 -h localhost -P 3306
Get-Content sql\notification-service.sql | mysql -u root -p123456 -h localhost -P 3306
```

**方式二：MySQL命令行**

```bash
# 直接执行SQL文件
mysql -u root -p123456 -h localhost -P 3306 < sql/shop-service.sql
mysql -u root -p123456 -h localhost -P 3306 < sql/menu-service.sql
mysql -u root -p123456 -h localhost -P 3306 < sql/order-service.sql
mysql -u root -p123456 -h localhost -P 3306 < sql/payment-service.sql
mysql -u root -p123456 -h localhost -P 3306 < sql/queue-service.sql
mysql -u root -p123456 -h localhost -P 3306 < sql/notification-service.sql
```

**方式三：交互式执行**

```bash
# 登录MySQL
mysql -u root -p123456 -h localhost -P 3306

# 在MySQL命令行中依次执行
source C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/shop-service.sql;
source C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/menu-service.sql;
source C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/order-service.sql;
source C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/payment-service.sql;
source C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/queue-service.sql;
source C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/sql/notification-service.sql;
```

### 3. 验证数据

检查每个数据库的表结构和示例数据是否正确导入。

```sql
-- 查看所有数据库
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

-- 查看表示例数据
SELECT * FROM shop_info LIMIT 5;
SELECT * FROM table_info LIMIT 5;
```

### 4. 配置Redis

启动Redis并配置持久化策略。

```bash
# Windows下启动Redis
redis-server.exe

# 验证Redis是否启动成功
redis-cli ping
# 应该返回: PONG
```

### 5. 配置消息队列

启动RabbitMQ或RocketMQ，创建必要的队列和交换机。

**RabbitMQ示例**:
```bash
# 启动RabbitMQ
rabbitmq-server

# 访问管理界面
# http://localhost:15672
# 默认用户名/密码: guest/guest
```

---

## 扩展与维护

### 1. 监控告警
- Prometheus + Grafana监控数据库性能
- 慢查询告警
- 连接数监控

### 2. 备份策略
- 每日全量备份
- binlog增量备份
- 定期恢复测试

### 3. 版本管理
- SQL脚本纳入版本控制
- 使用Flyway或Liquibase管理数据库迁移
- 保持开发、测试、生产环境一致

---

## 总结

本系统采用微服务架构，将业务拆分为6个独立的服务，每个服务拥有独立的数据库，实现了：

✅ **高内聚低耦合**: 各服务职责明确，互不干扰  
✅ **可扩展性**: 可独立扩展某个服务  
✅ **高可用性**: 单点故障不影响整体系统  
✅ **技术先进性**: 使用主流微服务技术栈  
✅ **业务完整性**: 覆盖点餐、支付、排队全流程  

数据库设计遵循规范化原则，同时考虑性能优化，为系统的稳定运行提供坚实基础。
