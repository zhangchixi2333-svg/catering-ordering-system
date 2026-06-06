# 支付服务数据库说明 (payment-service)

## 数据库概述

**数据库名称**: payment_service  
**功能描述**: 管理支付记录、支付流水、退款等业务  
**字符集**: utf8mb4  
**排序规则**: utf8mb4_unicode_ci

---

## 数据表清单

### 1. payment_order - 支付订单表

**功能**: 存储支付订单的核心信息，关联业务订单。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 支付ID | 主键，自增 |
| payment_no | VARCHAR(50) | 支付单号 | 唯一标识，格式：PAY+年月日+流水号 |
| order_no | VARCHAR(50) | 关联订单编号 | 不能为空，唯一 |
| order_id | BIGINT(20) | 关联订单ID | 不能为空 |
| shop_id | BIGINT(20) | 店铺ID | 关联shop_info.id，不能为空 |
| user_id | BIGINT(20) | 用户ID | 可选 |
| payment_amount | DECIMAL(10,2) | 支付金额（元） | 不能为空 |
| payment_method | TINYINT(1) | 支付方式 | 1-微信支付，2-支付宝，3-现金，4-会员卡，5-银行卡 |
| payment_status | TINYINT(1) | 支付状态 | 0-待支付，1-支付中，2-支付成功，3-支付失败，4-已退款，5-退款中 |
| currency | VARCHAR(10) | 货币类型 | 默认CNY人民币 |
| subject | VARCHAR(255) | 支付主题 | 如：美味餐厅订单支付 |
| body | VARCHAR(500) | 支付描述 | 可选 |
| transaction_id | VARCHAR(100) | 第三方支付交易号 | 微信/支付宝返回的交易号 |
| channel_order_no | VARCHAR(100) | 渠道订单号 | 可选 |
| pay_time | DATETIME | 支付成功时间 | 可选 |
| expire_time | DATETIME | 支付过期时间 | 可选 |
| refund_amount | DECIMAL(10,2) | 退款金额（元） | 默认0.00 |
| refund_time | DATETIME | 退款时间 | 可选 |
| refund_reason | VARCHAR(255) | 退款原因 | 可选 |
| client_ip | VARCHAR(50) | 客户端IP地址 | 可选 |
| device_info | VARCHAR(255) | 设备信息 | 可选 |
| notify_url | VARCHAR(255) | 异步通知地址 | 可选 |
| return_url | VARCHAR(255) | 同步返回地址 | 可选 |
| extra_params | TEXT | 扩展参数 | JSON格式 |
| error_code | VARCHAR(50) | 错误码 | 可选 |
| error_msg | VARCHAR(500) | 错误信息 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_payment_no` (`payment_no`)
- UNIQUE KEY: `uk_order_no` (`order_no`)
- KEY: `idx_order_id` (`order_id`)
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_user_id` (`user_id`)
- KEY: `idx_payment_status` (`payment_status`)
- KEY: `idx_transaction_id` (`transaction_id`)
- KEY: `idx_created_at` (`created_at`)

**示例数据**:
- PAY2026051700001: 微信支付98元，已成功
- PAY2026051700002: 支付宝支付120元，已成功
- PAY2026051700003: 微信支付68元，待支付

---

### 2. payment_transaction - 支付流水表

**功能**: 记录每一笔支付交易的详细信息，包括请求和响应数据。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 流水ID | 主键，自增 |
| transaction_no | VARCHAR(50) | 流水号 | 唯一标识 |
| payment_no | VARCHAR(50) | 支付单号 | 关联payment_order.payment_no |
| order_no | VARCHAR(50) | 订单编号 | 不能为空 |
| shop_id | BIGINT(20) | 店铺ID | 不能为空 |
| user_id | BIGINT(20) | 用户ID | 可选 |
| transaction_type | TINYINT(1) | 交易类型 | 1-支付，2-退款，3-转账 |
| amount | DECIMAL(10,2) | 交易金额（元） | 不能为空 |
| payment_method | TINYINT(1) | 支付方式 | 1-微信支付，2-支付宝，3-现金，4-会员卡，5-银行卡 |
| status | TINYINT(1) | 交易状态 | 0-处理中，1-成功，2-失败 |
| request_data | TEXT | 请求数据 | JSON格式 |
| response_data | TEXT | 响应数据 | JSON格式 |
| third_party_response | TEXT | 第三方返回原始数据 | 可选 |
| error_code | VARCHAR(50) | 错误码 | 可选 |
| error_msg | VARCHAR(500) | 错误信息 | 可选 |
| duration | INT(11) | 交易耗时（毫秒） | 可选 |
| ip_address | VARCHAR(50) | IP地址 | 可选 |
| user_agent | VARCHAR(500) | 用户代理 | 可选 |
| remark | VARCHAR(500) | 备注 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_transaction_no` (`transaction_no`)
- KEY: `idx_payment_no` (`payment_no`)
- KEY: `idx_order_no` (`order_no`)
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_created_at` (`created_at`)

**示例数据**:
- TXN2026051700001: 微信支付98元，耗时1250ms，成功
- TXN2026051700002: 支付宝支付120元，耗时980ms，成功

---

### 3. refund_record - 退款记录表

**功能**: 管理退款申请和处理流程。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 退款ID | 主键，自增 |
| refund_no | VARCHAR(50) | 退款单号 | 唯一标识，格式：REF+年月日+流水号 |
| payment_no | VARCHAR(50) | 原支付单号 | 不能为空 |
| order_no | VARCHAR(50) | 订单编号 | 不能为空 |
| order_id | BIGINT(20) | 订单ID | 不能为空 |
| shop_id | BIGINT(20) | 店铺ID | 不能为空 |
| user_id | BIGINT(20) | 用户ID | 可选 |
| refund_amount | DECIMAL(10,2) | 退款金额（元） | 不能为空 |
| refund_reason | VARCHAR(500) | 退款原因 | 不能为空 |
| refund_type | TINYINT(1) | 退款类型 | 1-全额退款，2-部分退款，默认1 |
| refund_status | TINYINT(1) | 退款状态 | 0-申请中，1-审核中，2-退款中，3-退款成功，4-退款失败，5-已关闭 |
| payment_method | TINYINT(1) | 原支付方式 | 不能为空 |
| refund_method | TINYINT(1) | 退款方式 | 1-原路返回，2-余额，3-线下退款 |
| transaction_id | VARCHAR(100) | 第三方退款交易号 | 可选 |
| apply_time | DATETIME | 申请时间 | 默认当前时间 |
| approve_time | DATETIME | 审核时间 | 可选 |
| approve_by | BIGINT(20) | 审核人ID | 可选 |
| approve_remark | VARCHAR(255) | 审核备注 | 可选 |
| refund_time | DATETIME | 退款成功时间 | 可选 |
| failure_reason | VARCHAR(500) | 失败原因 | 可选 |
| evidence_images | TEXT | 凭证图片URL | 多个用逗号分隔 |
| remark | VARCHAR(500) | 备注 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_refund_no` (`refund_no`)
- KEY: `idx_payment_no` (`payment_no`)
- KEY: `idx_order_no` (`order_no`)
- KEY: `idx_order_id` (`order_id`)
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_refund_status` (`refund_status`)
- KEY: `idx_created_at` (`created_at`)

---

### 4. payment_config - 支付配置表

**功能**: 存储各支付方式的配置信息。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 配置ID | 主键，自增 |
| shop_id | BIGINT(20) | 店铺ID | 0表示全局配置 |
| payment_method | TINYINT(1) | 支付方式 | 1-微信支付，2-支付宝，3-现金，4-会员卡，5-银行卡 |
| is_enabled | TINYINT(1) | 是否启用 | 0-禁用，1-启用，默认1 |
| app_id | VARCHAR(100) | 应用ID | 可选 |
| merchant_id | VARCHAR(100) | 商户号 | 可选 |
| api_key | VARCHAR(255) | API密钥 | 加密存储 |
| private_key | TEXT | 私钥 | 加密存储 |
| public_key | TEXT | 公钥 | 可选 |
| notify_url | VARCHAR(255) | 异步通知地址 | 可选 |
| return_url | VARCHAR(255) | 同步返回地址 | 可选 |
| min_amount | DECIMAL(10,2) | 最小支付金额（元） | 默认0.01 |
| max_amount | DECIMAL(10,2) | 最大支付金额（元） | 默认50000.00 |
| timeout_minutes | INT(11) | 支付超时时间（分钟） | 默认15 |
| auto_refund | TINYINT(1) | 是否自动退款 | 0-否，1-是，默认0 |
| extra_config | TEXT | 额外配置 | JSON格式 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_shop_payment` (`shop_id`, `payment_method`)

**示例配置**:
- 全局微信支付配置
- 全局支付宝配置
- SHOP001店铺专属微信支付配置

---

### 5. payment_callback_log - 支付回调日志表

**功能**: 记录第三方支付平台的回调通知。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 日志ID | 主键，自增 |
| payment_no | VARCHAR(50) | 支付单号 | 可选 |
| transaction_id | VARCHAR(100) | 第三方交易号 | 可选 |
| callback_type | TINYINT(1) | 回调类型 | 1-支付回调，2-退款回调 |
| request_data | TEXT | 回调请求数据 | 不能为空 |
| response_data | TEXT | 响应数据 | 可选 |
| signature_valid | TINYINT(1) | 签名验证 | 0-失败，1-成功，默认0 |
| process_status | TINYINT(1) | 处理状态 | 0-待处理，1-处理成功，2-处理失败 |
| error_msg | VARCHAR(500) | 错误信息 | 可选 |
| ip_address | VARCHAR(50) | 回调IP地址 | 可选 |
| retry_count | INT(11) | 重试次数 | 默认0 |
| created_at | DATETIME | 创建时间 | 自动生成 |

**索引**:
- PRIMARY KEY: `id`
- KEY: `idx_payment_no` (`payment_no`)
- KEY: `idx_transaction_id` (`transaction_id`)
- KEY: `idx_created_at` (`created_at`)

---

## 业务场景

### 1. 支付流程
1. 用户提交订单，创建支付单
2. 调用第三方支付接口（微信/支付宝）
3. 记录支付流水
4. 等待异步回调
5. 更新支付状态
6. 通知订单服务

### 2. 退款流程
1. 用户申请退款
2. 创建退款记录
3. 商家审核
4. 调用第三方退款接口
5. 更新退款状态
6. 通知订单服务

### 3. 对账管理
- 每日下载第三方支付对账单
- 比对本地支付记录
- 发现并处理差异

### 4. 支付统计
- 按支付方式统计
- 按店铺统计
- 按时间段统计
- 成功率分析

---

## 注意事项

1. **安全性**: 
   - API密钥、私钥等敏感信息必须加密存储
   - 回调签名必须验证
   - 防止重复回调

2. **幂等性**: 
   - 支付回调需要支持幂等处理
   - 使用payment_no或transaction_id作为幂等键

3. **事务一致性**: 
   - 支付成功后需要同步更新订单状态
   - 建议使用本地消息表或MQ保证最终一致性

4. **超时处理**: 
   - 设置合理的支付超时时间
   - 定时任务清理过期未支付订单

5. **异常处理**: 
   - 记录详细的错误信息
   - 支持人工介入处理异常订单

6. **熔断降级**: 
   - 支付服务故障时，订单服务应降级提示"稍后支付"
   - 使用Sentinel实现服务熔断

---

## 扩展建议

1. 可以添加支付分账表，支持多方分账
2. 可以添加支付优惠券表，记录使用的支付优惠
3. 可以添加支付风控表，记录可疑交易
4. 可以添加支付渠道路由表，智能选择支付渠道
5. 可以添加支付对账表，自动化对账流程
