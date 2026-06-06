# 排队服务数据库说明 (queue-service)

## 数据库概述

**数据库名称**: queue_service  
**功能描述**: 管理排队取号、叫号、过号等业务流程  
**字符集**: utf8mb4  
**排序规则**: utf8mb4_unicode_ci

---

## 数据表清单

### 1. queue_number - 排队号码表

**功能**: 存储排队号码的核心信息，包括当前状态、位置等。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 排队ID | 主键，自增 |
| queue_no | VARCHAR(50) | 排队号码 | 唯一标识，格式：A001、B002 |
| shop_id | BIGINT(20) | 店铺ID | 关联shop_info.id，不能为空 |
| user_id | BIGINT(20) | 用户ID | 可选 |
| phone | VARCHAR(20) | 联系电话 | 可选 |
| party_size | INT(11) | 用餐人数 | 默认1 |
| queue_type | TINYINT(1) | 排队类型 | 1-堂食，2-外带，默认1 |
| table_type | TINYINT(1) | 期望桌台类型 | 1-普通桌，2-卡座，3-包厢，NULL-不限制 |
| queue_status | TINYINT(1) | 排队状态 | 0-等待中，1-已叫号，2-已入座，3-已取消，4-已过号 |
| current_position | INT(11) | 当前排队位置 | 可选 |
| total_ahead | INT(11) | 前方等待人数 | 可选 |
| estimated_wait_time | INT(11) | 预计等待时间（分钟） | 可选 |
| call_time | DATETIME | 叫号时间 | 可选 |
| seat_time | DATETIME | 入座时间 | 可选 |
| cancel_time | DATETIME | 取消时间 | 可选 |
| cancel_reason | VARCHAR(255) | 取消原因 | 可选 |
| expire_time | DATETIME | 过期时间 | 可选 |
| is_notified | TINYINT(1) | 是否已通知 | 0-否，1-是，默认0 |
| notify_count | INT(11) | 通知次数 | 默认0 |
| last_notify_time | DATETIME | 最后通知时间 | 可选 |
| remark | VARCHAR(255) | 备注 | 可选 |
| created_at | DATETIME | 创建时间（取号时间） | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_queue_no` (`shop_id`, `queue_no`)
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_user_id` (`user_id`)
- KEY: `idx_queue_status` (`queue_status`)
- KEY: `idx_phone` (`phone`)
- KEY: `idx_created_at` (`created_at`)

**排队状态流转**:
```
等待中(0) → 已叫号(1) → 已入座(2)
     ↓           ↓
  已取消(3)   已过号(4)
```

**示例数据**:
- A001: 已入座，3人，普通桌
- A002: 已叫号，2人，前方0人，预计5分钟
- A003: 等待中，4人，前方1人，预计10分钟
- A004: 等待中，2人，前方2人，预计15分钟
- B001: 等待中，5人，前方3人，预计20分钟（卡座）

---

### 2. call_record - 叫号记录表

**功能**: 记录每次叫号的详细信息。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 记录ID | 主键，自增 |
| queue_id | BIGINT(20) | 排队ID | 关联queue_number.id，不能为空 |
| queue_no | VARCHAR(50) | 排队号码 | 不能为空 |
| shop_id | BIGINT(20) | 店铺ID | 不能为空 |
| call_type | TINYINT(1) | 叫号类型 | 1-首次叫号，2-重复叫号，3-过号重呼，默认1 |
| call_method | TINYINT(1) | 叫号方式 | 1-语音播报，2-短信通知，3-微信推送，4-APP推送，默认1 |
| call_count | INT(11) | 叫号次数 | 默认1 |
| operator_id | BIGINT(20) | 操作人ID（店员） | 可选 |
| operator_name | VARCHAR(50) | 操作人姓名 | 可选 |
| is_answered | TINYINT(1) | 是否应答 | 0-未应答，1-已应答，默认0 |
| answer_time | DATETIME | 应答时间 | 可选 |
| timeout_duration | INT(11) | 超时时长（秒） | 默认300（5分钟） |
| is_expired | TINYINT(1) | 是否已过期 | 0-否，1-是，默认0 |
| expire_time | DATETIME | 过期时间 | 可选 |
| remark | VARCHAR(255) | 备注 | 可选 |
| created_at | DATETIME | 创建时间（叫号时间） | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_queue_id` (`queue_id`)
- KEY: `idx_queue_no` (`queue_no`)
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_created_at` (`created_at`)

**示例数据**:
- A001: 首次叫号，语音播报，已应答
- A002: 首次叫号，语音播报，未应答

---

### 3. queue_config - 排队配置表

**功能**: 存储排队系统的各种配置项。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 配置ID | 主键，自增 |
| shop_id | BIGINT(20) | 店铺ID | 关联shop_info.id，不能为空 |
| config_key | VARCHAR(100) | 配置键名 | 不能为空 |
| config_value | TEXT | 配置值 | 可选 |
| config_desc | VARCHAR(255) | 配置说明 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_shop_config` (`shop_id`, `config_key`)

**示例配置**:
- queue_enabled: 是否启用排队功能
- max_queue_size: 最大排队数量（50）
- auto_call_enabled: 是否自动叫号
- auto_call_interval: 自动叫号间隔（300秒）
- call_timeout: 叫号超时时间（300秒）
- max_reservations: 最多可预约次数（3）
- notify_sms_enabled: 是否启用短信通知
- notify_wechat_enabled: 是否启用微信通知
- notify_advance_time: 提前通知时间（10分钟）
- queue_prefix_A: A类队列前缀（普通桌）
- queue_prefix_B: B类队列前缀（卡座）
- queue_prefix_V: V类队列前缀（包厢）

---

### 4. queue_statistics - 排队统计表

**功能**: 按日统计排队数据，用于分析排队情况。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 统计ID | 主键，自增 |
| shop_id | BIGINT(20) | 店铺ID | 不能为空 |
| stat_date | DATE | 统计日期 | 不能为空 |
| total_queue_count | INT(11) | 总排队数 | 默认0 |
| served_count | INT(11) | 已服务数 | 默认0 |
| cancelled_count | INT(11) | 取消数 | 默认0 |
| expired_count | INT(11) | 过号数 | 默认0 |
| avg_wait_time | INT(11) | 平均等待时间（分钟） | 可选 |
| max_wait_time | INT(11) | 最长等待时间（分钟） | 可选 |
| min_wait_time | INT(11) | 最短等待时间（分钟） | 可选 |
| peak_hour | VARCHAR(20) | 高峰时段 | 如：12:00-13:00 |
| peak_queue_count | INT(11) | 高峰排队数 | 可选 |
| avg_party_size | DECIMAL(5,2) | 平均用餐人数 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_shop_date` (`shop_id`, `stat_date`)
- KEY: `idx_stat_date` (`stat_date`)

**示例数据**:
- 2026-05-16: 85个排队，服务78个，取消5个，过号2个，平均等待18分钟
- 2026-05-17: 42个排队，服务35个，取消3个，过号1个，平均等待16分钟

---

### 5. queue_operation_log - 排队操作日志表

**功能**: 记录排队相关的所有操作。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 日志ID | 主键，自增 |
| queue_id | BIGINT(20) | 排队ID | 可选 |
| queue_no | VARCHAR(50) | 排队号码 | 可选 |
| shop_id | BIGINT(20) | 店铺ID | 不能为空 |
| operation_type | VARCHAR(50) | 操作类型 | TAKE_NUMBER-取号，CALL_NUMBER-叫号，CANCEL_NUMBER-取消，SEAT_NUMBER-入座，SKIP_NUMBER-过号 |
| operation_desc | VARCHAR(255) | 操作描述 | 可选 |
| old_status | TINYINT(1) | 原状态 | 可选 |
| new_status | TINYINT(1) | 新状态 | 可选 |
| request_data | TEXT | 请求数据 | JSON格式 |
| response_data | TEXT | 响应数据 | JSON格式 |
| operator_type | TINYINT(1) | 操作人类型 | 1-系统，2-用户，3-店员 |
| operator_id | BIGINT(20) | 操作人ID | 可选 |
| operator_name | VARCHAR(50) | 操作人姓名 | 可选 |
| ip_address | VARCHAR(50) | IP地址 | 可选 |
| duration | INT(11) | 操作耗时（毫秒） | 可选 |
| result | TINYINT(1) | 操作结果 | 0-失败，1-成功，默认1 |
| error_msg | TEXT | 错误信息 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_queue_id` (`queue_id`)
- KEY: `idx_queue_no` (`queue_no`)
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_operation_type` (`operation_type`)
- KEY: `idx_created_at` (`created_at`)

**示例数据**:
- A001: 用户取号 → 店员叫号 → 用户入座
- A002: 用户取号 → 店员叫号

---

### 6. queue_blacklist - 黑名单表

**功能**: 防止恶意取号，记录违规用户。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 黑名单ID | 主键，自增 |
| shop_id | BIGINT(20) | 店铺ID | 不能为空 |
| user_id | BIGINT(20) | 用户ID | 可选 |
| phone | VARCHAR(20) | 电话号码 | 可选 |
| ip_address | VARCHAR(50) | IP地址 | 可选 |
| device_id | VARCHAR(100) | 设备ID | 可选 |
| blacklist_type | TINYINT(1) | 黑名单类型 | 1-用户，2-电话，3-IP，4-设备 |
| reason | VARCHAR(255) | 拉黑原因 | 不能为空 |
| violation_count | INT(11) | 违规次数 | 默认1 |
| expire_time | DATETIME | 过期时间 | NULL表示永久 |
| status | TINYINT(1) | 状态 | 0-已解除，1-生效中，默认1 |
| operator_id | BIGINT(20) | 操作人ID | 可选 |
| operator_name | VARCHAR(50) | 操作人姓名 | 可选 |
| remark | VARCHAR(500) | 备注 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_user_id` (`user_id`)
- KEY: `idx_phone` (`phone`)
- KEY: `idx_ip_address` (`ip_address`)
- KEY: `idx_status` (`status`)

---

## 业务场景

### 1. 取号流程
1. 顾客扫码或现场取号
2. 输入用餐人数
3. 选择期望桌台类型（可选）
4. 生成排队号码
5. 显示当前位置和预计等待时间
6. 发送通知（短信/微信）

### 2. 叫号流程
1. 店员手动叫号或系统自动叫号
2. 语音播报号码
3. 发送通知给顾客
4. 记录叫号时间
5. 启动超时计时器

### 3. 入座流程
1. 顾客到达餐厅
2. 店员确认号码
3. 引导顾客入座
4. 更新排队状态为"已入座"
5. 关联订单和桌台

### 4. 过号处理
1. 叫号后超过设定时间未应答
2. 标记为"已过号"
3. 可选择重新排队或人工处理

### 5. 实时排队查询
- 使用Redis Sorted Set实现实时排队
- key格式: `queue:{shop_id}:waiting`
- score: 取号时间戳
- member: queue_no
- 前端通过WebSocket接收实时更新

---

## Redis数据结构设计

### 1. 排队队列（Sorted Set）
```
key: queue:{shop_id}:waiting
score: 取号时间戳
member: queue_no
```

### 2. 叫号队列（Sorted Set）
```
key: queue:{shop_id}:called
score: 叫号时间戳
member: queue_no
```

### 3. 今日号码计数器（String）
```
key: queue:{shop_id}:daily_count:{date}
value: 当前号码数
```

### 4. 排队位置缓存（Hash）
```
key: queue:{shop_id}:position
field: queue_no
value: 当前位置
```

---

## 注意事项

1. **Redis依赖**: 
   - 排队核心逻辑依赖Redis实现
   - 数据库作为持久化存储
   - 需要保证Redis高可用

2. **号码生成**: 
   - 按日期重置号码计数
   - 不同桌台类型使用不同前缀
   - 避免号码冲突

3. **并发控制**: 
   - 取号时需要原子操作
   - 使用Redis Lua脚本保证原子性

4. **通知机制**: 
   - 支持多种通知方式
   - 提前通知即将叫号
   - 避免频繁打扰顾客

5. **过号处理**: 
   - 设置合理的超时时间
   - 提供过号重排机制
   - 记录过号原因

6. **防作弊**: 
   - 限制同一用户/手机号/设备的取号频率
   - 黑名单机制
   - IP限流

---

## 扩展建议

1. 可以添加预约排队表，支持提前预约
2. 可以添加VIP优先排队功能
3. 可以添加拼桌功能，提高座位利用率
4. 可以添加排队转订单功能，一键下单
5. 可以添加排队数据分析，优化服务质量
6. 可以添加智能预估算法，更准确预测等待时间
