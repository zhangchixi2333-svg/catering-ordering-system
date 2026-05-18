<template>
  <div class="settings-view">
    <h2>⚙️ 系统设置</h2>
    
    <div class="card">
      <h3>系统信息</h3>
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
          <label>服务注册</label>
          <span>Eureka</span>
        </div>
      </div>
    </div>
    
    <div class="card">
      <h3>服务状态</h3>
      <div class="service-list">
        <div class="service-item">
          <span class="service-name">Eureka Server</span>
          <span class="status running">运行中</span>
          <span class="port">:8761</span>
        </div>
        <div class="service-item">
          <span class="service-name">Shop Service</span>
          <span class="status running">运行中</span>
          <span class="port">:8081</span>
        </div>
        <div class="service-item">
          <span class="service-name">Order Service</span>
          <span class="status running">运行中</span>
          <span class="port">:8083</span>
        </div>
        <div class="service-item">
          <span class="service-name">Queue Service</span>
          <span class="status running">运行中</span>
          <span class="port">:8085</span>
        </div>
        <div class="service-item">
          <span class="service-name">Notification Service</span>
          <span class="status running">运行中</span>
          <span class="port">:8086</span>
        </div>
      </div>
    </div>
    
    <div class="card">
      <h3>管理员操作</h3>
      <div class="admin-actions">
        <button @click="handleClearCache" class="btn-warning">
          🗑️ 清除缓存
        </button>
        <button @click="handleExportData" class="btn-primary">
          📊 导出数据
        </button>
        <button @click="handleSystemCheck" class="btn-success">
          🔍 系统检查
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

// 清除缓存
const handleClearCache = () => {
  if (confirm('确定要清除所有缓存吗？')) {
    localStorage.clear()
    alert('✅ 缓存已清除')
  }
}

// 导出数据
const handleExportData = () => {
  alert('📊 导出功能开发中...')
}

// 系统检查
const handleSystemCheck = () => {
  alert('🔍 系统检查功能开发中...')
}
</script>

<style scoped>
.settings-view h2 {
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

.card h3 {
  margin-bottom: 20px;
  color: #667eea;
  font-size: 18px;
}

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
  color: #666;
  font-weight: 500;
}

.info-item span {
  color: #333;
  font-weight: bold;
}

.service-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px;
  background: #f7fafc;
  border-radius: 6px;
}

.service-name {
  flex: 1;
  font-weight: 500;
}

.status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status.running {
  background: #c6f6d5;
  color: #22543d;
}

.port {
  color: #999;
  font-family: monospace;
}

.admin-actions {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

button {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5568d3;
}

.btn-warning {
  background: #ed8936;
  color: white;
}

.btn-warning:hover {
  background: #dd6b20;
}

.btn-success {
  background: #48bb78;
  color: white;
}

.btn-success:hover {
  background: #38a169;
}
</style>
