# Payment Service API 测试文档

## 📋 服务说明

**服务名称**: payment-service  
**服务端口**: 8084  
**API基础路径**: `/api/payment`  
**Knife4j文档**: http://localhost:8084/doc.html

---

## 🔧 核心优化说明

### ✅ DTO重构完成
- **PaymentOrderCreateRequest** 已精简为10个必要字段
- 删除了2个冗余字段（paymentAmount, shopId）
- 支付金额和店铺ID从order-service获取，提高安全性

### ✅ 服务间调用已实现
- **OrderFeignClient**: 验证订单是否存在、获取订单金额和店铺ID
- 所有FeignClient都有熔断器保护（100%覆盖）

### ✅ 业务规则
1. 创建支付订单时自动验证订单是否存在
2. 验证订单是否已支付，防止重复支付
3. 使用订单的actualAmount作为支付金额
4. 使用订单的shopId作为店铺ID
5. 防止客户端篡改支付金额

---

## 📝 API接口列表

### 1. 获取所有支付订单列表

**接口**: `GET /api/payment/list`

**请求示例**:
```bash
curl -X GET "http://localhost:8084/api/payment/list"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "paymentNo": "PAY2026051700001",
      "orderNo": "ORD2026051700001",
      "shopId": 1,
      "userId": 1001,
      "paymentAmount": 98.00,
      "paymentMethod": 1,
      "paymentStatus": 2,
      "currency": "CNY"
    }
  ]
}
```

---

### 2. 根据ID获取支付订单详情

**接口**: `GET /api/payment/{id}`

**请求示例**:
```bash
curl -X GET "http://localhost:8084/api/payment/1"
```

---

### 3. 根据支付单号获取支付订单

**接口**: `GET /api/payment/no/{paymentNo}`

**请求示例**:
```bash
curl -X GET "http://localhost:8084/api/payment/no/PAY2026051700001"
```

---

### 4. 根据订单编号获取支付订单

**接口**: `GET /api/payment/order/{orderNo}`

**请求示例**:
```bash
curl -X GET "http://localhost:8084/api/payment/order/ORD2026051700001"
```

---

### 5. 根据店铺ID获取支付订单列表

**接口**: `GET /api/payment/shop/{shopId}`

**请求示例**:
```bash
curl -X GET "http://localhost:8084/api/payment/shop/1"
```

---

### 6. 根据用户ID获取支付订单列表

**接口**: `GET /api/payment/user/{userId}`

**请求示例**:
```bash
curl -X GET "http://localhost:8084/api/payment/user/1001"
```

---

### 7. 根据状态获取支付订单列表

**接口**: `GET /api/payment/status/{paymentStatus}`

**支付状态说明**:
- 0: 待支付
- 1: 支付中
- 2: 支付成功
- 3: 支付失败
- 4: 已退款

**请求示例**:
```bash
curl -X GET "http://localhost:8084/api/payment/status/2"
```

---

### 8. 创建支付订单 ⭐核心接口（已优化）

**接口**: `POST /api/payment`

**🔥 重要说明**: 
- PaymentOrderCreateRequest已重构，只包含10个必要字段
- paymentAmount和shopId由服务端从order-service获取
- 系统会验证订单是否存在且未支付
- 防止客户端篡改支付金额

**请求体** (PaymentOrderCreateRequest):
```json
{
  "orderNo": "ORD2026051700001",
  "orderId": null,
  "userId": 1001,
  "paymentMethod": 1,
  "currency": "CNY",
  "subject": "美味餐厅订单支付",
  "body": "宫保鸡丁等3件商品",
  "clientIp": "192.168.1.100",
  "deviceInfo": "iOS",
  "notifyUrl": null,
  "returnUrl": null,
  "extraParams": null
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderNo | String | ✅ | 关联订单编号 |
| orderId | Long | ❌ | 关联订单ID（可选） |
| userId | Long | ❌ | 用户ID |
| paymentMethod | Integer | ✅ | 支付方式：1-微信，2-支付宝，3-现金，4-会员卡，5-银行卡 |
| currency | String | ❌ | 货币类型，默认CNY |
| subject | String | ❌ | 支付主题 |
| body | String | ❌ | 支付描述 |
| clientIp | String | ❌ | 客户端IP地址 |
| deviceInfo | String | ❌ | 设备信息 |
| notifyUrl | String | ❌ | 异步通知地址 |
| returnUrl | String | ❌ | 同步返回地址 |
| extraParams | String | ❌ | 扩展参数，JSON格式 |

**❌ 已删除的字段**（不再需要前端传递）:
- paymentAmount - 从order-service获取订单的actualAmount
- shopId - 从order-service获取订单的shopId

**请求示例**:
```bash
curl -X POST "http://localhost:8084/api/payment" \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD2026051700001",
    "userId": 1001,
    "paymentMethod": 1,
    "subject": "美味餐厅订单支付",
    "body": "宫保鸡丁等3件商品",
    "clientIp": "192.168.1.100"
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

**失败响应示例**（订单不存在）:
```json
{
  "code": 500,
  "message": "订单不存在，订单编号: ORD2026051700001",
  "data": null
}
```

**失败响应示例**（订单已支付）:
```json
{
  "code": 500,
  "message": "订单已支付，请勿重复支付",
  "data": null
}
```

**失败响应示例**（订单服务不可用）:
```json
{
  "code": 500,
  "message": "订单服务暂时不可用，请稍后重试",
  "data": null
}
```

**业务规则**:
1. ✅ 调用order-service验证订单是否存在
2. ✅ 验证订单是否已支付（paymentStatus=1）
3. ✅ 使用订单的actualAmount作为支付金额
4. ✅ 使用订单的shopId作为店铺ID
5. ✅ 生成支付单号
6. ✅ 设置默认值：paymentStatus=0(待支付), currency=CNY

**安全优势**:
- 🔒 防止客户端篡改支付金额
- 🔒 确保支付金额与订单金额一致
- 🔒 防止重复支付
- 🔒 所有敏感数据由服务端控制

**容错处理**:
- 如果order-service不可用，返回友好提示："订单服务暂时不可用，请稍后重试"

---

### 9. 更新支付订单信息

**接口**: `PUT /api/payment`

**请求示例**:
```bash
curl -X PUT "http://localhost:8084/api/payment" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "body": "修改支付描述"
  }'
```

---

### 10. 删除支付订单

**接口**: `DELETE /api/payment/{id}`

**请求示例**:
```bash
curl -X DELETE "http://localhost:8084/api/payment/1"
```

---

### 11. 更新支付状态

**接口**: `PUT /api/payment/{id}/status?paymentStatus={status}`

**请求示例**:
```bash
curl -X PUT "http://localhost:8084/api/payment/1/status?paymentStatus=2"
```

---

## 🧪 完整测试流程

### 测试场景1：正常创建支付订单

```bash
# 1. 先在order-service创建一个订单
curl -X POST "http://localhost:8083/api/order" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "tableId": 5,
    "userId": 1001,
    "orderType": 1,
    "remark": "测试订单"
  }'

# 2. 使用返回的订单号创建支付订单
curl -X POST "http://localhost:8084/api/payment" \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD2026051700001",
    "userId": 1001,
    "paymentMethod": 1,
    "subject": "测试支付",
    "clientIp": "192.168.1.100"
  }'

# 3. 查询支付订单验证
curl -X GET "http://localhost:8084/api/payment/order/ORD2026051700001"
```

### 测试场景2：防止重复支付

```bash
# 1. 第一次创建支付订单（成功）
curl -X POST "http://localhost:8084/api/payment" \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD2026051700001",
    "paymentMethod": 1
  }'

# 2. 手动更新订单为已支付状态
curl -X PUT "http://localhost:8083/api/order/1/status?orderStatus=1"

# 3. 第二次尝试创建支付订单（应该失败）
curl -X POST "http://localhost:8084/api/payment" \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD2026051700001",
    "paymentMethod": 1
  }'
# 预期响应: {"code": 500, "message": "订单已支付，请勿重复支付"}
```

### 测试场景3：异常情况测试

```bash
# 测试订单不存在
curl -X POST "http://localhost:8084/api/payment" \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD999999999",
    "paymentMethod": 1
  }'
# 预期响应: {"code": 500, "message": "订单不存在，订单编号: ORD999999999"}

# 测试缺少必填字段
curl -X POST "http://localhost:8084/api/payment" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1001
  }'
# 预期响应: 参数验证失败（orderNo和paymentMethod必填）
```

---

## 🔍 服务间调用验证

### 验证OrderFeignClient

当创建支付订单时，系统会自动调用order-service获取订单信息：

```java
// Payment Service内部调用
Result<OrderInfoDTO> orderResult = orderFeignClient.getOrderByOrderNo(request.getOrderNo());
OrderInfoDTO orderInfo = orderResult.getData();

// 使用订单的金额和店铺ID
payment.setPaymentAmount(orderInfo.getActualAmount());
payment.setShopId(orderInfo.getShopId());
```

**测试方法**:
1. 停止order-service
2. 尝试创建支付订单
3. 应该收到："订单服务暂时不可用，请稍后重试"

---

## 📊 性能测试建议

### 并发测试
```bash
# 使用ab工具进行压力测试
ab -n 1000 -c 10 -p payment.json -T application/json http://localhost:8084/api/payment
```

### 监控指标
- 支付订单创建平均响应时间
- 服务间调用成功率
- 熔断器触发次数
- 重复支付拦截次数

---

## 🎯 注意事项

1. **DTO变更**: PaymentOrderCreateRequest已简化，不再需要传递paymentAmount和shopId
2. **订单验证**: 确保order-service正常运行，否则无法创建支付订单
3. **防重复支付**: 系统会自动检查订单是否已支付
4. **熔断器**: 所有服务间调用都有fallback保护
5. **日志记录**: 所有服务调用失败都会记录详细日志
6. **金额一致性**: 支付金额始终与订单金额保持一致，无法被篡改

---

## 🔐 安全性说明

### 防止金额篡改
- ✅ 支付金额从order-service获取，不由前端传递
- ✅ 确保支付金额与订单实际金额一致
- ✅ 即使前端被攻击，也无法篡改支付金额

### 防止重复支付
- ✅ 创建支付订单前检查订单支付状态
- ✅ 如果订单已支付（paymentStatus=1），拒绝创建新的支付订单
- ✅ 保护用户不会被重复扣款

### 服务间调用安全
- ✅ 使用FeignClient进行服务间通信
- ✅ 所有调用都有熔断器保护
- ✅ 服务不可用时提供友好提示
- ✅ 详细的日志记录便于审计

---

## 📚 相关文档

- [Payment Service设计文档](DTO_DESIGN.md)
- [代码恢复指南](../../service_readme/CODE_RECOVERY_GUIDE.md)
- [编译检查报告](../../service_readme/COMPILATION_CHECK_REPORT.md)

---

**文档版本**: v2.0 (已更新DTO重构和服务间调用)  
**最后更新**: 2026-05-18  
**维护者**: 开发团队
