<template>
  <div class="queue-view">
    <h2>🎫 排队取号</h2>
    
    <!-- 取号表单 -->
    <div class="card take-number-card">
      <h3>📝 在线取号</h3>
      <form @submit.prevent="handleTakeNumber">
        <div class="form-group">
          <label>选择店铺</label>
          <select v-model.number="form.shopId" required>
            <option value="">请选择店铺</option>
            <option v-for="shop in shops" :key="shop.id" :value="shop.id">
              {{ shop.shopName }} ({{ shop.address }})
            </option>
          </select>
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label>用餐人数</label>
            <input v-model.number="form.partySize" type="number" min="1" max="20" placeholder="请输入人数" required />
          </div>
          
          <div class="form-group">
            <label>排队类型</label>
            <select v-model.number="form.queueType">
              <option value="1">堂食</option>
              <option value="2">外带</option>
            </select>
          </div>
        </div>
        
        <div class="form-group">
          <label>桌位偏好</label>
          <select v-model.number="form.tableType">
            <option value="">不限制</option>
            <option value="1">普通桌</option>
            <option value="2">卡座</option>
            <option value="3">包厢</option>
          </select>
        </div>
        
        <button type="submit" :disabled="takingNumber" class="btn-take">
          {{ takingNumber ? '取号中...' : '🎯 立即取号' }}
        </button>
      </form>
    </div>

    <!-- 实时队列信息 -->
    <div class="card queue-info-card">
      <h3>📊 实时排队信息</h3>
      <div class="info-grid">
        <div class="info-item">
          <div class="info-label">当前等待人数</div>
          <div class="info-value waiting">{{ realTimeQueue?.waitingCount || 0 }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">预计等待时间</div>
          <div class="info-value estimate">{{ estimatedWaitTime }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">正在叫号</div>
          <div class="info-value calling">{{ currentCalling || '暂无' }}</div>
        </div>
      </div>
      
      <!-- 叫号列表 -->
      <div v-if="callingList.length > 0" class="calling-section">
        <h4>🔔 当前叫号列表</h4>
        <div class="calling-list">
          <div v-for="item in callingList" :key="item.queueId" class="calling-item">
            <div class="calling-no">{{ item.queueNo }}</div>
            <div class="calling-info">
              <span>{{ item.partySize }}人</span>
              <span>{{ item.queueType === 1 ? '堂食' : '外带' }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <button @click="loadRealTimeQueue" class="btn-refresh">
        🔄 刷新数据
      </button>
    </div>

    <!-- 我的排队 -->
    <div class="card my-queue-card">
      <div class="card-header">
        <h3>📋 我的排队</h3>
        <button @click="loadQueues" class="btn-refresh-small">🔄 刷新</button>
      </div>
      
      <div v-if="queues.length === 0" class="empty-state">
        <div class="empty-icon">🎫</div>
        <p>暂无排队记录</p>
        <p class="empty-hint">点击上方"立即取号"开始排队</p>
      </div>
      
      <div v-else class="queue-list">
        <div v-for="queue in queues" :key="queue.id" class="queue-item" :class="getStatusClass(queue.queueStatus)">
          <div class="queue-header">
            <div class="queue-no">{{ queue.queueNo }}</div>
            <div class="queue-status">
              <span :class="['status-badge', getStatusBadgeClass(queue.queueStatus)]">
                {{ getStatusText(queue.queueStatus) }}
              </span>
            </div>
          </div>
          
          <div class="queue-details">
            <div class="detail-row">
              <span class="detail-label">店铺：</span>
              <span class="detail-value">{{ getShopName(queue.shopId) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">人数：</span>
              <span class="detail-value">{{ queue.partySize }}人</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">类型：</span>
              <span class="detail-value">{{ queue.queueType === 1 ? '堂食' : '外带' }}</span>
            </div>
            <div class="detail-row" v-if="queue.currentPosition">
              <span class="detail-label">当前位置：</span>
              <span class="detail-value highlight">第 {{ queue.currentPosition }} 位</span>
            </div>
          </div>
          
          <div class="queue-actions">
            <button 
              v-if="queue.queueStatus === 0" 
              @click="handleCancel(queue.id)" 
              class="btn-cancel"
            >
              取消排队
            </button>
            <!-- 已叫号但未下单，显示前往点菜按钮 -->
            <button 
              v-if="queue.queueStatus === 1 && !hasOrder(queue.id)" 
              @click="goToOrdering(queue)" 
              class="btn-ordering"
            >
              🍽️ 前往点菜
            </button>
            <span v-if="queue.queueStatus === 1 && hasOrder(queue.id)" class="called-tip">
              ✅ 已下单，请等待上菜
            </span>
            <span v-if="queue.queueStatus === 1 && !hasOrder(queue.id)" class="called-tip">
              🔔 请前往点菜就餐
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { queueApi, shopApi, orderApi, createWebSocket } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const queues = ref([])
const shops = ref([])
const realTimeQueue = ref(null)
const callingList = ref([])
const takingNumber = ref(false)
const queueOrderStatus = ref({}) // { queueId: hasOrder }
let ws = null

const form = ref({
  shopId: '',
  userId: null,
  phone: '',
  partySize: 2,
  queueType: 1,
  tableType: ''
})

// 计算预计等待时间（假设每人平均等待5分钟）
const estimatedWaitTime = computed(() => {
  const count = realTimeQueue.value?.waitingCount || 0
  if (count === 0) return '无需等待'
  const minutes = count * 5
  if (minutes < 60) return `约${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `约${hours}小时${mins > 0 ? mins + '分钟' : ''}`
})

// 当前叫号
const currentCalling = computed(() => {
  // 这里可以从 Redis 获取正在叫号的队列
  return null
})

// 取号
const handleTakeNumber = async () => {
  if (!form.value.shopId) {
    alert('请选择店铺')
    return
  }
  
  takingNumber.value = true
  try {
    // 设置用户ID和电话
    form.value.userId = userStore.user?.id || 1
    form.value.phone = userStore.user?.phone || '13800138001'
    
    const res = await queueApi.takeNumber(form.value)
    
    if (res.code === 200) {
      alert(`✅ 取号成功！\n排队号码：${res.data.queueNo}\n前方等待：${res.data.currentPosition || 0}人`)
      loadQueues()
      loadRealTimeQueue()
      // 重置表单
      form.value.partySize = 2
      form.value.queueType = 1
      form.value.tableType = ''
    } else {
      alert(res.message || '取号失败')
    }
  } catch (error) {
    console.error('取号失败:', error)
    alert(error.response?.data?.message || '网络错误，请稍后重试')
  } finally {
    takingNumber.value = false
  }
}

// 加载排队列表 - 只显示当前用户的排队
const loadQueues = async () => {
  try {
    // 获取当前登录用户ID
    const userId = userStore.user?.id
    if (!userId) {
      console.warn('未获取到用户ID，无法加载排队列表')
      queues.value = []
      return
    }
    
    // 调用按用户查询的API
    const res = await queueApi.getByUser(userId)
    queues.value = res.data || []
    
    // 检查每个已叫号的排队是否有订单
    for (const queue of queues.value) {
      if (queue.queueStatus === 1) {
        try {
          // 根据排队号码查询关联的订单
          const orderRes = await orderApi.getByQueue(queue.queueNo)
          queueOrderStatus.value[queue.id] = orderRes.data && orderRes.data.length > 0
        } catch (error) {
          console.error(`查询排队${queue.id}的订单失败:`, error)
          queueOrderStatus.value[queue.id] = false
        }
      }
    }
  } catch (error) {
    console.error('加载排队失败:', error)
  }
}

// 加载实时队列
const loadRealTimeQueue = async () => {
  if (!form.value.shopId) {
    // 如果没有选择店铺，使用第一个排队记录的店铺ID
    if (queues.value.length > 0) {
      form.value.shopId = queues.value[0].shopId
    } else {
      return
    }
  }
  
  try {
    const res = await queueApi.getRealTimeWaiting(form.value.shopId)
    realTimeQueue.value = res.data
  } catch (error) {
    console.error('加载实时队列失败:', error)
  }
}

// 加载叫号列表
const loadCallingList = async () => {
  if (!form.value.shopId) return
  
  try {
    const res = await queueApi.getRealTimeCalling(form.value.shopId)
    if (res.data && res.data.callingIds && res.data.callingIds.length > 0) {
      // 获取叫号队列中的详细信息
      const queueDetails = []
      for (const queueId of res.data.callingIds) {
        // 这里可以调用 API 获取每个排队的详细信息
        // 为了简化，我们先从本地数据中查找
        const queue = queues.value.find(q => q.id === parseInt(queueId))
        if (queue) {
          queueDetails.push(queue)
        }
      }
      callingList.value = queueDetails
      
      // 更新当前叫号显示
      if (queueDetails.length > 0) {
        currentCalling.value = queueDetails.map(q => q.queueNo).join(', ')
      }
    } else {
      callingList.value = []
      currentCalling.value = null
    }
  } catch (error) {
    console.error('加载叫号列表失败:', error)
  }
}

// 取消排队
const handleCancel = async (id) => {
  if (!confirm('确定要取消排队吗？')) return
  
  try {
    await queueApi.cancel(id, '用户主动取消')
    alert('✅ 已取消排队')
    loadQueues()
    loadRealTimeQueue()
  } catch (error) {
    console.error('取消失败:', error)
    alert(error.response?.data?.message || '取消失败')
  }
}

// 获取店铺名称
const getShopName = (shopId) => {
  const shop = shops.value.find(s => s.id === shopId)
  return shop ? shop.shopName : `店铺${shopId}`
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = { 
    0: '等待中', 
    1: '已叫号', 
    2: '已完成', 
    3: '已取消', 
    4: '已过号' 
  }
  return texts[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classes = {
    0: 'status-waiting',
    1: 'status-called',
    2: 'status-completed',
    3: 'status-cancelled',
    4: 'status-missed'
  }
  return classes[status] || ''
}

// 获取状态徽章样式
const getStatusBadgeClass = (status) => {
  const classes = {
    0: 'badge-waiting',
    1: 'badge-called',
    2: 'badge-completed',
    3: 'badge-cancelled',
    4: 'badge-missed'
  }
  return classes[status] || ''
}

// 检查排队是否已关联订单（同步版本，依赖预加载的数据）
const hasOrder = (queueId) => {
  return queueOrderStatus.value[queueId] || false
}

// 异步检查并更新排队订单状态
const checkQueueOrderStatus = async (queueId) => {
  try {
    // 首先通过排队ID获取排队信息，以获取排队号码
    const queueRes = await queueApi.getById(queueId);
    if (queueRes.code !== 200 || !queueRes.data) {
      console.error('获取排队信息失败:', queueId);
      queueOrderStatus.value[queueId] = false;
      return false;
    }
    
    const queueNumber = queueRes.data.queueNo;
    if (!queueNumber) {
      console.error('排队号码为空:', queueId);
      queueOrderStatus.value[queueId] = false;
      return false;
    }
    
    // 根据排队号码查询关联的订单
    const orderRes = await orderApi.getByQueue(queueNumber);
    if (orderRes.code === 200 && orderRes.data && Array.isArray(orderRes.data)) {
      const hasExistingOrder = orderRes.data.length > 0;
      // 更新缓存状态
      queueOrderStatus.value[queueId] = hasExistingOrder;
      return hasExistingOrder;
    }
    
    queueOrderStatus.value[queueId] = false;
    return false;
  } catch (error) {
    console.error('检查订单关联失败:', error);
    queueOrderStatus.value[queueId] = false;
    return false;
  }
}

// 前往点菜页面
const goToOrdering = async (queue) => {
  console.log('前往点菜 - 排队ID:', queue.id)
  
  // 在前往点菜前再次检查是否已有关联订单，防止并发问题
  const hasExistingOrder = await checkQueueOrderStatus(queue.id)
  if (hasExistingOrder) {
    alert('该排队号码已关联订单，不能重复下单')
    return
  }
  
  router.push({
    path: '/ordering',
    query: { queueId: queue.id }
  })
}

// 加载店铺列表
const loadShops = async () => {
  try {
    const res = await shopApi.getList()
    shops.value = res.data || []
    // 默认选择第一个店铺
    if (shops.value.length > 0 && !form.value.shopId) {
      form.value.shopId = shops.value[0].id
    }
  } catch (error) {
    console.error('加载店铺失败:', error)
  }
}

onMounted(() => {
  loadShops()
  loadQueues()
  
  // 连接 WebSocket
  if (userStore.user?.id) {
    ws = createWebSocket(userStore.user.id, handleWebSocketMessage)
  }
  
  // 定时刷新实时队列信息（每30秒）
  const interval = setInterval(() => {
    if (form.value.shopId) {
      loadRealTimeQueue()
      loadCallingList()
    }
  }, 30000)
  
  // 保存 interval ID 以便清理
  window.queueRefreshInterval = interval
})

// 处理 WebSocket 消息
const handleWebSocketMessage = (data) => {
  console.log('📨 收到 WebSocket 消息:', data)
  
  if (data.type === 'QUEUE_CREATED') {
    // 取号成功通知
    alert(`🎉 取号成功！\n排队号码：${data.data.queueNo}\n前方等待：${data.data.currentPosition || 0}人`)
    loadQueues()
    loadRealTimeQueue()
  } else if (data.type === 'QUEUE_CALLED') {
    // 叫号通知
    const queueNo = data.data.queueNo
    alert(`🔔 叫号通知\n排队号码：${queueNo}\n请前往就餐！`)
    
    // 播放提示音（如果浏览器支持）
    playNotificationSound()
    
    // 刷新数据
    loadQueues()
    loadCallingList()
  } else if (data.type === 'QUEUE_COMPLETED') {
    // 排队完成通知
    alert(`✅ 排队已完成\n感谢您的光临！`)
    loadQueues()
  }
}

// 播放提示音
const playNotificationSound = () => {
  try {
    const audioContext = new (window.AudioContext || window.webkitAudioContext)()
    const oscillator = audioContext.createOscillator()
    const gainNode = audioContext.createGain()
    
    oscillator.connect(gainNode)
    gainNode.connect(audioContext.destination)
    
    oscillator.frequency.value = 800
    oscillator.type = 'sine'
    
    gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
    gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5)
    
    oscillator.start(audioContext.currentTime)
    oscillator.stop(audioContext.currentTime + 0.5)
  } catch (e) {
    console.log('无法播放提示音', e)
  }
}

onUnmounted(() => {
  // 关闭 WebSocket
  if (ws) {
    ws.close()
  }
  
  // 清除定时器
  if (window.queueRefreshInterval) {
    clearInterval(window.queueRefreshInterval)
  }
})
</script>

<style scoped>
.queue-view {
  max-width: 1200px;
  margin: 0 auto;
}

.queue-view h2 {
  margin-bottom: 24px;
  color: #333;
  font-size: 28px;
}

.card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  transition: box-shadow 0.3s;
}

.card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
}

.card h3 {
  margin-bottom: 20px;
  color: #667eea;
  font-size: 20px;
}

/* 取号表单样式 */
.take-number-card form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

input, select {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

input:focus, select:focus {
  outline: none;
  border-color: #667eea;
}

.btn-take {
  padding: 14px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn-take:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-take:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 实时队列信息卡片 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.info-item {
  text-align: center;
  padding: 16px;
  background: #f7fafc;
  border-radius: 8px;
}

.info-label {
  font-size: 13px;
  color: #718096;
  margin-bottom: 8px;
}

.info-value {
  font-size: 24px;
  font-weight: bold;
}

.info-value.waiting {
  color: #ed8936;
}

.info-value.estimate {
  color: #48bb78;
}

.info-value.calling {
  color: #4299e1;
}

.btn-refresh {
  width: 100%;
  padding: 12px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.btn-refresh:hover {
  background: #38a169;
}

/* 我的排队卡片 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.btn-refresh-small {
  padding: 8px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.3s;
}

.btn-refresh-small:hover {
  background: #5568d3;
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
}

/* 排队列表 */
.queue-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.queue-item {
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px;
  transition: all 0.3s;
}

.queue-item:hover {
  border-color: #cbd5e0;
  transform: translateX(4px);
}

.queue-item.status-waiting {
  border-left: 4px solid #ed8936;
}

.queue-item.status-called {
  border-left: 4px solid #48bb78;
  background: #f0fff4;
}

.queue-item.status-completed {
  border-left: 4px solid #4299e1;
  opacity: 0.7;
}

.queue-item.status-cancelled {
  border-left: 4px solid #f56565;
  opacity: 0.6;
}

.queue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.queue-no {
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.badge-waiting {
  background: #fef5e7;
  color: #ed8936;
}

.badge-called {
  background: #d4edda;
  color: #28a745;
  animation: pulse 2s infinite;
}

.badge-completed {
  background: #d1ecf1;
  color: #17a2b8;
}

.badge-cancelled {
  background: #f8d7da;
  color: #dc3545;
}

.badge-missed {
  background: #fff3cd;
  color: #856404;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.queue-details {
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
  min-width: 80px;
}

.detail-value {
  color: #2d3748;
  font-weight: 500;
}

.detail-value.highlight {
  color: #ed8936;
  font-weight: bold;
}

.queue-actions {
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

.btn-ordering {
  padding: 8px 16px;
  background: #f6ad55;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.3s;
}

.btn-ordering:hover {
  background: #ed8936;
}

.called-tip {
  color: #48bb78;
  font-weight: bold;
  font-size: 14px;
}

/* 叫号列表样式 */
.calling-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #e2e8f0;
}

.calling-section h4 {
  margin-bottom: 12px;
  color: #ed8936;
  font-size: 16px;
}

.calling-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.calling-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #fff5e6 0%, #ffe6cc 100%);
  border-left: 4px solid #ed8936;
  border-radius: 8px;
  animation: pulse 2s infinite;
}

.calling-no {
  font-size: 20px;
  font-weight: bold;
  color: #c05621;
}

.calling-info {
  display: flex;
  gap: 12px;
  font-size: 14px;
  color: #744210;
}

.calling-info span {
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>