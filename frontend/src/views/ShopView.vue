<template>
  <div class="shop-view">
    <h2>🏪 店铺管理</h2>
    
    <!-- 操作栏 -->
    <div class="action-bar">
      <button @click="showCreateDialog" class="btn-create">➕ 新建店铺</button>
      <button @click="loadShops" class="btn-refresh">🔄 刷新</button>
    </div>
    
    <!-- 店铺列表 -->
    <div v-if="shops.length > 0" class="shop-grid">
      <div v-for="shop in shops" :key="shop.id" class="shop-card">
        <div class="shop-header">
          <div class="shop-title">
            <h3>{{ shop.shopName }}</h3>
            <span :class="['status-badge', shop.shopStatus === 1 ? 'status-open' : 'status-closed']">
              {{ getShopStatusText(shop.shopStatus) }}
            </span>
          </div>
          <div class="shop-code">{{ shop.shopCode }}</div>
        </div>
        
        <div class="shop-info">
          <div class="info-item">
            <span class="label">📍 地址：</span>
            <span class="value">{{ shop.address || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">📞 电话：</span>
            <span class="value">{{ shop.phone || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">🏷️ 类型：</span>
            <span class="value">{{ getShopTypeText(shop.shopType) }}</span>
          </div>
          <div class="info-item">
            <span class="label">⏰ 营业时间：</span>
            <span class="value">{{ shop.businessHours || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">💺 桌台数：</span>
            <span class="value highlight">{{ shop.tableCount || 0 }}</span>
          </div>
        </div>
        
        <div v-if="shop.description" class="shop-description">
          {{ shop.description }}
        </div>
        
        <div class="shop-actions">
          <button @click="viewDetail(shop)" class="btn-detail">📋 详情</button>
          <button @click="editShop(shop)" class="btn-edit">✏️ 编辑</button>
          <button 
            @click="toggleStatus(shop)" 
            :class="shop.shopStatus === 1 ? 'btn-close' : 'btn-open'"
          >
            {{ shop.shopStatus === 1 ? '⏸️ 休息' : '▶️ 营业' }}
          </button>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-state">
      <div class="empty-icon">🏪</div>
      <p>暂无店铺数据</p>
      <button @click="showCreateDialog" class="btn-create-empty">创建第一个店铺</button>
    </div>
    
    <!-- 创建/编辑对话框 -->
    <div v-if="showDialog" class="dialog-overlay" @click="closeDialog">
      <div class="dialog" @click.stop>
        <div class="dialog-header">
          <h3>{{ isEdit ? '✏️ 编辑店铺' : '➕ 新建店铺' }}</h3>
          <button @click="closeDialog" class="btn-close">×</button>
        </div>
        
        <div class="dialog-content">
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label>店铺名称 <span class="required">*</span></label>
              <input v-model="form.shopName" placeholder="请输入店铺名称" required />
            </div>
            
            <div class="form-group">
              <label>店铺编码 <span class="required">*</span></label>
              <input 
                v-model="form.shopCode" 
                placeholder="例如：SHOP001" 
                required 
                :disabled="isEdit"
              />
              <small v-if="!isEdit" class="hint">店铺编码创建后不可修改</small>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>店铺类型</label>
                <select v-model.number="form.shopType">
                  <option value="1">快餐店</option>
                  <option value="2">中餐厅</option>
                  <option value="3">西餐厅</option>
                  <option value="4">咖啡厅</option>
                  <option value="5">其他</option>
                </select>
              </div>
              
              <div class="form-group">
                <label>桌台数量</label>
                <input v-model.number="form.tableCount" type="number" min="0" placeholder="0" />
              </div>
            </div>
            
            <div class="form-group">
              <label>联系电话</label>
              <input v-model="form.phone" placeholder="请输入联系电话" />
            </div>
            
            <div class="form-group">
              <label>店铺地址</label>
              <input v-model="form.address" placeholder="请输入详细地址" />
            </div>
            
            <div class="form-group">
              <label>营业时间</label>
              <input v-model="form.businessHours" placeholder="例如：09:00-22:00" />
            </div>
            
            <div class="form-group">
              <label>店铺描述</label>
              <textarea v-model="form.description" placeholder="请输入店铺描述" rows="3"></textarea>
            </div>
            
            <div class="form-group">
              <label>
                <input type="checkbox" v-model="form.shopStatus" :true-value="1" :false-value="0" />
                立即营业
              </label>
            </div>
          </form>
        </div>
        
        <div class="dialog-footer">
          <button @click="closeDialog" class="btn-cancel">取消</button>
          <button @click="handleSubmit" class="btn-confirm">{{ isEdit ? '保存' : '创建' }}</button>
        </div>
      </div>
    </div>
    
    <!-- 详情对话框 -->
    <div v-if="showDetailDialog" class="dialog-overlay" @click="closeDetailDialog">
      <div class="dialog dialog-large" @click.stop>
        <div class="dialog-header">
          <h3>📋 店铺详情</h3>
          <button @click="closeDetailDialog" class="btn-close">×</button>
        </div>
        
        <div v-if="selectedShop" class="dialog-content">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="detail-row">
              <span class="label">店铺名称：</span>
              <span class="value">{{ selectedShop.shopName }}</span>
            </div>
            <div class="detail-row">
              <span class="label">店铺编码：</span>
              <span class="value code">{{ selectedShop.shopCode }}</span>
            </div>
            <div class="detail-row">
              <span class="label">店铺类型：</span>
              <span class="value">{{ getShopTypeText(selectedShop.shopType) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">营业状态：</span>
              <span :class="['value', selectedShop.shopStatus === 1 ? 'text-success' : 'text-danger']">
                {{ getShopStatusText(selectedShop.shopStatus) }}
              </span>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>联系信息</h4>
            <div class="detail-row">
              <span class="label">联系电话：</span>
              <span class="value">{{ selectedShop.phone || '未设置' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">店铺地址：</span>
              <span class="value">{{ selectedShop.address || '未设置' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">营业时间：</span>
              <span class="value">{{ selectedShop.businessHours || '未设置' }}</span>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>经营信息</h4>
            <div class="detail-row">
              <span class="label">桌台数量：</span>
              <span class="value highlight">{{ selectedShop.tableCount || 0 }}</span>
            </div>
            <div class="detail-row">
              <span class="label">创建时间：</span>
              <span class="value">{{ formatTime(selectedShop.createdAt) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">更新时间：</span>
              <span class="value">{{ formatTime(selectedShop.updatedAt) }}</span>
            </div>
          </div>
          
          <div v-if="selectedShop.description" class="detail-section">
            <h4>店铺描述</h4>
            <p class="description-full">{{ selectedShop.description }}</p>
          </div>
        </div>
        
        <div class="dialog-footer">
          <button @click="closeDetailDialog" class="btn-confirm">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { shopApi } from '../api'

const shops = ref([])
const showDialog = ref(false)
const showDetailDialog = ref(false)
const isEdit = ref(false)
const selectedShop = ref(null)

const form = ref({
  shopName: '',
  shopCode: '',
  shopType: 1,
  shopStatus: 1,
  phone: '',
  address: '',
  businessHours: '',
  tableCount: 0,
  description: ''
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

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  resetForm()
  showDialog.value = true
}

// 编辑店铺
const editShop = (shop) => {
  isEdit.value = true
  selectedShop.value = shop
  form.value = { ...shop }
  showDialog.value = true
}

// 查看详情
const viewDetail = (shop) => {
  selectedShop.value = shop
  showDetailDialog.value = true
}

// 关闭对话框
const closeDialog = () => {
  showDialog.value = false
  resetForm()
}

// 关闭详情对话框
const closeDetailDialog = () => {
  showDetailDialog.value = false
  selectedShop.value = null
}

// 重置表单
const resetForm = () => {
  form.value = {
    shopName: '',
    shopCode: '',
    shopType: 1,
    shopStatus: 1,
    phone: '',
    address: '',
    businessHours: '',
    tableCount: 0,
    description: ''
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await shopApi.update(form.value)
      alert('✅ 店铺更新成功！')
    } else {
      await shopApi.create(form.value)
      alert('✅ 店铺创建成功！')
    }
    closeDialog()
    loadShops()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 切换营业状态
const toggleStatus = async (shop) => {
  const newStatus = shop.shopStatus === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '营业' : '休息'
  
  if (!confirm(`确定要将"${shop.shopName}"设置为${statusText}状态吗？`)) return
  
  try {
    const updateData = { ...shop, shopStatus: newStatus }
    await shopApi.update(updateData)
    alert(`✅ 已设置为${statusText}状态`)
    loadShops()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 获取店铺类型文本
const getShopTypeText = (type) => {
  const types = {
    1: '快餐店',
    2: '中餐厅',
    3: '西餐厅',
    4: '咖啡厅',
    5: '其他'
  }
  return types[type] || '未知'
}

// 获取营业状态文本
const getShopStatusText = (status) => {
  const statuses = {
    0: '休息中',
    1: '营业中',
    2: '已关闭'
  }
  return statuses[status] || '未知'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
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

/* 操作栏 */
.action-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.btn-create, .btn-refresh {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-create {
  background: #667eea;
  color: white;
}

.btn-create:hover {
  background: #5568d3;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-refresh {
  background: #48bb78;
  color: white;
}

.btn-refresh:hover {
  background: #38a169;
}

/* 店铺网格 */
.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.shop-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s;
  border: 2px solid transparent;
}

.shop-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  border-color: #667eea;
}

.shop-header {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.shop-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.shop-title h3 {
  margin: 0;
  color: #2d3748;
  font-size: 18px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status-open {
  background: #f0fff4;
  color: #48bb78;
}

.status-closed {
  background: #fed7d7;
  color: #f56565;
}

.shop-code {
  color: #718096;
  font-size: 13px;
  font-family: monospace;
}

.shop-info {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  padding: 6px 0;
  font-size: 14px;
}

.info-item .label {
  color: #718096;
  min-width: 90px;
}

.info-item .value {
  color: #2d3748;
  flex: 1;
}

.info-item .value.highlight {
  color: #667eea;
  font-weight: bold;
}

.shop-description {
  padding: 10px;
  background: #f7fafc;
  border-radius: 6px;
  font-size: 13px;
  color: #4a5568;
  margin-bottom: 15px;
  line-height: 1.6;
}

.shop-actions {
  display: flex;
  gap: 8px;
}

.shop-actions button {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.btn-detail {
  background: #667eea;
  color: white;
}

.btn-detail:hover {
  background: #5568d3;
}

.btn-edit {
  background: #ed8936;
  color: white;
}

.btn-edit:hover {
  background: #dd6b20;
}

.btn-open {
  background: #48bb78;
  color: white;
}

.btn-open:hover {
  background: #38a169;
}

.btn-close {
  background: #f56565;
  color: white;
}

.btn-close:hover {
  background: #e53e3e;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-state p {
  color: #a0aec0;
  font-size: 16px;
  margin-bottom: 20px;
}

.btn-create-empty {
  padding: 12px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.3s;
}

.btn-create-empty:hover {
  background: #5568d3;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
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

.dialog {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 85vh;
  overflow-y: auto;
}

.dialog-large {
  max-width: 700px;
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
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: #4a5568;
  font-weight: 500;
  font-size: 14px;
}

.required {
  color: #f56565;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group input:disabled {
  background: #f7fafc;
  cursor: not-allowed;
}

.hint {
  display: block;
  margin-top: 4px;
  color: #718096;
  font-size: 12px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.dialog-footer {
  padding: 15px 20px;
  border-top: 2px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-cancel, .btn-confirm {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-cancel {
  background: #e2e8f0;
  color: #4a5568;
}

.btn-cancel:hover {
  background: #cbd5e0;
}

.btn-confirm {
  background: #667eea;
  color: white;
}

.btn-confirm:hover {
  background: #5568d3;
}

/* 详情样式 */
.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  color: #4a5568;
  font-size: 15px;
  border-bottom: 2px solid #667eea;
  padding-bottom: 8px;
}

.detail-row {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #f7fafc;
}

.detail-row .label {
  color: #718096;
  min-width: 110px;
  font-size: 14px;
}

.detail-row .value {
  color: #2d3748;
  flex: 1;
  font-size: 14px;
}

.detail-row .value.code {
  font-family: monospace;
  background: #f7fafc;
  padding: 2px 8px;
  border-radius: 4px;
}

.detail-row .value.highlight {
  color: #667eea;
  font-weight: bold;
  font-size: 16px;
}

.text-success {
  color: #48bb78;
  font-weight: bold;
}

.text-danger {
  color: #f56565;
  font-weight: bold;
}

.description-full {
  margin: 0;
  padding: 12px;
  background: #f7fafc;
  border-radius: 6px;
  color: #4a5568;
  line-height: 1.8;
  font-size: 14px;
}
</style>
