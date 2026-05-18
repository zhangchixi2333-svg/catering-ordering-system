<template>
  <div class="call-number-view">
    <h2>🔔 叫号管理</h2>
    
    <!-- 店铺选择 -->
    <div class="card shop-selector">
      <div class="selector-row">
        <label>选择店铺：</label>
        <select v-model.number="shopId" @change="handleShopChange">
          <option value="">请选择店铺</option>
          <option v-for="shop in shops" :key="shop.id" :value="shop.id">
            {{ shop.shopName }}
          </option>
        </select>
        <button @click="refreshAll" class="btn-refresh-all">🔄 刷新全部</button>
      </div>
    </div>
    
    <!-- 实时等待队列 -->
    <div class="card waiting-card">
      <div class="card-header">
        <h3>⏳ 实时等待队列 (Redis)</h3>
        <div class="header-info">
          <span class="badge waiting-badge">{{ waitingQueue?.waitingCount || 0 }} 人等待</span>
        </div>
      </div>
      
      <div v-if="waitingList.length > 0" class="queue-list">
        <div 
          v-for="(item, index) in waitingList" 
          :key="item.id"
          class="queue-item waiting-item"
          :class="{ 'first-in-line': index === 0 }"
        >
          <div class="queue-main">
            <div class="queue-number-section">
              <span class="position-badge" :class="index === 0 ? 'first' : ''">
                #{{ index + 1 }}
              </span>
              <div class="queue-no">{{ item.queueNo }}</div>
            </div>
            
            <div class="queue-details">
              <div class="detail-row">
                <span class="detail-label">人数：</span>
                <span class="detail-value">{{ item.partySize }}人</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">类型：</span>
                <span class="detail-value">{{ item.queueType === 1 ? '堂食' : '外带' }}</span>
              </div>
              <div class="detail-row" v-if="item.tableType">
                <span class="detail-label">桌位：</span>
                <span class="detail-value">{{ getTableTypeName(item.tableType) }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">电话：</span>
                <span class="detail-value">{{ item.phone }}</span>
              </div>
            </div>
          </div>
          
          <div class="actions">
            <button @click="handleCall(item)" class="btn-call" :disabled="calling">
              {{ calling && callingId === item.id ? '叫号中...' : '📢 叫号' }}
            </button>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-state">
        <div class="empty-icon">⏳</div>
        <p>当前没有等待中的排队</p>
      </div>
    </div>

    <!-- 已叫号队列 -->
    <div class="card calling-card">
      <div class="card-header">
        <h3>✅ 已叫号队列</h3>
        <div class="header-info">
          <span class="badge calling-badge">{{ callingList.length }} 人已叫号</span>
        </div>
      </div>
      
      <div v-if="callingList.length > 0" class="queue-list">
        <div 
          v-for="item in callingList" 
          :key="item.id"
          class="queue-item called-item"
        >
          <div class="queue-main">
            <div class="queue-number-section">
              <span class="status-badge called">已叫号</span>
              <div class="queue-no">{{ item.queueNo }}</div>
            </div>
            
            <div class="queue-details">
              <div class="detail-row">
                <span class="detail-label">人数：</span>
                <span class="detail-value">{{ item.partySize }}人</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">类型：</span>
                <span class="detail-value">{{ item.queueType === 1 ? '堂食' : '外带' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">电话：</span>
                <span class="detail-value">{{ item.phone }}</span>
              </div>
              <div class="detail-row" v-if="item.callTime">
                <span class="detail-label">叫号时间：</span>
                <span class="detail-value highlight">{{ formatTime(item.callTime) }}</span>
              </div>
            </div>
          </div>
          
          <div class="actions">
            <button @click="handleComplete(item)" class="btn-complete" :disabled="completing">
              {{ completing && completingId === item.id ? '处理中...' : '✔️ 完成' }}
            </button>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-state">
        <div class="empty-icon">✅</div>
        <p>当前没有已叫号的排队</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { queueApi, shopApi } from '../api'

const shopId = ref(null)
const shops = ref([])
const waitingQueue = ref(null)
const callingQueue = ref(null)
const waitingList = ref([])
const callingList = ref([])
const calling = ref(false)
const callingId = ref(null)
const completing = ref(false)
const completingId = ref(null)

// 加载店铺列表
const loadShops = async () => {
  try {
    const res = await shopApi.getList()
    shops.value = res.data || []
    // 默认选择第一个店铺
    if (shops.value.length > 0 && !shopId.value) {
      shopId.value = shops.value[0].id
      await refreshAll()
    }
  } catch (error) {
    console.error('加载店铺失败:', error)
  }
}

// 店铺变更处理
const handleShopChange = async () => {
  if (shopId.value) {
    await refreshAll()
  }
}

// 刷新全部数据
const refreshAll = async () => {
  await Promise.all([
    loadWaitingQueue(),
    loadCallingQueue()
  ])
}

// 加载等待队列（包含详细信息）
const loadWaitingQueue = async () => {
  if (!shopId.value) return
  
  try {
    // 获取 Redis 等待队列 ID 列表
    const res = await queueApi.getRealTimeWaiting(shopId.value)
    waitingQueue.value = res.data
    
    // 获取排队详细信息
    if (res.data.queueIds && res.data.queueIds.length > 0) {
      const details = []
      for (const queueId of res.data.queueIds) {
        try {
          const detailRes = await queueApi.getById(queueId)
          if (detailRes.data) {
            details.push(detailRes.data)
          }
        } catch (e) {
          console.error(`获取排队 ${queueId} 详情失败:`, e)
        }
      }
      waitingList.value = details
    } else {
      waitingList.value = []
    }
  } catch (error) {
    console.error('加载等待队列失败:', error)
  }
}

// 加载叫号队列（包含详细信息）
const loadCallingQueue = async () => {
  if (!shopId.value) return
  
  try {
    // 获取 Redis 叫号队列 ID 列表
    const res = await queueApi.getRealTimeCalling(shopId.value)
    callingQueue.value = res.data
    
    // 获取排队详细信息
    if (res.data.callingIds && res.data.callingIds.length > 0) {
      const details = []
      for (const queueId of res.data.callingIds) {
        try {
          const detailRes = await queueApi.getById(queueId)
          if (detailRes.data) {
            details.push(detailRes.data)
          }
        } catch (e) {
          console.error(`获取排队 ${queueId} 详情失败:`, e)
        }
      }
      callingList.value = details
    } else {
      callingList.value = []
    }
  } catch (error) {
    console.error('加载叫号队列失败:', error)
  }
}

// 叫号
const handleCall = async (item) => {
  if (!confirm(`确定要叫号：${item.queueNo}（${item.partySize}人）吗？`)) return
  
  calling.value = true
  callingId.value = item.id
  
  try {
    await queueApi.callNumber(item.id)
    alert('✅ 叫号成功！')
    
    // 刷新数据
    await refreshAll()
  } catch (error) {
    console.error('叫号失败:', error)
    alert(error.response?.data?.message || '叫号失败')
  } finally {
    calling.value = false
    callingId.value = null
  }
}

// 完成排队
const handleComplete = async (item) => {
  if (!confirm(`确定完成排队：${item.queueNo} 吗？`)) return
  
  completing.value = true
  completingId.value = item.id
  
  try {
    await queueApi.complete(item.id)
    alert('✅ 已完成')
    
    // 刷新数据
    await refreshAll()
  } catch (error) {
    console.error('完成失败:', error)
    alert(error.response?.data?.message || '完成失败')
  } finally {
    completing.value = false
    completingId.value = null
  }
}

// 获取桌位类型名称
const getTableTypeName = (type) => {
  const types = {
    1: '普通桌',
    2: '卡座',
    3: '包厢'
  }
  return types[type] || '不限'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  loadShops()
})
</script>

<style scoped>
.call-number-view h2 {
  margin-bottom: 20px;
  color: #333;
}

.card {
  background: white;
  padding: 24px;
  border-radius: 10px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.shop-selector {
  padding: 16px 24px;
}

.selector-row {
  display: flex;
  align-items: center;
  gap: 15px;
}

.selector-row label {
  font-weight: 600;
  color: #4a5568;
}

.selector-row select {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  min-width: 200px;
  cursor: pointer;
}

.btn-refresh-all {
  background: #667eea;
  color: white;
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.btn-refresh-all:hover {
  background: #5568d3;
  transform: translateY(-1px);
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
  font-size: 20px;
}

.header-info {
  display: flex;
  gap: 10px;
}

.badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.waiting-badge {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  color: white;
}

.calling-badge {
  background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
  color: white;
}

.queue-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.queue-item {
  background: #f7fafc;
  padding: 20px;
  border-radius: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
  border-left: 4px solid #cbd5e0;
}

.waiting-item {
  border-left-color: #ed8936;
}

.waiting-item.first-in-line {
  border-left-color: #48bb78;
  background: linear-gradient(to right, #f0fff4 0%, #ffffff 100%);
  box-shadow: 0 2px 8px rgba(72, 187, 120, 0.2);
}

.called-item {
  border-left-color: #4299e1;
  background: linear-gradient(to right, #ebf8ff 0%, #ffffff 100%);
}

.queue-main {
  display: flex;
  gap: 20px;
  flex: 1;
}

.queue-number-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-width: 80px;
}

.position-badge {
  background: #667eea;
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 16px;
}

.position-badge.first {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.queue-no {
  font-size: 24px;
  font-weight: bold;
  color: #2d3748;
}

.status-badge {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.status-badge.called {
  background: #4299e1;
  color: white;
}

.queue-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.detail-row {
  display: flex;
  gap: 8px;
  font-size: 14px;
}

.detail-label {
  color: #718096;
  font-weight: 500;
  min-width: 70px;
}

.detail-value {
  color: #2d3748;
  font-weight: 600;
}

.detail-value.highlight {
  color: #ed8936;
}

.actions {
  display: flex;
  gap: 10px;
}

button {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
  font-size: 14px;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-call {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  color: white;
}

.btn-call:hover:not(:disabled) {
  background: linear-gradient(135deg, #dd6b20 0%, #c05621 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(237, 137, 54, 0.4);
}

.btn-complete {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
}

.btn-complete:hover:not(:disabled) {
  background: linear-gradient(135deg, #38a169 0%, #2f855a 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.4);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #a0aec0;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}

@media (max-width: 768px) {
  .queue-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .actions {
    width: 100%;
  }
  
  button {
    flex: 1;
  }
}
</style>
