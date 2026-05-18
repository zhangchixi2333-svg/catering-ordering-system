# Order Service DTO 设计文档

## 📋 文档说明

本文档详细说明Order Service中使用的DTO（Data Transfer Object）设计原则和结构。

---

## 🎯 设计原则

### 1. 职责分离
- **Request DTO**: 接收客户端请求数据
- **Response DTO**: 返回给客户端的响应数据
- **Entity**: 数据库实体对象

### 2. 安全性优先
- 敏感字段（金额、数量）由服务端计算，不从客户端接收
- 防止客户端篡改关键数据

### 3. 最小化原则
- Request DTO只包含客户端必须提供的字段
- 可选字段提供默认值

---

## 📦 DTO列表

### 1. OrderCreateRequest ⭐核心DTO

**用途**: 创建订单时的请求参数

**设计版本**: v2.0 (已优化)

**字段列表**:
```java
public class OrderCreateRequest {
    @NotNull(message = "店铺ID不能为空")
    private Long shopId;              // ✅ 必填：店铺ID
    
    private Long tableId;             // ⚪ 可选：桌台ID（堂食必填）
    
    private Long userId;              // ⚪ 可选：用户ID
    
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;        // ✅ 必填：订单类型（1-堂食，2-外带，3-外卖）
    
    private Long queueId;             // ⚪ 可选：排队ID
    
    private String remark;            // ⚪ 可选：订单备注（最多500字符）
}
```

**字段说明**:

| 字段 | 类型 | 必填 | 验证规则 | 说明 |
|------|------|------|---------|------|
| shopId | Long | ✅ | @NotNull | 店铺ID |
| tableId | Long | ❌ | - | 桌台ID，堂食时必填 |
| userId | Long | ❌ | - | 用户ID |
| orderType | Integer | ✅ | @NotNull | 订单类型：1-堂食，2-外带，3-外卖 |
| queueId | Long | ❌ | - | 排队ID，关联排队号 |
| remark | String | ❌ | @Size(max=500) | 订单备注 |

**❌ 已删除的字段**（v2.0优化）:

| 字段 | 删除原因 | 新方案 |
|------|---------|--------|
| totalAmount | 客户端不应计算金额 | 服务端根据菜品价格计算 |
| actualAmount | 客户端不应计算金额 | 服务端计算 |
| itemCount | 客户端不应统计数量 | 服务端统计order_item数量 |
| tableNumber | 冗余字段 | 服务端通过tableId查询 |
| queueNumber | 冗余字段 | 服务端通过queueId查询 |
| priority | 业务逻辑字段 | 服务端设置默认值0 |
| estimatedTime | 需要计算 | 服务端根据菜品制作时间计算 |
| discountAmount | 暂未使用 | 后续版本再添加 |
| paymentMethod | 支付时设置 | 在payment-service中设置 |

**设计优势**:
- 🔒 **安全性**: 金额相关字段由服务端控制，防止篡改
- 🎯 **准确性**: 使用服务端最新价格计算
- 📊 **一致性**: 统一的计算逻辑
- 🚀 **简洁性**: 减少9个字段，API更清晰

**使用示例**:
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

---

## 🔄 DTO与Entity的映射

### OrderCreateRequest → Orders

```java
// Controller层
@PostMapping
public Result<Boolean> createOrder(@RequestBody @Valid OrderCreateRequest request) {
    // 1. 验证店铺
    Result<ShopInfoDTO> shopResult = shopFeignClient.getShopById(request.getShopId());
    
    // 2. 验证排队（如果有）
    if (request.getQueueId() != null) {
        Result<QueueInfoDTO> queueResult = queueFeignClient.getQueueById(request.getQueueId());
    }
    
    // 3. 创建订单实体
    Orders order = new Orders();
    BeanUtils.copyProperties(request, order);
    
    // 4. 服务端设置字段
    order.setOrderNo(OrderNoGenerator.generate());          // 生成订单号
    order.setTotalAmount(calculatedTotal);                  // 计算总金额
    order.setActualAmount(calculatedTotal);                 // 实付金额
    order.setItemCount(totalQuantity);                      // 统计菜品数量
    order.setOrderStatus(0);                                // 默认待支付
    order.setPaymentStatus(0);                              // 默认未支付
    order.setPriority(0);                                   // 默认普通优先级
    
    // 5. 保存订单
    boolean success = ordersService.save(order);
    
    return Result.success(true);
}
```

---

## 📊 其他DTO

### 2. OrderUpdateRequest

**用途**: 更新订单信息

**字段列表**:
```java
public class OrderUpdateRequest {
    @NotNull
    private Long id;                    // 订单ID
    
    private String remark;              // 订单备注
    private Integer orderStatus;        // 订单状态
}
```

---

## 🎨 设计规范

### 1. 命名规范
- Request DTO: `{功能}Request`，如 `OrderCreateRequest`
- Response DTO: `{功能}Response` 或 `{功能}DTO`，如 `OrderInfoDTO`
- 使用驼峰命名法

### 2. 验证注解
- 必填字段: `@NotNull`, `@NotBlank`
- 字符串长度: `@Size(min=1, max=500)`
- 数值范围: `@Min(1)`, `@Max(100)`
- 格式验证: `@Email`, `@Pattern`

### 3. 注释规范
- 每个字段必须有中文注释
- 标注必填/可选
- 说明枚举值的含义

---

## 📝 最佳实践

### ✅ 推荐做法

1. **Request DTO精简**
   ```java
   // ✅ 好：只包含必要字段
   private Long shopId;
   private Integer orderType;
   ```

2. **服务端计算敏感数据**
   ```java
   // ✅ 好：服务端计算金额
   order.setTotalAmount(service.calculateTotal(items));
   ```

3. **提供默认值**
   ```java
   // ✅ 好：设置默认值
   if (order.getPriority() == null) {
       order.setPriority(0);
   }
   ```

### ❌ 避免做法

1. **Request包含过多字段**
   ```java
   // ❌ 坏：包含计算字段
   private BigDecimal totalAmount;  // 不应该由客户端传递
   private Integer itemCount;       // 不应该由客户端统计
   ```

2. **直接使用Entity接收请求**
   ```java
   // ❌ 坏：暴露所有字段
   public Result createOrder(@RequestBody Orders order) { ... }
   ```

---

## 🔄 版本历史

### v2.0 (2026-05-18)
- ✅ 删除9个冗余字段
- ✅ 强化服务端计算逻辑
- ✅ 提高安全性

### v1.0 (初始版本)
- 包含所有字段
- 客户端传递金额

---

**维护者**: 开发团队  
**最后更新**: 2026-05-18  
**文档版本**: v2.0
