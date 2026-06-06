# 支付沙盒快速测试指南

## 🚀 快速开始

### 1. 启动服务

确保以下服务已启动：
- ✅ eureka-server (端口 8761)
- ✅ order-service (端口 8083)
- ✅ payment-service (端口 8084)

---

### 2. 打开测试页面

直接双击打开文件：
```
payment-service/sandbox-test.html
```

或者在浏览器中访问：
```
file:///C:/Users/lenovo/IdeaProjects/springcloud/CateringOrderingAndQueuingSystem/payment-service/sandbox-test.html
```

---

## 📝 测试步骤

### 步骤1：创建支付订单

1. 输入订单编号（例如：ORD20260520710935）
2. **选择支付方式**（微信/支付宝/现金/会员卡/银行卡）
3. 点击"创建支付订单"按钮
4. 查看结果，系统会自动填充支付ID

---

### 步骤2：模拟支付成功

1. 确认支付ID已自动填充
2. 点击对应的支付方式按钮：
   - 💚 微信支付
   - 💙 支付宝
   - 💵 现金支付
   - 💳 会员卡支付
   - 🏦 银行卡支付
3. 查看结果和日志

---

### 步骤3：验证结果

#### 方式1：查看日志
测试页面底部会显示所有操作日志

#### 方式2：查询数据库
```sql
-- 查询支付订单
SELECT * FROM payment_order WHERE order_no = 'ORD20260520710935';

-- 预期结果：
-- payment_status = 2 (支付成功)
-- transaction_id = WXxxx 或 ALIxxx 等
-- pay_time = 当前时间
```

#### 方式3：调用API
```bash
# 查询支付订单
curl http://localhost:8084/api/payment/order/ORD20260520710935

# 查询订单状态
curl http://localhost:8083/api/order/no/ORD20260520710935
```

---

## 🔧 常见问题

### Q1: 提示"订单不存在"？

**原因**：order-service 中没有该订单

**解决**：
1. 先在 order-service 中创建订单
2. 或使用已有的订单编号

---

### Q2: 提示"订单服务暂时不可用"？

**原因**：order-service 未启动或未注册到 Eureka

**解决**：
1. 检查 order-service 是否启动
2. 访问 http://localhost:8761 查看服务注册状态

---

### Q3: 支付ID如何获取？

**方式1**：创建订单后，测试页面会自动查询并填充

**方式2**：从数据库查询
```sql
SELECT id FROM payment_order WHERE order_no = 'ORD20260520710935';
```

**方式3**：调用API
```bash
curl http://localhost:8084/api/payment/order/ORD20260520710935
```

---

## 📊 支付方式代码

| 代码 | 支付方式 | 交易号前缀 |
|------|---------|-----------|
| 1 | 微信支付 | WX |
| 2 | 支付宝 | ALI |
| 3 | 现金支付 | CASH |
| 4 | 会员卡支付 | MEMBER |
| 5 | 银行卡支付 | BANK |

---

## 🎯 完整测试示例

### 测试微信支付

```bash
# 1. 创建支付订单
curl -X POST http://localhost:8084/api/payment \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD20260520710935",
    "userId": 1001,
    "paymentMethod": 1
  }'

# 2. 假设返回的支付ID为1，模拟支付成功
curl -X POST http://localhost:8084/api/payment/sandbox/success/1

# 3. 查询结果
curl http://localhost:8084/api/payment/order/ORD20260520710935
```

---

### 测试支付宝

```bash
# 1. 创建支付订单（使用支付宝）
curl -X POST http://localhost:8084/api/payment \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD20260520710935",
    "userId": 1001,
    "paymentMethod": 2
  }'

# 2. 模拟支付宝支付成功
curl -X POST http://localhost:8084/api/payment/sandbox/success/2
```

---

### 测试现金支付

```bash
# 1. 创建支付订单（使用现金）
curl -X POST http://localhost:8084/api/payment \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD20260520710935",
    "userId": 1001,
    "paymentMethod": 3
  }'

# 2. 模拟现金支付确认
curl -X POST http://localhost:8084/api/payment/sandbox/success/3
```

---

## 💡 提示

1. **每次测试前**：确保使用不同的订单编号，或重置之前的订单状态
2. **查看日志**：payment-service 控制台会输出详细的处理日志
3. **状态流转**：创建订单 → 支付中(1) → 支付成功(2)
4. **订单同步**：支付成功后会自动调用 order-service 更新订单状态

---

## 🎉 完成！

现在你可以：
- ✅ 测试5种不同的支付方式
- ✅ 模拟支付成功、失败、超时等场景
- ✅ 查看完整的状态流转过程
- ✅ 验证订单同步机制

祝测试顺利！🚀
