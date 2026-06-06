<template>
  <div class="my-orders-view">
    <h2>📦 我的订单</h2>
    
    <!-- 筛选条件 -->
    <div class="card filter-card">
      <div class="filter-bar">
        <select v-model="filterStatus" @change="loadOrders">
          <option value="">全部状态</option>
          <option value="0">待支付</option>
          <option value="1">制作中</option>
          <option value="2">已完成</option>
          <option value="3">已取消</option>
        </select>
        
        <button @click="loadOrders" class="btn-refresh" :disabled="loading">
          {{ loading ? '刷新中...' : '🔄 刷新列表' }}
        </button>
        
        <span class="order-count" v-if="!loading">
          共 <strong>{{ orders.length }}</strong> 个订单
        </span>
      </div>
    </div>
    
    <!-- 订单列表 -->
    <div v-loading="loading" class="order-list">
      <div v-if="!loading && orders.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <p>暂无订单</p>
        <p class="empty-hint">点击“去点餐”开始您的第一次下单</p>
        <button @click="$router.push('/ordering')" class="btn-primary">
          🍽️ 去点餐
        </button>
      </div>
      
      <div v-else class="order-items">
        <div v-for="order in orders" :key="order.id" class="order-item" :class="getStatusClass(order.orderStatus)">
          <div class="order-header">
            <div class="order-no">{{ order.orderNo }}</div>
            <div class="order-status">
              <span :class="['status-badge', getStatusBadgeClass(order.orderStatus)]">
                {{ getStatusText(order.orderStatus) }}
              </span>
            </div>
          </div>
          
          <div class="order-details">
            <div class="detail-row">
              <span class="detail-label">桌台号：</span>
              <span class="detail-value">{{ order.tableNumber || '未分配' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">订单金额：</span>
              <span class="detail-value price">¥{{ order.totalAmount?.toFixed(2) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">实付金额：</span>
              <span class="detail-value price-highlight">¥{{ order.actualAmount?.toFixed(2) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">支付方式：</span>
              <span class="detail-value">{{ getPaymentMethodText(order.paymentMethod) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">创建时间：</span>
              <span class="detail-value">{{ formatTime(order.createdAt) }}</span>
            </div>
          </div>
          
          <div class="order-actions">
            <button 
              v-if="order.orderStatus === 0" 
              @click="handleCancel(order)" 
              class="btn-cancel"
            >
              取消订单
            </button>
            <button 
              v-if="order.orderStatus === 0" 
              @click="handlePay(order)" 
              class="btn-pay"
            >
              💳 去支付
            </button>
            <button 
              @click="handleViewDetail(order)" 
              class="btn-detail"
            >
              📋 查看详情
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi, paymentApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const orders = ref([])
const filterStatus = ref('')

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const userId = userStore.user?.id
    if (!userId) {
      alert('❌ 用户信息不存在')
      return
    }
    
    const result = await orderApi.getByUser(userId)
    
    if (result.code === 200) {
      // 根据状态筛选
      let filteredOrders = result.data || []
      if (filterStatus.value !== '') {
        filteredOrders = filteredOrders.filter(
          order => order.orderStatus === parseInt(filterStatus.value)
        )
      }
      
      orders.value = filteredOrders.sort((a, b) => 
        new Date(b.createdAt) - new Date(a.createdAt)
      )
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    alert('❌ 加载订单失败')
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  // 小于1小时显示“xx分钟前”
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  }
  
  // 小于24小时显示“xx小时前”
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }
  
  // 其他显示完整日期
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  
  // 如果是今年，不显示年份
  if (year === now.getFullYear()) {
    return `${month}-${day} ${hour}:${minute}`
  }
  
  return `${year}-${month}-${day} ${hour}:${minute}`
}

// 获取订单状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '制作中',
    2: '已完成',
    3: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-making',
    2: 'status-completed',
    3: 'status-cancelled'
  }
  return classMap[status] || ''
}

// 获取状态徽章样式类
const getStatusBadgeClass = (status) => {
  const classMap = {
    0: 'badge-pending',
    1: 'badge-making',
    2: 'badge-completed',
    3: 'badge-cancelled'
  }
  return classMap[status] || ''
}

// 获取支付方式文本
const getPaymentMethodText = (method) => {
  const methodMap = {
    1: '微信支付',
    2: '支付宝',
    3: '现金',
    4: '会员卡',
    5: '银行卡'
  }
  return methodMap[method] || '未选择'
}

// 取消订单
const handleCancel = async (order) => {
  if (!confirm('确定要取消该订单吗？')) {
    return
  }
  
  try {
    const result = await orderApi.cancel(order.id, '用户主动取消')
    
    if (result.code === 200) {
      alert('✅ 订单已取消')
      loadOrders()
    }
  } catch (error) {
    console.error('取消订单失败:', error)
    alert('❌ 取消订单失败')
  }
}

// 去支付 - 创建支付订单并跳转到支付页面
const handlePay = async (order) => {
  try {
    // 创建支付记录
    await paymentApi.create({
      orderNo: order.orderNo,
      orderId: order.id,
      userId: userStore.user?.id,
      paymentMethod: 0
    })
    router.push('/payment')
  } catch (error) {
    console.error('创建支付订单失败:', error)
    alert('❌ 创建支付订单失败')
  }
}

// 查看详情
const handleViewDetail = (order) => {
  const detailText = `
订单号：${order.orderNo}
桌台号：${order.tableNumber || '未分配'}
订单状态：${getStatusText(order.orderStatus)}
支付状态：${order.paymentStatus ? '已支付' : '未支付'}
订单金额：¥${order.totalAmount?.toFixed(2)}
实付金额：¥${order.actualAmount?.toFixed(2)}
支付方式：${getPaymentMethodText(order.paymentMethod)}
创建时间：${formatTime(order.createdAt)}
  `.trim()
  
  alert(detailText)
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.my-orders-view {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.my-orders-view h2 {
  margin-bottom: 20px;
  color: #333;
  font-size: 24px;
}

/* 卡片通用样式 */
.card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

/* 筛选卡片 */
.filter-card {
  display: flex;
  align-items: center;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  width: 100%;
}

.filter-bar select {
  padding: 10px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  min-width: 150px;
  cursor: pointer;
}

.btn-refresh {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.btn-refresh:hover:not(:disabled) {
  background: #5568d3;
}

.btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.order-count {
  margin-left: auto;
  color: #606266;
  font-size: 14px;
}

.order-count strong {
  color: #667eea;
  font-size: 18px;
}

/* 订单列表 */
.order-list {
  min-height: 400px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-hint {
  font-size: 13px;
  color: #bbb;
  margin-top: 8px;
  margin-bottom: 20px;
}

.btn-primary {
  padding: 12px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.btn-primary:hover {
  background: #5568d3;
}

/* 订单项 */
.order-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px;
  transition: all 0.3s;
}

.order-item:hover {
  border-color: #cbd5e0;
  transform: translateX(4px);
}

.order-item.status-pending {
  border-left: 4px solid #ed8936;
}

.order-item.status-making {
  border-left: 4px solid #4299e1;
}

.order-item.status-completed {
  border-left: 4px solid #48bb78;
}

.order-item.status-cancelled {
  border-left: 4px solid #f56565;
  opacity: 0.7;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e2e8f0;
}

.order-no {
  font-size: 18px;
  font-weight: bold;
  color: #667eea;
  font-family: 'Courier New', monospace;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.badge-pending {
  background: #fef5e7;
  color: #ed8936;
}

.badge-making {
  background: #ebf8ff;
  color: #4299e1;
}

.badge-completed {
  background: #d4edda;
  color: #28a745;
}

.badge-cancelled {
  background: #f8d7da;
  color: #dc3545;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.detail-row {
  display: flex;
  font-size: 14px;
}

.detail-label {
  color: #718096;
  min-width: 90px;
}

.detail-value {
  color: #2d3748;
  font-weight: 500;
}

.detail-value.price {
  color: #f56565;
  font-size: 16px;
  font-weight: bold;
}

.detail-value.price-highlight {
  color: #f56565;
  font-size: 18px;
  font-weight: bold;
}

.order-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-cancel {
  padding: 8px 16px;
  background: #f56565;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.3s;
}

.btn-cancel:hover {
  background: #e53e3e;
}

.btn-pay {
  padding: 8px 16px;
  background: #f6ad55;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.3s;
}

.btn-pay:hover {
  background: #ed8936;
}

.btn-detail {
  padding: 8px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.3s;
}

.btn-detail:hover {
  background: #5568d3;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-orders-view {
    padding: 16px;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .order-count {
    margin-left: 0;
    text-align: center;
  }
  
  .order-actions {
    flex-direction: column;
  }
  
  .order-actions button {
    width: 100%;
  }
}
</style>