<template>
  <div class="payment-view">
    <h2>💳 支付订单</h2>
    
    <!-- 操作提示 -->
    <div class="card info-card">
      <p class="info-text">请选择支付方式并点击"确认支付"完成支付（沙盒测试模式）</p>
    </div>
    
    <!-- 支付状态筛选 -->
    <div class="card filter-card">
      <div class="filter-section">
        <label class="filter-label">支付状态：</label>
        <select v-model="selectedStatus" class="status-select" @change="loadPayments">
          <option value="">全部状态</option>
          <option value="0">待支付</option>
          <option value="1">支付中</option>
          <option value="2">支付成功</option>
          <option value="3">支付失败</option>
          <option value="4">已退款</option>
        </select>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="empty-state">
      <p>加载中...</p>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="!loading && payments.length === 0" class="empty-state">
      <div class="empty-icon">💳</div>
      <p>{{ selectedStatus === '' ? '暂无支付订单' : `暂无${getPaymentStatusText(parseInt(selectedStatus))}订单` }}</p>
      <p class="empty-hint">可尝试切换其他支付状态</p>
      <button @click="$router.push('/my-orders')" class="btn-primary">
        📋 返回我的订单
      </button>
    </div>
    
    <!-- 支付列表 -->
    <div v-else class="payment-items">
      <div v-for="payment in paginatedPayments" :key="payment.id" class="card payment-card" :class="getPaymentCardClass(payment.paymentStatus)">
        <div class="payment-header">
          <div class="payment-no">支付单号：{{ payment.paymentNo }}</div>
          <div class="payment-status">
            <span :class="['status-badge', getPaymentBadgeClass(payment.paymentStatus)]">
              {{ getPaymentStatusText(payment.paymentStatus) }}
            </span>
          </div>
        </div>
        
        <div class="payment-body">
          <div class="payment-details">
            <div class="detail-row">
              <span class="detail-label">关联订单：</span>
              <span class="detail-value">{{ payment.orderNo }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">支付金额：</span>
              <span class="detail-value price-highlight">¥{{ payment.paymentAmount?.toFixed(2) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">支付方式：</span>
              <span class="detail-value">
                <select v-model="paymentMethods[payment.id]" class="method-select">
                  <option value="">请选择支付方式</option>
                  <option :value="1">💚 微信支付</option>
                  <option :value="2">💙 支付宝</option>
                  <option :value="3">💵 现金</option>
                  <option :value="4">💳 会员卡</option>
                  <option :value="5">🏦 银行卡</option>
                </select>
              </span>
            </div>
            <div class="detail-row">
              <span class="detail-label">创建时间：</span>
              <span class="detail-value">{{ formatTime(payment.createdAt) }}</span>
            </div>
          </div>
          
          <div class="payment-actions">
            <button 
              v-if="payment.paymentStatus === 0"
              @click="goToPay(payment)" 
              class="btn-go-pay"
            >
              👉 去支付
            </button>
            <button 
              v-else-if="payment.paymentStatus === 1"
              @click="handlePay(payment)" 
              class="btn-pay"
              :disabled="isPaying[payment.id]"
            >
              {{ isPaying[payment.id] ? '支付中...' : '✅ 确认支付' }}
            </button>
            <button 
              v-if="payment.paymentStatus === 0 || payment.paymentStatus === 1"
              @click="handleCancelPayment(payment)" 
              class="btn-cancel-pay"
            >
              取消支付
            </button>
            <button 
              @click="handleViewDetail(payment)" 
              class="btn-detail"
            >
              📋 详情
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 分页控件 -->
    <div v-if="payments.length > pageSize" class="pagination">
      <button 
        @click="prevPage" 
        :disabled="currentPage === 1" 
        class="page-btn"
      >
        上一页
      </button>
      
      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页 (共 {{ payments.length }} 条)
      </span>
      
      <button 
        @click="nextPage" 
        :disabled="currentPage === totalPages" 
        class="page-btn"
      >
        下一页
      </button>
      
      <div class="jump-to">
        <input 
          type="number" 
          v-model.number="jumpPage" 
          min="1" 
          :max="totalPages" 
          placeholder="页码" 
          class="jump-input"
        />
        <button @click="goToPage(jumpPage)" class="jump-btn">跳转</button>
      </div>
    </div>
    
    <!-- 支付成功弹窗 -->
    <div v-if="showSuccessDialog" class="dialog-overlay" @click="closeSuccessDialog">
      <div class="dialog success-dialog" @click.stop>
        <div class="success-icon">✅</div>
        <h3>支付成功！</h3>
        <p>订单状态已更新为"待接单"</p>
        <div class="dialog-actions">
          <button @click="closeSuccessDialog" class="btn-primary">
            知道了
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { paymentApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const payments = ref([])
const paymentMethods = ref({}) // { paymentId: selectedMethod }
const isPaying = ref({}) // { paymentId: true/false }
const showSuccessDialog = ref(false)
const selectedStatus = ref('') // 筛选状态：''表示全部，具体数字表示特定状态

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10) // 每页显示10条
const total = ref(0)
const jumpPage = ref(null)

// 加载支付订单列表
const loadPayments = async () => {
  loading.value = true
  try {
    const userId = userStore.user?.id
    if (!userId) {
      alert('❌ 用户信息不存在')
      return
    }
    
    let result
    if (selectedStatus.value === '') {
      // 如果没有选择特定状态，则获取所有用户支付订单
      result = await paymentApi.getByUser(userId)
    } else {
      // 如果选择了特定状态，则按状态查询
      result = await paymentApi.getByStatus(selectedStatus.value)
    }
    
    if (result.code === 200) {
      let filteredPayments = result.data || []
      
      // 如果选择了特定状态，再次过滤确保只有该状态的订单
      if (selectedStatus.value !== '') {
        filteredPayments = filteredPayments.filter(
          p => p.paymentStatus == selectedStatus.value
        )
      }
      
      payments.value = filteredPayments.sort((a, b) => 
        new Date(b.createdAt) - new Date(a.createdAt)
      )
      // 重置到第一页
      currentPage.value = 1
    }
  } catch (error) {
    console.error('加载支付订单失败:', error)
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
  
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  }
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  
  if (year === now.getFullYear()) {
    return `${month}-${day} ${hour}:${minute}`
  }
  return `${year}-${month}-${day} ${hour}:${minute}`
}

// 获取支付状态文本
const getPaymentStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '支付中',
    2: '支付成功',
    3: '支付失败',
    4: '已退款'
  }
  return statusMap[status] || '未知'
}

// 获取支付卡片样式类
const getPaymentCardClass = (status) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-processing',
    2: 'status-success',
    3: 'status-failed',
    4: 'status-refunded'
  }
  return classMap[status] || ''
}

// 获取支付状态徽章样式类
const getPaymentBadgeClass = (status) => {
  const classMap = {
    0: 'badge-pending',
    1: 'badge-processing',
    2: 'badge-success',
    3: 'badge-failed',
    4: 'badge-refunded'
  }
  return classMap[status] || ''
}

// 去支付 - 更新支付状态为"支付中"
const goToPay = async (payment) => {
  if (!confirm(`确认去支付 ¥${payment.paymentAmount?.toFixed(2)} 吗？`)) {
    return
  }
  
  isPaying.value[payment.id] = true
  
  try {
    // 更新支付状态为"支付中"(1)
    const result = await paymentApi.updateStatus(payment.id, 1)
    
    if (result.code === 200) {
      // 成功更新状态后，刷新列表
      loadPayments()
      alert('✅ 已进入支付流程，请选择支付方式并完成支付')
    } else {
      throw new Error(result.message || '更新支付状态失败')
    }
  } catch (error) {
    console.error('去支付失败:', error)
    alert('❌ 去支付失败：' + error.message)
  } finally {
    isPaying.value[payment.id] = false
  }
}

// 确认支付
const handlePay = async (payment) => {
  const method = paymentMethods.value[payment.id]
  if (!method) {
    alert('⚠️ 请先选择支付方式')
    return
  }
  
  if (!confirm(`确认支付 ¥${payment.paymentAmount?.toFixed(2)} 吗？`)) {
    return
  }
  
  isPaying.value[payment.id] = true
  
  try {
    // 步骤1: 更新支付方式
    const updateResult = await paymentApi.update(payment.id, {
      paymentMethod: parseInt(method)
    })
    
    if (updateResult.code !== 200) {
      throw new Error('更新支付方式失败')
    }
    
    // 步骤2: 调用沙盒接口模拟支付成功（后端自动更新支付状态→成功，订单状态→待接单）
    const response = await fetch(`/api/payment/sandbox/success/${payment.id}`, {
      method: 'POST'
    })
    
    const result = await response.json()
    
    if (result.code === 200) {
      showSuccessDialog.value = true
      loadPayments()
    } else {
      throw new Error(result.message || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    alert('❌ 支付失败：' + error.message)
  } finally {
    isPaying.value[payment.id] = false
  }
}

// 取消支付
const handleCancelPayment = async (payment) => {
  if (!confirm('确定要取消该支付订单吗？')) {
    return
  }
  
  try {
    const response = await fetch(`/api/payment/sandbox/failure/${payment.id}?reason=用户取消支付`, {
      method: 'POST'
    })
    
    const result = await response.json()
    
    if (result.code === 200) {
      alert('✅ 支付已取消')
      loadPayments()
    }
  } catch (error) {
    console.error('取消失败:', error)
  }
}

// 查看详情
const handleViewDetail = (payment) => {
  const detailText = `
支付单号：${payment.paymentNo}
关联订单：${payment.orderNo}
支付状态：${getPaymentStatusText(payment.paymentStatus)}
支付金额：¥${payment.paymentAmount?.toFixed(2)}
交易流水号：${payment.transactionId || '无'}
支付时间：${payment.payTime || '未支付'}
创建时间：${formatTime(payment.createdAt)}
  `.trim()
  
  alert(detailText)
}

// 关闭成功弹窗
const closeSuccessDialog = () => {
  showSuccessDialog.value = false
}

// 计算属性：分页后的支付列表
const paginatedPayments = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return payments.value.slice(start, end)
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(payments.value.length / pageSize.value)
})

// 下一页
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

// 上一页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

// 跳转到指定页
const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

onMounted(() => {
  loadPayments()
})
</script>

<style scoped>
.payment-view {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.payment-view h2 {
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

/* 提示卡片 */
.info-card {
  background: linear-gradient(135deg, #ebf8ff 0%, #bee3f8 100%);
  border-left: 4px solid #4299e1;
}

.info-text {
  margin: 0;
  color: #2b6cb0;
  font-size: 14px;
}

/* 筛选卡片 */
.filter-card {
  background: linear-gradient(135deg, #f0fff4 0%, #c6f6d5 100%);
  border-left: 4px solid #48bb78;
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-label {
  font-weight: bold;
  color: #2f855a;
  white-space: nowrap;
}

.status-select {
  padding: 8px 12px;
  border: 2px solid #9ae6b4;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  min-width: 120px;
  cursor: pointer;
  transition: border-color 0.3s;
}

.status-select:hover,
.status-select:focus {
  border-color: #48bb78;
  outline: none;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
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

/* 支付卡片列表 */
.payment-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.payment-card {
  border: 2px solid #e2e8f0;
  transition: all 0.3s;
}

.payment-card:hover {
  border-color: #cbd5e0;
  transform: translateX(4px);
}

.payment-card.status-pending {
  border-left: 4px solid #ed8936;
}

.payment-card.status-processing {
  border-left: 4px solid #4299e1;
  background: #f0f9ff;
}

.payment-card.status-success {
  border-left: 4px solid #48bb78;
  opacity: 0.8;
}

.payment-card.status-failed {
  border-left: 4px solid #f56565;
  opacity: 0.7;
}

/* 支付头部 */
.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e2e8f0;
}

.payment-no {
  font-size: 16px;
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

.badge-processing {
  background: #ebf8ff;
  color: #4299e1;
}

.badge-success {
  background: #d4edda;
  color: #28a745;
}

.badge-failed {
  background: #f8d7da;
  color: #dc3545;
}

.badge-refunded {
  background: #fff3cd;
  color: #856404;
}

/* 支付方式选择 */
.method-select {
  padding: 6px 10px;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  min-width: 160px;
  cursor: pointer;
  transition: border-color 0.3s;
  background: white;
}

.method-select:hover,
.method-select:focus {
  border-color: #667eea;
  outline: none;
}

/* 支付详情 */
.payment-body {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  flex-wrap: wrap;
}
/* 支付详情 */
.payment-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-row {
  display: flex;
  font-size: 14px;
  align-items: center;
}

/* 分页控件 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 20px;
  margin-top: 20px;
  flex-wrap: wrap;
}

.page-btn {
  padding: 8px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #5568d3;
}

.page-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

.jump-to {
  display: flex;
  align-items: center;
  gap: 8px;
}

.jump-input {
  width: 60px;
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  text-align: center;
}

.jump-btn {
  padding: 6px 12px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.jump-btn:hover {
  background: #38a169;
}

/* 去支付按钮 */
.btn-go-pay {
  padding: 8px 16px;
  background: #ed8936; /* 橙色，表示待处理 */
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
  margin-right: 8px;
}

.btn-go-pay:hover {
  background: #dd6b20;
}

.detail-label {
  color: #718096;
  min-width: 90px;
  flex-shrink: 0;
}

.detail-value {
  color: #2d3748;
  font-weight: 500;
}

.detail-value.price-highlight {
  color: #f56565;
  font-size: 20px;
  font-weight: bold;
}



/* 操作按钮 */
.payment-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
  flex-wrap: wrap;
}

.btn-pay {
  padding: 8px 16px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
  font-weight: 600;
}

.btn-pay:hover:not(:disabled) {
  background: #38a169;
}

.btn-pay:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel-pay {
  padding: 8px 16px;
  background: #f56565;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.3s;
}

.btn-cancel-pay:hover {
  background: #e53e3e;
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

/* 成功弹窗 */
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

.success-dialog {
  background: white;
  border-radius: 16px;
  padding: 40px;
  text-align: center;
  max-width: 400px;
  width: 90%;
  animation: popIn 0.3s ease-out;
}

@keyframes popIn {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.success-dialog h3 {
  margin: 0 0 8px 0;
  color: #2d3748;
  font-size: 22px;
}

.success-dialog p {
  margin: 0 0 24px 0;
  color: #718096;
  font-size: 15px;
}

.dialog-actions {
  display: flex;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .payment-view {
    padding: 16px;
  }
  
  .payment-body {
    flex-direction: column;
  }
  
  .payment-actions {
    width: 100%;
    justify-content: stretch;
  }
  
  .payment-actions button {
    flex: 1;
  }
  
  .method-select {
    width: 100%;
  }
}
</style>