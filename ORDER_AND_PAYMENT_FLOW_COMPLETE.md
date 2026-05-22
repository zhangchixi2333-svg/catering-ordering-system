# 订单和支付流程完整说明

## 📋 概述

本文档详细说明餐饮点餐排队系统的订单创建和支付流程，包括前端界面、后端接口和业务流程。

---

## 🎯 核心业务流程

### 1. 订单创建流程

```
用户在线点餐
    ↓
选择菜品和数量
    ↓
提交订单（调用 order-service）
    ↓
【后端处理】
1. 验证店铺是否存在且营业中（shop-service）
2. 验证排队号码是否已叫号（queue-service，如果有）
3. 自动分配空闲桌子或获取指定桌子的编号（shop-service）
4. 计算菜品制作时间，估算订单完成时间（menu-service）
5. 服务端计算订单总金额和数量
6. 保存订单到数据库
7. 更新桌台状态为"已占用"（shop-service）
8. 更新排队状态为"已入座"（queue-service）
9. 发送WebSocket通知给用户（notification-service）
    ↓
订单创建成功
```

**关键点**：
- ✅ 订单金额由服务端计算，防止客户端篡改
- ✅ 自动分配桌子（堂食场景）
- ✅ 自动计算 estimated_time（基于最长菜品制作时间 + 5分钟缓冲）
- ✅ 自动填充 tableNumber（从 shop-service 查询）
- ✅ 自动更新桌台状态和排队状态

---

### 2. 支付流程

```
用户在"我的订单"页面点击"去支付"
    ↓
【前端处理】
1. 调用 paymentApi.create({ orderNo })
2. 创建支付订单（状态：支付中）
3. 获取支付订单ID
4. 跳转到支付页面 /payment?id={paymentId}
    ↓
【支付页面】
1. 显示支付信息（订单号、支付单号、金额）
2. 用户选择支付方式（微信/支付宝/现金/会员卡/银行卡）
3. 点击"确认支付"
    ↓
【后端处理 - 沙盒测试】
1. 更新支付订单的 paymentMethod
2. 调用沙盒接口模拟支付成功
3. 更新支付状态为"支付成功"
4. 调用 order-service 更新订单状态为"待接单"
    ↓
支付成功，跳转到"支付订单"页面
```

**关键点**：
- ✅ 创建支付订单时不需要填写支付方式
- ✅ 支付页面让用户选择支付方式
- ✅ 支付成功后自动更新订单状态
- ✅ 沙盒测试模拟真实支付流程

---

## 🔧 技术实现细节

### 1. 订单创建接口

**接口**: `POST /api/order`  
**服务**: order-service (端口 8083)

**请求体**:
```json
{
  "shopId": 1,
  "userId": 1001,
  "orderType": 1,
  "queueNumber": "A001",
  "tableId": null,
  "remark": "不要辣",
  "items": [
    {
      "itemId": 1,
      "itemName": "宫保鸡丁",
      "price": 38.00,
      "quantity": 2,
      "remark": "微辣"
    }
  ]
}
```

**后端处理逻辑**:

#### 步骤1: 验证店铺
```java
Result<ShopFeignClient.ShopInfoDTO> shopResult = shopFeignClient.getShopById(shopId);
if (!shop.getData().isOpen()) {
    return Result.error("店铺当前未营业");
}
```

#### 步骤2: 验证排队（如果有 queueNumber）
```java
if (queueNumber != null) {
    Result<QueueFeignClient.QueueInfoDTO> queueResult = queueFeignClient.getQueueByNo(queueNumber);
    if (queueResult.getData().getQueueStatus() != 1) {
        return Result.error("排队记录未被叫号");
    }
}
```

#### 步骤3: 自动分配桌子
```java
if (orderType == 1 && tableId == null) {
    // 堂食且未指定桌子，自动分配
    Result<List<TableFeignClient.TableInfoDTO>> tablesResult = 
        tableFeignClient.getAvailableTables(shopId);
    tableId = tablesResult.getData().get(0).getId();
}
```

#### 步骤4: 获取桌台编号
```java
if (tableId != null) {
    Result<TableFeignClient.TableInfoDTO> tableResult = tableFeignClient.getTableById(tableId);
    tableNumber = tableResult.getData().getTableNumber();
}
```

#### 步骤5: 计算 estimated_time
```java
int maxPrepareTime = 0;
for (OrderItemRequest item : items) {
    Result<MenuFeignClient.MenuItemInfoDTO> itemResult = menuFeignClient.getMenuItemById(item.getItemId());
    Integer prepareTime = itemResult.getData().getPrepareTime();
    if (prepareTime > maxPrepareTime) {
        maxPrepareTime = prepareTime;
    }
}
int estimatedTime = maxPrepareTime > 0 ? maxPrepareTime + 5 : 15;
```

#### 步骤6: 计算订单金额
```java
BigDecimal totalAmount = BigDecimal.ZERO;
int itemCount = 0;
for (OrderItemRequest item : items) {
    totalAmount = totalAmount.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
    itemCount += item.getQuantity();
}
```

#### 步骤7: 保存订单
```java
Orders order = new Orders();
order.setOrderNo(OrderNoGenerator.generate());
order.setTotalAmount(totalAmount);
order.setEstimatedTime(estimatedTime);
order.setTableNumber(tableNumber);
ordersService.save(order);
```

#### 步骤8: 更新桌台状态
```java
if (tableId != null) {
    tableFeignClient.updateTableStatus(tableId, 1); // 1-已占用
}
```

#### 步骤9: 更新排队状态
```java
if (queueNumber != null) {
    queueFeignClient.updateQueueStatus(queueNumber, 2); // 2-已入座
}
```

---

### 2. 支付创建接口

**接口**: `POST /api/payment`  
**服务**: payment-service (端口 8084)

**请求体**:
```json
{
  "orderNo": "ORD20260520710935"
}
```

**注意**: 只需要传入 orderNo，其他字段由后端自动填充：
- paymentAmount: 从订单获取
- shopId: 从订单获取
- paymentStatus: 自动设置为 1（支付中）
- paymentNo: 自动生成

**后端处理**:
```java
// 1. 调用 order-service 验证订单
Result<OrderFeignClient.OrderInfoDTO> orderResult = orderFeignClient.getOrderByOrderNo(orderNo);

// 2. 创建支付订单
PaymentOrder payment = new PaymentOrder();
payment.setPaymentNo(PaymentNoGenerator.generate());
payment.setPaymentAmount(orderInfo.getActualAmount()); // 从订单获取
payment.setShopId(orderInfo.getShopId());               // 从订单获取
payment.setPaymentStatus(1); // 支付中
paymentOrderService.save(payment);
```

---

### 3. 支付页面

**路由**: `/payment?id={paymentId}`  
**文件**: `frontend/src/views/PaymentView.vue`

**功能**:
1. 显示支付信息（订单号、支付单号、金额）
2. 让用户选择支付方式
3. 调用后端更新支付方式
4. 调用沙盒接口模拟支付成功

**代码示例**:
```javascript
// 选择支付方式
const selectMethod = (code) => {
  selectedMethod.value = code
}

// 确认支付
const handleConfirmPayment = async () => {
  // 1. 更新支付方式
  await paymentApi.update(paymentId, {
    paymentMethod: selectedMethod.value
  })
  
  // 2. 调用沙盒接口
  const result = await fetch(`/api/payment/sandbox/success/${paymentId}`, {
    method: 'POST'
  })
  
  if (result.code === 200) {
    ElMessage.success('支付成功！')
    router.push('/payment-orders')
  }
}
```

---

### 4. 沙盒测试接口

**接口**: `POST /api/payment/sandbox/success/{id}`  
**服务**: payment-service

**支持的支付方式**:
- 1 - 微信支付
- 2 - 支付宝
- 3 - 现金
- 4 - 会员卡
- 5 - 银行卡

**处理逻辑**:
```java
@PostMapping("/sandbox/success/{id}")
public Result<Boolean> simulateSuccess(@PathVariable Long id) {
    // 1. 查询支付订单
    PaymentOrder payment = paymentOrderService.getById(id);
    
    // 2. 生成模拟交易号
    String transactionId = generateTransactionId(payment.getPaymentMethod());
    
    // 3. 更新支付状态
    payment.setPaymentStatus(2); // 支付成功
    payment.setTransactionId(transactionId);
    payment.setPayTime(LocalDateTime.now());
    paymentOrderService.updateById(payment);
    
    // 4. 调用 order-service 更新订单状态
    orderFeignClient.updateOrderStatus(payment.getOrderId(), 1); // 1-待接单
    
    return Result.success(true);
}
```

---

## 📊 状态流转图

### 订单状态流转

```
待支付(0) → 待接单(1) → 制作中(2) → 待取餐(3) → 已完成(4)
                ↓
            已取消(5)
```

**触发条件**:
- 待支付 → 待接单: 支付成功
- 待接单 → 制作中: 店员接单
- 制作中 → 待取餐: 制作完成
- 待取餐 → 已完成: 用户取餐
- 任意状态 → 已取消: 用户或店员取消

---

### 支付状态流转

```
待支付(0) → 支付中(1) → 支付成功(2)
                        ↓
                    支付失败(3) → 已退款(4)
```

**触发条件**:
- 待支付 → 支付中: 创建支付订单
- 支付中 → 支付成功: 支付完成
- 支付中 → 支付失败: 支付失败
- 支付成功 → 已退款: 申请退款

---

### 桌台状态流转

```
空闲(0) → 已占用(1) → 清洁中(2) → 空闲(0)
```

**触发条件**:
- 空闲 → 已占用: 订单创建
- 已占用 → 清洁中: 店员点击"清洁桌面"
- 清洁中 → 空闲: 店员点击"清洁完成"

---

### 排队状态流转

```
等待中(0) → 已叫号(1) → 已入座(2) → 已完成(3)
                              ↓
                          已取消(4)
```

**触发条件**:
- 等待中 → 已叫号: 店员叫号
- 已叫号 → 已入座: 用户下单
- 已入座 → 已完成: 用餐完成
- 任意状态 → 已取消: 用户取消

---

## 🎨 前端页面说明

### 1. 我的订单页面

**路径**: `/my-orders`  
**文件**: `frontend/src/views/MyOrdersView.vue`

**功能**:
- ✅ 查看用户的所有订单
- ✅ 按状态筛选订单
- ✅ 取消订单（待接单状态）
- ✅ 去支付（已完成但未支付）
- ✅ 查看订单详情

**关键代码**:
```javascript
// 去支付
const handlePay = async (order) => {
  // 1. 创建支付订单
  const result = await paymentApi.create({
    orderNo: order.orderNo
  })
  
  // 2. 获取支付订单ID
  const paymentResult = await paymentApi.getByOrderNo(order.orderNo)
  
  // 3. 跳转到支付页面
  router.push({
    path: '/payment',
    query: { id: paymentResult.data.id }
  })
}
```

---

### 2. 支付页面

**路径**: `/payment?id={paymentId}`  
**文件**: `frontend/src/views/PaymentView.vue`

**功能**:
- ✅ 显示支付信息
- ✅ 选择支付方式（5种）
- ✅ 确认支付
- ✅ 沙盒测试提示

**UI设计**:
```
┌─────────────────────────────────┐
│ 💳 确认支付                      │
├─────────────────────────────────┤
│ 订单编号：ORD20260520710935     │
│ 支付单号：PAY2026052100001      │
│ 支付金额：¥94.00                │
├─────────────────────────────────┤
│ 选择支付方式：                   │
│ ┌─────────────────────────────┐ │
│ │ 💚 微信支付           ✓    │ │
│ │ 💙 支付宝                  │ │
│ │ 💵 现金                    │ │
│ │ 💳 会员卡                  │ │
│ │ 🏦 银行卡                  │ │
│ └─────────────────────────────┘ │
│                                 │
│ [取消]  [确认支付]              │
└─────────────────────────────────┘
```

---

### 3. 支付订单页面

**路径**: `/payment-orders`  
**文件**: `frontend/src/views/PaymentOrdersView.vue`

**功能**:
- ✅ 查看用户的所有支付记录
- ✅ 按状态筛选
- ✅ 模拟支付成功（沙盒测试）
- ✅ 查看支付详情

---

## 📝 API 文档

### Order Service (8083)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/order | 创建订单 |
| GET | /api/order/list | 获取所有订单 |
| GET | /api/order/{id} | 根据ID获取订单 |
| GET | /api/order/no/{orderNo} | 根据订单号获取 |
| GET | /api/order/user/{userId} | 根据用户获取 |
| PUT | /api/order/{id}/status | 更新订单状态 |
| PUT | /api/order/{id}/cancel | 取消订单 |

### Payment Service (8084)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/payment | 创建支付订单 |
| GET | /api/payment/list | 获取所有支付订单 |
| GET | /api/payment/{id} | 根据ID获取 |
| GET | /api/payment/no/{paymentNo} | 根据支付单号获取 |
| GET | /api/payment/order/{orderNo} | 根据订单号获取 |
| PUT | /api/payment/{id} | 更新支付订单 |
| PUT | /api/payment/{id}/status | 更新支付状态 |
| POST | /api/payment/sandbox/success/{id} | 沙盒测试-支付成功 |

---

## ⚠️ 注意事项

### 1. 订单创建
- ✅ 订单金额由服务端计算，不使用前端传入的值
- ✅ 自动分配桌子需要 shop-service 正常运行
- ✅ estimated_time 基于最长菜品制作时间 + 5分钟
- ✅ 桌台状态更新失败不影响订单创建（降级策略）

### 2. 支付流程
- ✅ 创建支付订单时不需要 paymentMethod
- ✅ 支付页面让用户选择支付方式
- ✅ 沙盒测试仅用于开发环境
- ✅ 支付成功后自动更新订单状态

### 3. 前端配置
- ✅ Vite 代理必须配置 payment-service
- ✅ 修改 vite.config.js 后需要重启前端服务
- ✅ 支付页面需要传递 paymentId 参数

---

## ✅ 测试清单

### 订单创建测试
- [ ] 验证店铺是否存在
- [ ] 验证排队是否已叫号
- [ ] 自动分配桌子功能
- [ ] 获取桌台编号功能
- [ ] 计算 estimated_time
- [ ] 计算订单金额
- [ ] 更新桌台状态
- [ ] 更新排队状态

### 支付流程测试
- [ ] 创建支付订单
- [ ] 跳转到支付页面
- [ ] 选择支付方式
- [ ] 确认支付
- [ ] 沙盒测试成功
- [ ] 订单状态更新

### 前端测试
- [ ] 我的订单页面正常显示
- [ ] 支付页面正常显示
- [ ] 支付订单页面正常显示
- [ ] 路由跳转正常
- [ ] API 调用正常

---

## 🔗 相关文件

### 后端
- `order-service/src/main/java/org/example/orderservice/controller/OrdersController.java`
- `payment-service/src/main/java/org/example/paymentservice/controller/PaymentOrderController.java`
- `payment-service/src/main/java/org/example/paymentservice/controller/PaymentSandboxController.java`

### 前端
- `frontend/src/views/MyOrdersView.vue`
- `frontend/src/views/PaymentView.vue`
- `frontend/src/views/PaymentOrdersView.vue`
- `frontend/src/router/index.js`
- `frontend/src/api/index.js`
- `frontend/vite.config.js`

---

## 📅 后续优化建议

1. **桌台管理页面** - 店员端查看和管理桌台状态
2. **实时通知** - WebSocket 推送订单状态变化
3. **支付超时** - 超过30分钟未支付自动取消
4. **退款功能** - 支持申请退款和处理退款
5. **评价功能** - 订单完成后可评价
6. **统计报表** - 订单和支付统计分析

---

**文档版本**: v1.0  
**更新时间**: 2026-05-21  
**维护者**: Development Team
