<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="logo">🍽️ 餐饮点餐排队系统</div>
      
      <div class="header-right">
        <div v-if="userStore.isLoggedIn" class="user-info">
          <span class="role-badge">{{ userStore.getRoleName(userStore.currentRole) }}</span>
          <img 
            :src="userStore.user.avatar" 
            :alt="userStore.user.username" 
            class="avatar"
            @click="goToProfile"
            title="点击查看个人信息"
          />
          <span class="username" @click="goToProfile" style="cursor: pointer;" title="点击查看个人信息">
            {{ userStore.user.nickname }}
          </span>
          <button @click="handleLogout" class="btn-logout">登出</button>
        </div>
      </div>
    </header>

    <div class="main-container">
      <!-- 左侧菜单 -->
      <aside class="sidebar">
        <nav class="menu">
          <router-link 
            v-for="menu in menus" 
            :key="menu.path"
            :to="menu.path"
            class="menu-item"
            active-class="active"
          >
            <span class="icon">{{ menu.icon }}</span>
            <span class="name">{{ menu.name }}</span>
          </router-link>
        </nav>
      </aside>

      <!-- 内容区域 -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

// 根据角色获取菜单
const menus = computed(() => {
  return userStore.getMenuByRole()
})

// 登出
const handleLogout = () => {
  if (confirm('确定要登出吗？')) {
    userStore.logout()
    router.push('/login')
  }
}

// 跳转到个人信息页
const goToProfile = () => {
  router.push('/profile')
}

onMounted(() => {
  // 如果未登录，跳转到登录页
  if (!userStore.isLoggedIn) {
    router.push('/login')
  }
})
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0 30px;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-badge {
  background: rgba(255,255,255,0.2);
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: bold;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid white;
  cursor: pointer;
  transition: transform 0.2s;
}

.avatar:hover {
  transform: scale(1.1);
}

.username {
  font-size: 14px;
}

.btn-logout {
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.3);
  color: white;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 13px;
}

.btn-logout:hover {
  background: rgba(255,255,255,0.3);
}

/* 主容器 */
.main-container {
  display: flex;
  flex: 1;
}

/* 左侧菜单 */
.sidebar {
  width: 220px;
  background: white;
  box-shadow: 2px 0 10px rgba(0,0,0,0.05);
  padding: 20px 0;
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  color: #666;
  text-decoration: none;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.menu-item:hover {
  background: #f5f5f5;
  color: #667eea;
}

.menu-item.active {
  background: linear-gradient(90deg, rgba(102,126,234,0.1) 0%, transparent 100%);
  color: #667eea;
  border-left-color: #667eea;
  font-weight: bold;
}

.icon {
  font-size: 20px;
}

.name {
  font-size: 14px;
}

/* 内容区域 */
.content {
  flex: 1;
  padding: 30px;
  background: #f5f5f5;
  overflow-y: auto;
}
</style>
