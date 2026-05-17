# 通知服务数据库说明 (notification-service)

## 数据库概述

**数据库名称**: notification_service  
**功能描述**: 管理消息推送、短信通知、WebSocket连接等  
**字符集**: utf8mb4  
**排序规则**: utf8mb4_unicode_ci

---

## 数据表清单

### 1. message_template - 消息模板表

**功能**: 存储各种业务场景的消息模板。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 模板ID | 主键，自增 |
| template_code | VARCHAR(50) | 模板编码 | 唯一标识，不能为空 |
| template_name | VARCHAR(100) | 模板名称 | 不能为空 |
| template_type | TINYINT(1) | 模板类型 | 1-短信，2-微信，3-APP推送，4-邮件，5-语音 |
| business_type | VARCHAR(50) | 业务类型 | ORDER_CREATE-订单创建，ORDER_STATUS_CHANGE-订单状态变更，QUEUE_CALL-排队叫号，PAYMENT_SUCCESS-支付成功 |
| title | VARCHAR(200) | 消息标题 | 可选 |
| content | TEXT | 消息内容模板 | 支持变量占位符，如：{order_no}，不能为空 |
| variables | VARCHAR(500) | 变量说明 | JSON格式，如：["order_no","shop_name"] |
| is_enabled | TINYINT(1) | 是否启用 | 0-禁用，1-启用，默认1 |
| priority | TINYINT(1) | 优先级 | 0-普通，1-重要，2-紧急，默认0 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_template_code` (`template_code`)
- KEY: `idx_business_type` (`business_type`)

**示例数据**:
- SMS_ORDER_CREATE: 订单创建短信通知
- SMS_PAYMENT_SUCCESS: 支付成功短信通知
- SMS_QUEUE_CALL: 排队叫号短信通知
- WX_ORDER_STATUS: 订单状态微信通知
- WX_QUEUE_CALL: 排队叫号微信通知
- PUSH_ORDER_READY: 订单制作完成推送

---

### 2. message_send_log - 消息发送记录表

**功能**: 记录每条消息的发送详情和状态。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 日志ID | 主键，自增 |
| message_id | VARCHAR(50) | 消息ID | 唯一标识，不能为空 |
| template_code | VARCHAR(50) | 模板编码 | 可选 |
| recipient_type | TINYINT(1) | 接收者类型 | 1-用户，2-店员，3-后厨 |
| recipient_id | BIGINT(20) | 接收者ID | 可选 |
| phone | VARCHAR(20) | 手机号 | 可选 |
| email | VARCHAR(100) | 邮箱 | 可选 |
| open_id | VARCHAR(100) | 微信OpenID | 可选 |
| device_token | VARCHAR(255) | 设备Token（APP推送） | 可选 |
| message_type | TINYINT(1) | 消息类型 | 1-短信，2-微信，3-APP推送，4-邮件，5-语音 |
| business_type | VARCHAR(50) | 业务类型 | 不能为空 |
| business_id | VARCHAR(100) | 业务ID | 订单号、排队号等 |
| title | VARCHAR(200) | 消息标题 | 可选 |
| content | TEXT | 消息内容 | 不能为空 |
| send_status | TINYINT(1) | 发送状态 | 0-待发送，1-发送中，2-发送成功，3-发送失败 |
| send_time | DATETIME | 发送时间 | 可选 |
| receive_time | DATETIME | 接收时间 | 可选 |
| read_time | DATETIME | 阅读时间 | 可选 |
| retry_count | INT(11) | 重试次数 | 默认0 |
| max_retry | INT(11) | 最大重试次数 | 默认3 |
| error_code | VARCHAR(50) | 错误码 | 可选 |
| error_msg | VARCHAR(500) | 错误信息 | 可选 |
| channel_response | TEXT | 渠道返回数据 | 可选 |
| duration | INT(11) | 发送耗时（毫秒） | 可选 |
| cost | DECIMAL(10,4) | 发送成本（元） | 默认0.0000 |
| extra_data | TEXT | 扩展数据 | JSON格式 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_message_id` (`message_id`)
- KEY: `idx_recipient_id` (`recipient_id`)
- KEY: `idx_phone` (`phone`)
- KEY: `idx_business_type` (`business_type`)
- KEY: `idx_business_id` (`business_id`)
- KEY: `idx_send_status` (`send_status`)
- KEY: `idx_created_at` (`created_at`)

**示例数据**:
- MSG2026051700001: 订单创建短信，已成功
- MSG2026051700002: 支付成功短信，已成功
- MSG2026051700003: 排队叫号微信通知，已成功

---

### 3. websocket_session - WebSocket连接会话表

**功能**: 管理WebSocket长连接会话。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 会话ID | 主键，自增 |
| session_id | VARCHAR(100) | WebSocket会话ID | 唯一标识，不能为空 |
| user_id | BIGINT(20) | 用户ID | 可选 |
| user_type | TINYINT(1) | 用户类型 | 1-顾客，2-店员，3-后厨，4-管理员，默认1 |
| shop_id | BIGINT(20) | 店铺ID | 可选 |
| device_type | VARCHAR(20) | 设备类型 | WEB、IOS、ANDROID |
| ip_address | VARCHAR(50) | IP地址 | 可选 |
| user_agent | VARCHAR(500) | 用户代理 | 可选 |
| connect_time | DATETIME | 连接时间 | 默认当前时间 |
| disconnect_time | DATETIME | 断开时间 | 可选 |
| last_heartbeat_time | DATETIME | 最后心跳时间 | 可选 |
| status | TINYINT(1) | 状态 | 0-已断开，1-在线，默认1 |
| subscribe_channels | TEXT | 订阅频道列表 | JSON数组格式 |
| extra_data | TEXT | 扩展数据 | JSON格式 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_session_id` (`session_id`)
- KEY: `idx_user_id` (`user_id`)
- KEY: `idx_shop_id` (`shop_id`)
- KEY: `idx_status` (`status`)
- KEY: `idx_connect_time` (`connect_time`)

---

### 4. notification_config - 通知配置表

**功能**: 存储通知服务的各种配置。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 配置ID | 主键，自增 |
| shop_id | BIGINT(20) | 店铺ID | 0表示全局配置 |
| config_key | VARCHAR(100) | 配置键名 | 不能为空 |
| config_value | TEXT | 配置值 | 可选 |
| config_desc | VARCHAR(255) | 配置说明 | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_shop_config` (`shop_id`, `config_key`)

**示例配置**:
- sms_provider: 短信服务商（aliyun/tencent）
- sms_access_key: 短信AccessKey（加密存储）
- sms_secret_key: 短信SecretKey（加密存储）
- sms_sign_name: 短信签名
- wechat_app_id: 微信公众号AppID
- wechat_app_secret: 微信公众号AppSecret（加密存储）
- push_provider: 推送服务商（jiguang/getui）
- push_api_key: 推送API Key（加密存储）
- sms_enabled: 是否启用短信通知
- wechat_enabled: 是否启用微信通知
- push_enabled: 是否启用APP推送
- voice_enabled: 是否启用语音通知
- order_create_notify: 订单创建通知方式（sms,wechat）
- payment_success_notify: 支付成功通知方式（sms,wechat,push）
- queue_call_notify: 排队叫号通知方式（sms,wechat,voice）

---

### 5. message_statistics - 消息统计表

**功能**: 按日统计消息发送情况。

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT(20) | 统计ID | 主键，自增 |
| shop_id | BIGINT(20) | 店铺ID | 0表示全局 |
| stat_date | DATE | 统计日期 | 不能为空 |
| message_type | TINYINT(1) | 消息类型 | 1-短信，2-微信，3-APP推送，4-邮件，5-语音 |
| business_type | VARCHAR(50) | 业务类型 | ALL表示全部 |
| total_sent | INT(11) | 总发送数 | 默认0 |
| success_count | INT(11) | 成功数 | 默认0 |
| failed_count | INT(11) | 失败数 | 默认0 |
| pending_count | INT(11) | 待发送数 | 默认0 |
| success_rate | DECIMAL(5,2) | 成功率（%） | 默认0.00 |
| total_cost | DECIMAL(10,4) | 总成本（元） | 默认0.0000 |
| avg_duration | INT(11) | 平均耗时（毫秒） | 可选 |
| created_at | DATETIME | 创建时间 | 自动生成 |
| updated_at | DATETIME | 更新时间 | 自动更新 |

**索引**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_shop_date_type` (`shop_id`, `stat_date`, `message_type`, `business_type`)
- KEY: `idx_stat_date` (`stat_date`)

**示例数据**:
- 2026-05-16: 短信256条，成功率99.22%，成本12.80元
- 2026-05-16: 微信189条，成功率99.47%
- 2026-05-17: 订单创建短信45条，成功率100%

---

## 业务场景

### 1. 消息发送流程
1. 业务服务触发通知事件
2. 查询消息模板
3. 替换模板变量
4. 选择通知渠道（短信/微信/推送）
5. 调用第三方接口发送
6. 记录发送结果
7. 失败时重试

### 2. WebSocket实时推送
1. 客户端建立WebSocket连接
2. 服务端保存会话信息
3. 客户端订阅频道（如：订单状态、排队叫号）
4. 服务端推送实时消息
5. 心跳保活
6. 断线重连

### 3. 通知渠道选择
根据配置和业务类型选择合适的通知渠道：
- **订单创建**: 短信 + 微信
- **支付成功**: 短信 + 微信 + APP推送
- **排队叫号**: 短信 + 微信 + 语音
- **订单状态变更**: 微信 + APP推送

### 4. 消息重试机制
- 发送失败时自动重试
- 最多重试3次
- 指数退避策略（1s, 2s, 4s）
- 重试失败后记录错误信息

### 5. WebSocket频道设计
```
订单状态频道: order:{order_no}
排队叫号频道: queue:{shop_id}
店铺公告频道: shop:{shop_id}:notice
后厨订单频道: kitchen:{shop_id}
```

---

## Redis数据结构设计

### 1. WebSocket会话缓存（Hash）
```
key: websocket:sessions:{session_id}
field: user_id, shop_id, status, subscribe_channels等
```

### 2. 用户会话列表（Set）
```
key: websocket:user:{user_id}
members: [session_id1, session_id2, ...]
```

### 3. 频道订阅关系（Set）
```
key: websocket:channel:{channel}
members: [session_id1, session_id2, ...]
```

### 4. 消息去重（String）
```
key: message:duplicate:{message_id}
value: 1
ttl: 3600秒
```

---

## 注意事项

1. **安全性**: 
   - API密钥、Secret等敏感信息必须加密存储
   - WebSocket连接需要鉴权
   - 防止恶意频繁发送消息

2. **性能优化**: 
   - 大量消息发送时使用异步处理
   - 使用消息队列削峰填谷
   - WebSocket连接数较多时考虑分布式部署

3. **可靠性**: 
   - 消息发送失败需要重试
   - 重要消息需要确认机制
   - 记录详细的发送日志

4. **成本控制**: 
   - 短信有成本，合理使用
   - 优先使用免费渠道（微信、推送）
   - 统计各渠道成本

5. **用户体验**: 
   - 避免频繁打扰用户
   - 支持用户自定义通知偏好
   - 提供退订选项

6. **WebSocket管理**: 
   - 实现心跳机制检测断线
   - 定期清理无效会话
   - 支持集群环境下会话共享

---

## 扩展建议

1. 可以添加消息免打扰时段配置
2. 可以添加用户通知偏好设置表
3. 可以添加消息阅读状态追踪
4. 可以添加智能推送算法，提高打开率
5. 可以添加消息A/B测试功能
6. 可以添加富媒体消息支持（图片、视频）
7. 可以添加消息模板版本管理
8. 可以添加通知频率限制，防止骚扰
