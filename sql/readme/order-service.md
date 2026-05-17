# 订单服务数据库说明 (order-service)

## 数据库概述

**数据库名称**: order_service  
**功能描述**: 管理订单生命周期（待支付→待接单→制作中→待取餐→已完成）  
**字符集**: utf8mb4  
**排序规则**: utf8mb4_unicode_ci

---

## 数据表清单

### 1. orders - 订单主表

**功能**: 存储订单的核心信息，包括订单状态、金额、时间等。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 订单ID | 主键，自增 |
| order_no | VARCHAR(50) | 订单编号 | 唯一标识，格式：ORD+年月日+流水号 |
| shop_id | BIGINT(20) | 店铺ID | 关联shop_info.id，不能为空 |
| table_id | BIGINT(20) | 桌台ID | 关联table_info.id |
| table_number | VARCHAR(20) | 桌台编号 | 冗余字段便于查询 |
| user_id | BIGINT(20) | 用户ID | 可选 |
| order_type | TINYINT(1) | 订单类型 | 1-堂食，2-外带，3-外卖，默认1 |
| order_status | TINYINT(1) | 订单状态 | 0-待支付，1-待接单，2-制作中，3-待取餐，4-已完成，5-已取消，6-退款中，7-已退款 |
| total_amount | DECIMAL(10,2) | 订单总金额（元） | 不能为空 |
| discount_amount | DECIMAL(10,2) | 优惠金额（元） | 默认0.00 |
| actual_amount | DECIMAL(10,2) | 实付金额（元） | 不能为空 |
| item_count | INT(11) | 菜品总数量 | 默认0 |
| remark | VARCHAR(500) | 订单备注 | 如：少辣、不要香菜 |
| payment_method | TINYINT(1) | 支付方式 | 1-微信支付，2-支付宝，3-现金，4-会员卡 |
| payment_status | TINYINT(1) | 支付状态 | 0-未支付，1-已支付，2-支付失败，3-退款中，4-已退款 |
| payment_time | DATETIME | 支付时间 | 可选 |
| accept_time | DATETIME | 接单时间 | 可选 |
| prepare_time | DATETIME | 开始制作时间 | 可选 |
| ready_time | DATETIME | 制作完成时间 | 可选 |
| complete_time | DATETIME | 订单完成时间 | 可选 |
| cancel_time | DATETIME | 取消时间 | 可选 |
| cancel_reason | VARCHAR(255) | 取消原因 | 可选 |
| queue_number | VARCHAR(20) | 排队号码 | 如：A001 |
| estimated_time | INT(11) | 预计等待时间（分钟） | 可选 |
| priority | TINYINT(1) | 优先级 | 0-普通，1-加急，默认0 |
| is_evaluated | TINYINT(1) | 是否已评价 | 0-否，1-是，默认0 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_order_no` (`order_no`)
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_table_id` (`table_id`)
- KEY: `idx_user_id` (`user_id`)
- KEY: `idx_order_status` (`order_status`)
- KEY: `idx_created_at` (`created_at`)

**订单状态流转**:
```
待支付(0) → 待接单(1) → 制作中(2) → 待取餐(3) → 已完成(4)
               ↓              ↓
            已取消(5)      已取消(5)
```

**示例数据**:
- ORD2026051700001: 已完成订单，98元，3个菜品
- ORD2026051700002: 制作中订单，120元，4个菜品
- ORD2026051700003: 待接单订单，68元，2个菜品

---

### 2. order_item - 订单明细表

**功能**: 存储订单中的每个菜品明细，包括规格、配料等。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 明细ID | 主键，自增 |
| order_id | BIGINT(20) | 订单ID | 关联orders.id，不能为空 |
| order_no | VARCHAR(50) | 订单编号 | 冗余字段 |
| item_id | BIGINT(20) | 菜品ID | 关联menu_item.id，不能为空 |
| item_name | VARCHAR(100) | 菜品名称 | 冗余字段 |
| item_code | VARCHAR(50) | 菜品编码 | 冗余字段 |
| image_url | VARCHAR(255) | 菜品图片URL | 冗余字段 |
| price | DECIMAL(10,2) | 菜品单价（元） | 不能为空 |
| quantity | INT(11) | 购买数量 | 默认1 |
| subtotal | DECIMAL(10,2) | 小计金额（元） | price * quantity |
| specification | VARCHAR(255) | 规格信息 | JSON格式，如：{"size":"大份","spicy":"微辣"} |
| toppings | VARCHAR(500) | 配料信息 | JSON格式，如：[{"name":"加蛋","price":3}] |
| remark | VARCHAR(255) | 单项备注 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_order_id` (`order_id`)
- KEY: `idx_order_no` (`order_no`)
- KEY: `idx_item_id` (`item_id`)

**设计说明**:
- 冗余字段（item_name、item_code等）用于避免联表查询，提高性能
- specification和toppings使用JSON格式存储，灵活支持多种规格和配料

---

### 3. order_status_log - 订单状态流转记录表

**功能**: 记录订单状态的每次变更，便于追踪和审计。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 记录ID | 主键，自增 |
| order_id | BIGINT(20) | 订单ID | 关联orders.id，不能为空 |
| order_no | VARCHAR(50) | 订单编号 | 冗余字段 |
| old_status | TINYINT(1) | 原状态 | 可选 |
| new_status | TINYINT(1) | 新状态 | 不能为空 |
| operator_type | TINYINT(1) | 操作人类型 | 1-系统，2-用户，3-店员，4-后厨 |
| operator_id | BIGINT(20) | 操作人ID | 可选 |
| operator_name | VARCHAR(50) | 操作人姓名 | 可选 |
| remark | VARCHAR(255) | 状态变更说明 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_order_id` (`order_id`)
- KEY: `idx_order_no` (`order_no`)
- KEY: `idx_created_at` (`created_at`)

**示例数据** (订单ORD2026051700001):
1. NULL → 0: 用户创建订单
2. 0 → 1: 用户完成支付
3. 1 → 2: 后厨开始制作
4. 2 → 3: 后厨制作完成
5. 3 → 4: 服务员确认订单完成

---

### 4. order_operation_log - 订单操作日志表

**功能**: 记录订单相关的所有操作，包括请求和响应数据。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 日志ID | 主键，自增 |
| order_id | BIGINT(20) | 订单ID | 关联orders.id，不能为空 |
| order_no | VARCHAR(50) | 订单编号 | 冗余字段 |
| operation_type | VARCHAR(50) | 操作类型 | 如：CREATE_ORDER、PAY_ORDER、ACCEPT_ORDER |
| operation_desc | VARCHAR(255) | 操作描述 | 可选 |
| request_data | TEXT | 请求数据 | JSON格式 |
| response_data | TEXT | 响应数据 | JSON格式 |
| ip_address | VARCHAR(50) | 操作IP地址 | 可选 |
| user_agent | VARCHAR(500) | 用户代理信息 | 可选 |
| operator_id | BIGINT(20) | 操作人ID | 可选 |
| operator_name | VARCHAR(50) | 操作人姓名 | 可选 |
| duration | INT(11) | 操作耗时（毫秒） | 可选 |
| result | TINYINT(1) | 操作结果 | 0-失败，1-成功，默认1 |
| error_msg | TEXT | 错误信息 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_order_id` (`order_id`)
- KEY: `idx_order_no` (`order_no`)
- KEY: `idx_operation_type` (`operation_type`)
- KEY: `idx_created_at` (`created_at`)

---

### 5. order_evaluation - 订单评价表

**功能**: 存储顾客对订单的整体评价。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 评价ID | 主键，自增 |
| order_id | BIGINT(20) | 订单ID | 关联orders.id，不能为空 |
| order_no | VARCHAR(50) | 订单编号 | 冗余字段 |
| shop_id | BIGINT(20) | 店铺ID | 关联shop_info.id，不能为空 |
| user_id | BIGINT(20) | 用户ID | 可选 |
| overall_rating | TINYINT(1) | 总体评分 | 1-5星，不能为空 |
| taste_rating | TINYINT(1) | 口味评分 | 1-5星 |
| service_rating | TINYINT(1) | 服务评分 | 1-5星 |
| environment_rating | TINYINT(1) | 环境评分 | 1-5星 |
| comment | TEXT | 评价内容 | 可选 |
| images | TEXT | 评价图片URL | 多个用逗号分隔 |
| is_anonymous | TINYINT(1) | 是否匿名 | 0-否，1-是，默认0 |
| reply_content | TEXT | 商家回复内容 | 可选 |
| reply_time | DATETIME | 商家回复时间 | 可选 |
| like_count | INT(11) | 点赞数 | 默认0 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_order_id` (`order_id`)
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_user_id` (`user_id`)
- KEY: `idx_created_at` (`created_at`)

---

### 6. order_statistics - 订单统计表

**功能**: 按日统计订单数据，用于快速查询和分析。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 统计ID | 主键，自增 |
| shop_id | BIGINT(20) | 店铺ID | 关联shop_info.id，不能为空 |
| stat_date | DATE | 统计日期 | 不能为空 |
| total_orders | INT(11) | 总订单数 | 默认0 |
| completed_orders | INT(11) | 完成订单数 | 默认0 |
| cancelled_orders | INT(11) | 取消订单数 | 默认0 |
| total_amount | DECIMAL(12,2) | 总营业额（元） | 默认0.00 |
| avg_order_amount | DECIMAL(10,2) | 平均订单金额（元） | 默认0.00 |
| total_items_sold | INT(11) | 总售出菜品数 | 默认0 |
| peak_hour | VARCHAR(20) | 高峰时段 | 如：12:00-13:00 |
| avg_prepare_time | INT(11) | 平均制作时长（分钟） | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_shop_date` (`shop_id`, `stat_date`)
- KEY: `idx_stat_date` (`stat_date`)

**示例数据**:
- 2026-05-16: 156单，完成148单，营业额15680元，平均100.51元
- 2026-05-17: 89单，完成75单，营业额8920元，平均100.22元

---

## 业务场景

### 1. 订单创建
- 顾客扫码点餐
- 选择菜品、规格、配料
- 添加备注
- 生成订单

### 2. 订单支付
- 调用支付服务
- 更新支付状态
- 发送支付成功通知

### 3. 订单处理
- 后厨接单
- 开始制作
- 制作完成
- 通知顾客取餐

### 4. 订单完成
- 顾客取餐
- 确认订单完成
- 邀请评价

### 5. 订单查询
- 按订单号查询
- 按用户查询历史订单
- 按店铺查询订单列表
- 按状态筛选订单

### 6. 订单统计
- 每日订单量统计
- 营业额统计
- 高峰时段分析
- 菜品销量排行

---

## 注意事项

1. **订单编号**: 使用唯一订单号，格式建议：ORD + YYYYMMDD + 6位流水号
2. **金额计算**: 所有金额使用DECIMAL类型，避免精度问题
3. **状态管理**: 订单状态变更必须记录到order_status_log表
4. **数据冗余**: order_item表冗余菜品信息，避免菜单修改影响历史订单
5. **事务一致性**: 创建订单时需要同时插入orders和order_item表，需保证事务
6. **并发控制**: 高并发场景下需要考虑乐观锁或分布式锁
7. **消息队列**: 订单创建后通过MQ异步通知后厨，实现解耦
8. **定时任务**: 定期统计订单数据到order_statistics表

---

## 扩展建议

1. 可以添加订单优惠券表，记录使用的优惠券信息
2. 可以添加订单积分表，记录积分变动
3. 可以添加订单发票表，支持电子发票
4. 可以添加订单配送表，支持外卖配送
5. 可以添加订单拆分表，支持大订单拆分为多个子订单
6. 可以添加订单异常表，记录处理异常的订单
