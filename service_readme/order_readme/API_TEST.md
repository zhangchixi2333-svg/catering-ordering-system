# Order Service API 测试文档

## 📋 服务说明

**服务名称**: order-service  
**服务端口**: 8083  
**API基础路径**: `/api/order`  
**Knife4j文档**: http://localhost:8083/doc.html

---

## 🔧 核心优化说明

### ✅ DTO重构完成
- **OrderCreateRequest** 已精简为6个必要字段
- 删除了9个冗余字段（totalAmount, actualAmount, itemCount, tableNumber, queueNumber, priority, estimatedTime）
- 金额和数量由服务端计算，提高安全性

### ✅ 服务间调用已实现
- **ShopFeignClient**: 验证店铺是否存在且营业中
- **MenuFeignClient**: 获取菜品信息、更新库存
- **QueueFeignClient**: 验证排队记录
- 所有FeignClient都有熔断器保护（100%覆盖）

### ✅ 业务规则
1. 创建订单时自动验证店铺状态
2. 服务端计算订单金额和菜品数量
3. 可选关联排队号
4. 防止未营业店铺接单

---

## 📝 API接口列表

### 1. 获取所有订单列表

**接口**: `GET /api/order/list`

**请求示例**:
```bash
curl -X GET "http://localhost:8083/api/order/list"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "orderNo": "ORD2026051700001",
      "shopId": 1,
      "userId": 1001,
      "orderType": 1,
      "orderStatus": 0,
      "totalAmount": 98.00,
      "actualAmount": 98.00,
      "itemCount": 3,
      "paymentStatus": 0
    }
  ]
}
```

---

### 2. 根据ID获取订单详情

**接口**: `GET /api/order/{id}`

**请求示例**:
```bash
curl -X GET "http://localhost:8083/api/order/1"
```

---

### 3. 根据订单编号获取订单

**接口**: `GET /api/order/no/{orderNo}`

**请求示例**:
```bash
curl -X GET "http://localhost:8083/api/order/no/ORD2026051700001"
```

---

### 4. 根据店铺ID获取订单列表

**接口**: `GET /api/order/shop/{shopId}`

**请求示例**:
```bash
curl -X GET "http://localhost:8083/api/order/shop/1"
```

---

### 5. 根据用户ID获取订单列表

**接口**: `GET /api/order/user/{userId}`

**请求示例**:
```bash
curl -X GET "http://localhost:8083/api/order/user/1001"
```

---

### 6. 根据状态获取订单列表

**接口**: `GET /api/order/status/{orderStatus}`

**订单状态说明**:
- 0: 待支付
- 1: 待接单
- 2: 制作中
- 3: 待取餐
- 4: 已完成
- 5: 已取消

**请求示例**:
```bash
curl -X GET "http://localhost:8083/api/order/status/0"
```

---

### 7. 创建订单 ⭐核心接口（已优化）

**接口**: `POST /api/order`

**🔥 重要说明**: 
- OrderCreateRequest已重构，只包含6个必要字段
- 金额和数量由服务端自动计算
- 系统会验证店铺是否存在且营业中
- 可选关联排队号

**请求体** (OrderCreateRequest):
```json
{
  "shopId": 1,
  "tableId": 5,
  "userId": 1001,
  "orderType": 1,
  "queueId": null,
  "remark": "不要辣，少盐"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| shopId | Long | ✅ | 店铺ID |
| tableId | Long | ❌ | 桌台ID（堂食必填） |
| userId | Long | ❌ | 用户ID |
| orderType | Integer | ✅ | 订单类型：1-堂食，2-外带，3-外卖 |
| queueId | Long | ❌ | 排队ID（可选，关联排队号） |
| remark | String | ❌ | 订单备注 |

**❌ 已删除的字段**（不再需要前端传递）:
- totalAmount - 服务端自动计算
- actualAmount - 服务端自动计算
- itemCount - 服务端自动统计
- tableNumber - 服务端查询
- queueNumber - 服务端查询
- priority - 服务端设置默认值
- estimatedTime - 服务端计算

**请求示例**:
```bash
curl -X POST "http://localhost:8083/api/order" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "tableId": 5,
    "userId": 1001,
    "orderType": 1,
    "queueId": null,
    "remark": "不要辣"
  }'
```

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**失败响应示例**（店铺不存在）:
```json
{
  "code": 500,
  "message": "店铺不存在",
  "data": null
}
```

**失败响应示例**（店铺未营业）:
```json
{
  "code": 500,
  "message": "店铺当前未营业，无法下单",
  "data": null
}
```

**失败响应示例**（排队记录不存在）:
```json
{
  "code": 500,
  "message": "排队记录不存在",
  "data": null
}
```

**业务规则**:
1. ✅ 验证店铺是否存在 - 不存在则返回错误
2. ✅ 验证店铺是否营业中 - 未营业则返回错误
3. ✅ 如果有queueId，验证排队记录是否存在
4. ✅ 服务端生成订单号
5. ✅ 服务端计算订单总金额和菜品数量
6. ✅ 设置默认值：orderStatus=0(待支付), paymentStatus=0(未支付), priority=0(普通)

**容错处理**:
- 如果shop-service不可用，返回友好提示："店铺服务暂时不可用，请稍后重试"
- 如果queue-service不可用，仅记录警告，不阻止订单创建

---

### 8. 更新订单信息

**接口**: `PUT /api/order`

**请求示例**:
```bash
curl -X PUT "http://localhost:8083/api/order" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "remark": "修改备注"
  }'
```

---

### 9. 删除订单

**接口**: `DELETE /api/order/{id}`

**请求示例**:
```bash
curl -X DELETE "http://localhost:8083/api/order/1"
```

---

### 10. 更新订单状态

**接口**: `PUT /api/order/{id}/status?orderStatus={status}`

**请求示例**:
```bash
curl -X PUT "http://localhost:8083/api/order/1/status?orderStatus=2"
```

---

### 11. 取消订单

**接口**: `PUT /api/order/{id}/cancel?cancelReason={reason}`

**请求示例**:
```bash
curl -X PUT "http://localhost:8083/api/order/1/cancel?cancelReason=用户取消"
```

---

## 🧪 完整测试流程

### 测试场景1：正常创建订单

```bash
# 1. 创建订单
curl -X POST "http://localhost:8083/api/order" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "tableId": 5,
    "userId": 1001,
    "orderType": 1,
    "remark": "测试订单"
  }'

# 2. 获取订单列表验证
curl -X GET "http://localhost:8083/api/order/list"
```

### 测试场景2：关联排队号创建订单

```bash
# 1. 先创建一个排队号（通过queue-service）
# 2. 使用queueId创建订单
curl -X POST "http://localhost:8083/api/order" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "tableId": null,
    "userId": 1001,
    "orderType": 2,
    "queueId": 1,
    "remark": "外带订单"
  }'
```

### 测试场景3：异常情况测试

```bash
# 测试店铺不存在
curl -X POST "http://localhost:8083/api/order" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 99999,
    "userId": 1001,
    "orderType": 1
  }'
# 预期响应: {"code": 500, "message": "店铺不存在"}

# 测试缺少必填字段
curl -X POST "http://localhost:8083/api/order" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1001,
    "orderType": 1
  }'
# 预期响应: 参数验证失败
```

---

## 🔍 服务间调用验证

### 验证ShopFeignClient

当创建订单时，系统会自动调用shop-service验证店铺：

```java
// Order Service内部调用
Result<ShopInfoDTO> shopResult = shopFeignClient.getShopById(request.getShopId());
```

**测试方法**:
1. 停止shop-service
2. 尝试创建订单
3. 应该收到："店铺服务暂时不可用，请稍后重试"

### 验证QueueFeignClient

当提供queueId时，系统会验证排队记录：

```java
// Order Service内部调用
Result<QueueInfoDTO> queueResult = queueFeignClient.getQueueById(request.getQueueId());
```

**测试方法**:
1. 提供一个不存在的queueId
2. 应该收到："排队记录不存在"

---

## 📊 性能测试建议

### 并发测试
```bash
# 使用ab工具进行压力测试
ab -n 1000 -c 10 -p order.json -T application/json http://localhost:8083/api/order
```

### 监控指标
- 订单创建平均响应时间
- 服务间调用成功率
- 熔断器触发次数

---

## 🎯 注意事项

1. **DTO变更**: OrderCreateRequest已大幅简化，前端需要适配新的字段结构
2. **金额计算**: 不再需要前端传递金额，服务端会根据菜品价格自动计算
3. **店铺验证**: 确保shop-service正常运行，否则无法创建订单
4. **熔断器**: 所有服务间调用都有fallback保护，不会因依赖服务故障而崩溃
5. **日志记录**: 所有服务调用失败都会记录详细日志，便于排查问题

---

## 📚 相关文档

- [Order Service设计文档](DTO_DESIGN.md)
- [代码恢复指南](../../service_readme/CODE_RECOVERY_GUIDE.md)
- [编译检查报告](../../service_readme/COMPILATION_CHECK_REPORT.md)

---

**文档版本**: v2.0 (已更新DTO重构和服务间调用)  
**最后更新**: 2026-05-18  
**维护者**: 开发团队
