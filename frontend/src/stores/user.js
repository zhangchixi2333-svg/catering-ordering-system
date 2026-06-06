import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api'

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const user = ref(null)
  const token = ref(sessionStorage.getItem('token') || localStorage.getItem('token') || '')
  // 当前店铺ID
  const currentShopId = ref(null)
  
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
    sessionStorage.setItem('token', authToken)
    sessionStorage.setItem('user', JSON.stringify(userData))
    localStorage.removeItem('token')
    localStorage.removeItem('user')
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
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      console.log('🚪 用户已登出')
    }
  }

  // 初始化用户信息（从 localStorage）
  const initUser = () => {
    const savedToken = sessionStorage.getItem('token') || localStorage.getItem('token')
    const savedUser = sessionStorage.getItem('user') || localStorage.getItem('user')
    const savedShopId = sessionStorage.getItem('currentShopId') || localStorage.getItem('currentShopId')
    
    if (savedToken && savedUser) {
      token.value = savedToken
      user.value = JSON.parse(savedUser)
    }
    
    if (savedShopId) {
      currentShopId.value = parseInt(savedShopId)
    }
  }
  
  // 设置当前店铺ID
  const setCurrentShopId = (shopId) => {
    currentShopId.value = shopId
    if (shopId) {
      sessionStorage.setItem('currentShopId', shopId.toString())
    } else {
      sessionStorage.removeItem('currentShopId')
    }
  }

  // 根据角色获取菜单
  const refreshMenus = async () => {
    if (!token.value || !user.value) return []
    const res = await authApi.getUserMenus(token.value)
    user.value = {
      ...user.value,
      menus: res.data || []
    }
    sessionStorage.setItem('user', JSON.stringify(user.value))
    return user.value.menus
  }

  const getMenuByRole = () => {
    console.log('========== 开始获取菜单 ==========')
    console.log('用户信息:', user.value)
    console.log('用户菜单数据:', user.value?.menus)
    console.log('用户菜单数据类型:', typeof user.value?.menus)
    console.log('用户菜单数据是否为数组:', Array.isArray(user.value?.menus))
    
    // 如果用户有从后端获取的菜单数据，则使用它
    if (user.value?.menus && Array.isArray(user.value.menus) && user.value.menus.length > 0) {
      console.log('✅ 使用后端菜单数据')
      console.log('原始菜单数据:', user.value.menus)
      console.log('原始菜单数据数量:', user.value.menus.length)
      
      const treeMenus = buildMenuTree(user.value.menus)
      console.log('构建的树形菜单:', treeMenus)
      console.log('==========================================')
      return treeMenus
    }
    
    console.log('⚠️ 使用默认菜单数据（未从后端获取到菜单）')
    // 否则使用默认的基于角色的菜单
    // 优先使用 roles 数组中的第一个角色，其次使用 role 字段
    const userRole = Array.isArray(user.value?.roles) 
      ? user.value.roles[0]?.roleCode 
      : user.value?.role
    
    console.log('用户角色:', userRole)
    
    const menus = {
      [ROLES.USER]: [
        { id: 1, name: '首页', path: '/dashboard', icon: '🏠', menuType: 2 },
        { id: 2, name: '在线点餐', path: '/ordering', icon: '🍽️', menuType: 2 },
        { id: 3, name: '取号排队', path: '/queue', icon: '🎫', menuType: 2 },
        { id: 4, name: '我的订单', path: '/my-orders', icon: '📦', menuType: 2 },
        { id: 5, name: '支付订单', path: '/payment', icon: '💳', menuType: 2 }
      ],
      [ROLES.STAFF]: [
        { id: 1, name: '首页', path: '/dashboard', icon: '🏠', menuType: 2 },
        { id: 2, name: '在线点餐', path: '/ordering', icon: '🍽️', menuType: 2 },
        { id: 3, name: '取号排队', path: '/queue', icon: '🎫', menuType: 2 },
        { id: 4, name: '叫号管理', path: '/call-number', icon: '🔔', menuType: 2 },
        { id: 5, name: '订单管理', path: '/orders', icon: '📦', menuType: 2 },
        { id: 6, name: '桌台管理', path: '/table-management', icon: '🪑', menuType: 2 }
      ],
      [ROLES.MANAGER]: [
        { id: 1, name: '首页', path: '/dashboard', icon: '🏠', menuType: 2 },
        { id: 2, name: '在线点餐', path: '/ordering', icon: '🍽️', menuType: 2 },
        { id: 3, name: '取号排队', path: '/queue', icon: '🎫', menuType: 2 },
        { id: 4, name: '叫号管理', path: '/call-number', icon: '🔔', menuType: 2 },
        { id: 5, name: '订单管理', path: '/orders', icon: '📦', menuType: 2 },
        { id: 6, name: '桌台管理', path: '/table-management', icon: '🪑', menuType: 2 },
        { id: 7, name: '店铺管理', path: '/shops', icon: '🏪', menuType: 2 }
      ],
      [ROLES.ADMIN]: [
        { id: 1, name: '首页', path: '/dashboard', icon: '🏠', menuType: 2 },
        { id: 2, name: '在线点餐', path: '/ordering', icon: '🍽️', menuType: 2 },
        { id: 3, name: '取号排队', path: '/queue', icon: '🎫', menuType: 2 },
        { id: 4, name: '叫号管理', path: '/call-number', icon: '🔔', menuType: 2 },
        { id: 5, name: '订单管理', path: '/orders', icon: '📦', menuType: 2 },
        { id: 6, name: '桌台管理', path: '/table-management', icon: '🪑', menuType: 2 },
        { id: 7, name: '店铺管理', path: '/shops', icon: '🏪', menuType: 2 },
        { id: 8, name: '系统设置', path: '/settings', icon: '⚙️', menuType: 2 }
      ]
    }
    
    const result = menus[userRole] || []
    console.log('返回的菜单:', result)
    console.log('==========================================')
    return result
  }

  // 构建树形菜单结构
  const buildMenuTree = (menus) => {
    console.log('========== 开始构建树形菜单 ==========')
    console.log('原始菜单数据:', menus)
    console.log('菜单数量:', menus.length)
    
    // 打印每个菜单的详细信息
    menus.forEach((menu, index) => {
      console.log(`菜单 ${index}:`, {
        id: menu.id,
        parentId: menu.parentId,
        menuName: menu.menuName,
        menuType: menu.menuType,
        path: menu.path,
        icon: menu.icon,
        visible: menu.visible,
        status: menu.status
      })
    })
    
    // 过滤掉隐藏的菜单
    const visibleMenus = menus.filter(menu => menu.visible === 1)
    console.log('可见菜单数量:', visibleMenus.length)
    console.log('可见菜单:', visibleMenus)
    
    // 如果没有可见菜单，尝试使用其他条件
    if (visibleMenus.length === 0) {
      console.log('没有可见菜单，尝试使用所有菜单')
      // 尝试使用 status === 1 或者不过滤
      const statusMenus = menus.filter(menu => menu.status === 1)
      console.log('状态为1的菜单数量:', statusMenus.length)
      
      if (statusMenus.length > 0) {
        return buildTreeFromMenus(statusMenus)
      } else {
        console.log('没有状态为1的菜单，使用所有菜单')
        return buildTreeFromMenus(menus)
      }
    }
    
    return buildTreeFromMenus(visibleMenus)
  }

  // 从菜单列表构建树形结构
  const buildTreeFromMenus = (menus) => {
    console.log('========== 从菜单列表构建树形结构 ==========')
    console.log('输入菜单数量:', menus.length)
    
    // 找出所有顶级菜单（parentId=0）
    const rootMenus = menus.filter(menu => menu.parentId === 0)
    console.log('根菜单数量:', rootMenus.length)
    console.log('根菜单:', rootMenus)
    
    // 为每个顶级菜单递归构建子菜单
    rootMenus.forEach(rootMenu => {
      console.log('为根菜单构建子菜单:', rootMenu.menuName, 'ID:', rootMenu.id)
      rootMenu.children = buildChildren(rootMenu.id, menus)
      console.log('子菜单:', rootMenu.children)
    })
    
    // 转换为前端需要的格式
    const result = rootMenus.map(menu => ({
      id: menu.id,
      name: menu.menuName,
      path: menu.path,
      icon: menu.icon || '📄',
      menuType: menu.menuType,
      children: menu.children && menu.children.length > 0 
        ? menu.children.map(child => ({
            id: child.id,
            name: child.menuName,
            path: child.path,
            icon: child.icon || '📄',
            menuType: child.menuType
          }))
        : []
    }))
    
    console.log('最终构建的树形菜单:', result)
    console.log('==========================================')
    return result
  }

  // 递归构建子菜单
  const buildChildren = (parentId, menus) => {
    console.log('构建子菜单 - 父ID:', parentId)
    const children = menus.filter(menu => menu.parentId === parentId)
    console.log('找到子菜单数量:', children.length)
    
    children.forEach(child => {
      child.children = buildChildren(child.id, menus)
    })
    
    return children
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
    currentShopId,
    setCurrentShopId,
    refreshMenus,
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
