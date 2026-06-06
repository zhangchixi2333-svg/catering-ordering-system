# 支付功能前端调用指南

## ✅ 已修复的问题

### 1. Vite 代理配置
**问题**: 前端请求 `/api/payment` 时，Vite 没有代理到 payment-service  
**修复**: 在 `vite.config.js` 中添加了 payment-service 的代理配置

```javascript
'/api/payment': {
  target: 'http://localhost:8084',
  changeOrigin: true
}
```

---

### 2. 前端 API 封装
**问题**: 前端缺少 paymentApi 的定义  
**修复**: 在 `frontend/src/api/index.js` 中添加了完整的 paymentApi

---

## 🚀 如何使用

### 1. 重启前端开发服务器

修改了 `vite.config.js` 后，需要重启前端服务：

```bash
# 停止当前的前端服务（Ctrl+C）
# 然后重新启动
cd frontend
npm run dev
```

---

### 2. 在 Vue 组件中使用

```vue
<script setup>
import { ref } from 'vue'
import { paymentApi } from '@/api'

const createPayment = async () => {
  try {
    // 只需要传入订单编号
    const result = await paymentApi.create({
      orderNo: 'ORD20260520710935'
    })
    
    console.log('支付订单创建成功:', result)
    alert('支付订单创建成功！')
  } catch (error) {
    console.error('创建失败:', error)
  }
}
</script>

<template>
  <button @click="createPayment">创建支付订单</button>
</template>
```

---

## 📋 正确的请求格式

### 创建支付订单

**请求**:
```javascript
POST /api/payment
Content-Type: application/json

{
  "orderNo": "ORD20260520710935"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

---

## 🔍 调试技巧

### 1. 查看浏览器控制台

打开浏览器开发者工具（F12），查看 Console 和 Network 标签：

- **Console**: 查看日志输出
- **Network**: 查看请求详情

### 2. 检查请求是否发送成功

在 Network 标签中查找：
- 请求 URL: `http://localhost:3000/api/payment`
- 请求方法: `POST`
- 状态码: `200`
- 请求体: `{"orderNo":"ORD20260520710935"}`

### 3. 检查后端日志

查看 payment-service 的控制台输出：

```
========== 开始创建支付订单 ==========
【请求参数】订单编号: ORD20260520710935
✅ 订单验证成功 - 订单ID: 26, 金额: ¥94.00
✅ 支付订单创建成功 - 支付单号: PAY2026052100001, 金额: ¥94.00
```

---

## ❌ 常见错误

### 错误1: 404 Not Found

**原因**: Vite 代理未生效  
**解决**: 重启前端开发服务器

```bash
npm run dev
```

---

### 错误2: 订单不存在

**原因**: orderNo 不正确或订单不存在  
**解决**: 检查订单编号是否正确

---

### 错误3: 订单服务暂时不可用

**原因**: order-service 未启动或未注册到 Eureka  
**解决**: 
1. 检查 order-service 是否运行在 8083 端口
2. 访问 http://localhost:8761 查看 ORDER-SERVICE 状态

---

## 🎯 完整的支付流程示例

```vue
<script setup>
import { ref } from 'vue'
import { paymentApi } from '@/api'
import { useRouter } from 'vue-router'

const router = useRouter()
const orderNo = ref('ORD20260520710935')
const paymentId = ref(null)

// 步骤1: 创建支付订单
const handleCreatePayment = async () => {
  try {
    const result = await paymentApi.create({
      orderNo: orderNo.value
    })
    
    if (result.code === 200) {
      alert('✅ 支付订单创建成功！')
      
      // 获取支付ID（可以通过订单编号查询）
      const paymentResult = await paymentApi.getByOrderNo(orderNo.value)
      paymentId.value = paymentResult.data.id
      
      // 步骤2: 跳转到支付页面
      router.push({
        path: '/payment/sandbox',
        query: { id: paymentId.value }
      })
    }
  } catch (error) {
    console.error('创建支付订单失败:', error)
  }
}
</script>

<template>
  <div>
    <h2>支付页面</h2>
    <p>订单编号: {{ orderNo }}</p>
    <button @click="handleCreatePayment">
      创建支付订单
    </button>
  </div>
</template>
```

---

## 📝 注意事项

1. **只需要传入 orderNo**：创建支付订单时，不需要传入 userId、paymentMethod 等字段
2. **后端自动设置**：
   - 支付金额：从订单中获取
   - 店铺ID：从订单中获取
   - 支付状态：自动设置为 1（支付中）
   - 支付单号：自动生成

3. **必须重启前端服务**：修改 vite.config.js 后，必须重启才能生效

---

## 🔗 相关API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/payment | 创建支付订单 |
| GET | /api/payment/list | 获取所有支付订单 |
| GET | /api/payment/{id} | 根据ID获取支付订单 |
| GET | /api/payment/no/{paymentNo} | 根据支付单号获取 |
| GET | /api/payment/order/{orderNo} | 根据订单编号获取 |
| PUT | /api/payment/{id}/status | 更新支付状态 |
| DELETE | /api/payment/{id} | 删除支付订单 |

---

## ✅ 验证清单

- [ ] vite.config.js 已添加 payment 代理配置
- [ ] src/api/index.js 已添加 paymentApi
- [ ] 前端开发服务器已重启
- [ ] payment-service 已启动（端口 8084）
- [ ] order-service 已启动（端口 8083）
- [ ] eureka-server 已启动（端口 8761）
- [ ] 浏览器控制台无跨域错误
- [ ] Network 标签显示请求成功（状态码 200）
