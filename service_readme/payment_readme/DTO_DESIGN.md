# Payment Service DTO 设计文档

## 📋 文档说明

本文档详细说明Payment Service中使用的DTO设计原则和结构。

---

## 🎯 设计原则

### 1. 安全性优先
- 支付金额由服务端从order-service获取
- 防止客户端篡改支付金额

### 2. 职责分离
- Request DTO: 接收客户端请求
- Entity: 数据库实体

### 3. 最小化原则
- 只包含客户端必须提供的字段

---

## 📦 DTO列表

### 1. PaymentOrderCreateRequest ⭐核心DTO

**用途**: 创建支付订单时的请求参数

**设计版本**: v2.0 (已优化)

**字段列表**:
```java
public class PaymentOrderCreateRequest {
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;           // ✅ 必填：订单编号
    
    private Long orderId;             // ⚪ 可选：订单ID
    
    private Long userId;              // ⚪ 可选：用户ID
    
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;    // ✅ 必填：支付方式
    
    private String currency;          // ⚪ 可选：货币类型（默认CNY）
    
    private String subject;           // ⚪ 可选：支付主题
    
    private String body;              // ⚪ 可选：支付描述
    
    private String clientIp;          // ⚪ 可选：客户端IP
    
    private String deviceInfo;        // ⚪ 可选：设备信息
    
    private String notifyUrl;         // ⚪ 可选：异步通知地址
    
    private String returnUrl;         // ⚪ 可选：同步返回地址
    
    private String extraParams;       // ⚪ 可选：扩展参数
}
```

**字段说明**:

| 字段 | 类型 | 必填 | 验证规则 | 说明 |
|------|------|------|---------|------|
| orderNo | String | ✅ | @NotBlank | 关联订单编号 |
| orderId | Long | ❌ | - | 关联订单ID |
| userId | Long | ❌ | - | 用户ID |
| paymentMethod | Integer | ✅ | @NotNull | 支付方式：1-微信，2-支付宝，3-现金，4-会员卡，5-银行卡 |
| currency | String | ❌ | - | 货币类型，默认CNY |
| subject | String | ❌ | @Size(max=200) | 支付主题 |
| body | String | ❌ | @Size(max=500) | 支付描述 |
| clientIp | String | ❌ | - | 客户端IP地址 |
| deviceInfo | String | ❌ | - | 设备信息 |
| notifyUrl | String | ❌ | - | 异步通知地址 |
| returnUrl | String | ❌ | - | 同步返回地址 |
| extraParams | String | ❌ | - | 扩展参数，JSON格式 |

**❌ 已删除的字段**（v2.0优化）:

| 字段 | 删除原因 | 新方案 |
|------|---------|--------|
| paymentAmount | 客户端不应传递金额 | 服务端从order-service获取actualAmount |
| shopId | 冗余字段 | 服务端从order-service获取shopId |

**设计优势**:
- 🔒 **安全性**: 支付金额由服务端控制，防止篡改
- 🎯 **准确性**: 使用订单实际金额
- 🛡️ **防重复支付**: 服务端验证订单支付状态
- 🚀 **简洁性**: 减少2个敏感字段

**使用示例**:
```json
{
  "orderNo": "ORD2026051700001",
  "userId": 1001,
  "paymentMethod": 1,
  "subject": "美味餐厅订单支付",
  "body": "宫保鸡丁等3件商品",
  "clientIp": "192.168.1.100"
}
```

---

## 🔄 DTO与Entity的映射

### PaymentOrderCreateRequest → PaymentOrder

```java
// Controller层
@PostMapping
public Result<Boolean> createPayment(@RequestBody @Valid PaymentOrderCreateRequest request) {
    // 1. 调用order-service验证订单
    Result<OrderInfoDTO> orderResult = orderFeignClient.getOrderByOrderNo(request.getOrderNo());
    
    if (orderResult == null || orderResult.getData() == null) {
        return Result.error("订单不存在");
    }
    
    OrderInfoDTO orderInfo = orderResult.getData();
    
    // 2. 验证订单是否已支付
    if (orderInfo.getPaymentStatus() != null && orderInfo.getPaymentStatus() == 1) {
        return Result.error("订单已支付，请勿重复支付");
    }
    
    // 3. 创建支付订单实体
    PaymentOrder payment = new PaymentOrder();
    BeanUtils.copyProperties(request, payment);
    
    // 4. 服务端设置字段
    payment.setPaymentNo(PaymentNoGenerator.generate());    // 生成支付单号
    payment.setPaymentAmount(orderInfo.getActualAmount());  // ✅ 从订单获取金额
    payment.setShopId(orderInfo.getShopId());               // ✅ 从订单获取店铺ID
    payment.setPaymentStatus(0);                            // 默认待支付
    payment.setCurrency("CNY");                             // 默认人民币
    
    // 5. 保存支付订单
    boolean success = paymentOrderService.save(payment);
    
    return Result.success(true);
}
```

---

## 🎨 设计规范

### 1. 命名规范
- Request DTO: `{功能}Request`
- Response DTO: `{功能}Response` 或 `{功能}DTO`
- 使用驼峰命名法

### 2. 验证注解
- 必填字段: `@NotNull`, `@NotBlank`
- 字符串长度: `@Size(min=1, max=500)`
- 格式验证: `@Email`, `@Pattern`

### 3. 注释规范
- 每个字段必须有中文注释
- 标注必填/可选
- 说明枚举值的含义

---

## 📝 最佳实践

### ✅ 推荐做法

1. **金额由服务端控制**
   ```java
   // ✅ 好：从order-service获取金额
   payment.setPaymentAmount(orderInfo.getActualAmount());
   ```

2. **验证订单状态**
   ```java
   // ✅ 好：防止重复支付
   if (orderInfo.getPaymentStatus() == 1) {
       return Result.error("订单已支付");
   }
   ```

### ❌ 避免做法

1. **客户端传递金额**
   ```java
   // ❌ 坏：不安全
   private BigDecimal paymentAmount;  // 不应该由客户端传递
   ```

2. **不验证订单状态**
   ```java
   // ❌ 坏：可能导致重复支付
   paymentOrderService.save(payment);
   ```

---

## 🔄 版本历史

### v2.0 (2026-05-18)
- ✅ 删除paymentAmount和shopId字段
- ✅ 添加order-service验证逻辑
- ✅ 提高安全性

### v1.0 (初始版本)
- 包含所有字段
- 客户端传递金额

---

**维护者**: 开发团队  
**最后更新**: 2026-05-18  
**文档版本**: v2.0
