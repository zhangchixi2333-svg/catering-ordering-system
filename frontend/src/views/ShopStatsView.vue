<template>
  <div class="shop-stats">
    <div class="stats-header">
      <h2>📊 店铺统计分析</h2>
      <div class="header-controls">
        <select v-model="selectedShopId" @change="handleShopChange" class="shop-select">
          <option value="">选择店铺</option>
          <option v-for="shop in shops" :key="shop.id" :value="shop.id">
            {{ shop.shopName }} ({{ shop.shopCode }})
          </option>
        </select>
        <div class="time-range">
          <button 
            v-for="range in timeRanges" 
            :key="range.value"
            @click="selectTimeRange(range.value)"
            :class="['range-btn', { active: currentTimeRange === range.value }]"
          >
            {{ range.label }}
          </button>
        </div>
        <button @click="loadStatsData" class="btn-refresh" :disabled="loading">
          🔄 {{ loading ? '刷新中...' : '刷新' }}
        </button>
        <button @click="exportData" class="btn-export" :disabled="!selectedShopId || loading">
          📥 导出数据
        </button>
      </div>
    </div>

    <div v-if="selectedShopId && loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载数据中...</p>
    </div>

    <div v-if="selectedShopId && !loading" class="stats-content">
      <!-- 核心指标卡片 -->
      <div class="core-metrics">
        <div class="metric-card metric-orders">
          <div class="metric-header">
            <span class="metric-icon">📋</span>
            <span class="metric-title">订单数量</span>
          </div>
          <div class="metric-value">{{ statsData.orderCount }}</div>
          <div class="metric-trend" :class="statsData.orderTrend >= 0 ? 'trend-up' : 'trend-down'">
            {{ statsData.orderTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(statsData.orderTrend).toFixed(1) }}%
            <span class="trend-label">较上期</span>
          </div>
        </div>

        <div class="metric-card metric-revenue">
          <div class="metric-header">
            <span class="metric-icon">💰</span>
            <span class="metric-title">营业收入</span>
          </div>
          <div class="metric-value">¥{{ formatNumber(statsData.revenue) }}</div>
          <div class="metric-trend" :class="statsData.revenueTrend >= 0 ? 'trend-up' : 'trend-down'">
            {{ statsData.revenueTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(statsData.revenueTrend).toFixed(1) }}%
            <span class="trend-label">较上期</span>
          </div>
        </div>

        <div class="metric-card metric-customers">
          <div class="metric-header">
            <span class="metric-icon">👥</span>
            <span class="metric-title">顾客数量</span>
          </div>
          <div class="metric-value">{{ statsData.customerCount }}</div>
          <div class="metric-trend" :class="statsData.customerTrend >= 0 ? 'trend-up' : 'trend-down'">
            {{ statsData.customerTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(statsData.customerTrend).toFixed(1) }}%
            <span class="trend-label">较上期</span>
          </div>
        </div>

        <div class="metric-card metric-avg">
          <div class="metric-header">
            <span class="metric-icon">📈</span>
            <span class="metric-title">客单价</span>
          </div>
          <div class="metric-value">¥{{ formatNumber(statsData.avgOrderValue) }}</div>
          <div class="metric-trend" :class="statsData.avgTrend >= 0 ? 'trend-up' : 'trend-down'">
            {{ statsData.avgTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(statsData.avgTrend).toFixed(1) }}%
            <span class="trend-label">较上期</span>
          </div>
        </div>

        <div class="metric-card metric-queue">
          <div class="metric-header">
            <span class="metric-icon">🎫</span>
            <span class="metric-title">排队人数</span>
          </div>
          <div class="metric-value">{{ statsData.totalQueueCount }}</div>
          <div class="metric-sub">
            <span>平均等待: {{ statsData.avgWaitTime }}分钟</span>
          </div>
        </div>

        <div class="metric-card metric-payment">
          <div class="metric-header">
            <span class="metric-icon">💳</span>
            <span class="metric-title">支付成功率</span>
          </div>
          <div class="metric-value">{{ calculatePaymentSuccessRate }}%</div>
          <div class="metric-sub">
            <span>成功: {{ statsData.paymentSuccessCount }} | 失败: {{ statsData.paymentFailedCount }}</span>
          </div>
        </div>
      </div>

      <!-- 趋势图表区域 -->
      <div class="charts-section">
        <div class="chart-card">
          <div class="chart-header">
            <h3>📈 营收趋势</h3>
            <div class="chart-legend">
              <span class="legend-item"><span class="legend-color revenue"></span>营收</span>
              <span class="legend-item"><span class="legend-color orders"></span>订单</span>
            </div>
          </div>
          <div class="chart-container">
            <div class="simple-chart">
              <div class="chart-bars">
                <div 
                  v-for="(item, index) in statsData.dailyStats" 
                  :key="index"
                  class="chart-bar-group"
                >
                  <div 
                    class="chart-bar revenue-bar" 
                    :style="{ height: getBarHeight(item.revenue, 'revenue') + '%' }"
                    :title="`营收: ¥${formatNumber(item.revenue)}`"
                  ></div>
                  <div 
                    class="chart-bar orders-bar" 
                    :style="{ height: getBarHeight(item.orderCount, 'orders') + '%' }"
                    :title="`订单: ${item.orderCount}`"
                  ></div>
                  <div class="chart-label">{{ item.date }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="chart-card">
          <div class="chart-header">
            <h3>🎫 排队完成率</h3>
          </div>
          <div class="chart-container">
            <div class="simple-chart">
              <div class="chart-bars">
                <div 
                  v-for="(item, index) in statsData.queueStats" 
                  :key="index"
                  class="chart-bar-group"
                >
                  <div 
                    class="chart-bar completion-bar" 
                    :style="{ height: item.completionRate + '%' }"
                    :title="`完成率: ${item.completionRate.toFixed(1)}%`"
                  ></div>
                  <div class="chart-label">{{ item.date }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 详细统计表格 -->
      <div class="detailed-stats">
        <div class="stats-tabs">
          <button 
            v-for="tab in tabs" 
            :key="tab.value"
            @click="activeTab = tab.value"
            :class="['tab-btn', { active: activeTab === tab.value }]"
          >
            {{ tab.label }}
          </button>
        </div>

        <div v-if="activeTab === 'orders'" class="tab-content">
          <div class="table-header">
            <h3>📋 订单统计详情</h3>
            <div class="table-summary">
              <span>总订单数: <strong>{{ statsData.orderCount }}</strong></span>
              <span>总营收: <strong>¥{{ formatNumber(statsData.revenue) }}</strong></span>
              <span>总顾客数: <strong>{{ statsData.customerCount }}</strong></span>
            </div>
          </div>
          <div class="table-responsive">
            <table class="stats-table">
              <thead>
                <tr>
                  <th>日期</th>
                  <th>订单数</th>
                  <th>营收</th>
                  <th>顾客数</th>
                  <th>客单价</th>
                  <th>趋势</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in statsData.dailyStats" :key="index">
                  <td>{{ item.date }}</td>
                  <td>{{ item.orderCount }}</td>
                  <td>¥{{ formatNumber(item.revenue) }}</td>
                  <td>{{ item.customerCount }}</td>
                  <td>¥{{ formatNumber(item.avgOrderValue) }}</td>
                  <td :class="item.trend >= 0 ? 'trend-up' : 'trend-down'">
                    {{ item.trend >= 0 ? '↑' : '↓' }} {{ Math.abs(item.trend).toFixed(1) }}%
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-if="activeTab === 'queue'" class="tab-content">
          <div class="table-header">
            <h3>🎫 排队统计详情</h3>
            <div class="table-summary">
              <span>总排队数: <strong>{{ statsData.totalQueueCount }}</strong></span>
              <span>平均等待: <strong>{{ statsData.avgWaitTime }}分钟</strong></span>
            </div>
          </div>
          <div class="table-responsive">
            <table class="stats-table">
              <thead>
                <tr>
                  <th>日期</th>
                  <th>排队数</th>
                  <th>平均等待时间</th>
                  <th>完成数</th>
                  <th>取消数</th>
                  <th>完成率</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in statsData.queueStats" :key="index">
                  <td>{{ item.date }}</td>
                  <td>{{ item.queueCount }}</td>
                  <td>{{ item.avgWaitTime }}分钟</td>
                  <td>{{ item.completedCount }}</td>
                  <td>{{ item.cancelledCount }}</td>
                  <td>
                    <div class="progress-bar">
                      <div 
                        class="progress-fill" 
                        :style="{ width: item.completionRate + '%' }"
                      ></div>
                    </div>
                    <span>{{ item.completionRate.toFixed(1) }}%</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-if="activeTab === 'payment'" class="tab-content">
          <div class="table-header">
            <h3>💳 支付统计详情</h3>
            <div class="table-summary">
              <span>支付成功: <strong>{{ statsData.paymentSuccessCount }}</strong></span>
              <span>支付失败: <strong>{{ statsData.paymentFailedCount }}</strong></span>
            </div>
          </div>
          <div class="table-responsive">
            <table class="stats-table">
              <thead>
                <tr>
                  <th>支付方式</th>
                  <th>订单数</th>
                  <th>金额</th>
                  <th>成功率</th>
                  <th>平均处理时间</th>
                  <th>占比</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in statsData.paymentStats" :key="index">
                  <td>{{ item.paymentMethod }}</td>
                  <td>{{ item.count }}</td>
                  <td>¥{{ formatNumber(item.amount) }}</td>
                  <td :class="item.successRate >= 95 ? 'success-high' : 'success-normal'">
                    {{ item.successRate.toFixed(1) }}%
                  </td>
                  <td>{{ item.avgProcessTime }}秒</td>
                  <td>
                    <div class="progress-bar">
                      <div 
                        class="progress-fill payment-fill" 
                        :style="{ width: calculatePaymentPercentage(item.count) + '%' }"
                      ></div>
                    </div>
                    <span>{{ calculatePaymentPercentage(item.count).toFixed(1) }}%</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!selectedShopId" class="empty-state">
      <div class="empty-icon">🏪</div>
      <h3>请选择店铺查看统计数据</h3>
      <p>选择店铺后将显示详细的统计分析数据</p>
      <div class="quick-tips">
        <h4>💡 快速提示</h4>
        <ul>
          <li>选择店铺后可查看今日、本周、本月的统计数据</li>
          <li>支持数据导出功能，便于进一步分析</li>
          <li>实时更新排队和支付数据</li>
          <li>多维度图表展示业务趋势</li>
        </ul>
      </div>
    </div>

    <!-- 自动刷新提示 -->
    <div v-if="autoRefresh && selectedShopId" class="auto-refresh-indicator">
      <span class="refresh-icon">🔄</span>
      <span>自动刷新中 ({{ countdown }}秒)</span>
      <button @click="toggleAutoRefresh" class="btn-stop">停止</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import axios from 'axios'

const selectedShopId = ref('')
const shops = ref([])
const loading = ref(false)
const currentTimeRange = ref('today')
const autoRefresh = ref(false)
const countdown = ref(30)
let refreshTimer = null
let countdownTimer = null

const timeRanges = [
  { label: '今日', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '自定义', value: 'custom' }
]

const tabs = [
  { label: '订单统计', value: 'orders' },
  { label: '排队统计', value: 'queue' },
  { label: '支付统计', value: 'payment' }
]

const activeTab = ref('orders')

const statsData = ref({
  orderCount: 0,
  revenue: 0,
  customerCount: 0,
  avgOrderValue: 0,
  orderTrend: 0,
  revenueTrend: 0,
  customerTrend: 0,
  avgTrend: 0,
  totalQueueCount: 0,
  avgWaitTime: 0,
  paymentSuccessCount: 0,
  paymentFailedCount: 0,
  dailyStats: [],
  queueStats: [],
  paymentStats: []
})

const calculatePaymentSuccessRate = computed(() => {
  const total = statsData.value.paymentSuccessCount + statsData.value.paymentFailedCount
  if (total === 0) return 0
  return ((statsData.value.paymentSuccessCount / total) * 100).toFixed(1)
})

const loadShops = async () => {
  try {
    const response = await axios.get('/api/shop/list')
    if (response.data.code === 200) {
      shops.value = response.data.data || []
      console.log('加载店铺成功:', shops.value)
    }
  } catch (error) {
    console.error('加载店铺失败:', error)
  }
}

const handleShopChange = () => {
  if (selectedShopId.value) {
    loadStatsData()
    if (autoRefresh.value) {
      startAutoRefresh()
    }
  }
}

const selectTimeRange = (range) => {
  currentTimeRange.value = range
  loadStatsData()
}

const loadStatsData = async () => {
  if (!selectedShopId.value) return
  
  loading.value = true
  try {
    const response = await axios.get(`/api/shop/${selectedShopId.value}/stats`, {
      params: { timeRange: currentTimeRange.value }
    })
    
    if (response.data.code === 200) {
      const data = response.data.data
      statsData.value = {
        orderCount: data.orderCount || 0,
        revenue: data.revenue || 0,
        customerCount: data.customerCount || 0,
        avgOrderValue: data.avgOrderValue || 0,
        orderTrend: data.orderTrend || 0,
        revenueTrend: data.revenueTrend || 0,
        customerTrend: data.customerTrend || 0,
        avgTrend: data.avgTrend || 0,
        totalQueueCount: data.totalQueueCount || 0,
        avgWaitTime: data.avgWaitTime || 0,
        paymentSuccessCount: data.paymentSuccessCount || 0,
        paymentFailedCount: data.paymentFailedCount || 0,
        dailyStats: data.dailyStats || [],
        queueStats: data.queueStats || [],
        paymentStats: data.paymentStats || []
      }
      console.log('加载统计数据成功:', statsData.value)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

const formatNumber = (num) => {
  if (num === null || num === undefined) return '0.00'
  return Number(num).toFixed(2)
}

const getBarHeight = (value, type) => {
  const values = type === 'revenue' 
    ? statsData.value.dailyStats.map(d => d.revenue)
    : statsData.value.dailyStats.map(d => d.orderCount)
  
  const max = Math.max(...values, 1)
  return (value / max) * 80
}

const calculatePaymentPercentage = (count) => {
  const total = statsData.value.paymentStats.reduce((sum, item) => sum + item.count, 0)
  if (total === 0) return 0
  return (count / total) * 100
}

const exportData = () => {
  if (!selectedShopId.value) return
  
  const csvContent = generateCSV()
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  
  link.setAttribute('href', url)
  link.setAttribute('download', `店铺统计_${selectedShopId.value}_${new Date().toISOString().split('T')[0]}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const generateCSV = () => {
  let csv = '日期,订单数,营收,顾客数,客单价,趋势\n'
  
  statsData.value.dailyStats.forEach(item => {
    csv += `${item.date},${item.orderCount},${item.revenue},${item.customerCount},${item.avgOrderValue},${item.trend}\n`
  })
  
  return csv
}

const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
  if (autoRefresh.value) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

const startAutoRefresh = () => {
  stopAutoRefresh()
  countdown.value = 30
  
  refreshTimer = setInterval(() => {
    loadStatsData()
    countdown.value = 30
  }, 30000)
  
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      countdown.value = 30
    }
  }, 1000)
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

onMounted(() => {
  console.log('ShopStatsView组件已挂载')
  loadShops()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.shop-stats {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.stats-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.header-controls {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.shop-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  min-width: 200px;
}

.time-range {
  display: flex;
  gap: 4px;
  background: #f5f5f5;
  padding: 4px;
  border-radius: 6px;
}

.range-btn {
  padding: 6px 16px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.2s;
}

.range-btn:hover {
  background: rgba(0, 0, 0, 0.05);
}

.range-btn.active {
  background: white;
  color: #1890ff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.btn-refresh, .btn-export {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-refresh {
  background: #1890ff;
  color: white;
}

.btn-refresh:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-refresh:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.btn-export {
  background: #52c41a;
  color: white;
}

.btn-export:hover:not(:disabled) {
  background: #73d13d;
}

.btn-export:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.core-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.metric-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.metric-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.metric-icon {
  font-size: 20px;
}

.metric-title {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.metric-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.metric-trend {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.metric-sub {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.trend-up {
  color: #52c41a;
}

.trend-down {
  color: #ff4d4f;
}

.trend-label {
  color: #999;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 16px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.chart-legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-color.revenue {
  background: #1890ff;
}

.legend-color.orders {
  background: #52c41a;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.simple-chart {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-bars {
  flex: 1;
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding-bottom: 24px;
  border-bottom: 1px solid #eee;
}

.chart-bar-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  position: relative;
}

.chart-bar {
  width: 30px;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
  cursor: pointer;
}

.chart-bar:hover {
  opacity: 0.8;
}

.revenue-bar {
  background: linear-gradient(180deg, #1890ff 0%, #40a9ff 100%);
}

.orders-bar {
  background: linear-gradient(180deg, #52c41a 0%, #73d13d 100%);
}

.completion-bar {
  background: linear-gradient(180deg, #722ed1 0%, #9254de 100%);
}

.chart-label {
  font-size: 11px;
  color: #999;
  text-align: center;
  margin-top: 4px;
}

.detailed-stats {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.stats-tabs {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.tab-btn {
  padding: 10px 20px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.tab-btn:hover {
  color: #1890ff;
}

.tab-btn.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
}

.tab-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.table-summary {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #666;
  flex-wrap: wrap;
}

.table-summary strong {
  color: #333;
}

.table-responsive {
  overflow-x: auto;
}

.stats-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 600px;
}

.stats-table thead {
  background: #fafafa;
}

.stats-table th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #eee;
  white-space: nowrap;
}

.stats-table td {
  padding: 12px;
  border-bottom: 1px solid #eee;
  color: #666;
}

.stats-table tbody tr:hover {
  background: #fafafa;
}

.success-high {
  color: #52c41a;
  font-weight: 600;
}

.success-normal {
  color: #faad14;
}

.progress-bar {
  display: inline-block;
  width: 60px;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  margin-right: 8px;
  vertical-align: middle;
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.payment-fill {
  background: linear-gradient(90deg, #1890ff 0%, #40a9ff 100%);
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #333;
}

.empty-state p {
  margin: 0 0 24px;
  color: #999;
}

.quick-tips {
  text-align: left;
  max-width: 500px;
  margin: 0 auto;
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.quick-tips h4 {
  margin: 0 0 12px;
  color: #333;
  font-size: 14px;
}

.quick-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
  font-size: 13px;
}

.quick-tips li {
  margin-bottom: 8px;
}

.auto-refresh-indicator {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: white;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 1000;
}

.refresh-icon {
  animation: spin 2s linear infinite;
}

.btn-stop {
  padding: 4px 12px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background 0.2s;
}

.btn-stop:hover {
  background: #ff7875;
}

@media (max-width: 768px) {
  .stats-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-controls {
    flex-direction: column;
  }
  
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .core-metrics {
    grid-template-columns: 1fr;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .auto-refresh-indicator {
    left: 20px;
    right: 20px;
    justify-content: center;
  }
}
</style>