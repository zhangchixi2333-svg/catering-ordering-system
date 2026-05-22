# 支付沙盒测试指南

## 📋 概述

支付沙盒系统用于在开发和测试环境中模拟各种支付方式的回调，无需真正对接第三方支付平台。

---

## 🎯 支持的支付方式

| 代码 | 支付方式 | 说明 |
|------|---------|------|
| 1 | 微信支付 | 模拟微信支付成功回调 |
| 2 | 支付宝 | 模拟支付宝成功回调 |
| 3 | 现金支付 | 模拟现金支付确认 |
| 4 | 会员卡支付 | 模拟会员卡扣款 |
| 5 | 银行卡支付 | 模拟银行卡转账 |

---

## 🚀 快速开始

### 方式1：使用HTML测试页面（推荐）

1. **启动 payment-service**
   ```bash
   # 确保 payment-service 已启动在 8084 端口
   ```

2. **打开测试页面**
   ```
   直接在浏览器中打开：
   payment-service/sandbox-test.html
   ```

3. **测试流程**
   - 步骤1：输入订单编号，创建支付订单
   - 步骤2：选择支付方式，模拟支付成功
   - 步骤3：查看操作日志，确认状态更新

---

### 方式2：使用 Swagger UI

1. **访问 Swagger 文档**
   ```
   http://localhost:8084/doc.html
   ```

2. **找到"支付沙盒测试"模块**

3. **测试接口**
   - `POST /api/payment/sandbox/success/{paymentId}` - 模拟支付成功
   - `POST /api/payment/sandbox/failure/{paymentId}` - 模拟支付失败
   - `POST /api/payment/sandbox/timeout/{paymentId}` - 模拟支付超时

---

### 方式3：使用 cURL 命令

#### 1. 创建支付订单

```bash
curl -X POST http://localhost:8084/api/payment \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD20260520710935",
    "userId": 1001,
    "paymentMethod": 1
  }'
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

---

#### 2. 模拟微信支付成功

```bash
curl -X POST http://localhost:8084/api/payment/sandbox/success/1
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**后台日志**：
```
========== 🧪 沙盒测试：模拟支付成功 ==========
【支付ID】1
【订单编号】ORD20260520710935
【订单ID】26
【支付金额】¥94.00
【支付方式】微信支付
【模拟交易号】WX1716278400123456
✅ 支付订单状态已更新为：支付成功

【步骤5】调用 order-service 更新订单状态...
✅ 订单状态更新成功 - 订单ID: 26, 状态: 待接单
==================================================
```

---

#### 3. 模拟支付宝支付成功

```bash
curl -X POST http://localhost:8084/api/payment/sandbox/success/2
```

---

#### 4. 模拟现金支付确认

```bash
curl -X POST http://localhost:8084/api/payment/sandbox/success/3
```

---

#### 5. 模拟会员卡支付

```bash
curl -X POST http://localhost:8084/api/payment/sandbox/success/4
```

---

#### 6. 模拟银行卡支付

```bash
curl -X POST http://localhost:8084/api/payment/sandbox/success/5
```

---

#### 7. 模拟支付失败

```bash
curl -X POST "http://localhost:8084/api/payment/sandbox/failure/1?reason=用户取消支付"
```

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

---

#### 8. 模拟支付超时

```bash
curl -X POST http://localhost:8084/api/payment/sandbox/timeout/1
```

---

## 📊 状态流转

### 正常支付流程

```
创建支付订单
    ↓
paymentStatus = 1 (支付中)
    ↓
用户选择支付方式
    ↓
模拟支付成功 (沙盒)
    ↓
paymentStatus = 2 (支付成功)
transactionId = WXxxx (生成交易号)
payTime = 当前时间
    ↓
调用 order-service
    ↓
orderStatus = 1 (待接单)
```

### 支付失败流程

```
创建支付订单
    ↓
paymentStatus = 1 (支付中)
    ↓
模拟支付失败 (沙盒)
    ↓
paymentStatus = 3 (支付失败)
```

---

## 🔍 验证结果

### 1. 查询支付订单状态

```bash
curl http://localhost:8084/api/payment/order/ORD20260520710935
```

**预期响应**：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "paymentNo": "PAY2026052100001",
    "orderNo": "ORD20260520710935",
    "paymentAmount": 94.00,
    "paymentMethod": 1,
    "paymentStatus": 2,  // ✅ 支付成功
    "transactionId": "WX1716278400123456",
    "payTime": "2026-05-21T01:45:00"
  }
}
```

---

### 2. 查询订单状态

```bash
curl http://localhost:8083/api/order/no/ORD20260520710935
```

**预期响应**：
```json
{
  "code": 200,
  "data": {
    "id": 26,
    "orderNo": "ORD20260520710935",
    "orderStatus": 1,  // ✅ 待接单
    "paymentStatus": 1  // ✅ 已支付
  }
}
```

---

## 💡 常见问题

### Q1: 提示"支付订单不存在"？

**原因**：支付ID不正确或订单未创建

**解决**：
1. 检查支付ID是否正确
2. 先调用 `POST /api/payment` 创建支付订单
3. 从响应或数据库中获取正确的支付ID

---

### Q2: 提示"该支付订单已完成"？

**原因**：订单已经处于支付成功或更高状态

**解决**：
1. 查询当前订单状态
2. 使用新的订单进行测试
3. 或在数据库中重置订单状态

---

### Q3: order-service 调用失败？

**原因**：order-service 未启动或网络问题

**解决**：
1. 检查 order-service 是否启动（端口 8083）
2. 检查 Eureka 注册状态
3. 查看 payment-service 日志中的详细错误信息

**注意**：即使 order-service 调用失败，支付订单状态仍会更新为"支付成功"，只是订单状态未同步更新。

---

### Q4: 如何批量测试？

可以使用循环脚本：

```bash
#!/bin/bash
for i in {1..10}; do
  echo "测试支付ID: $i"
  curl -X POST http://localhost:8084/api/payment/sandbox/success/$i
  echo ""
  sleep 1
done
```

---

## ⚠️ 注意事项

1. **仅用于测试环境**
   - 沙盒接口不应在生产环境启用
   - 建议通过配置文件控制是否启用沙盒

2. **数据隔离**
   - 测试时使用测试数据库
   - 避免污染生产数据

3. **交易号生成**
   - 沙盒生成的交易号为模拟数据
   - 格式：`{前缀}{时间戳}{随机4位数}`
   - 例如：`WX17162784001234`

4. **幂等性**
   - 重复调用成功接口不会重复更新
   - 系统会检查当前状态，避免重复处理

---

## 🎨 HTML测试页面功能

### 主要功能

1. **创建支付订单**
   - 输入订单编号
   - 自动查询并填充支付ID

2. **模拟支付成功**
   - 支持5种支付方式
   - 彩色按钮，直观易用
   - 实时显示测试结果

3. **模拟支付失败**
   - 自定义失败原因
   - 一键测试失败场景

4. **操作日志**
   - 实时记录所有操作
   - 彩色区分成功/失败/信息
   - 方便调试和排查问题

---

## 📝 支付方式映射表

| 前端传值 | 数据库存储 | 交易号前缀 | 显示名称 |
|---------|-----------|-----------|---------|
| 1 | 1 | WX | 微信支付 |
| 2 | 2 | ALI | 支付宝 |
| 3 | 3 | CASH | 现金支付 |
| 4 | 4 | MEMBER | 会员卡支付 |
| 5 | 5 | BANK | 银行卡支付 |

---

## 🔗 相关接口

### 创建支付订单
- **接口**: `POST /api/payment`
- **说明**: 创建支付订单，状态自动设为"支付中"

### 支付成功回调（真实）
- **接口**: `PUT /api/payment/{id}/success`
- **说明**: 第三方支付平台真实回调接口

### 查询支付订单
- **接口**: `GET /api/payment/order/{orderNo}`
- **说明**: 根据订单编号查询支付信息

---

## 🎉 总结

支付沙盒系统提供了完整的测试环境，支持：
- ✅ 5种支付方式模拟
- ✅ 成功/失败/超时场景测试
- ✅ 可视化HTML测试页面
- ✅ 详细的日志输出
- ✅ 自动更新订单状态

使用沙盒可以大幅提高开发效率，无需依赖真实的第三方支付平台！
