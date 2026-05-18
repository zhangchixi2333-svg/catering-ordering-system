<template>
  <div class="shop-view">
    <h2>🏪 店铺管理</h2>
    
    <!-- 创建店铺表单 -->
    <div class="card">
      <h3>创建店铺</h3>
      <form @submit.prevent="handleCreate">
        <input v-model="form.name" placeholder="店铺名称" required />
        <input v-model="form.address" placeholder="地址" required />
        <input v-model="form.phone" placeholder="联系电话" required />
        <select v-model="form.shopType">
          <option value="">选择类型</option>
          <option value="1">快餐店</option>
          <option value="2">中餐厅</option>
          <option value="3">西餐厅</option>
        </select>
        <label>
          <input type="checkbox" v-model="form.isOpen" />
          营业中
        </label>
        <button type="submit">创建</button>
      </form>
    </div>

    <!-- 店铺列表 -->
    <div class="card">
      <h3>店铺列表</h3>
      <button @click="loadShops" class="btn-refresh">刷新</button>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>地址</th>
            <th>电话</th>
            <th>类型</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="shop in shops" :key="shop.id">
            <td>{{ shop.id }}</td>
            <td>{{ shop.name }}</td>
            <td>{{ shop.address }}</td>
            <td>{{ shop.phone }}</td>
            <td>{{ getTypeText(shop.shopType) }}</td>
            <td>
              <span :class="shop.isOpen ? 'status-open' : 'status-closed'">
                {{ shop.isOpen ? '营业中' : '休息中' }}
              </span>
            </td>
            <td>
              <button @click="handleDelete(shop.id)" class="btn-delete">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="shops.length === 0" class="empty">暂无店铺数据</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { shopApi } from '../api'

const shops = ref([])
const form = ref({
  name: '',
  address: '',
  phone: '',
  shopType: 1,
  isOpen: true
})

// 加载店铺列表
const loadShops = async () => {
  try {
    const res = await shopApi.getList()
    shops.value = res.data || []
  } catch (error) {
    console.error('加载店铺失败:', error)
  }
}

// 创建店铺
const handleCreate = async () => {
  try {
    await shopApi.create(form.value)
    alert('✅ 创建成功')
    form.value = { name: '', address: '', phone: '', shopType: 1, isOpen: true }
    loadShops()
  } catch (error) {
    console.error('创建失败:', error)
  }
}

// 删除店铺
const handleDelete = async (id) => {
  if (!confirm('确定删除吗？')) return
  try {
    await shopApi.delete(id)
    alert('✅ 删除成功')
    loadShops()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 获取类型文本
const getTypeText = (type) => {
  const types = { 1: '快餐店', 2: '中餐厅', 3: '西餐厅' }
  return types[type] || '未知'
}

onMounted(() => {
  loadShops()
})
</script>

<style scoped>
.shop-view h2 {
  margin-bottom: 20px;
  color: #333;
}

.card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card h3 {
  margin-bottom: 15px;
  color: #667eea;
}

form {
  display: grid;
  gap: 10px;
}

input, select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

button {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

button:hover {
  background: #5568d3;
}

.btn-refresh {
  margin-bottom: 10px;
  background: #48bb78;
}

.btn-delete {
  background: #f56565;
}

.btn-delete:hover {
  background: #e53e3e;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f7fafc;
  font-weight: bold;
  color: #4a5568;
}

.status-open {
  color: #48bb78;
  font-weight: bold;
}

.status-closed {
  color: #f56565;
  font-weight: bold;
}

.empty {
  text-align: center;
  color: #999;
  padding: 20px;
}
</style>
