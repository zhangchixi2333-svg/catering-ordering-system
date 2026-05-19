<template>
  <div class="order-view">
    <h2>📦 订单管理</h2>
    
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <select v-model="filter.status" @change="loadOrders">
        <option value="">全部状态</option>
        <option value="0">待支付</option>
        <option value="1">待接单</option>
        <option value="2">制作中</option>
        <option value="3">待取餐</option>
        <option value="4">已完成</option>
        <option value="5">已取消</option>
      </select>
      
      <select v-model="filter.orderType" @change="loadOrders">
        <option value="">全部类型</option>
        <option value="1">堂食</option>
        <option value="2">外带</option>
        <option value="3">外卖</option>
      </select>
      
      <input v-model="filter.keyword" placeholder="搜索订单号/手机号" @keyup.enter="loadOrders" />
      
      <button @click="loadOrders" class="btn-search">🔍 搜索</button>
      <button @click="resetFilter" class="btn-reset">🔄 重置</button>
    </div>
    
    <!-- 订单列表 -->
    <div class="card">
      <div class="card-header">
        <h3>订单列表</h3>
        <span class="count-badge">共 {{ orders.length }} 单</span>
      </div>
      
      <div v-if="orders.length > 0" class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-item">
          <div class="order-main">
            <div class="order-header">
              <div class="order-no-section">
                <span class="order-no">{{ order.orderNo }}</span>
                <span :class="['status-badge', getStatusClass(order.orderStatus)]">
                  {{ getStatusText(order.orderStatus) }}
                </span>
              </div>
              <div class="order-time">{{ formatTime(order.createdAt) }}</div>
            </div>
            
            <div class="order-info">
              <div class="info-row">
                <span class="label">店铺：</span>
                <span class="value">{{ order.shopName || '店铺' + order.shopId }}</span>
              </div>
              <div class="info-row">
                <span class="label">类型：</span>
                <span class="value">{{ getOrderTypeText(order.orderType) }}</span>
              </div>
              <div class="info-row" v-if="order.queueNo">
                <span class="label">排队号：</span>
                <span class="value highlight">{{ order.queueNo }}</span>
              </div>
              <div class="info-row">
                <span class="label">金额：</span>
                <span class="value price">¥{{ order.totalAmount?.toFixed(2) || '0.00' }}</span>
              </div>
            </div>
            
            <div v-if="order.remark" class="order-remark">
              <span class="remark-label">备注：</span>
              <span class="remark-text">{{ order.remark }}</span>
            </div>
          </div>
          
          <div class="order-actions">
            <button @click="viewDetail(order)" class="btn-detail">📋 详情</button>
            <!-- 排队已叫号但未下单，显示前往点菜按钮 -->
            <button 
              v-if="canGoToOrdering(order)" 
              @click="goToOrdering(order)" 
              class="btn-ordering"
            >
              🍽️ 前往点菜
            </button>
            <button 
              v-if="canCancel(order)" 
              @click="handleCancel(order)" 
              class="btn-cancel"
            >
              ❌ 取消
            </button>
            <button 
              v-if="canUpdateStatus(order)" 
              @click="showStatusDialog(order)" 
              class="btn-update"
            >
              ✏️ 更新状态
            </button>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-state">
        <div class="empty-icon">📦</div>
        <p>暂无订单数据</p>
      </div>
    </div>
    
    <!-- 订单详情对话框 -->
    <div v-if="showDetailDialog" class="dialog-overlay" @click="closeDetailDialog">
      <div class="dialog" @click.stop>
        <div class="dialog-header">
          <h3>📋 订单详情</h3>
          <button @click="closeDetailDialog" class="btn-close">×</button>
        </div>
        
        <div v-if="selectedOrder" class="dialog-content">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="detail-row">
              <span class="label">订单号：</span>
              <span class="value">{{ selectedOrder.orderNo }}</span>
            </div>
            <div class="detail-row">
              <span class="label">下单时间：</span>
              <span class="value">{{ formatTime(selectedOrder.createdAt) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">订单类型：</span>
              <span class="value">{{ getOrderTypeText(selectedOrder.orderType) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">订单状态：</span>
              <span :class="['value', 'status-text', getStatusClass(selectedOrder.orderStatus)]">
                {{ getStatusText(selectedOrder.orderStatus) }}
              </span>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>关联信息</h4>
            <div class="detail-row">
              <span class="label">店铺ID：</span>
              <span class="value">{{ selectedOrder.shopId }}</span>
            </div>
            <div class="detail-row" v-if="selectedOrder.queueId">
              <span class="label">排队ID：</span>
              <span class="value">{{ selectedOrder.queueId }}</span>
            </div>
            <div class="detail-row" v-if="selectedOrder.queueNo">
              <span class="label">排队号码：</span>
              <span class="value highlight">{{ selectedOrder.queueNo }}</span>
            </div>
            <div class="detail-row" v-if="selectedOrder.tableId">
              <span class="label">桌台ID：</span>
              <span class="value">{{ selectedOrder.tableId }}</span>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>金额信息</h4>
            <div class="detail-row">
              <span class="label">订单金额：</span>
              <span class="value price-large">¥{{ selectedOrder.totalAmount?.toFixed(2) || '0.00' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">支付状态：</span>
              <span class="value">{{ getPaymentStatusText(selectedOrder.paymentStatus) }}</span>
            </div>
          </div>
          
          <div v-if="selectedOrder.remark" class="detail-section">
            <h4>备注信息</h4>
            <p class="remark-full">{{ selectedOrder.remark }}</p>
          </div>
        </div>
        
        <div class="dialog-footer">
          <button @click="closeDetailDialog" class="btn-confirm">关闭</button>
        </div>
      </div>
    </div>
    
    <!-- 更新状态对话框 -->
    <div v-if="showStatusDialogFlag" class="dialog-overlay" @click="closeStatusDialog">
      <div class="dialog dialog-small" @click.stop>
        <div class="dialog-header">
          <h3>✏️ 更新订单状态</h3>
          <button @click="closeStatusDialog" class="btn-close">×</button>
        </div>
        
        <div class="dialog-content">
          <p class="dialog-tip">当前订单：{{ selectedOrder?.orderNo }}</p>
          
          <div class="status-options">
            <button 
              v-for="status in availableStatuses" 
              :key="status.value"
              @click="handleUpdateStatus(status.value)"
              :class="['status-btn', getStatusClass(status.value)]"
              :disabled="status.value === selectedOrder?.orderStatus"
            >
              {{ status.text }}
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
import { orderApi } from '../api'

const router = useRouter()
const orders = ref([])
const filter = ref({
  status: '',
  orderType: '',
  keyword: ''
})

const showDetailDialog = ref(false)
const showStatusDialogFlag = ref(false)
const selectedOrder = ref(null)

// 可用的订单状态
const availableStatuses = [
  { value: 0, text: '待支付' },
  { value: 1, text: '待接单' },
  { value: 2, text: '制作中' },
  { value: 3, text: '待取餐' },
  { value: 4, text: '已完成' }
]

// 加载订单列表
const loadOrders = async () => {
  try {
    const res = await orderApi.getList()
    let allOrders = res.data || []
    
    // 前端筛选
    if (filter.value.status) {
      allOrders = allOrders.filter(o => o.orderStatus === parseInt(filter.value.status))
    }
    if (filter.value.orderType) {
      allOrders = allOrders.filter(o => o.orderType === parseInt(filter.value.orderType))
    }
    if (filter.value.keyword) {
      const keyword = filter.value.keyword.toLowerCase()
      allOrders = allOrders.filter(o => 
        o.orderNo?.toLowerCase().includes(keyword)
      )
    }
    
    orders.value = allOrders
  } catch (error) {
    console.error('加载订单失败:', error)
  }
}

// 重置筛选
const resetFilter = () => {
  filter.value = {
    status: '',
    orderType: '',
    keyword: ''
  }
  loadOrders()
}

// 查看详情
const viewDetail = (order) => {
  selectedOrder.value = order
  showDetailDialog.value = true
}

// 关闭详情对话框
const closeDetailDialog = () => {
  showDetailDialog.value = false
  selectedOrder.value = null
}

// 显示状态更新对话框
const showStatusDialog = (order) => {
  selectedOrder.value = order
  showStatusDialogFlag.value = true
}

// 关闭状态对话框
const closeStatusDialog = () => {
  showStatusDialogFlag.value = false
  selectedOrder.value = null
}

// 更新订单状态
const handleUpdateStatus = async (newStatus) => {
  if (!selectedOrder.value) return
  
  try {
    await orderApi.updateStatus(selectedOrder.value.id, newStatus)
    alert('✅ 状态更新成功！')
    closeStatusDialog()
    loadOrders()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 取消订单
const handleCancel = async (order) => {
  if (!confirm(`确定要取消订单 ${order.orderNo} 吗？`)) return
  
  try {
    await orderApi.cancel(order.id, '用户主动取消')
    alert('✅ 订单已取消')
    loadOrders()
  } catch (error) {
    console.error('取消订单失败:', error)
  }
}

// 判断是否可以取消
const canCancel = (order) => {
  return order.orderStatus === 0 || order.orderStatus === 1
}

// 判断是否可以更新状态
const canUpdateStatus = (order) => {
  return order.orderStatus >= 0 && order.orderStatus < 4
}

// 判断是否可以前往点菜（有排队ID且未创建订单）
const canGoToOrdering = (order) => {
  // 如果订单已有queueId但还未支付，说明是排队记录而非订单
  // 这里需要根据实际业务逻辑调整
  return false // 暂时禁用，需要通过排队页面跳转
}

// 前往点菜页面
const goToOrdering = (order) => {
  console.log('前往点菜 - 排队ID:', order.queueId)
  router.push({
    path: '/ordering',
    query: { queueId: order.queueId }
  })
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

// 获取订单类型文本
const getOrderTypeText = (type) => {
  const types = {
    1: '堂食',
    2: '外带',
    3: '外卖'
  }
  return types[type] || '未知'
}

// 获取支付状态文本
const getPaymentStatusText = (status) => {
  const statuses = {
    0: '未支付',
    1: '已支付',
    2: '退款中',
    3: '已退款'
  }
  return statuses[status] || '未知'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
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

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.filter-bar select,
.filter-bar input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 120px;
}

.filter-bar input {
  flex: 1;
  min-width: 200px;
}

.btn-search {
  background: #667eea;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-reset {
  background: #718096;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  color: #667eea;
}

.count-badge {
  background: #edf2f7;
  color: #4a5568;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
}

/* 订单列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  padding: 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.3s;
}

.order-item:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.order-main {
  flex: 1;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.order-no-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.order-no {
  font-weight: bold;
  font-size: 16px;
  color: #2d3748;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status-pending {
  background: #fef5e7;
  color: #ed8936;
}

.status-waiting {
  background: #ebf8ff;
  color: #4299e1;
}

.status-making {
  background: #faf5ff;
  color: #9f7aea;
}

.status-ready {
  background: #e6fffa;
  color: #38b2ac;
}

.status-completed {
  background: #f0fff4;
  color: #48bb78;
}

.status-cancelled {
  background: #fed7d7;
  color: #f56565;
}

.order-time {
  color: #718096;
  font-size: 14px;
}

.order-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 10px;
}

.info-row {
  display: flex;
  gap: 8px;
  font-size: 14px;
}

.info-row .label {
  color: #718096;
  min-width: 60px;
}

.info-row .value {
  color: #2d3748;
}

.info-row .value.price {
  color: #e53e3e;
  font-weight: bold;
  font-size: 16px;
}

.info-row .value.highlight {
  color: #667eea;
  font-weight: bold;
}

.order-remark {
  padding: 8px 12px;
  background: #f7fafc;
  border-radius: 4px;
  font-size: 13px;
}

.remark-label {
  color: #718096;
}

.remark-text {
  color: #4a5568;
}

.order-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: 15px;
}

.order-actions button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.btn-detail {
  background: #667eea;
  color: white;
}

.btn-detail:hover {
  background: #5568d3;
}

.btn-ordering {
  background: #f6ad55;
  color: white;
}

.btn-ordering:hover {
  background: #ed8936;
}

.btn-cancel {
  background: #f56565;
  color: white;
}

.btn-cancel:hover {
  background: #e53e3e;
}

.btn-update {
  background: #48bb78;
  color: white;
}

.btn-update:hover {
  background: #38a169;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #a0aec0;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

/* 对话框 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.dialog-small {
  max-width: 400px;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
}

.dialog-header h3 {
  margin: 0;
  color: #2d3748;
}

.btn-close {
  background: none;
  border: none;
  font-size: 28px;
  color: #718096;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  line-height: 1;
}

.btn-close:hover {
  color: #2d3748;
}

.dialog-content {
  padding: 20px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  color: #4a5568;
  font-size: 15px;
  border-bottom: 2px solid #667eea;
  padding-bottom: 8px;
}

.detail-row {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #f7fafc;
}

.detail-row .label {
  color: #718096;
  min-width: 100px;
}

.detail-row .value {
  color: #2d3748;
  flex: 1;
}

.detail-row .value.price-large {
  color: #e53e3e;
  font-size: 20px;
  font-weight: bold;
}

.detail-row .value.highlight {
  color: #667eea;
  font-weight: bold;
}

.status-text {
  font-weight: bold;
}

.remark-full {
  margin: 0;
  padding: 12px;
  background: #f7fafc;
  border-radius: 4px;
  color: #4a5568;
  line-height: 1.6;
}

.dialog-footer {
  padding: 15px 20px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
}

.btn-confirm {
  padding: 8px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-confirm:hover {
  background: #5568d3;
}

.dialog-tip {
  margin: 0 0 16px 0;
  padding: 12px;
  background: #ebf8ff;
  border-left: 4px solid #4299e1;
  color: #2c5282;
  font-size: 14px;
}

.status-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.status-btn {
  padding: 12px;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.status-btn:hover:not(:disabled) {
  border-color: #667eea;
  background: #f7fafc;
}

.status-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.status-btn.status-pending {
  border-color: #ed8936;
  color: #ed8936;
}

.status-btn.status-waiting {
  border-color: #4299e1;
  color: #4299e1;
}

.status-btn.status-making {
  border-color: #9f7aea;
  color: #9f7aea;
}

.status-btn.status-ready {
  border-color: #38b2ac;
  color: #38b2ac;
}

.status-btn.status-completed {
  border-color: #48bb78;
  color: #48bb78;
}
</style>
