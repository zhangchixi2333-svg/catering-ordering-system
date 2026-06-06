<template>
  <div class="dashboard">
    <h1>👋 欢迎回来，{{ userStore.user?.nickname }}</h1>
    
    <div class="stats-grid">
      <div class="stat-card">
        <div class="icon">🎫</div>
        <div class="info">
          <div class="label">我的排队</div>
          <div class="value">{{ stats.myQueues }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="icon">📦</div>
        <div class="info">
          <div class="label">我的订单</div>
          <div class="value">{{ stats.myOrders }}</div>
        </div>
      </div>
      
      <div v-if="userStore.hasAnyRole(['STAFF', 'MANAGER', 'ADMIN'])" class="stat-card">
        <div class="icon">⏳</div>
        <div class="info">
          <div class="label">等待叫号</div>
          <div class="value">{{ stats.waitingCount }}</div>
        </div>
      </div>
      
      <div v-if="userStore.hasAnyRole(['MANAGER', 'ADMIN'])" class="stat-card">
        <div class="icon">🏪</div>
        <div class="info">
          <div class="label">店铺数量</div>
          <div class="value">{{ stats.shopCount }}</div>
        </div>
      </div>
    </div>
    
    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="actions-grid">
        <router-link to="/queue" class="action-btn">
          <span class="icon">🎫</span>
          <span>取号排队</span>
        </router-link>
        
        <router-link to="/orders" class="action-btn">
          <span class="icon">📦</span>
          <span>查看订单</span>
        </router-link>
        
        <router-link 
          v-if="userStore.hasAnyRole(['STAFF', 'MANAGER', 'ADMIN'])"
          to="/call-number" 
          class="action-btn"
        >
          <span class="icon">🔔</span>
          <span>叫号管理</span>
        </router-link>
        
        <router-link 
          v-if="userStore.hasAnyRole(['MANAGER', 'ADMIN'])"
          to="/shops" 
          class="action-btn"
        >
          <span class="icon">🏪</span>
          <span>店铺管理</span>
        </router-link>
      </div>
    </div>
    
    <div class="role-info">
      <h2>当前角色</h2>
      <div class="role-badge">
        <span class="role-icon">{{ getRoleIcon() }}</span>
        <span class="role-name">{{ userStore.getRoleName(userStore.currentRole) }}</span>
      </div>
      <p class="role-desc">{{ getRoleDescription() }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { queueApi, orderApi, shopApi } from '../api'

const userStore = useUserStore()
const stats = ref({
  myQueues: 0,
  myOrders: 0,
  waitingCount: 0,
  shopCount: 0
})

// 加载统计数据
const loadStats = async () => {
  try {
    // 获取我的排队数
    const queuesRes = await queueApi.getList()
    stats.value.myQueues = (queuesRes.data || []).filter(q => q.userId === userStore.user.id).length
    
    // 获取我的订单数
    const ordersRes = await orderApi.getByUser(userStore.user.id)
    stats.value.myOrders = (ordersRes.data || []).length
    
    // 如果是店员或以上，获取等待人数
    if (userStore.hasAnyRole(['STAFF', 'MANAGER', 'ADMIN'])) {
      const waitingRes = await queueApi.getRealTimeWaiting(1)
      stats.value.waitingCount = waitingRes.data?.waitingCount || 0
    }
    
    // 如果是店长或管理员，获取店铺数
    if (userStore.hasAnyRole(['MANAGER', 'ADMIN'])) {
      const shopsRes = await shopApi.getList()
      stats.value.shopCount = (shopsRes.data || []).length
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 获取角色图标
const getRoleIcon = () => {
  const icons = {
    USER: '👤',
    STAFF: '🧑‍💼',
    MANAGER: '👨‍💼',
    ADMIN: '⚙️'
  }
  return icons[userStore.currentRole] || '👤'
}

// 获取角色描述
const getRoleDescription = () => {
  const descriptions = {
    USER: '普通用户可以取号排队、查看订单状态',
    STAFF: '店员可以管理叫号、处理订单',
    MANAGER: '店长可以管理店铺、查看所有数据',
    ADMIN: '超级管理员拥有所有权限，包括系统设置'
  }
  return descriptions[userStore.currentRole] || ''
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard h1 {
  color: #333;
  margin-bottom: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-card .icon {
  font-size: 40px;
}

.stat-card .info {
  flex: 1;
}

.stat-card .label {
  color: #999;
  font-size: 14px;
  margin-bottom: 4px;
}

.stat-card .value {
  color: #667eea;
  font-size: 32px;
  font-weight: bold;
}

.quick-actions h2,
.role-info h2 {
  color: #333;
  margin-bottom: 20px;
  font-size: 20px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
  margin-bottom: 40px;
}

.action-btn {
  background: white;
  padding: 20px;
  border-radius: 10px;
  text-decoration: none;
  color: #333;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s;
}

.action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.action-btn .icon {
  font-size: 32px;
}

.role-info {
  background: white;
  padding: 24px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.role-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 12px 24px;
  border-radius: 25px;
  margin-bottom: 15px;
}

.role-icon {
  font-size: 24px;
}

.role-name {
  font-size: 16px;
  font-weight: bold;
}

.role-desc {
  color: #666;
  line-height: 1.6;
}
</style>
