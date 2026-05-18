<template>
  <div class="order-view">
    <h2>📦 订单管理</h2>
    
    <!-- 创建订单表单 -->
    <div class="card">
      <h3>创建订单</h3>
      <form @submit.prevent="handleCreateOrder">
        <input v-model.number="form.shopId" type="number" placeholder="店铺ID" required />
        <input v-model.number="form.userId" type="number" placeholder="用户ID" required />
        <input v-model.number="form.queueId" type="number" placeholder="排队ID (可选)" />
        <input v-model.number="form.tableId" type="number" placeholder="桌台ID" required />
        <textarea v-model="form.remark" placeholder="备注"></textarea>
        <button type="submit">创建订单</button>
      </form>
    </div>

    <!-- 订单列表 -->
    <div class="card">
      <h3>订单列表</h3>
      <button @click="loadOrders" class="btn-refresh">刷新</button>
      <table>
        <thead>
          <tr>
            <th>订单号</th>
            <th>店铺ID</th>
            <th>用户ID</th>
            <th>金额</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.id">
            <td><strong>{{ order.orderNo }}</strong></td>
            <td>{{ order.shopId }}</td>
            <td>{{ order.userId }}</td>
            <td>¥{{ order.totalAmount?.toFixed(2) || '0.00' }}</td>
            <td>
              <span :class="getStatusClass(order.orderStatus)">
                {{ getStatusText(order.orderStatus) }}
              </span>
            </td>
            <td>
              <button v-if="order.orderStatus === 0" @click="handleCancel(order.id)" class="btn-cancel">取消</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="orders.length === 0" class="empty">暂无订单数据</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '../api'

const orders = ref([])
const form = ref({
  shopId: 1,
  userId: 1001,
  queueId: null,
  tableId: 10,
  remark: ''
})

// 创建订单
const handleCreateOrder = async () => {
  try {
    const data = { ...form.value }
    if (!data.queueId) delete data.queueId
    
    await orderApi.create(data)
    alert('✅ 订单创建成功！')
    form.value = { shopId: 1, userId: 1001, queueId: null, tableId: 10, remark: '' }
    loadOrders()
  } catch (error) {
    console.error('创建订单失败:', error)
  }
}

// 加载订单列表
const loadOrders = async () => {
  try {
    const res = await orderApi.getList()
    orders.value = res.data || []
  } catch (error) {
    console.error('加载订单失败:', error)
  }
}

// 取消订单
const handleCancel = async (id) => {
  if (!confirm('确定取消订单吗？')) return
  try {
    await orderApi.cancel(id, '用户主动取消')
    alert('✅ 订单已取消')
    loadOrders()
  } catch (error) {
    console.error('取消订单失败:', error)
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    0: '待支付',
    1: '待接单',
    2: '制作中',
    3: '待取餐',
    4: '已完成',
    5: '已取消'
  }
  return texts[status] || '未知'
}

// 获取状态样式
const getStatusClass = (status) => {
  const classes = {
    0: 'status-pending',
    1: 'status-waiting',
    2: 'status-making',
    3: 'status-ready',
    4: 'status-completed',
    5: 'status-cancelled'
  }
  return classes[status] || ''
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-view h2 {
  margin-bottom: 20px;
  color: #333;
}

.card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card h3 {
  margin-bottom: 15px;
  color: #667eea;
}

form {
  display: grid;
  gap: 10px;
}

input, textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

textarea {
  min-height: 80px;
  resize: vertical;
}

button {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

button:hover {
  background: #5568d3;
}

.btn-refresh {
  margin-bottom: 10px;
  background: #48bb78;
}

.btn-cancel {
  background: #f56565;
}

.btn-cancel:hover {
  background: #e53e3e;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f7fafc;
  font-weight: bold;
  color: #4a5568;
}

.status-pending {
  color: #ed8936;
  font-weight: bold;
}

.status-waiting {
  color: #4299e1;
}

.status-making {
  color: #9f7aea;
}

.status-ready {
  color: #38b2ac;
}

.status-completed {
  color: #48bb78;
}

.status-cancelled {
  color: #f56565;
}

.empty {
  text-align: center;
  color: #999;
  padding: 20px;
}
</style>
