import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    console.log('📤 发送请求:', config.method.toUpperCase(), config.url)
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      console.error('❌ 请求失败:', res.message)
      alert(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    console.error('❌ 网络错误:', error.message)
    alert('网络错误，请稍后重试')
    return Promise.reject(error)
  }
)

// ==================== Auth API ====================
export const authApi = {
  // 用户登录
  login(data) {
    return request.post('/auth/login', data)
  },
  // 验证Token
  validateToken(token) {
    return request.get('/auth/validate', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
  },
  // 获取用户菜单
  getUserMenus(token) {
    return request.get('/auth/menus', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
  },
  // 获取当前用户信息
  getProfile(token) {
    return request.get('/auth/profile', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
  },
  // 更新用户信息
  updateProfile(token, data) {
    return request.put('/auth/profile', data, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
  },
  // 用户登出
  logout(token) {
    return request.post('/auth/logout', null, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
  }
}

// ==================== Shop API ====================
export const shopApi = {
  // 获取所有店铺
  getList() {
    return request.get('/shop/list')
  },
  // 根据ID获取店铺
  getById(id) {
    return request.get(`/shop/${id}`)
  },
  // 创建店铺
  create(data) {
    return request.post('/shop', data)
  },
  // 更新店铺
  update(data) {
    return request.put('/shop', data)
  },
  // 删除店铺
  delete(id) {
    return request.delete(`/shop/${id}`)
  }
}

// ==================== Queue API ====================
export const queueApi = {
  // 取号
  takeNumber(data) {
    return request.post('/queue', data)
  },
  // 获取排队列表（所有）
  getList() {
    return request.get('/queue/list')
  },
  // 根据用户ID获取排队列表
  getByUser(userId, shopId = null) {
    if (shopId) {
      return request.get(`/queue/user/${userId}`, { params: { shopId } })
    }
    return request.get(`/queue/user/${userId}`)
  },
  // 根据ID获取排队
  getById(id) {
    return request.get(`/queue/${id}`)
  },
  // 根据店铺获取排队
  getByShop(shopId) {
    return request.get(`/queue/shop/${shopId}`)
  },
  // 获取等待中的排队
  getWaiting(shopId) {
    return request.get(`/queue/waiting/${shopId}`)
  },
  // 叫号
  callNumber(id) {
    return request.put(`/queue/${id}/call`)
  },
  // 完成排队
  complete(id) {
    return request.put(`/queue/${id}/complete`)
  },
  // 取消排队
  cancel(id, reason) {
    return request.put(`/queue/${id}/cancel`, null, { params: { cancelReason: reason } })
  },
  // 获取实时等待队列(Redis)
  getRealTimeWaiting(shopId) {
    return request.get(`/queue/redis/waiting/${shopId}`)
  },
  // 获取实时叫号队列(Redis)
  getRealTimeCalling(shopId) {
    return request.get(`/queue/redis/calling/${shopId}`)
  },
  // 获取排队位置
  getPosition(shopId, queueId) {
    return request.get(`/queue/redis/position/${shopId}/${queueId}`)
  }
}

// ==================== Order API ====================
export const orderApi = {
  // 创建订单
  create(data) {
    return request.post('/order', data)
  },
  // 获取订单列表
  getList() {
    return request.get('/order/list')
  },
  // 根据用户获取订单
  getByUser(userId) {
    return request.get(`/order/user/${userId}`)
  },
  // 根据店铺获取订单
  getByShop(shopId) {
    return request.get(`/order/shop/${shopId}`)
  },
  // 根据排队ID获取订单
  getByQueue(queueId) {
    return request.get(`/order/queue/${queueId}`)
  },
  // 更新订单状态
  updateStatus(id, status) {
    return request.put(`/order/${id}/status`, null, { params: { orderStatus: status } })
  },
  // 取消订单
  cancel(id, reason) {
    return request.put(`/order/${id}/cancel`, null, { params: { cancelReason: reason } })
  }
}

// ==================== Payment API ====================
export const paymentApi = {
  // 创建支付订单（只需要订单编号）
  create(data) {
    return request.post('/payment', data)
  },
  // 获取支付订单列表
  getList() {
    return request.get('/payment/list')
  },
  // 根据ID获取支付订单
  getById(id) {
    return request.get(`/payment/${id}`)
  },
  // 根据支付单号获取支付订单
  getByNo(paymentNo) {
    return request.get(`/payment/no/${paymentNo}`)
  },
  // 根据订单编号获取支付订单
  getByOrderNo(orderNo) {
    return request.get(`/payment/order/${orderNo}`)
  },
  // 根据店铺获取支付订单
  getByShop(shopId) {
    return request.get(`/payment/shop/${shopId}`)
  },
  // 根据用户获取支付订单
  getByUser(userId) {
    return request.get(`/payment/user/${userId}`)
  },
  // 根据状态获取支付订单
  getByStatus(status) {
    return request.get(`/payment/status/${status}`)
  },
  // 更新支付订单
  update(id, data) {
    return request.put(`/payment/${id}`, data)
  },
  // 更新支付状态
  updateStatus(id, status) {
    return request.put(`/payment/${id}/status`, null, { params: { paymentStatus: status } })
  },
  // 删除支付订单
  delete(id) {
    return request.delete(`/payment/${id}`)
  }
}

// ==================== Notification API ====================
export const notificationApi = {
  // 获取在线用户数
  getOnlineCount() {
    return request.get('/notification/ws/online/count')
  }
}

// WebSocket 连接
export function createWebSocket(userId, onMessage) {
  const ws = new WebSocket(`ws://localhost:8086/ws/notification/${userId}`)
  
  ws.onopen = () => {
    console.log('✅ WebSocket 连接成功')
  }
  
  ws.onmessage = (event) => {
    const data = JSON.parse(event.data)
    console.log('📨 收到通知:', data)
    
    // 调用自定义消息处理函数
    if (onMessage) {
      onMessage(data)
    }
    
    // 默认处理（保持向后兼容）
    if (!onMessage) {
      if (data.type === 'QUEUE_CREATED') {
        alert(`🎉 取号成功！\n排队号码: ${data.data.queueNo}`)
      } else if (data.type === 'QUEUE_CALLED') {
        alert(`🔔 叫号通知\n排队号码: ${data.data.queueNo}\n请前往就餐！`)
      } else if (data.type === 'ORDER_CREATED') {
        alert(`✅ 订单创建成功\n订单号: ${data.data.orderNo}`)
      }
    }
  }
  
  ws.onerror = (error) => {
    console.error('❌ WebSocket 错误:', error)
  }
  
  return ws
}

export default request
