<template>
  <div class="ordering-view">
    <h2>🍽️ 在线点餐</h2>
    
    <!-- 订单类型选择 -->
    <div v-if="selectedShopId" class="order-type-selector">
      <label>订单类型：</label>
      <select v-model="orderType" :disabled="isFromQueue">
        <option v-for="type in orderTypes" :key="type.value" :value="type.value" :disabled="isFromQueue && type.value === 1">
          {{ type.label }}
          <span v-if="isFromQueue && type.value === 1" class="disabled-reason">(排队关联，不可选)</span>
        </option>
      </select>
      <span v-if="isFromQueue" class="from-queue-tip">📋 来自排队关联订单</span>
    </div>
    
    <!-- 堂食相关信息（仅当订单类型为堂食时显示） -->
    <div v-if="orderType === 1 && selectedShopId" class="dining-options">
      <!-- 排队号和店铺信息（仅当从排队界面跳转时显示） -->
      <div v-if="isFromQueue" class="queue-info">
        <div class="info-row">
          <label>排队号码：</label>
          <span class="info-value">{{ queueNumber }}</span>
        </div>
        <div class="info-row">
          <label>店铺名称：</label>
          <span class="info-value">{{ selectedShopName }}</span>
        </div>
      </div>
      
      <!-- 如果是堂食但没有排队号码，显示提示和输入框 -->
      <div v-else-if="orderType === 1 && !queueNumber" class="no-queue-warning">
        <div class="warning-message">
          <span class="warning-icon">⚠️</span>
          <span class="warning-text">堂食需要先获取排队号码</span>
        </div>
        <div class="queue-input-section">
          <label>请输入排队号码：</label>
          <input 
            v-model="manualQueueNumber" 
            type="text" 
            placeholder="例如：A001" 
            class="queue-input"
          />
          <button @click="verifyQueueNumber" class="btn-verify-queue">验证号码</button>
        </div>
      </div>
      
      <div class="table-selection">
        <label>选择桌台：</label>
        <select v-model="selectedTableId" @focus="loadAvailableTablesOnFocus" @change="onTableSelect">
          <option value="">请选择桌台</option>
          <option v-for="table in availableTablesByType" :key="table.id" :value="table.id">
            {{ table.tableNumber }} ({{ getTableTypeText(table.tableType) }}, {{ table.seats }}人)
          </option>
        </select>
        
        <!-- 桌台类型筛选 -->
        <div class="table-type-filter">
          <label>筛选桌台类型：</label>
          <select v-model="selectedTableType" @change="filterTablesByType">
            <option value="">全部类型</option>
            <option v-for="(typeName, typeValue) in tableTypeMap" :key="typeValue" :value="parseInt(typeValue)">
              {{ typeName }}
            </option>
          </select>
        </div>
      </div>
      <div class="seat-count">
        <label>用餐人数：</label>
        <input v-model.number="seatCount" type="number" min="1" max="20" placeholder="请输入用餐人数" />
      </div>
    </div>
    
    <!-- 店铺选择 -->
    <div class="shop-selector">
      <label>选择店铺：</label>
      <select v-model="selectedShopId" @change="onShopChange" :disabled="isShopLocked">
        <option value="">请选择店铺</option>
        <option v-for="shop in shops" :key="shop.id" :value="shop.id">
          {{ shop.shopName }}
        </option>
      </select>
      <span v-if="isShopLocked" class="shop-locked-tip">🔒 店铺已锁定（与排队店铺一致）</span>
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
import { shopApi, queueApi } from '../api'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

// 数据
const shops = ref([])
const selectedShopId = ref('')
const isShopLocked = ref(false) // 店铺是否被锁定（与排队店铺一致）
const categories = ref([])
const menuItems = ref([])
const selectedCategoryId = ref('')
const cart = ref({}) // { itemId: quantity }
const showCartDialog = ref(false)
const isSubmitting = ref(false)

// 订单类型相关
const orderType = ref(2) // 默认外带（2），如果是从排队跳转则设为堂食（1）
const isFromQueue = ref(false) // 是否从排队界面跳转而来
const selectedTableId = ref(null) // 选择的桌台ID
const seatCount = ref(2) // 用餐人数
const availableTables = ref([]) // 可用桌台列表
const queueNumber = ref('') // 排队号码
const selectedShopName = ref('') // 选择的店铺名称
const selectedTableType = ref('') // 选择的桌台类型用于筛选
const manualQueueNumber = ref('') // 手动输入的排队号码
const tableTypeMap = {
  1: '普通桌',
  2: '卡座',
  3: '包厢',
  4: '吧台'
} // 桌台类型映射

// 订单类型选项
const orderTypes = [
  { value: 1, label: '堂食' },
  { value: 2, label: '外带' },
  { value: 3, label: '外卖' }
]

// 从路由参数中获取 queueId（排队ID）
const queueId = ref(route.query.queueId ? parseInt(route.query.queueId) : null)
if (queueId.value) {
  console.log('检测到排队ID:', queueId.value)
  isFromQueue.value = true
  orderType.value = 1 // 从排队跳转，默认为堂食
}

// 加载可用桌台
const loadAvailableTables = async () => {
  console.log('开始执行 loadAvailableTables 函数');
  console.log('当前选择的店铺ID:', selectedShopId.value);
  console.log('当前订单类型:', orderType.value);
  
  if (!selectedShopId.value || orderType.value !== 1) {
    console.log('不满足加载条件，清空桌台列表');
    availableTables.value = []
    return
  }
  
  console.log('发送API请求获取店铺', selectedShopId.value, '的可用桌台');
  
  try {
    const response = await axios.get(`/api/table/shop/${selectedShopId.value}/available`)
    console.log('API响应:', response.data);
    
    if (response.data.code === 200) {
      console.log('API调用成功，返回桌台数量:', response.data.data?.length || 0);
      availableTables.value = response.data.data || []
      console.log('更新可用桌台列表:', availableTables.value);
    } else {
      console.error('加载可用桌台失败:', response.data.message)
      availableTables.value = []
    }
  } catch (error) {
    console.error('加载可用桌台失败:', error)
    console.error('错误详情:', error.message);
    if (error.response) {
      console.error('响应状态码:', error.response.status);
      console.error('响应数据:', error.response.data);
    }
    availableTables.value = []
  }
}

// 当店铺改变时加载菜单数据
const onShopChange = async () => {
  console.log('店铺选择发生变化，当前选择的店铺ID:', selectedShopId.value);
  loadMenuData()
  // 不再自动加载桌台，仅在用户聚焦桌台选择框时加载
}

// 获取桌台类型文本
const getTableTypeText = (type) => {
  const typeMap = {
    1: '普通桌',
    2: '卡座',
    3: '包厢',
    4: '吧台'
  }
  return typeMap[type] || '未知类型'
}

// 加载店铺列表
const loadShops = async () => {
  try {
    const res = await shopApi.getList()
    shops.value = res.data || []
    
    // 如果有排队ID，先获取排队信息，锁定店铺
    if (queueId.value) {
      console.log('获取排队信息，锁定店铺...')
      const queueRes = await queueApi.getById(queueId.value)
      if (queueRes.code === 200 && queueRes.data) {
        const queueShopId = queueRes.data.shopId
        console.log('排队店铺ID:', queueShopId)
        
        // 检查店铺是否存在
        const shopExists = shops.value.some(s => s.id === queueShopId)
        if (shopExists) {
          selectedShopId.value = queueShopId
          isShopLocked.value = true
          console.log('店铺已锁定:', queueShopId)
        } else {
          console.warn('排队店铺不存在，使用默认店铺')
          if (shops.value.length > 0) {
            selectedShopId.value = shops.value[0].id
          }
        }
      } else {
        console.warn('获取排队信息失败，使用默认店铺')
        if (shops.value.length > 0) {
          selectedShopId.value = shops.value[0].id
        }
      }
    } else {
      // 没有排队ID，自动选择第一个店铺
      if (shops.value.length > 0) {
        selectedShopId.value = shops.value[0].id
      }
    }
    
    // 加载菜单数据
    if (selectedShopId.value) {
      loadMenuData()
      // 不再自动加载桌台，仅在用户聚焦桌台选择框时加载
    }
    
    // 获取排队信息（包括排队号和店铺名）
    if (queueId.value) {
      await loadQueueInfo()
    }
  } catch (error) {
    console.error('加载店铺失败:', error)
  }
}

// 监听订单类型变化，加载或清空可用桌台
const onOrderTypeChange = async () => {
  console.log('订单类型发生变化，新类型:', orderType.value);
  if (orderType.value === 1) {
    console.log('选择堂食类型，等待用户聚焦桌台选择框');
    // 选择堂食时，不自动加载桌台，等待用户聚焦桌台选择框
  } else {
    console.log('选择非堂食类型，清空桌台选择');
    // 选择非堂食时清空桌台选择
    selectedTableId.value = null
    seatCount.value = 2
    // 清空桌台筛选条件
    selectedTableType.value = ''
  }
}

// 根据桌台类型筛选可用桌台
const availableTablesByType = computed(() => {
  if (!selectedTableType.value) {
    return availableTables.value
  }
  return availableTables.value.filter(table => table.tableType === parseInt(selectedTableType.value))
})

// 根据类型筛选桌台
const filterTablesByType = () => {
  // 这个函数会在selectedTableType变化时自动更新availableTablesByType
}

// 验证手动输入的排队号码
const verifyQueueNumber = async () => {
  if (!manualQueueNumber.value.trim()) {
    alert('请输入排队号码');
    return;
  }
  
  if (!selectedShopId.value) {
    alert('请先选择店铺');
    return;
  }
  
  try {
    console.log('验证排队号码:', manualQueueNumber.value, '店铺ID:', selectedShopId.value);
    
    // 查询排队号码是否存在且状态为待叫号
    const response = await queueApi.getByQueueNo(manualQueueNumber.value);
    console.log('排队验证响应:', response);
    
    if (response.code === 200 && response.data) {
      // 检查排队号码是否属于当前店铺
      if (response.data.shopId !== selectedShopId.value) {
        alert('排队号码不属于当前店铺');
        return;
      }
      
      // 检查排队状态是否为被叫号状态（通常被叫号状态为1，表示可以进行堂食）
      // 根据系统设计，可能需要允许状态为1（被叫号）或2（已入座）的排队号用于堂食订单
      if (response.data.status !== 1 && response.data.status !== 2) { // 状态1：被叫号，状态2：已入座
        alert('排队号码状态不符，无法用于堂食订单（需要已被叫号）');
        return;
      }
      
      // 验证通过，设置排队号码和相关信息
      queueNumber.value = response.data.queueNo;
      selectedShopName.value = response.data.shopName || shops.value.find(s => s.id === response.data.shopId)?.shopName || '';
      
      alert('排队号码验证成功！');
      console.log('排队号码验证成功:', {
        queueNo: queueNumber.value,
        shopName: selectedShopName.value
      });
    } else {
      alert('排队号码不存在或验证失败');
    }
  } catch (error) {
    console.error('验证排队号码失败:', error);
    alert('验证排队号码失败，请稍后重试');
  }
}

// 当桌台选择框获得焦点时加载可用桌台
const loadAvailableTablesOnFocus = async () => {
  console.log('桌台选择框获得焦点，准备加载可用桌台');
  console.log('当前订单类型:', orderType.value);
  console.log('当前选择的店铺ID:', selectedShopId.value);
  
  // 确保在堂食模式下且已选择店铺时才加载
  if (orderType.value === 1 && selectedShopId.value) {
    console.log('符合条件，开始加载可用桌台...');
    await loadAvailableTables();
  } else {
    console.log('不符合条件，不加载桌台列表');
    if (orderType.value !== 1) {
      console.log('订单类型不是堂食 (orderType !== 1)');
    }
    if (!selectedShopId.value) {
      console.log('未选择店铺 (selectedShopId is empty)');
    }
  }
}

// 处理桌台选择
const onTableSelect = () => {
  // 可以在这里添加桌台选择后的额外处理逻辑
  console.log('选择的桌台ID:', selectedTableId.value)
}

// 获取排队信息
const loadQueueInfo = async () => {
  if (queueId.value) {
    try {
      const queueRes = await queueApi.getById(queueId.value)
      if (queueRes.code === 200 && queueRes.data) {
        queueNumber.value = queueRes.data.queueNo
        // 获取店铺名称
        const shop = shops.value.find(s => s.id === queueRes.data.shopId)
        if (shop) {
          selectedShopName.value = shop.shopName
        }
      } else {
        console.error('获取排队信息失败:', queueRes.message)
      }
    } catch (error) {
      console.error('获取排队信息异常:', error)
    }
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
  
  // 堂食类型需要选择桌台
  if (orderType.value === 1 && !selectedTableId.value) {
    alert('请选择桌台')
    return
  }
  
  // 堂食类型需要有排队号码
  if (orderType.value === 1 && !queueNumber.value) {
    alert('堂食订单必须关联排队号码，请先获取排队号码')
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
      shopId: isShopLocked.value ? selectedShopId.value : selectedShopId.value, // 使用锁定的店铺ID或当前选择的店铺ID
      userId: userStore.user.id,
      orderType: orderType.value, // 使用选择的订单类型
      tableId: orderType.value === 1 ? selectedTableId.value : null, // 堂食才需要桌台ID
      seatCount: orderType.value === 1 ? seatCount.value : null, // 堂食才需要用餐人数
      remark: '', // 订单备注
      items: orderItems,
      totalAmount: totalAmount,
      itemCount: itemCount
    }
    
    console.log('🔒 [订单] 店铺锁定状态:', {
      isShopLocked: isShopLocked.value,
      selectedShopId: selectedShopId.value,
      queueId: queueId.value
    })
    
    // 如果有关联排队，使用已加载的排队号码
    if (queueNumber.value) {
      orderData.queueNumber = queueNumber.value; // 使用已加载的排队号码
      console.log('订单关联排队号码:', queueNumber.value);
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
      
      // 清空购物车
      cart.value = {}
      closeCartDialog()
      
      // 跳转到我的订单页面（普通用户）
      router.push('/my-orders')
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

/* 订单类型选择 */
.order-type-selector {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.order-type-selector label {
  font-weight: bold;
  color: #4a5568;
}

.order-type-selector select {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
}

.order-type-selector select:disabled {
  background-color: #f7fafc;
  cursor: not-allowed;
  color: #4a5568;
}

.from-queue-tip {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  background: #e6fffa;
  color: #234e52;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.disabled-reason {
  font-size: 12px;
  color: #e53e3e;
  margin-left: 5px;
}

/* 堂食选项 */
.dining-options {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
  padding: 15px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.dining-options .table-selection,
.dining-options .seat-count {
  flex: 1;
  min-width: 200px;
}

/* 排队信息 */
.queue-info {
  width: 100%;
  background: #ebf8ff;
  border: 1px solid #bee3f8;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 15px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row label {
  width: 80px;
  font-weight: bold;
  color: #2c5282;
}

.info-value {
  flex: 1;
  color: #2c5282;
  font-weight: 500;
}

/* 桌台类型筛选 */
.table-type-filter {
  margin-top: 10px;
}

.table-type-filter label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #4a5568;
  font-size: 14px;
}

.table-type-filter select {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 13px;
}

/* 无排队号警告 */
.no-queue-warning {
  width: 100%;
  margin-bottom: 15px;
}

.warning-message {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 6px 6px 0 0;
  color: #856404;
}

.warning-icon {
  margin-right: 8px;
  font-size: 16px;
}

.warning-text {
  font-weight: 500;
  font-size: 14px;
}

/* 排队号码输入区域 */
.queue-input-section {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-top: none;
  border-radius: 0 0 6px 6px;
  align-items: center;
}

.queue-input-section label {
  font-weight: bold;
  color: #495057;
  min-width: 100px;
}

.queue-input-section input {
  flex: 1;
  min-width: 150px;
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
}

.queue-input-section .btn-verify-queue {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.queue-input-section .btn-verify-queue:hover {
  background: #0056b3;
}

.dining-options label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #4a5568;
  font-size: 14px;
}

.dining-options select,
.dining-options input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
}

.dining-options input {
  padding: 10px;
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

.shop-selector select:disabled {
  background-color: #f7fafc;
  cursor: not-allowed;
  color: #4a5568;
}

.shop-locked-tip {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  background: #fff3cd;
  color: #856404;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
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