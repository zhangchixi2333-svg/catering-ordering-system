# 店铺配置 (Shop Config) 详细说明

## 📋 概述

店铺配置表 (`shop_config`) 用于存储每个店铺的个性化配置项，采用 **Key-Value** 键值对的方式存储，支持灵活扩展各种业务配置。

---

## 🗄️ 数据库表结构

### shop_config 表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 配置ID，主键自增 |
| `shop_id` | BIGINT | 店铺ID，关联 shop_info.id |
| `config_key` | VARCHAR(100) | 配置键名（唯一标识） |
| `config_value` | TEXT | 配置值（可以是字符串、数字、JSON等） |
| `config_desc` | VARCHAR(255) | 配置说明 |
| `created_at` | DATETIME | 创建时间 |
| `updated_at` | DATETIME | 更新时间 |

**唯一约束**: `(shop_id, config_key)` - 每个店铺的每个配置键只能有一条记录

---

## ⚙️ 配置项详解

根据示例数据，系统预定义了以下配置项：

### 1. queue_enabled - 是否启用排队功能

**配置键**: `queue_enabled`  
**配置值**: `true` / `false`  
**说明**: 控制店铺是否开启排队取号功能

**使用场景**:
- ✅ `true`: 顾客可以扫码取号排队
- ❌ `false`: 关闭排队功能，直接到店就餐

**示例**:
```json
{
  "shopId": 1,
  "configKey": "queue_enabled",
  "configValue": "true",
  "configDesc": "是否启用排队功能"
}
```

---

### 2. max_queue_number - 最大排队号码数

**配置键**: `max_queue_number`  
**配置值**: 数字（如 `50`）  
**说明**: 限制同时排队的最大号码数量，避免排队过长

**使用场景**:
- 当排队人数达到上限时，不再接受新的取号
- 提示顾客："当前排队人数较多，请稍后再试"

**示例**:
```json
{
  "shopId": 1,
  "configKey": "max_queue_number",
  "configValue": "50",
  "configDesc": "最大排队号码数"
}
```

**业务逻辑**:
```java
if (currentQueueCount >= Integer.parseInt(configValue)) {
    return Result.error("当前排队人数已达上限，请稍后再试");
}
```

---

### 3. auto_call_interval - 自动叫号间隔

**配置键**: `auto_call_interval`  
**配置值**: 数字（秒），如 `300`（5分钟）  
**说明**: 系统自动叫号的时间间隔

**使用场景**:
- 每隔指定时间自动叫下一批号码
- 减少人工操作，提高效率

**示例**:
```json
{
  "shopId": 1,
  "configKey": "auto_call_interval",
  "configValue": "300",
  "configDesc": "自动叫号间隔（秒）"
}
```

**业务逻辑**:
```java
@Scheduled(fixedRateString = "#{@shopConfigService.getConfigValue(shopId, 'auto_call_interval')}")
public void autoCallNumber() {
    // 自动叫号逻辑
}
```

---

### 4. payment_timeout - 支付超时时间

**配置键**: `payment_timeout`  
**配置值**: 数字（秒），如 `900`（15分钟）  
**说明**: 订单生成后，顾客需要在指定时间内完成支付

**使用场景**:
- 顾客扫码点餐后，生成订单
- 如果超过指定时间未支付，订单自动取消
- 释放桌台资源

**示例**:
```json
{
  "shopId": 1,
  "configKey": "payment_timeout",
  "configValue": "900",
  "configDesc": "支付超时时间（秒）"
}
```

**业务逻辑**:
```java
long timeoutSeconds = Long.parseLong(configValue);
if (currentTime - orderCreateTime > timeoutSeconds) {
    // 订单超时，自动取消
    cancelOrder(orderId);
}
```

---

## 💡 可扩展的配置项

除了上述示例外，还可以根据业务需求添加更多配置：

### 排队相关配置

| 配置键 | 配置值示例 | 说明 |
|--------|-----------|------|
| `queue_prefix` | `A` | 排队号码前缀（如 A001, B001） |
| `vip_queue_enabled` | `true` | 是否启用VIP优先排队 |
| `queue_sms_notify` | `true` | 排队进度短信通知 |
| `queue_wechat_notify` | `true` | 排队进度微信通知 |
| `estimated_wait_time` | `30` | 预估等待时间（分钟） |

### 营业相关配置

| 配置键 | 配置值示例 | 说明 |
|--------|-----------|------|
| `min_order_amount` | `50` | 最低消费金额 |
| `service_charge_rate` | `0.1` | 服务费比例（10%） |
| `reservation_enabled` | `true` | 是否启用预订功能 |
| `max_reservation_days` | `7` | 最多可提前预订天数 |
| `takeout_enabled` | `true` | 是否启用外卖功能 |

### 营销相关配置

| 配置键 | 配置值示例 | 说明 |
|--------|-----------|------|
| `coupon_enabled` | `true` | 是否启用优惠券 |
| `member_discount` | `0.95` | 会员折扣（95折） |
| `happy_hour_start` | `14:00` | 欢乐时光开始时间 |
| `happy_hour_end` | `17:00` | 欢乐时光结束时间 |
| `happy_hour_discount` | `0.8` | 欢乐时光折扣（8折） |

### 通知相关配置

| 配置键 | 配置值示例 | 说明 |
|--------|-----------|------|
| `order_ready_notify` | `true` | 餐品准备好通知 |
| `queue_arrival_notify` | `true` | 叫号到达通知 |
| `promotion_push` | `true` | 促销活动推送 |
| `birthday_coupon` | `true` | 生日优惠券自动发放 |

### 其他配置

| 配置键 | 配置值示例 | 说明 |
|--------|-----------|------|
| `wifi_ssid` | `Restaurant_Guest` | WiFi名称 |
| `wifi_password` | `12345678` | WiFi密码 |
| `parking_available` | `true` | 是否提供停车位 |
| `pet_friendly` | `false` | 是否允许携带宠物 |
| `smoking_area` | `false` | 是否有吸烟区 |

---

## 🔧 API 接口

### 1. 获取配置值

**请求**:
```http
GET /api/config/{shopId}/{configKey}
```

**示例**:
```bash
curl http://localhost:8081/api/config/1/queue_enabled
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "true"
}
```

---

### 2. 设置配置

**请求**:
```http
POST /api/config?shopId=1&configKey=queue_enabled&configValue=false&configDesc=是否启用排队功能
```

**参数说明**:
- `shopId`: 店铺ID（必填）
- `configKey`: 配置键（必填）
- `configValue`: 配置值（必填）
- `configDesc`: 配置说明（可选）

**示例**:
```bash
curl -X POST "http://localhost:8081/api/config?shopId=1&configKey=max_queue_number&configValue=100&configDesc=最大排队号码数"
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

---

## 💻 代码示例

### Service 层

```java
@Service
public class ShopConfigServiceImpl implements ShopConfigService {
    
    /**
     * 获取配置值
     */
    public String getConfigValue(Long shopId, String configKey) {
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopConfig::getShopId, shopId);
        wrapper.eq(ShopConfig::getConfigKey, configKey);
        ShopConfig config = getOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }
    
    /**
     * 设置配置（不存在则创建，存在则更新）
     */
    public boolean setConfig(Long shopId, String configKey, String configValue, String configDesc) {
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopConfig::getShopId, shopId);
        wrapper.eq(ShopConfig::getConfigKey, configKey);
        ShopConfig existingConfig = getOne(wrapper);
        
        if (existingConfig != null) {
            // 更新现有配置
            existingConfig.setConfigValue(configValue);
            if (configDesc != null) {
                existingConfig.setConfigDesc(configDesc);
            }
            return updateById(existingConfig);
        } else {
            // 创建新配置
            ShopConfig newConfig = new ShopConfig();
            newConfig.setShopId(shopId);
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            newConfig.setConfigDesc(configDesc);
            return save(newConfig);
        }
    }
}
```

### Controller 层使用配置

```java
@RestController
@RequestMapping("/api/queue")
public class QueueController {
    
    @Autowired
    private ShopConfigService shopConfigService;
    
    @PostMapping("/take-number")
    public Result<String> takeNumber(@RequestParam Long shopId) {
        // 检查是否启用排队功能
        String queueEnabled = shopConfigService.getConfigValue(shopId, "queue_enabled");
        if ("false".equals(queueEnabled)) {
            return Result.error("该店铺暂未开启排队功能");
        }
        
        // 检查排队人数是否达到上限
        String maxQueueNumber = shopConfigService.getConfigValue(shopId, "max_queue_number");
        int currentCount = getCurrentQueueCount(shopId);
        if (currentCount >= Integer.parseInt(maxQueueNumber)) {
            return Result.error("当前排队人数已达上限，请稍后再试");
        }
        
        // 生成排队号码
        String number = generateQueueNumber(shopId);
        return Result.success(number);
    }
}
```

---

## 🎯 最佳实践

### 1. 配置命名规范

- 使用小写字母和下划线：`queue_enabled` ✅
- 避免使用驼峰命名：`queueEnabled` ❌
- 语义清晰：`payment_timeout` ✅
- 避免缩写：`pay_to` ❌

### 2. 配置值类型

虽然 `config_value` 是 TEXT 类型，但建议：
- 布尔值：`"true"` / `"false"`（字符串）
- 数字：`"100"`（字符串形式的数字）
- JSON：`"{\"start\":\"14:00\",\"end\":\"17:00\"}"`（转义后的JSON字符串）

### 3. 默认值处理

```java
public String getConfigWithDefault(Long shopId, String configKey, String defaultValue) {
    String value = getConfigValue(shopId, configKey);
    return value != null ? value : defaultValue;
}

// 使用
String queueEnabled = getConfigWithDefault(shopId, "queue_enabled", "true");
```

### 4. 配置缓存

对于频繁读取的配置，建议使用 Redis 缓存：

```java
@Cacheable(value = "shopConfig", key = "#shopId + ':' + #configKey")
public String getConfigValue(Long shopId, String configKey) {
    // 从数据库查询
}

@CacheEvict(value = "shopConfig", key = "#shopId + ':' + #configKey")
public boolean setConfig(...) {
    // 更新数据库并清除缓存
}
```

### 5. 配置验证

在设置配置时进行验证：

```java
public boolean setConfig(Long shopId, String configKey, String configValue, String configDesc) {
    // 验证配置值
    if ("max_queue_number".equals(configKey)) {
        int value = Integer.parseInt(configValue);
        if (value <= 0 || value > 1000) {
            throw new IllegalArgumentException("最大排队号码数必须在 1-1000 之间");
        }
    }
    
    // 保存配置
    // ...
}
```

---

## 📊 配置管理界面建议

可以在后台管理系统中添加配置管理页面：

### 功能列表
- ✅ 查看所有配置项
- ✅ 编辑配置值
- ✅ 添加新配置
- ✅ 删除配置
- ✅ 批量导入/导出配置
- ✅ 配置历史记录
- ✅ 配置模板（快速应用到多个店铺）

### 界面示例

```
店铺配置管理 - 美味餐厅旗舰店 (SHOP001)

┌─────────────────────┬──────────┬──────────────────┬────────┐
│ 配置项              │ 配置值   │ 说明             │ 操作   │
├─────────────────────┼──────────┼──────────────────┼────────┤
│ queue_enabled       │ [true ▼] │ 是否启用排队功能 │ [编辑] │
│ max_queue_number    │ [50___]  │ 最大排队号码数   │ [编辑] │
│ auto_call_interval  │ [300___] │ 自动叫号间隔(秒) │ [编辑] │
│ payment_timeout     │ [900___] │ 支付超时时间(秒) │ [编辑] │
└─────────────────────┴──────────┴──────────────────┴────────┘

[+ 添加新配置]  [批量保存]  [恢复默认]
```

---

## 🔍 常见问题

### Q1: 为什么使用 Key-Value 而不是固定字段？

**A**: 
- ✅ 灵活扩展，无需修改表结构
- ✅ 不同店铺可以有不同配置
- ✅ 支持动态添加新配置项
- ❌ 缺点是查询效率略低，需要加缓存

### Q2: 配置值为什么要用字符串？

**A**:
- 统一存储格式，简化数据库设计
- 可以在应用层转换为任意类型
- 支持复杂结构（如 JSON）

### Q3: 如何保证配置的实时生效？

**A**:
- 使用 Redis 缓存，设置较短的过期时间
- 或者使用配置中心（如 Nacos）实现配置热更新
- 关键配置变更时，主动清除缓存

---

**最后更新**: 2026-05-17
