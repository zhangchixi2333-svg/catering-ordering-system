import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api'

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  
  // 角色常量
  const ROLES = {
    USER: 'USER',              // 普通用户
    STAFF: 'STAFF',            // 店员
    MANAGER: 'MANAGER',        // 店长
    ADMIN: 'ADMIN'             // 超级管理员
  }

  // 是否已登录
  const isLoggedIn = computed(() => !!token.value && !!user.value)
  
  // 当前用户角色
  const currentRole = computed(() => {
    if (!user.value) return null
    // 如果roles是数组，返回第一个角色；如果是字符串，直接返回
    if (Array.isArray(user.value.roles)) {
      return user.value.roles[0] || null
    }
    return user.value.role || null
  })
  
  // 权限检查
  const hasRole = (role) => {
    if (!user.value) return false
    if (Array.isArray(user.value.roles)) {
      return user.value.roles.includes(role)
    }
    return user.value.role === role
  }
  
  const hasAnyRole = (roles) => {
    if (!user.value) return false
    if (Array.isArray(user.value.roles)) {
      return roles.some(role => user.value.roles.includes(role))
    }
    return roles.includes(user.value.role)
  }

  // 登录
  const login = (userData, authToken) => {
    user.value = userData
    token.value = authToken
    localStorage.setItem('token', authToken)
    localStorage.setItem('user', JSON.stringify(userData))
  }

  // 登出
  const logout = async () => {
    try {
      // 调用后端登出接口，更新在线状态
      if (token.value) {
        await authApi.logout(token.value)
        console.log('✅ 后端登出成功，用户状态已更新为离线')
      }
    } catch (error) {
      console.error('❌ 后端登出失败:', error)
      // 即使后端失败，也要清除本地数据
    } finally {
      // 清除本地数据
      user.value = null
      token.value = ''
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      console.log('🚪 用户已登出')
    }
  }

  // 初始化用户信息（从 localStorage）
  const initUser = () => {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    
    if (savedToken && savedUser) {
      token.value = savedToken
      user.value = JSON.parse(savedUser)
    }
  }

  // 根据角色获取菜单
  const getMenuByRole = () => {
    // 如果用户有从后端获取的菜单数据，则使用它
    if (user.value?.menus && Array.isArray(user.value.menus) && user.value.menus.length > 0) {
      // 过滤掉目录类型的菜单（menuType=1），只返回页面菜单（menuType=2）
      return user.value.menus
        .filter(menu => menu.menuType === 2)
        .map(menu => ({
          name: menu.menuName,
          path: menu.path,
          icon: menu.icon || '📄'
        }))
    }
    
    // 否则使用默认的基于角色的菜单
    // 优先使用 roles 数组中的第一个角色，其次使用 role 字段
    const userRole = Array.isArray(user.value?.roles) 
      ? user.value.roles[0]?.roleCode 
      : user.value?.role
    
    const menus = {
      [ROLES.USER]: [
        { name: '首页', path: '/dashboard', icon: '🏠' },
        { name: '在线点餐', path: '/ordering', icon: '🍽️' },
        { name: '取号排队', path: '/queue', icon: '🎫' },
        { name: '我的订单', path: '/my-orders', icon: '📦' },
        { name: '支付订单', path: '/payment', icon: '💳' }
      ],
      [ROLES.STAFF]: [
        { name: '首页', path: '/dashboard', icon: '🏠' },
        { name: '在线点餐', path: '/ordering', icon: '🍽️' },
        { name: '取号排队', path: '/queue', icon: '🎫' },
        { name: '叫号管理', path: '/call-number', icon: '🔔' },
        { name: '订单管理', path: '/orders', icon: '📦' },
        { name: '桌台管理', path: '/table-management', icon: '🪑' }
      ],
      [ROLES.MANAGER]: [
        { name: '首页', path: '/dashboard', icon: '🏠' },
        { name: '在线点餐', path: '/ordering', icon: '🍽️' },
        { name: '取号排队', path: '/queue', icon: '🎫' },
        { name: '叫号管理', path: '/call-number', icon: '🔔' },
        { name: '订单管理', path: '/orders', icon: '📦' },
        { name: '桌台管理', path: '/table-management', icon: '🪑' },
        { name: '店铺管理', path: '/shops', icon: '🏪' }
      ],
      [ROLES.ADMIN]: [
        { name: '首页', path: '/dashboard', icon: '🏠' },
        { name: '在线点餐', path: '/ordering', icon: '🍽️' },
        { name: '取号排队', path: '/queue', icon: '🎫' },
        { name: '叫号管理', path: '/call-number', icon: '🔔' },
        { name: '订单管理', path: '/orders', icon: '📦' },
        { name: '桌台管理', path: '/table-management', icon: '🪑' },
        { name: '店铺管理', path: '/shops', icon: '🏪' },
        { name: '系统设置', path: '/settings', icon: '⚙️' }
      ]
    }
    
    return menus[userRole] || []
  }

  // 获取角色显示名称
  const getRoleName = (role) => {
    const names = {
      [ROLES.USER]: '普通用户',
      [ROLES.STAFF]: '店员',
      [ROLES.MANAGER]: '店长',
      [ROLES.ADMIN]: '超级管理员'
    }
    return names[role] || '未知角色'
  }

  return {
    user,
    token,
    ROLES,
    isLoggedIn,
    currentRole,
    login,
    logout,
    initUser,
    hasRole,
    hasAnyRole,
    getMenuByRole,
    getRoleName
  }
})
