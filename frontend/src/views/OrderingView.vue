<template>
  <div class="ordering-view">
    <h2>🍽️ 在线点餐</h2>
    
    <!-- 店铺选择 -->
    <div class="shop-selector">
      <label>选择店铺：</label>
      <select v-model="selectedShopId" @change="loadMenuData">
        <option value="">请选择店铺</option>
        <option v-for="shop in shops" :key="shop.id" :value="shop.id">
          {{ shop.shopName }}
        </option>
      </select>
    </div>
    
    <div v-if="selectedShopId" class="ordering-container">
      <!-- 左侧分类导航 -->
      <div class="category-sidebar">
        <div 
          v-for="category in categories" 
          :key="category.id"
          @click="selectCategory(category.id)"
          :class="['category-item', { active: selectedCategoryId === category.id }]"
        >
          <span class="category-name">{{ category.categoryName }}</span>
          <span v-if="getCategoryItemCount(category.id) > 0" class="item-count">
            {{ getCategoryItemCount(category.id) }}
          </span>
        </div>
      </div>
      
      <!-- 右侧菜品列表 -->
      <div class="menu-content">
        <div v-if="filteredItems.length > 0" class="items-grid">
          <div v-for="item in filteredItems" :key="item.id" class="menu-item-card">
            <div class="item-image">
              <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.itemName" />
              <div v-else class="placeholder-image">🍽️</div>
              <div v-if="item.isRecommended" class="recommended-badge">推荐</div>
              <div v-if="item.stock === 0" class="sold-out-badge">售罄</div>
            </div>
            
            <div class="item-info">
              <h3 class="item-name">{{ item.itemName }}</h3>
              <p v-if="item.description" class="item-desc">{{ item.description }}</p>
              
              <div class="item-meta">
                <span v-if="item.spicyLevel > 0" class="spicy-badge">
                  🌶️ {{ getSpicyText(item.spicyLevel) }}
                </span>
                <span v-if="item.preparationTime" class="time-badge">
                  ⏱️ {{ item.preparationTime }}分钟
                </span>
              </div>
              
              <div class="item-footer">
                <div class="price-section">
                  <span class="current-price">¥{{ item.price.toFixed(2) }}</span>
                  <span v-if="item.originalPrice && item.originalPrice > item.price" class="original-price">
                    ¥{{ item.originalPrice.toFixed(2) }}
                  </span>
                </div>
                
                <div class="quantity-control">
                  <button 
                    @click="decreaseQuantity(item)" 
                    :disabled="!getItemQuantity(item.id)"
                    class="btn-decrease"
                  >
                    -
                  </button>
                  <span class="quantity">{{ getItemQuantity(item.id) || 0 }}</span>
                  <button 
                    @click="increaseQuantity(item)" 
                    :disabled="item.stock === 0"
                    class="btn-increase"
                  >
                    +
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-items">
          <p>该分类下暂无菜品</p>
        </div>
      </div>
    </div>
    
    <div v-else-if="shops.length > 0" class="empty-state">
      <div class="empty-icon">🏪</div>
      <p>请选择店铺开始点餐</p>
    </div>
    
    <div v-else class="empty-state">
      <div class="empty-icon">📭</div>
      <p>暂无可用店铺</p>
    </div>
    
    <!-- 购物车底部栏 -->
    <div v-if="cartItems.length > 0" class="cart-bar">
      <div class="cart-info" @click="showCartDialog = true">
        <div class="cart-icon">
          🛒
          <span v-if="totalQuantity > 0" class="cart-badge">{{ totalQuantity }}</span>
        </div>
        <div class="cart-summary">
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
          <span class="total-items">共 {{ totalQuantity }} 件</span>
        </div>
      </div>
      
      <button @click="submitOrder" class="btn-submit-order" :disabled="isSubmitting">
        {{ isSubmitting ? '提交中...' : '提交订单' }}
      </button>
    </div>
    
    <!-- 购物车详情对话框 -->
    <div v-if="showCartDialog" class="dialog-overlay" @click="closeCartDialog">
      <div class="dialog cart-dialog" @click.stop>
        <div class="dialog-header">
          <h3>🛒 购物车</h3>
          <button @click="closeCartDialog" class="btn-close">×</button>
        </div>
        
        <div class="dialog-content">
          <div v-if="cartItems.length > 0" class="cart-items-list">
            <div v-for="cartItem in cartItems" :key="cartItem.id" class="cart-item">
              <div class="cart-item-info">
                <h4>{{ cartItem.itemName }}</h4>
                <p class="cart-item-price">¥{{ cartItem.price.toFixed(2) }}</p>
              </div>
              
              <div class="cart-item-actions">
                <button @click="decreaseQuantity(cartItem)" class="btn-small">-</button>
                <span class="cart-quantity">{{ cartItem.quantity }}</span>
                <button @click="increaseQuantity(cartItem)" class="btn-small">+</button>
              </div>
            </div>
          </div>
          
          <div v-else class="empty-cart">
            <p>购物车为空</p>
          </div>
        </div>
        
        <div class="dialog-footer">
          <div class="footer-total">
            <span>合计：</span>
            <span class="total-amount">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <button @click="submitOrder" class="btn-confirm" :disabled="isSubmitting || cartItems.length === 0">
            {{ isSubmitting ? '提交中...' : '提交订单' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { shopApi } from '../api'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

// 数据
const shops = ref([])
const selectedShopId = ref('')
const categories = ref([])
const menuItems = ref([])
const selectedCategoryId = ref('')
const cart = ref({}) // { itemId: quantity }
const showCartDialog = ref(false)
const isSubmitting = ref(false)

// 从路由参数中获取 queueId（排队ID）
const queueId = ref(route.query.queueId ? parseInt(route.query.queueId) : null)
if (queueId.value) {
  console.log('检测到排队ID:', queueId.value)
}

// 加载店铺列表
const loadShops = async () => {
  try {
    const res = await shopApi.getList()
    shops.value = res.data || []
    
    // 自动选择第一个店铺
    if (shops.value.length > 0) {
      selectedShopId.value = shops.value[0].id
      loadMenuData()
    }
  } catch (error) {
    console.error('加载店铺失败:', error)
  }
}

// 加载菜单数据
const loadMenuData = async () => {
  if (!selectedShopId.value) {
    categories.value = []
    menuItems.value = []
    return
  }
  
  try {
    // 加载分类
    const catRes = await axios.get(`/api/menu/category/shop/${selectedShopId.value}`)
    categories.value = catRes.data.data || []
    
    // 加载菜品
    const itemRes = await axios.get(`/api/menu/item/available/${selectedShopId.value}`)
    menuItems.value = itemRes.data.data || []
    
    // 默认选中第一个分类
    if (categories.value.length > 0) {
      selectedCategoryId.value = categories.value[0].id
    }
  } catch (error) {
    console.error('加载菜单失败:', error)
  }
}

// 选择分类
const selectCategory = (categoryId) => {
  selectedCategoryId.value = categoryId
}

// 获取分类下的菜品数量
const getCategoryItemCount = (categoryId) => {
  return Object.keys(cart.value).reduce((sum, itemId) => {
    const item = menuItems.value.find(i => i.id === parseInt(itemId))
    if (item && item.categoryId === categoryId) {
      return sum + cart.value[itemId]
    }
    return sum
  }, 0)
}

// 筛选当前分类的菜品
const filteredItems = computed(() => {
  if (!selectedCategoryId.value) {
    return menuItems.value
  }
  return menuItems.value.filter(item => item.categoryId === selectedCategoryId.value)
})

// 购物车物品列表
const cartItems = computed(() => {
  return Object.entries(cart.value)
    .filter(([_, quantity]) => quantity > 0)
    .map(([itemId, quantity]) => {
      const item = menuItems.value.find(i => i.id === parseInt(itemId))
      return item ? { ...item, quantity } : null
    })
    .filter(Boolean)
})

// 总数量
const totalQuantity = computed(() => {
  return Object.values(cart.value).reduce((sum, qty) => sum + qty, 0)
})

// 总价
const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// 获取菜品数量
const getItemQuantity = (itemId) => {
  return cart.value[itemId] || 0
}

// 增加数量
const increaseQuantity = (item) => {
  if (item.stock === 0) return
  
  const currentQty = cart.value[item.id] || 0
  if (item.stock > 0 && currentQty >= item.stock) {
    alert('库存不足')
    return
  }
  
  cart.value[item.id] = currentQty + 1
}

// 减少数量
const decreaseQuantity = (item) => {
  const currentQty = cart.value[item.id] || 0
  if (currentQty > 0) {
    cart.value[item.id] = currentQty - 1
    if (cart.value[item.id] === 0) {
      delete cart.value[item.id]
    }
  }
}

// 关闭购物车对话框
const closeCartDialog = () => {
  showCartDialog.value = false
}

// 提交订单
const submitOrder = async () => {
  if (cartItems.value.length === 0) {
    alert('购物车为空')
    return
  }
  
  // 获取当前用户信息
  const userStore = useUserStore()
  if (!userStore.user || !userStore.user.id) {
    alert('请先登录')
    router.push('/login')
    return
  }
  
  isSubmitting.value = true
  
  try {
    // 构建订单明细
    const orderItems = cartItems.value.map(item => ({
      itemId: item.id,
      itemName: item.itemName,
      price: item.price,
      quantity: item.quantity,
      subtotal: item.price * item.quantity,
      remark: ''
    }))
    
    // 计算总金额
    const totalAmount = totalPrice.value
    const itemCount = totalQuantity.value
    
    // 构建订单数据
    const orderData = {
      shopId: selectedShopId.value,
      userId: userStore.user.id,
      orderType: 1, // 1-堂食，2-外带，3-外卖
      tableId: null, // 堂食时需要选择桌台
      queueId: queueId.value, // 关联排队号（如果有）
      remark: '', // 订单备注
      items: orderItems,
      totalAmount: totalAmount,
      itemCount: itemCount
    }
    
    if (queueId.value) {
      console.log('订单关联排队ID:', queueId.value)
    }
    
    console.log('提交订单数据:', orderData)
    
    // 调用订单服务 API
    const response = await axios.post(
      '/api/order',
      orderData,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )
    
    if (response.data.code === 200) {
      alert('✅ 订单提交成功！订单号：' + (response.data.data?.orderNo || '未知'))
      
      // 清空购物车
      cart.value = {}
      closeCartDialog()
      
      // 跳转到订单页面
      router.push('/orders')
    } else {
      throw new Error(response.data.message || '订单创建失败')
    }
  } catch (error) {
    console.error('提交订单失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '网络错误'
    alert('❌ 提交订单失败：' + errorMsg)
  } finally {
    isSubmitting.value = false
  }
}

// 获取辣度文本
const getSpicyText = (level) => {
  const texts = {
    1: '微辣',
    2: '中辣',
    3: '特辣'
  }
  return texts[level] || ''
}

onMounted(() => {
  loadShops()
})
</script>

<style scoped>
.ordering-view h2 {
  margin-bottom: 20px;
  color: #333;
}

/* 店铺选择 */
.shop-selector {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.shop-selector label {
  font-weight: bold;
  color: #4a5568;
}

.shop-selector select {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
}

/* 点餐容器 */
.ordering-container {
  display: flex;
  gap: 20px;
  height: calc(100vh - 200px);
}

/* 分类侧边栏 */
.category-sidebar {
  width: 180px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow-y: auto;
  padding: 10px;
}

.category-item {
  padding: 12px 15px;
  margin-bottom: 5px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-item:hover {
  background: #f7fafc;
}

.category-item.active {
  background: #667eea;
  color: white;
}

.category-name {
  font-size: 14px;
}

.item-count {
  background: #f56565;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
}

.category-item.active .item-count {
  background: white;
  color: #f56565;
}

/* 菜单内容区 */
.menu-content {
  flex: 1;
  overflow-y: auto;
  padding-right: 10px;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 15px;
}

.menu-item-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s;
}

.menu-item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
}

.item-image {
  position: relative;
  height: 160px;
  background: #f7fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-image {
  font-size: 64px;
}

.recommended-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #ed8936;
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.sold-out-badge {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: bold;
}

.item-info {
  padding: 15px;
}

.item-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #2d3748;
}

.item-desc {
  margin: 0 0 10px 0;
  font-size: 13px;
  color: #718096;
  line-height: 1.5;
  height: 40px;
  overflow: hidden;
}

.item-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.spicy-badge, .time-badge {
  font-size: 12px;
  color: #718096;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-section {
  display: flex;
  flex-direction: column;
}

.current-price {
  font-size: 20px;
  font-weight: bold;
  color: #f56565;
}

.original-price {
  font-size: 13px;
  color: #a0aec0;
  text-decoration: line-through;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-decrease, .btn-increase {
  width: 32px;
  height: 32px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.3s;
}

.btn-decrease:hover:not(:disabled), .btn-increase:hover:not(:disabled) {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.btn-decrease:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.quantity {
  font-size: 16px;
  font-weight: bold;
  min-width: 30px;
  text-align: center;
}

/* 空状态 */
.empty-items, .empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #a0aec0;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

/* 购物车底部栏 */
.cart-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  box-shadow: 0 -4px 12px rgba(0,0,0,0.1);
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
}

.cart-info {
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
}

.cart-icon {
  position: relative;
  font-size: 32px;
}

.cart-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #f56565;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
}

.cart-summary {
  display: flex;
  flex-direction: column;
}

.total-price {
  font-size: 20px;
  font-weight: bold;
  color: #f56565;
}

.total-items {
  font-size: 13px;
  color: #718096;
}

.btn-submit-order {
  padding: 12px 32px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.3s;
}

.btn-submit-order:hover:not(:disabled) {
  background: #5568d3;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-submit-order:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 对话框 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.cart-dialog {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.dialog-header h3 {
  margin: 0;
  color: #2d3748;
}

.btn-close {
  background: none;
  border: none;
  font-size: 28px;
  color: #718096;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  line-height: 1;
}

.btn-close:hover {
  color: #2d3748;
}

.dialog-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.cart-items-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f7fafc;
  border-radius: 8px;
}

.cart-item-info h4 {
  margin: 0 0 5px 0;
  color: #2d3748;
}

.cart-item-price {
  margin: 0;
  color: #f56565;
  font-weight: bold;
}

.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-small {
  width: 28px;
  height: 28px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.btn-small:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.cart-quantity {
  font-size: 16px;
  font-weight: bold;
  min-width: 30px;
  text-align: center;
}

.empty-cart {
  text-align: center;
  padding: 40px;
  color: #a0aec0;
}

.dialog-footer {
  padding: 15px 20px;
  border-top: 2px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-total {
  font-size: 16px;
}

.total-amount {
  font-size: 24px;
  font-weight: bold;
  color: #f56565;
}

.btn-confirm {
  padding: 12px 32px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.3s;
}

.btn-confirm:hover:not(:disabled) {
  background: #5568d3;
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
