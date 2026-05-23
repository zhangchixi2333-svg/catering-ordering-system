import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import MainLayout from '../layouts/MainLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/DashboardView.vue')
      },
      {
        path: 'queue',
        name: 'Queue',
        component: () => import('../views/QueueView.vue')
      },
      {
        path: 'ordering',
        name: 'Ordering',
        component: () => import('../views/OrderingView.vue')
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('../views/OrderView.vue')
      },
      {
        path: 'my-orders',
        name: 'MyOrders',
        component: () => import('../views/MyOrdersView.vue')
      },
      {
        path: 'payment-orders',
        redirect: '/payment'
      },
      {
        path: 'payment',
        name: 'Payment',
        component: () => import('../views/PaymentView.vue')
      },
      {
        path: 'table-management',
        name: 'TableManagement',
        component: () => import('../views/TableManagementView.vue'),
        meta: { roles: ['STAFF', 'MANAGER', 'ADMIN'] }
      },
      {
        path: 'call-number',
        name: 'CallNumber',
        component: () => import('../views/CallNumberView.vue'),
        meta: { roles: ['STAFF', 'MANAGER', 'ADMIN'] }
      },
      {
        path: 'shops',
        name: 'Shops',
        component: () => import('../views/ShopView.vue'),
        meta: { roles: ['MANAGER', 'ADMIN'] }
      },
      {
        path: 'manager-dashboard',
        name: 'ManagerDashboard',
        component: () => import('../views/ManagerDashboard.vue'),
        meta: { roles: ['MANAGER', 'ADMIN'] }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/SettingsView.vue'),
        meta: { roles: ['ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/ProfileView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 初始化用户信息
  if (!userStore.user) {
    userStore.initUser()
  }
  
  // 检查是否需要登录
  if (to.meta.requiresAuth !== false && !userStore.isLoggedIn) {
    next('/login')
    return
  }
  
  // 检查角色权限
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!userStore.hasAnyRole(to.meta.roles)) {
      alert('❌ 您没有权限访问此页面')
      next('/dashboard')
      return
    }
  }
  
  // 如果已登录，访问登录页时跳转到首页
  if (to.path === '/login' && userStore.isLoggedIn) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router