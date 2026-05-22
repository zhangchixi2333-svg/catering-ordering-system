<template>
  <div class="table-management">
    <h2>🪑 桌台管理</h2>
    
    <!-- 筛选条件 -->
    <div class="filter-bar">
      <select v-model="selectedShopId" @change="handleShopChange">
        <option value="">选择店铺</option>
        <option v-for="shop in shops" :key="shop.id" :value="shop.id">
          🏪 {{ shop.shopName }}
        </option>
      </select>
      
      <select v-model="filterStatus" @change="loadTables">
        <option value="">全部状态</option>
        <option value="0">✅ 空闲</option>
        <option value="1">🔴 已占用</option>
        <option value="2">🧹 清洁中</option>
      </select>
      
      <select v-model="filterType" @change="loadTables">
        <option value="">全部类型</option>
        <option value="1">🪑 普通桌</option>
        <option value="2">🛋️ 卡座</option>
        <option value="3">🏠 包厢</option>
        <option value="4">🍸 吧台</option>
      </select>
      
      <button @click="loadTables" class="btn-search">🔄 刷新</button>
      <button @click="showCreateDialog" class="btn-add">➕ 新增桌台</button>
    </div>
    
    <!-- 桌台列表 -->
    <div class="card">
      <div class="card-header">
        <h3>桌台列表</h3>
        <span class="count-badge">共 {{ filteredTables.length }} 张</span>
      </div>
      
      <div class="table-list">
        <div v-if="loading" class="loading-state">
          <p>加载中...</p>
        </div>
        <div v-else-if="filteredTables.length > 0" class="table-items">
          <div v-for="table in filteredTables" :key="table.id" class="table-item">
            <div class="table-main">
              <div class="table-header">
                <div class="table-number-section">
                  <span class="table-number">🪑 {{ table.tableNumber }}</span>
                  <span :class="['status-badge', getStatusClass(table.tableStatus)]">
                    {{ getStatusIcon(table.tableStatus) }} {{ getStatusText(table.tableStatus) }}
                  </span>
                </div>
                <div class="table-availability">
                  {{ table.isAvailable === 1 ? '✅ 可用' : '❌ 不可用' }}
                </div>
              </div>
              
              <div class="table-info">
                <div class="info-row">
                  <span class="label">桌台名称：</span>
                  <span class="value">📋 {{ table.tableName || '-' }}</span>
                </div>
                <div class="info-row">
                  <span class="label">桌台类型：</span>
                  <span class="value">{{ getTableTypeIcon(table.tableType) }} {{ getTableTypeText(table.tableType) }}</span>
                </div>
                <div class="info-row">
                  <span class="label">容纳人数：</span>
                  <span class="value">👥 {{ table.seats }}人</span>
                </div>
                <div class="info-row" v-if="table.location">
                  <span class="label">位置：</span>
                  <span class="value">📍 {{ table.location }}</span>
                </div>
              </div>
            </div>
            
            <div class="table-actions">
              <button @click="viewDetail(table)" class="btn-detail">📋 详情</button>
              <button 
                v-if="table.tableStatus === 1" 
                @click="handleStartCleaning(table)" 
                class="btn-cleaning"
              >
                🧹 清洁
              </button>
              <button 
                v-if="table.tableStatus === 2" 
                @click="handleCompleteCleaning(table)" 
                class="btn-complete"
              >
                ✅ 完成
              </button>
              <button @click="handleEditTable(table)" class="btn-edit">✏️ 编辑</button>
              <button @click="handleDeleteTable(table)" class="btn-delete">🗑️ 删除</button>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-state">
          <div class="empty-icon">🪑</div>
          <p>暂无桌台数据</p>
          <p>原始数据数量: {{ tables.length }}</p>
          <p>筛选后数量: {{ filteredTables.length }}</p>
          <p>筛选状态: {{ filterStatus }}</p>
          <p>筛选类型: {{ filterType }}</p>
        </div>
      </div>
    </div>
    
    <!-- 桌台详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`桌台 ${selectedTable?.tableNumber}`"
      width="500px"
    >
      <div v-if="selectedTable" class="dialog-content">
        <div class="detail-row">
          <span class="label">桌台编号：</span>
          <span class="value">{{ selectedTable.tableNumber }}</span>
        </div>
        <div class="detail-row">
          <span class="label">桌台名称：</span>
          <span class="value">{{ selectedTable.tableName || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">桌台状态：</span>
          <el-tag :type="getStatusType(selectedTable.tableStatus)">
            {{ getStatusText(selectedTable.tableStatus) }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="label">容纳人数：</span>
          <span class="value">{{ selectedTable.seats }}人</span>
        </div>
        <div class="detail-row">
          <span class="label">桌台类型：</span>
          <span class="value">{{ getTableTypeText(selectedTable.tableType) }}</span>
        </div>
        <div class="detail-row" v-if="selectedTable.location">
          <span class="label">位置：</span>
          <span class="value">{{ selectedTable.location }}</span>
        </div>
        
        <div class="action-buttons">
          <!-- 已占用状态：显示"清洁桌面"按钮 -->
          <el-button 
            v-if="selectedTable.tableStatus === 1" 
            type="warning" 
            size="large"
            @click="handleStartCleaning"
          >
            🧹 清洁桌面
          </el-button>
          
          <!-- 清洁中状态：显示"清洁完成"按钮 -->
          <el-button 
            v-if="selectedTable.tableStatus === 2" 
            type="success" 
            size="large"
            @click="handleCompleteCleaning"
          >
            ✅ 清洁完成
          </el-button>
          
          <el-button size="large" @click="dialogVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 新增/编辑桌台对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEditMode ? '编辑桌台' : '新增桌台'"
      width="600px"
    >
      <el-form :model="tableForm" :rules="formRules" ref="tableFormRef" label-width="100px">
        <el-form-item label="桌台编号" prop="tableNumber">
          <el-input v-model="tableForm.tableNumber" placeholder="如：A01、B02" />
        </el-form-item>
        <el-form-item label="桌台名称" prop="tableName">
          <el-input v-model="tableForm.tableName" placeholder="如：一号桌、VIP包厢" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="seats">
          <el-input-number v-model="tableForm.seats" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="桌台类型" prop="tableType">
          <el-select v-model="tableForm.tableType" placeholder="请选择桌台类型">
            <el-option label="普通桌" :value="1" />
            <el-option label="卡座" :value="2" />
            <el-option label="包厢" :value="3" />
            <el-option label="吧台" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置描述" prop="location">
          <el-input v-model="tableForm.location" placeholder="如：A区靠窗" />
        </el-form-item>
        <el-form-item label="是否可用" prop="isAvailable">
          <el-switch v-model="tableForm.isAvailable" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTable" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElDialog, ElForm, ElFormItem, ElInput, ElInputNumber, ElSelect, ElOption, ElSwitch } from 'element-plus'
import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const router = useRouter()

const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const tables = ref([])
const shops = ref([])
const selectedShopId = ref('')
const filterStatus = ref('')
const filterType = ref('')
const dialogVisible = ref(false)
const formDialogVisible = ref(false)
const isEditMode = ref(false)
const selectedTable = ref(null)
const tableFormRef = ref(null)

const tableForm = ref({
  id: null,
  tableNumber: '',
  tableName: '',
  seats: 4,
  tableType: 1,
  location: '',
  isAvailable: 1
})

const formRules = {
  tableNumber: [
    { required: true, message: '请输入桌台编号', trigger: 'blur' }
  ],
  seats: [
    { required: true, message: '请输入容纳人数', trigger: 'blur' }
  ],
  tableType: [
    { required: true, message: '请选择桌台类型', trigger: 'change' }
  ]
}

// 统计数据
const stats = computed(() => {
  return {
    available: tables.value.filter(t => t.tableStatus === 0).length,
    occupied: tables.value.filter(t => t.tableStatus === 1).length,
    cleaning: tables.value.filter(t => t.tableStatus === 2).length
  }
})

// 总座位数
const totalSeats = computed(() => {
  return tables.value.reduce((sum, table) => sum + (table.seats || 0), 0)
})

// 筛选后的桌台列表
const filteredTables = computed(() => {
  let result = tables.value
  
  console.log('筛选前桌台数量:', result.length)
  console.log('筛选状态:', filterStatus.value)
  console.log('筛选类型:', filterType.value)
  
  if (filterStatus.value !== '') {
    result = result.filter(t => t.tableStatus === parseInt(filterStatus.value))
  }
  
  if (filterType.value !== '') {
    result = result.filter(t => t.tableType === parseInt(filterType.value))
  }
  
  console.log('筛选后桌台数量:', result.length)
  
  return result
})

// 加载店铺列表
const loadShops = async () => {
  try {
    console.log('开始加载店铺列表')
    const response = await axios.get('/api/shop/list')
    console.log('店铺API响应:', response.data)
    
    if (response.data.code === 200) {
      shops.value = response.data.data || []
      console.log('加载店铺成功，店铺数量:', shops.value.length)
      console.log('店铺列表:', shops.value)
      
      // 默认选择第一个店铺
      if (shops.value.length > 0 && !selectedShopId.value) {
        selectedShopId.value = shops.value[0].id
        userStore.currentShopId = shops.value[0].id
        console.log('默认选择店铺ID:', selectedShopId.value)
        await loadTables()
      } else {
        console.log('没有店铺数据或已选择店铺')
      }
    } else {
      console.error('店铺API返回错误:', response.data.message)
      ElMessage.error(response.data.message || '加载店铺失败')
    }
  } catch (error) {
    console.error('加载店铺失败:', error)
    if (error.response) {
      console.error('错误响应:', error.response.data)
      ElMessage.error(error.response.data?.message || '加载店铺失败')
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

// 处理店铺切换
const handleShopChange = async () => {
  if (selectedShopId.value) {
    userStore.currentShopId = selectedShopId.value
    await loadTables()
  }
}

// 加载桌台列表
const loadTables = async () => {
  if (!selectedShopId.value) {
    console.log('未选择店铺，跳过加载桌台')
    return
  }
  
  loading.value = true
  try {
    console.log('加载桌台，店铺ID:', selectedShopId.value)
    const response = await axios.get(`/api/table/shop/${selectedShopId.value}`)
    
    console.log('桌台数据响应:', response.data)
    
    if (response.data.code === 200) {
      tables.value = response.data.data || []
      console.log('加载桌台成功，桌台数量:', tables.value.length)
    } else {
      ElMessage.error(response.data.message || '加载桌台失败')
    }
  } catch (error) {
    console.error('加载桌台失败:', error)
    // 更详细的错误处理
    if (error.response) {
      ElMessage.error(error.response.data?.message || '加载桌台失败')
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 获取桌台状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '空闲',
    1: '已占用',
    2: '清洁中'
  }
  return textMap[status] || '未知'
}

// 获取桌台状态图标
const getStatusIcon = (status) => {
  const iconMap = {
    0: '✅',
    1: '🔴',
    2: '🧹'
  }
  return iconMap[status] || ''
}

// 获取桌台状态CSS类
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-available',
    1: 'status-occupied',
    2: 'status-cleaning'
  }
  return classMap[status] || ''
}

// 获取桌台状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'success',
    1: 'danger',
    2: 'warning'
  }
  return typeMap[status] || ''
}

// 获取桌台类型图标
const getTableTypeIcon = (type) => {
  const iconMap = {
    1: '🪑',
    2: '🛋️',
    3: '🏠',
    4: '🍸'
  }
  return iconMap[type] || ''
}

// 获取桌台类型文本
const getTableTypeText = (type) => {
  const typeMap = {
    1: '普通桌',
    2: '卡座',
    3: '包厢',
    4: '吧台'
  }
  return typeMap[type] || '未知'
}

// 获取桌台卡片样式类
const getTableClass = (status) => {
  const classMap = {
    0: 'table-available',
    1: 'table-occupied',
    2: 'table-cleaning'
  }
  return classMap[status] || ''
}

// 点击桌台
const handleTableClick = (table) => {
  selectedTable.value = table
  dialogVisible.value = true
}

// 查看详情
const viewDetail = (table) => {
  selectedTable.value = table
  dialogVisible.value = true
}

// 显示新增对话框
const showCreateDialog = () => {
  isEditMode.value = false
  tableForm.value = {
    id: null,
    tableNumber: '',
    tableName: '',
    seats: 4,
    tableType: 1,
    location: '',
    isAvailable: 1
  }
  formDialogVisible.value = true
}

// 编辑桌台
const handleEditTable = (table) => {
  isEditMode.value = true
  tableForm.value = {
    id: table.id,
    tableNumber: table.tableNumber,
    tableName: table.tableName,
    seats: table.seats,
    tableType: table.tableType,
    location: table.location,
    isAvailable: table.isAvailable
  }
  formDialogVisible.value = true
}

// 保存桌台
const handleSaveTable = async () => {
  if (!tableFormRef.value) return
  
  try {
    await tableFormRef.value.validate()
  } catch (error) {
    return
  }
  
  if (!selectedShopId.value) {
    ElMessage.error('请先选择店铺')
    return
  }
  
  saving.value = true
  try {
    const data = {
      shopId: selectedShopId.value,
      tableNumber: tableForm.value.tableNumber,
      tableName: tableForm.value.tableName,
      seats: tableForm.value.seats,
      tableType: tableForm.value.tableType,
      location: tableForm.value.location,
      isAvailable: tableForm.value.isAvailable
    }
    
    let response
    if (isEditMode.value) {
      data.id = tableForm.value.id
      response = await axios.put('/api/table', data)
    } else {
      response = await axios.post('/api/table', data)
    }
    
    if (response.data.code === 200) {
      ElMessage.success(isEditMode.value ? '更新成功' : '创建成功')
      formDialogVisible.value = false
      loadTables()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    console.error('保存桌台失败:', error)
    // 更详细的错误处理
    if (error.response) {
      ElMessage.error(error.response.data?.message || '操作失败')
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    saving.value = false
  }
}

// 删除桌台
const handleDeleteTable = async (table) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除桌台 ${table.tableNumber} 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'custom-message-box'
      }
    )
    
    const response = await axios.delete(`/api/table/${table.id}`)
    
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadTables()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除桌台失败:', error)
      // 更详细的错误处理
      if (error.response) {
        ElMessage.error(error.response.data?.message || '删除失败')
      } else {
        ElMessage.error('网络错误，请稍后重试')
      }
    }
  }
}

// 开始清洁
const handleStartCleaning = async (table = null) => {
  const targetTable = table || selectedTable.value
  
  try {
    await ElMessageBox.confirm(
      `确定要开始清洁桌台 ${targetTable.tableNumber} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'custom-message-box'
      }
    )
    
    const response = await axios.put(`/api/table/${targetTable.id}/start-cleaning`)
    
    if (response.data.code === 200) {
      ElMessage.success('已开始清洁')
      dialogVisible.value = false
      loadTables()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始清洁失败:', error)
      // 更详细的错误处理
      if (error.response) {
        ElMessage.error(error.response.data?.message || '操作失败')
      } else {
        ElMessage.error('网络错误，请稍后重试')
      }
    }
  }
}

// 完成清洁
const handleCompleteCleaning = async (table = null) => {
  const targetTable = table || selectedTable.value
  
  try {
    await ElMessageBox.confirm(
      `确定桌台 ${targetTable.tableNumber} 已清洁完成吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    const response = await axios.put(`/api/table/${targetTable.id}/complete-cleaning`)
    
    if (response.data.code === 200) {
      ElMessage.success('清洁完成，桌台已变为空闲状态')
      dialogVisible.value = false
      loadTables()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成清洁失败:', error)
      // 更详细的错误处理
      if (error.response) {
        ElMessage.error(error.response.data?.message || '操作失败')
      } else {
        ElMessage.error('网络错误，请稍后重试')
      }
    }
  }
}

onMounted(() => {
  // 检查用户权限
  if (!userStore.hasAnyRole([userStore.ROLES.STAFF, userStore.ROLES.MANAGER, userStore.ROLES.ADMIN])) {
    ElMessage.error('您没有权限访问桌台管理功能')
    // 重定向到首页
    router.push('/')
    return
  }
  
  // 先加载店铺列表，然后加载桌台
  loadShops()
})
</script>

<style scoped>
.table-management {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  min-height: calc(100vh - 60px); /* 减去头部导航的高度 */
}

/* 与主布局一致的卡片样式 */
.card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  color: #667eea;
}

.count-badge {
  background: #edf2f7;
  color: #4a5568;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
}

.table-management h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 22px;
  font-weight: 600;
}

/* 筛选条件 */
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.filter-bar select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 120px;
}

.btn-search {
  background: #667eea;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-add {
  background: #38a169;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.table-management-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* 桌台列表样式 */
.table-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.table-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.table-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.3s;
  background: white;
  min-height: 100px;
}

.table-item:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.table-main {
  flex: 1;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.table-number-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-number {
  font-weight: bold;
  font-size: 16px;
  color: #2d3748;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status-available {
  background: #d4edda;
  color: #155724;
}

.status-occupied {
  background: #f8d7da;
  color: #721c24;
}

.status-cleaning {
  background: #fff3cd;
  color: #856404;
}

.table-availability {
  font-size: 13px;
  color: #666;
}

.table-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.info-row .label {
  color: #718096;
  font-weight: 500;
  min-width: 80px;
}

.info-row .value {
  color: #2d3748;
  font-weight: 500;
}

.table-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: 15px;
  min-width: 80px;
  flex-shrink: 0;
}

.table-actions button {
  padding: 6px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  text-align: center;
}

.table-actions button:hover {
  background: #f7fafc;
  border-color: #cbd5e0;
}

.btn-detail {
  color: #4299e1;
  border-color: #bee3f8;
}

.btn-detail:hover {
  background: #ebf8ff;
  border-color: #90cdf4;
}

.btn-cleaning {
  color: #d69e2e;
  border-color: #fbd38d;
}

.btn-cleaning:hover {
  background: #fffff0;
  border-color: #f6e05e;
}

.btn-complete {
  color: #38a169;
  border-color: #9ae6b4;
}

.btn-complete:hover {
  background: #f0fff4;
  border-color: #68d391;
}

.btn-edit {
  color: #667eea;
  border-color: #c3dafe;
}

.btn-edit:hover {
  background: #ebf4ff;
  border-color: #a3bffa;
}

.btn-delete {
  color: #f56565;
  border-color: #feb2b2;
}

.btn-delete:hover {
  background: #fff5f5;
  border-color: #fc8181;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #a0aec0;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #718096;
}

.loading-state {
  text-align: center;
  padding: 40px 20px;
  color: #a0aec0;
}

.loading-state p {
  font-size: 16px;
  color: #718096;
}

.dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
}

.detail-row:last-of-type {
  border-bottom: none;
}

.detail-row .label {
  color: #2c5282;
  font-size: 15px;
  width: 80px;
}

.detail-row .value {
  color: #2c5282;
  font-size: 15px;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.action-buttons .el-button {
  min-width: 120px;
}

/* 表单元素样式 */
.el-form-item__label {
  font-weight: bold;
  color: #2c5282;
  font-size: 14px;
}

.el-input__wrapper,
.el-select .el-input__wrapper {
  padding: 8px 12px;
  border: 1px solid #e2e8f0 !important;
  border-radius: 4px;
  font-size: 14px;
}

.el-input__wrapper.is-focus,
.el-select .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1) !important;
}

/* 与主布局一致的按钮样式 */
.table-management .el-button--primary,
.filter-bar .el-button--primary {
  --el-button-bg-color: #667eea;
  --el-button-border-color: #667eea;
  --el-button-hover-bg-color: #7a8feb;
  --el-button-hover-border-color: #7a8feb;
  --el-button-active-bg-color: #5a6fd8;
  --el-button-active-border-color: #5a6fd8;
}

/* 信息行样式 */
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

/* 与 ordering view 一致的信息展示样式 */
.table-management .info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e2e8f0;
}

.table-management .info-item:last-child {
  border-bottom: none;
}

.table-management .info-item .label {
  font-weight: bold;
  color: #2c5282;
  width: 100px;
}

.table-management .info-item .value {
  color: #2c5282;
  font-weight: 500;
  text-align: right;
}

/* 提示信息样式 */
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

/* 警告信息样式 */
.warning-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fffbeb;
  border: 1px solid #faf089;
  border-radius: 6px;
  margin-bottom: 15px;
}

.warning-icon {
  font-size: 18px;
}

.warning-text {
  color: #92400e;
  font-weight: 500;
}
</style>