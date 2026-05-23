<template>
  <div class="manager-dashboard">
    <h2>👨‍💼 店长管理控制台</h2>
    
    <!-- 店铺选择 -->
    <div class="shop-selector">
      <label>🏪 当前管理店铺：</label>
      <select v-model="selectedShopId" @change="handleShopChange">
        <option value="">选择店铺</option>
        <option v-for="shop in shops" :key="shop.id" :value="shop.id">
          {{ shop.shopName }} ({{ shop.shopCode }})
        </option>
      </select>
      <button @click="loadDashboardData" class="btn-refresh">🔄 刷新数据</button>
      <span v-if="loading" class="loading-text">加载中...</span>
    </div>
    
    <!-- 统计卡片 -->
    <div v-if="selectedShopId" class="stats-grid">
      <div class="stat-card stat-orders">
        <div class="stat-icon">📋</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.todayOrders }}</div>
          <div class="stat-label">今日订单</div>
          <div class="stat-trend" :class="stats.ordersTrend >= 0 ? 'trend-up' : 'trend-down'">
            {{ stats.ordersTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(stats.ordersTrend) }}%
          </div>
        </div>
      </div>
      
      <div class="stat-card stat-revenue">
        <div class="stat-icon">💰</div>
        <div class="stat-content">
          <div class="stat-value">¥{{ stats.todayRevenue.toFixed(2) }}</div>
          <div class="stat-label">今日营收</div>
          <div class="stat-trend" :class="stats.revenueTrend >= 0 ? 'trend-up' : 'trend-down'">
            {{ stats.revenueTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(stats.revenueTrend) }}%
          </div>
        </div>
      </div>
      
      <div class="stat-card stat-queue">
        <div class="stat-icon">🎫</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.waitingQueue }}</div>
          <div class="stat-label">等待排队</div>
          <div class="stat-trend trend-neutral">
            预计等待 {{ stats.avgWaitTime }} 分钟
          </div>
        </div>
      </div>
      
      <div class="stat-card stat-tables">
        <div class="stat-icon">🪑</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.availableTables }}/{{ stats.totalTables }}</div>
          <div class="stat-label">可用桌台</div>
          <div class="stat-trend trend-neutral">
            占用率 {{ stats.tableOccupancyRate }}%
          </div>
        </div>
      </div>
      
      <div class="stat-card stat-customers">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.todayCustomers }}</div>
          <div class="stat-label">今日顾客</div>
          <div class="stat-trend trend-neutral">
            服务人次
          </div>
        </div>
      </div>
      
      <div class="stat-card stat-calling">
        <div class="stat-icon">📢</div>
        <div class="stat-content">
          <div class="stat-value">{{ redisData.callingCount || 0 }}</div>
          <div class="stat-label">已叫号</div>
          <div class="stat-trend trend-neutral">
            待入座人数
          </div>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">👨‍💼</div>
      <p>请选择要管理的店铺</p>
    </div>
    
    <!-- 实时Redis排队数据 -->
    <div v-if="selectedShopId" class="redis-queue-data">
      <h3>📡 实时Redis排队数据</h3>
      <div class="queue-data-grid">
        <div class="queue-data-card">
          <div class="queue-data-title">等待队列</div>
          <div class="queue-data-content">
            <div v-if="redisData.waitingQueue && redisData.waitingQueue.length > 0" class="queue-list">
              <div v-for="(item, index) in redisData.waitingQueue" :key="index" class="queue-item">
                <span class="queue-index">{{ index + 1 }}</span>
                <span class="queue-id">{{ item }}</span>
              </div>
            </div>
            <div v-else class="queue-empty">暂无等待排队</div>
          </div>
        </div>
        
        <div class="queue-data-card">
          <div class="queue-data-title">叫号队列</div>
          <div class="queue-data-content">
            <div v-if="redisData.callingQueue && redisData.callingQueue.length > 0" class="queue-list">
              <div v-for="(item, index) in redisData.callingQueue" :key="index" class="queue-item">
                <span class="queue-index">{{ index + 1 }}</span>
                <span class="queue-id">{{ item }}</span>
              </div>
            </div>
            <div v-else class="queue-empty">暂无叫号记录</div>
          </div>
        </div>
      </div>
      <div class="queue-stats">
        <div class="queue-stat-item">
          <span class="queue-stat-label">等待中：</span>
          <span class="queue-stat-value">{{ redisData.waitingCount || 0 }} 人</span>
        </div>
        <div class="queue-stat-item">
          <span class="queue-stat-label">已叫号：</span>
          <span class="queue-stat-value">{{ redisData.callingCount || 0 }} 人</span>
        </div>
        <div class="queue-stat-item">
          <span class="queue-stat-label">总计：</span>
          <span class="queue-stat-value">{{ (redisData.waitingCount || 0) + (redisData.callingCount || 0) }} 人</span>
        </div>
      </div>
    </div>
    
    <!-- 功能快捷入口 -->
    <div v-if="selectedShopId" class="quick-actions">
      <h3>🚀 快捷操作</h3>
      <div class="action-grid">
        <div @click="goToPage('/ordering')" class="action-card">
          <div class="action-icon">🍽️</div>
          <div class="action-title">在线点餐</div>
          <div class="action-desc">处理顾客点餐请求</div>
        </div>
        
        <div @click="goToPage('/call-number')" class="action-card">
          <div class="action-icon">📢</div>
          <div class="action-title">叫号管理</div>
          <div class="action-desc">管理排队叫号</div>
        </div>
        
        <div @click="goToPage('/table-management')" class="action-card">
          <div class="action-icon">🪑</div>
          <div class="action-title">桌台管理</div>
          <div class="action-desc">管理店铺桌台</div>
        </div>
        
        <div @click="goToPage('/orders')" class="action-card">
          <div class="action-icon">📋</div>
          <div class="action-title">订单管理</div>
          <div class="action-desc">查看所有订单</div>
        </div>
        
        <div @click="goToPage('/queue')" class="action-card">
          <div class="action-icon">🎫</div>
          <div class="action-title">排队管理</div>
          <div class="action-desc">查看排队情况</div>
        </div>
        
        <div @click="goToPage('/shops')" class="action-card">
          <div class="action-icon">🏪</div>
          <div class="action-title">店铺管理</div>
          <div class="action-desc">管理店铺信息</div>
        </div>
      </div>
    </div>
    
    <!-- 实时状态 -->
    <div v-if="selectedShopId" class="realtime-status">
      <h3>📊 实时状态</h3>
      <div class="status-grid">
        <div class="status-item">
          <span class="status-label">营业状态：</span>
          <span :class="['status-value', shopStatus === 1 ? 'status-open' : 'status-closed']">
            {{ shopStatus === 1 ? '🟢 营业中' : '🔴 休息中' }}
          </span>
        </div>
        
        <div class="status-item">
          <span class="status-label">桌台占用率：</span>
          <span class="status-value">
            {{ stats.tableOccupancyRate }}%
          </span>
        </div>
        
        <div class="status-item">
          <span class="status-label">平均等待时间：</span>
          <span class="status-value">
            {{ stats.avgWaitTime }} 分钟
          </span>
        </div>
        
        <div class="status-item">
          <span class="status-label">今日服务顾客：</span>
          <span class="status-value">
            {{ stats.todayCustomers }} 人
          </span>
        </div>
      </div>
    </div>
    
    <!-- 最近订单 -->
    <div v-if="selectedShopId" class="recent-orders">
      <h3>📋 最近订单</h3>
      <div v-if="recentOrders.length > 0" class="order-list">
        <div v-for="order in recentOrders" :key="order.id" class="order-item">
          <div class="order-header">
            <span class="order-no">{{ order.orderNo }}</span>
            <span :class="['order-status', getStatusClass(order.orderStatus)]">
              {{ getStatusText(order.orderStatus) }}
            </span>
          </div>
          <div class="order-info">
            <span class="order-type">{{ getOrderTypeText(order.orderType) }}</span>
            <span class="order-amount">¥{{ order.totalAmount?.toFixed(2) || '0.00' }}</span>
            <span class="order-time">{{ formatTime(order.createdAt) }}</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-orders">
        <p>暂无最近订单</p>
      </div>
      <div v-if="recentOrders.length > 0" class="view-more">
        <button @click="goToPage('/orders')" class="btn-view-more">查看全部订单 →</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const selectedShopId = ref('')
const shops = ref([])
const shopStatus = ref(0)
const loading = ref(false)
let refreshInterval = null

const stats = ref({
  todayOrders: 0,
  todayRevenue: 0,
  waitingQueue: 0,
  availableTables: 0,
  totalTables: 0,
  ordersTrend: 0,
  revenueTrend: 0,
  todayCustomers: 0,
  tableOccupancyRate: 0,
  avgWaitTime: 15
})

const redisData = ref({
  waitingQueue: [],
  callingQueue: [],
  waitingCount: 0,
  callingCount: 0
})

const recentOrders = ref([])

const loadShops = async () => {
  try {
    const response = await axios.get('/api/shop/list')
    if (response.data.code === 200) {
      shops.value = response.data.data || []
      
      if (shops.value.length > 0 && !selectedShopId.value) {
        selectedShopId.value = shops.value[0].id
        userStore.currentShopId = shops.value[0].id
        await loadDashboardData()
      }
    }
  } catch (error) {
    console.error('加载店铺失败:', error)
  }
}

const handleShopChange = async () => {
  if (selectedShopId.value) {
    userStore.currentShopId = selectedShopId.value
    await loadDashboardData()
  }
}

const loadDashboardData = async () => {
  if (!selectedShopId.value) return
  
  loading.value = true
  try {
    await Promise.all([
      loadStats(),
      loadRecentOrders(),
      loadShopStatus(),
      loadRedisQueueData()
    ])
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await axios.get(`/api/shop/${selectedShopId.value}/stats`)
    if (response.data.code === 200) {
      const data = response.data.data
      stats.value = {
        ...stats.value,
        todayOrders: data.todayOrders || 0,
        todayRevenue: data.todayRevenue || 0,
        waitingQueue: data.waitingQueue || 0,
        availableTables: data.availableTables || 0,
        totalTables: data.totalTables || 0,
        ordersTrend: data.ordersTrend || 0,
        revenueTrend: data.revenueTrend || 0,
        todayCustomers: data.todayCustomers || 0,
        tableOccupancyRate: data.tableOccupancyRate || 0,
        avgWaitTime: data.avgWaitTime || 15
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadRecentOrders = async () => {
  try {
    const response = await axios.get(`/api/order/shop/${selectedShopId.value}/recent`)
    if (response.data.code === 200) {
      recentOrders.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载最近订单失败:', error)
  }
}

const loadShopStatus = async () => {
  try {
    const response = await axios.get(`/api/shop/${selectedShopId.value}`)
    if (response.data.code === 200) {
      shopStatus.value = response.data.data?.shopStatus || 0
    }
  } catch (error) {
    console.error('加载店铺状态失败:', error)
  }
}

const loadRedisQueueData = async () => {
  try {
    const response = await axios.get(`/api/queue/stats/redis/${selectedShopId.value}`)
    if (response.data.code === 200) {
      const data = response.data.data
      redisData.value = {
        waitingQueue: data.waitingQueue || [],
        callingQueue: data.callingQueue || [],
        waitingCount: data.waitingCount || 0,
        callingCount: data.callingCount || 0
      }
    }
  } catch (error) {
    console.error('加载Redis排队数据失败:', error)
  }
}

const startAutoRefresh = () => {
  refreshInterval = setInterval(() => {
    if (selectedShopId.value) {
      loadRedisQueueData()
    }
  }, 5000) // 每5秒刷新一次Redis数据
}

const stopAutoRefresh = () => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

const goToPage = (path) => {
  router.push(path)
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '待接单',
    2: '制作中',
    3: '待取餐',
    4: '已完成',
    5: '已取消'
  }
  return statusMap[status] || '未知'
}

const getStatusClass = (status) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-processing',
    2: 'status-making',
    3: 'status-ready',
    4: 'status-completed',
    5: 'status-cancelled'
  }
  return classMap[status] || ''
}

const getOrderTypeText = (type) => {
  const typeMap = {
    1: '堂食',
    2: '外带',
    3: '外卖'
  }
  return typeMap[type] || '未知'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

onMounted(() => {
  if (!userStore.hasAnyRole([userStore.ROLES.MANAGER, userStore.ROLES.ADMIN])) {
    alert('您没有权限访问店长管理界面')
    router.push('/dashboard')
    return
  }
  
  loadShops()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.manager-dashboard {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  min-height: calc(100vh - 60px);
}

.manager-dashboard h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 22px;
  font-weight: 600;
}

.shop-selector {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.shop-selector label {
  font-weight: bold;
  color: #2c5282;
  font-size: 14px;
}

.shop-selector select {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: white;
}

.btn-refresh {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-refresh:hover {
  background: #7a8feb;
}

.loading-text {
  color: #667eea;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.stat-icon {
  font-size: 40px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f0f4ff;
}

.stat-orders .stat-icon {
  background: #e3f2fd;
}

.stat-revenue .stat-icon {
  background: #e8f5e9;
}

.stat-queue .stat-icon {
  background: #fff3e0;
}

.stat-tables .stat-icon {
  background: #fce4ec;
}

.stat-customers .stat-icon {
  background: #e0f2f1;
}

.stat-calling .stat-icon {
  background: #f3e5f5;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #718096;
  margin-bottom: 5px;
}

.stat-trend {
  font-size: 12px;
  font-weight: 500;
}

.trend-up {
  color: #48bb78;
}

.trend-down {
  color: #f56565;
}

.trend-neutral {
  color: #718096;
}

.redis-queue-data {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.redis-queue-data h3 {
  margin: 0 0 20px 0;
  color: #2d3748;
  font-size: 18px;
  font-weight: 600;
}

.queue-data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.queue-data-card {
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
}

.queue-data-title {
  background: #f7fafc;
  padding: 12px 15px;
  font-weight: 600;
  color: #2d3748;
  border-bottom: 1px solid #e2e8f0;
}

.queue-data-content {
  max-height: 200px;
  overflow-y: auto;
}

.queue-list {
  padding: 10px;
}

.queue-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.queue-item:hover {
  background: #f7fafc;
}

.queue-item:last-child {
  border-bottom: none;
}

.queue-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: bold;
  margin-right: 10px;
}

.queue-id {
  color: #2d3748;
  font-size: 14px;
}

.queue-empty {
  text-align: center;
  padding: 40px 20px;
  color: #a0aec0;
  font-size: 14px;
}

.queue-stats {
  display: flex;
  gap: 20px;
  padding: 15px;
  background: #f7fafc;
  border-radius: 6px;
}

.queue-stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.queue-stat-label {
  color: #718096;
  font-size: 14px;
}

.queue-stat-value {
  color: #2d3748;
  font-weight: 600;
  font-size: 16px;
}

.quick-actions {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.quick-actions h3 {
  margin: 0 0 20px 0;
  color: #2d3748;
  font-size: 18px;
  font-weight: 600;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.action-card {
  padding: 20px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  border-color: #667eea;
  background: #f7fafc;
  transform: translateY(-3px);
}

.action-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 5px;
}

.action-desc {
  font-size: 13px;
  color: #718096;
}

.realtime-status {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.realtime-status h3 {
  margin: 0 0 20px 0;
  color: #2d3748;
  font-size: 18px;
  font-weight: 600;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f7fafc;
  border-radius: 6px;
}

.status-label {
  font-size: 14px;
  color: #718096;
}

.status-value {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.status-open {
  color: #48bb78;
}

.status-closed {
  color: #f56565;
}

.recent-orders {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.recent-orders h3 {
  margin: 0 0 20px 0;
  color: #2d3748;
  font-size: 18px;
  font-weight: 600;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  padding: 15px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  transition: all 0.3s;
}

.order-item:hover {
  border-color: #667eea;
  background: #f7fafc;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.order-no {
  font-weight: bold;
  color: #2d3748;
}

.order-status {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-processing {
  background: #dbeafe;
  color: #1e40af;
}

.status-making {
  background: #e0e7ff;
  color: #3730a3;
}

.status-ready {
  background: #d1fae5;
  color: #065f46;
}

.status-completed {
  background: #dcfce7;
  color: #166534;
}

.status-cancelled {
  background: #fee2e2;
  color: #991b1b;
}

.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #718096;
}

.order-type {
  font-weight: 500;
}

.order-amount {
  font-weight: bold;
  color: #2d3748;
}

.empty-orders {
  text-align: center;
  padding: 40px 20px;
  color: #a0aec0;
}

.view-more {
  margin-top: 15px;
  text-align: center;
}

.btn-view-more {
  padding: 10px 20px;
  background: transparent;
  color: #667eea;
  border: 1px solid #667eea;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-view-more:hover {
  background: #667eea;
  color: white;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #a0aec0;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #718096;
}
</style>