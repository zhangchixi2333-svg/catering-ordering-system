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
          <div v-for="menu in menus" :key="menu.id || menu.path" class="menu-group">
            <!-- 一级菜单（目录） -->
            <div 
              v-if="menu.menuType === 1" 
              class="menu-item menu-directory"
              @click="toggleMenu(menu.id)"
              :class="{ 'active': isMenuActive(menu.id), 'expanded': expandedMenus.includes(menu.id) }"
            >
              <el-icon class="icon"><component :is="getIconComponent(menu.icon)" /></el-icon>
              <span class="name">{{ menu.name }}</span>
              <span class="arrow" v-if="menu.children && menu.children.length > 0">
                {{ expandedMenus.includes(menu.id) ? '▼' : '▶' }}
              </span>
            </div>
            
            <!-- 二级菜单（页面菜单） -->
            <div v-else class="menu-item menu-page">
              <router-link 
                :to="menu.path"
                class="menu-link"
                active-class="active"
              >
                <el-icon class="icon"><component :is="getIconComponent(menu.icon)" /></el-icon>
                <span class="name">{{ menu.name }}</span>
              </router-link>
            </div>
            
            <!-- 子菜单 -->
            <div v-if="menu.children && menu.children.length > 0 && expandedMenus.includes(menu.id)" class="submenu">
              <router-link 
                v-for="child in menu.children" 
                :key="child.id || child.path"
                :to="child.path"
                class="menu-item submenu-item"
                active-class="active"
              >
                <el-icon class="icon"><component :is="getIconComponent(child.icon)" /></el-icon>
                <span class="name">{{ child.name }}</span>
              </router-link>
            </div>
          </div>
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
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { 
  House, 
  Tickets, 
  List, 
  Shop, 
  Setting, 
  Ticket, 
  Bell, 
  Food, 
  Document, 
  CreditCard, 
  DataAnalysis, 
  TrendCharts, 
  Monitor, 
  User, 
  Key,
  ArrowDown,
  ArrowRight
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 定义图标映射
const iconMap = {
  'House': House,
  'Tickets': Tickets,
  'List': List,
  'Shop': Shop,
  'Setting': Setting,
  'Ticket': Ticket,
  'Bell': Bell,
  'Food': Food,
  'Document': Document,
  'CreditCard': CreditCard,
  'DataAnalysis': DataAnalysis,
  'TrendCharts': TrendCharts,
  'Monitor': Monitor,
  'User': User,
  'Key': Key,
  'ArrowDown': ArrowDown,
  'ArrowRight': ArrowRight
}

// 获取图标组件
const getIconComponent = (iconName) => {
  return iconMap[iconName] || Shop // 默认图标
}

// 当前角色名称
const roleName = computed(() => {
  if (!userStore.user?.roles) return ''
  const role = userStore.user.roles[0]
  return userStore.getRoleName(role?.roleCode)
})

// 根据角色获取菜单
const menus = computed(() => {
  const result = userStore.getMenuByRole()
  console.log('MainLayout - 菜单数据:', result)
  console.log('MainLayout - 菜单数量:', result.length)
  return result
})

// 展开的菜单ID列表
const expandedMenus = ref([])

// 切换菜单展开/收起
const toggleMenu = (menuId) => {
  const index = expandedMenus.value.indexOf(menuId)
  if (index > -1) {
    expandedMenus.value.splice(index, 1)
  } else {
    expandedMenus.value.push(menuId)
  }
}

// 检查菜单是否激活
const isMenuActive = (menuId) => {
  const menu = menus.value.find(m => m.id === menuId)
  if (!menu || !menu.children) return false
  return menu.children.some(child => router.currentRoute.value.path === child.path)
}

// 处理登出
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
  ElMessage.success('已登出')
}

// 自动展开包含当前路由的菜单
onMounted(() => {
  const currentPath = router.currentRoute.value.path
  menus.value.forEach(menu => {
    if (menu.children && menu.children.some(child => child.path === currentPath)) {
      expandedMenus.value.push(menu.id)
    }
  })
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

.menu-group {
  display: flex;
  flex-direction: column;
}

/* 菜单项通用样式 */
.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  color: #666;
  transition: all 0.3s;
  border-left: 3px solid transparent;
  cursor: pointer;
  position: relative;
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

/* 目录菜单样式 */
.menu-directory {
  justify-content: space-between;
}

.menu-directory.expanded {
  background: #f8f9fa;
}

.menu-directory .arrow {
  font-size: 10px;
  color: #999;
  transition: transform 0.3s;
}

.menu-directory.expanded .arrow {
  transform: rotate(90deg);
}

/* 页面菜单样式 */
.menu-page {
  padding: 0;
}

.menu-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  color: #666;
  text-decoration: none;
  transition: all 0.3s;
  border-left: 3px solid transparent;
  width: 100%;
}

.menu-link:hover {
  background: #f5f5f5;
  color: #667eea;
}

.menu-link.active {
  background: linear-gradient(90deg, rgba(102,126,234,0.1) 0%, transparent 100%);
  color: #667eea;
  border-left-color: #667eea;
  font-weight: bold;
}

/* 子菜单样式 */
.submenu {
  background: #fafafa;
  padding-left: 20px;
  border-top: 1px solid #eee;
}

.submenu-item {
  padding: 10px 24px 10px 34px;
  font-size: 13px;
  color: #666;
  text-decoration: none;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.submenu-item:hover {
  background: #f0f0f0;
  color: #667eea;
}

.submenu-item.active {
  background: linear-gradient(90deg, rgba(102,126,234,0.1) 0%, transparent 100%);
  color: #667eea;
  border-left-color: #667eea;
  font-weight: bold;
}

.icon {
  font-size: 20px;
  min-width: 20px;
}

.name {
  font-size: 14px;
  flex: 1;
}

/* 内容区域 */
.content {
  flex: 1;
  padding: 30px;
  background: #f5f5f5;
  overflow-y: auto;
}
</style>