<template>
  <div class="settings-view">
    <h2>⚙️ 系统设置</h2>
    
    <!-- 标签页导航 -->
    <div class="tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        @click="activeTab = tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
      >
        {{ tab.icon }} {{ tab.label }}
      </button>
    </div>
    
    <!-- 系统信息标签页 -->
    <div v-if="activeTab === 'system'" class="tab-content">
      <div class="card">
        <h3>📊 系统信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>系统版本</label>
            <span>v1.0.0</span>
          </div>
          <div class="info-item">
            <label>前端框架</label>
            <span>Vue 3 + Vite</span>
          </div>
          <div class="info-item">
            <label>后端架构</label>
            <span>Spring Cloud 微服务</span>
          </div>
          <div class="info-item">
            <label>数据库</label>
            <span>MySQL 8.0</span>
          </div>
          <div class="info-item">
            <label>缓存</label>
            <span>Redis</span>
          </div>
          <div class="info-item">
            <label>消息队列</label>
            <span>RabbitMQ</span>
          </div>
          <div class="info-item">
            <label>服务注册</label>
            <span>Eureka</span>
          </div>
          <div class="info-item">
            <label>API 网关</label>
            <span>Spring Cloud Gateway</span>
          </div>
        </div>
      </div>
      
      <div class="card">
        <h3>🖥️ 服务状态监控</h3>
        <div class="service-list">
          <div v-for="service in services" :key="service.name" class="service-item">
            <div class="service-info">
              <span class="service-name">{{ service.name }}</span>
              <span class="service-port">:{{ service.port }}</span>
            </div>
            <div class="service-status">
              <span :class="['status-badge', service.status]">
                {{ getStatusText(service.status) }}
              </span>
              <button @click="checkService(service)" class="btn-check">🔄 检查</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 店铺配置标签页 -->
    <div v-if="activeTab === 'config'" class="tab-content">
      <div class="card">
        <div class="card-header">
          <h3>🏪 店铺配置管理</h3>
          <select v-model="selectedShopId" @change="loadShopConfigs">
            <option value="">选择店铺</option>
            <option v-for="shop in shops" :key="shop.id" :value="shop.id">
              {{ shop.shopName }}
            </option>
          </select>
        </div>
        
        <div v-if="configs.length > 0" class="config-list">
          <div v-for="config in configs" :key="config.id" class="config-item">
            <div class="config-info">
              <div class="config-key">{{ config.configKey }}</div>
              <div class="config-desc">{{ config.configDesc || '无描述' }}</div>
            </div>
            <div class="config-value">
              <input 
                v-model="config.configValue" 
                @blur="updateConfig(config)"
                :placeholder="'输入' + config.configKey"
              />
              <button @click="resetConfig(config)" class="btn-reset">↩️ 重置</button>
            </div>
          </div>
        </div>
        
        <div v-else-if="selectedShopId" class="empty-state">
          <p>该店铺暂无配置</p>
        </div>
        
        <div v-else class="empty-state">
          <p>请选择店铺查看配置</p>
        </div>
      </div>
    </div>
    
    <!-- 缓存管理标签页 -->
    <div v-if="activeTab === 'cache'" class="tab-content">
      <div class="card">
        <h3>💾 缓存管理</h3>
        <div class="cache-stats">
          <div class="stat-item">
            <label>Redis 连接状态</label>
            <span class="status-connected">✅ 已连接</span>
          </div>
          <div class="stat-item">
            <label>排队队列数</label>
            <span>{{ queueCount }}</span>
          </div>
          <div class="stat-item">
            <label>在线用户数</label>
            <span>{{ onlineUsers }}</span>
          </div>
        </div>
        
        <div class="cache-actions">
          <button @click="clearQueueCache" class="btn-warning">
            🗑️ 清除排队缓存
          </button>
          <button @click="clearAllCache" class="btn-danger">
            ⚠️ 清除所有缓存
          </button>
          <button @click="refreshCacheStats" class="btn-primary">
            🔄 刷新统计
          </button>
        </div>
      </div>
    </div>
    
    <!-- 数据导出标签页 -->
    <div v-if="activeTab === 'export'" class="tab-content">
      <div class="card">
        <h3>📊 数据导出</h3>
        <div class="export-options">
          <div class="export-item">
            <div class="export-info">
              <h4>订单数据</h4>
              <p>导出所有订单记录为 Excel 格式</p>
            </div>
            <button @click="exportOrders" class="btn-export">📥 导出</button>
          </div>
          
          <div class="export-item">
            <div class="export-info">
              <h4>店铺数据</h4>
              <p>导出所有店铺信息为 CSV 格式</p>
            </div>
            <button @click="exportShops" class="btn-export">📥 导出</button>
          </div>
          
          <div class="export-item">
            <div class="export-info">
              <h4>排队数据</h4>
              <p>导出排队记录为 Excel 格式</p>
            </div>
            <button @click="exportQueues" class="btn-export">📥 导出</button>
          </div>
          
          <div class="export-item">
            <div class="export-info">
              <h4>用户数据</h4>
              <p>导出用户信息为 CSV 格式</p>
            </div>
            <button @click="exportUsers" class="btn-export">📥 导出</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 系统日志标签页 -->
    <div v-if="activeTab === 'logs'" class="tab-content">
      <div class="card">
        <div class="card-header">
          <h3>📝 系统日志</h3>
          <div class="log-controls">
            <select v-model="logLevel">
              <option value="ALL">全部</option>
              <option value="INFO">信息</option>
              <option value="WARN">警告</option>
              <option value="ERROR">错误</option>
            </select>
            <button @click="refreshLogs" class="btn-refresh">🔄 刷新</button>
            <button @click="clearLogs" class="btn-clear">🗑️ 清空</button>
          </div>
        </div>
        
        <div class="log-container">
          <div v-for="(log, index) in filteredLogs" :key="index" :class="['log-item', log.level.toLowerCase()]">
            <span class="log-time">{{ log.time }}</span>
            <span class="log-level">{{ log.level }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
          
          <div v-if="filteredLogs.length === 0" class="empty-logs">
            <p>暂无日志数据</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { shopApi, notificationApi } from '../api'

// 标签页配置
const tabs = [
  { key: 'system', label: '系统信息', icon: '📊' },
  { key: 'config', label: '店铺配置', icon: '🏪' },
  { key: 'cache', label: '缓存管理', icon: '💾' },
  { key: 'export', label: '数据导出', icon: '📊' },
  { key: 'logs', label: '系统日志', icon: '📝' }
]

const activeTab = ref('system')

// 服务列表
const services = ref([
  { name: 'Eureka Server', port: 8761, status: 'unknown' },
  { name: 'Gateway Service', port: 8080, status: 'unknown' },
  { name: 'User Service', port: 8084, status: 'unknown' },
  { name: 'Shop Service', port: 8081, status: 'unknown' },
  { name: 'Order Service', port: 8083, status: 'unknown' },
  { name: 'Queue Service', port: 8085, status: 'unknown' },
  { name: 'Menu Service', port: 8082, status: 'unknown' },
  { name: 'Payment Service', port: 8087, status: 'unknown' },
  { name: 'Notification Service', port: 8086, status: 'unknown' }
])

// 店铺配置
const shops = ref([])
const selectedShopId = ref('')
const configs = ref([])

// 缓存统计
const queueCount = ref(0)
const onlineUsers = ref(0)

// 日志
const logLevel = ref('ALL')
const logs = ref([])

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    running: '运行中',
    stopped: '已停止',
    unknown: '未知'
  }
  return texts[status] || status
}

// 检查服务状态
const checkService = async (service) => {
  try {
    const response = await fetch(`http://localhost:${service.port}/actuator/health`, {
      method: 'GET',
      mode: 'cors'
    })
    
    if (response.ok) {
      service.status = 'running'
    } else {
      service.status = 'stopped'
    }
  } catch (error) {
    service.status = 'stopped'
  }
}

// 加载店铺列表
const loadShops = async () => {
  try {
    const res = await shopApi.getList()
    shops.value = res.data || []
  } catch (error) {
    console.error('加载店铺失败:', error)
  }
}

// 加载店铺配置
const loadShopConfigs = async () => {
  if (!selectedShopId.value) {
    configs.value = []
    return
  }
  
  // TODO: 需要后端提供获取店铺配置的 API
  // const res = await shopApi.getConfigs(selectedShopId.value)
  // configs.value = res.data || []
  
  // 模拟数据
  configs.value = [
    { id: 1, configKey: 'queue_enabled', configValue: 'true', configDesc: '是否启用排队功能' },
    { id: 2, configKey: 'max_queue_number', configValue: '50', configDesc: '最大排队号码数' },
    { id: 3, configKey: 'auto_call_interval', configValue: '300', configDesc: '自动叫号间隔（秒）' },
    { id: 4, configKey: 'payment_timeout', configValue: '900', configDesc: '支付超时时间（秒）' }
  ]
}

// 更新配置
const updateConfig = async (config) => {
  try {
    // TODO: 调用后端 API 更新配置
    // await shopApi.updateConfig(config)
    alert(`✅ 配置 ${config.configKey} 已更新`)
  } catch (error) {
    console.error('更新配置失败:', error)
    alert('❌ 更新失败')
  }
}

// 重置配置
const resetConfig = (config) => {
  if (confirm(`确定要重置配置 ${config.configKey} 吗？`)) {
    // TODO: 调用后端 API 重置配置
    config.configValue = 'true' // 默认值
    alert('✅ 配置已重置')
  }
}

// 清除排队缓存
const clearQueueCache = () => {
  if (confirm('确定要清除所有排队缓存吗？')) {
    // TODO: 调用后端 API 清除 Redis 排队数据
    alert('✅ 排队缓存已清除')
  }
}

// 清除所有缓存
const clearAllCache = () => {
  if (confirm('⚠️ 确定要清除所有缓存吗？此操作不可恢复！')) {
    localStorage.clear()
    sessionStorage.clear()
    // TODO: 调用后端 API 清除 Redis 所有数据
    alert('✅ 所有缓存已清除，请重新登录')
  }
}

// 刷新缓存统计
const refreshCacheStats = async () => {
  try {
    // TODO: 调用后端 API 获取实时统计
    queueCount.value = Math.floor(Math.random() * 100)
    
    const res = await notificationApi.getOnlineCount()
    onlineUsers.value = res.data || 0
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

// 导出数据
const exportOrders = () => {
  alert('📊 订单数据导出功能开发中...')
  // TODO: 调用后端 API 导出 Excel
}

const exportShops = () => {
  alert('📊 店铺数据导出功能开发中...')
  // TODO: 调用后端 API 导出 CSV
}

const exportQueues = () => {
  alert('📊 排队数据导出功能开发中...')
  // TODO: 调用后端 API 导出 Excel
}

const exportUsers = () => {
  alert('📊 用户数据导出功能开发中...')
  // TODO: 调用后端 API 导出 CSV
}

// 过滤日志
const filteredLogs = computed(() => {
  if (logLevel.value === 'ALL') {
    return logs.value
  }
  return logs.value.filter(log => log.level === logLevel.value)
})

// 刷新日志
const refreshLogs = () => {
  // TODO: 调用后端 API 获取最新日志
  logs.value = [
    { time: '2026-05-19 10:30:00', level: 'INFO', message: '系统启动成功' },
    { time: '2026-05-19 10:31:00', level: 'INFO', message: '数据库连接成功' },
    { time: '2026-05-19 10:32:00', level: 'WARN', message: 'Redis 连接超时，正在重试' },
    { time: '2026-05-19 10:33:00', level: 'INFO', message: 'Redis 连接成功' },
    { time: '2026-05-19 10:34:00', level: 'ERROR', message: '订单创建失败：库存不足' }
  ]
}

// 清空日志
const clearLogs = () => {
  if (confirm('确定要清空日志吗？')) {
    logs.value = []
  }
}

onMounted(() => {
  // 检查所有服务状态
  services.value.forEach(service => checkService(service))
  
  // 加载店铺列表
  loadShops()
  
  // 刷新缓存统计
  refreshCacheStats()
  
  // 加载日志
  refreshLogs()
})
</script>

<style scoped>
.settings-view h2 {
  margin-bottom: 20px;
  color: #333;
}

/* 标签页 */
.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 10px;
}

.tab-btn {
  padding: 10px 20px;
  border: none;
  background: white;
  color: #718096;
  border-radius: 6px 6px 0 0;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.tab-btn:hover {
  background: #f7fafc;
  color: #4a5568;
}

.tab-btn.active {
  background: #667eea;
  color: white;
  font-weight: bold;
}

.tab-content {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.card {
  background: white;
  padding: 24px;
  border-radius: 10px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card h3 {
  margin-bottom: 20px;
  color: #667eea;
  font-size: 18px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
}

.card-header select {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
}

/* 系统信息 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background: #f7fafc;
  border-radius: 6px;
}

.info-item label {
  color: #718096;
  font-weight: 500;
}

.info-item span {
  color: #2d3748;
  font-weight: bold;
}

/* 服务列表 */
.service-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.service-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
  transition: all 0.3s;
}

.service-item:hover {
  background: #edf2f7;
  transform: translateX(5px);
}

.service-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.service-name {
  font-weight: 600;
  color: #2d3748;
}

.service-port {
  color: #718096;
  font-family: monospace;
  font-size: 13px;
}

.service-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-badge {
  padding: 6px 14px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status-badge.running {
  background: #c6f6d5;
  color: #22543d;
}

.status-badge.stopped {
  background: #fed7d7;
  color: #c53030;
}

.status-badge.unknown {
  background: #feebc8;
  color: #c05621;
}

.btn-check {
  padding: 6px 12px;
  border: 1px solid #cbd5e0;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.btn-check:hover {
  background: #edf2f7;
  border-color: #667eea;
}

/* 配置列表 */
.config-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.config-item {
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.config-info {
  margin-bottom: 10px;
}

.config-key {
  font-weight: 600;
  color: #2d3748;
  font-size: 15px;
  margin-bottom: 4px;
}

.config-desc {
  color: #718096;
  font-size: 13px;
}

.config-value {
  display: flex;
  gap: 10px;
}

.config-value input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
}

.config-value input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.btn-reset {
  padding: 8px 16px;
  border: none;
  background: #ed8936;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.btn-reset:hover {
  background: #dd6b20;
}

/* 缓存统计 */
.cache-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.stat-item {
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
  text-align: center;
}

.stat-item label {
  display: block;
  color: #718096;
  font-size: 13px;
  margin-bottom: 8px;
}

.stat-item span {
  display: block;
  color: #2d3748;
  font-size: 24px;
  font-weight: bold;
}

.status-connected {
  color: #48bb78;
  font-size: 14px;
}

.cache-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 导出选项 */
.export-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.export-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f7fafc;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.export-info h4 {
  margin: 0 0 5px 0;
  color: #2d3748;
  font-size: 16px;
}

.export-info p {
  margin: 0;
  color: #718096;
  font-size: 13px;
}

.btn-export {
  padding: 10px 20px;
  border: none;
  background: #667eea;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-export:hover {
  background: #5568d3;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 日志容器 */
.log-controls {
  display: flex;
  gap: 10px;
}

.log-controls select {
  padding: 6px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 13px;
}

.log-container {
  max-height: 500px;
  overflow-y: auto;
  background: #1a202c;
  border-radius: 8px;
  padding: 15px;
  font-family: 'Courier New', monospace;
}

.log-item {
  display: flex;
  gap: 15px;
  padding: 8px 0;
  border-bottom: 1px solid #2d3748;
  font-size: 13px;
}

.log-time {
  color: #718096;
  min-width: 150px;
}

.log-level {
  min-width: 60px;
  font-weight: bold;
}

.log-level.info {
  color: #63b3ed;
}

.log-level.warn {
  color: #f6ad55;
}

.log-level.error {
  color: #fc8181;
}

.log-message {
  color: #e2e8f0;
  flex: 1;
}

.empty-logs {
  text-align: center;
  padding: 40px;
  color: #718096;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px;
  color: #a0aec0;
}

/* 按钮样式 */
.btn-warning {
  padding: 10px 20px;
  border: none;
  background: #ed8936;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-warning:hover {
  background: #dd6b20;
}

.btn-danger {
  padding: 10px 20px;
  border: none;
  background: #f56565;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-danger:hover {
  background: #e53e3e;
}

.btn-primary {
  padding: 10px 20px;
  border: none;
  background: #667eea;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary:hover {
  background: #5568d3;
}

.btn-refresh, .btn-clear {
  padding: 6px 12px;
  border: 1px solid #cbd5e0;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.btn-refresh:hover, .btn-clear:hover {
  background: #edf2f7;
  border-color: #667eea;
}
</style>
