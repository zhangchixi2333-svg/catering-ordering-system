<template>
  <div class="payment-orders">
    <h2>💳 支付订单</h2>
    
    <!-- 筛选条件 -->
    <div class="filter-bar">
      <el-select v-model="filterStatus" placeholder="支付状态" clearable @change="loadPayments">
        <el-option label="全部" value="" />
        <el-option label="待支付" :value="0" />
        <el-option label="支付中" :value="1" />
        <el-option label="支付成功" :value="2" />
        <el-option label="支付失败" :value="3" />
        <el-option label="已退款" :value="4" />
      </el-select>
      
      <el-button type="primary" @click="loadPayments">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>
    
    <!-- 支付订单列表 -->
    <div v-loading="loading" class="payment-list">
      <el-empty v-if="!loading && payments.length === 0" description="暂无支付订单" />
      
      <el-card v-for="payment in payments" :key="payment.id" class="payment-card" shadow="hover">
        <div class="payment-header">
          <div class="payment-info">
            <span class="payment-no">支付单号：{{ payment.paymentNo }}</span>
            <span class="order-no">关联订单：{{ payment.orderNo }}</span>
            <span class="payment-time">{{ formatTime(payment.createdAt) }}</span>
          </div>
          <el-tag :type="getPaymentStatusType(payment.paymentStatus)">
            {{ getPaymentStatusText(payment.paymentStatus) }}
          </el-tag>
        </div>
        
        <div class="payment-body">
          <div class="payment-details">
            <div class="detail-item">
              <span class="label">支付金额：</span>
              <span class="value price">¥{{ payment.paymentAmount }}</span>
            </div>
            <div class="detail-item">
              <span class="label">支付方式：</span>
              <span class="value">{{ getPaymentMethodText(payment.paymentMethod) }}</span>
            </div>
            <div class="detail-item" v-if="payment.transactionId">
              <span class="label">交易流水号：</span>
              <span class="value">{{ payment.transactionId }}</span>
            </div>
            <div class="detail-item" v-if="payment.payTime">
              <span class="label">支付时间：</span>
              <span class="value">{{ payment.payTime }}</span>
            </div>
          </div>
          
          <div class="payment-actions">
            <!-- 支付中状态：可以模拟支付成功 -->
            <el-button 
              v-if="payment.paymentStatus === 1" 
              type="success" 
              size="small"
              @click="handleSimulateSuccess(payment)"
            >
              ✅ 模拟支付成功
            </el-button>
            
            <!-- 待支付状态：可以去支付 -->
            <el-button 
              v-if="payment.paymentStatus === 0" 
              type="primary" 
              size="small"
              @click="handlePay(payment)"
            >
              去支付
            </el-button>
            
            <el-button 
              size="small"
              @click="handleViewDetail(payment)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { paymentApi } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const payments = ref([])
const filterStatus = ref('')

// 加载支付订单列表
const loadPayments = async () => {
  loading.value = true
  try {
    const userId = userStore.user?.id
    if (!userId) {
      ElMessage.error('用户信息不存在')
      return
    }
    
    const result = await paymentApi.getByUser(userId)
    
    if (result.code === 200) {
      // 根据状态筛选
      let filteredPayments = result.data
      if (filterStatus.value !== '') {
        filteredPayments = filteredPayments.filter(
          payment => payment.paymentStatus === parseInt(filterStatus.value)
        )
      }
      
      payments.value = filteredPayments.sort((a, b) => 
        new Date(b.createdAt) - new Date(a.createdAt)
      )
    }
  } catch (error) {
    console.error('加载支付订单失败:', error)
    ElMessage.error('加载支付订单失败')
  } finally {
    loading.value = false
  }
}

// 获取支付状态文本
const getPaymentStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '支付中',
    2: '支付成功',
    3: '支付失败',
    4: '已退款'
  }
  return statusMap[status] || '未知'
}

// 获取支付状态类型
const getPaymentStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: ''
  }
  return typeMap[status] || ''
}

// 获取支付方式文本
const getPaymentMethodText = (method) => {
  const methodMap = {
    1: '微信支付',
    2: '支付宝',
    3: '现金',
    4: '会员卡',
    5: '银行卡'
  }
  return methodMap[method] || '未选择'
}

// 模拟支付成功（沙盒测试）
const handleSimulateSuccess = async (payment) => {
  try {
    await ElMessageBox.confirm(
      `确定要模拟支付成功吗？\n\n支付单号：${payment.paymentNo}\n支付金额：¥${payment.paymentAmount}`,
      '沙盒测试',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用沙盒接口模拟支付成功
    const response = await fetch(`/api/payment/sandbox/success/${payment.id}`, {
      method: 'POST'
    })
    
    const result = await response.json()
    
    if (result.code === 200) {
      ElMessage.success('✅ 支付成功！订单状态已更新为"待接单"')
      loadPayments()
    } else {
      ElMessage.error(result.message || '支付失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('模拟支付失败:', error)
      ElMessage.error('模拟支付失败')
    }
  }
}

// 去支付（创建新的支付订单）
const handlePay = (payment) => {
  ElMessage.info('该订单已在支付中，请等待支付完成')
}

// 查看详情
const handleViewDetail = (payment) => {
  ElMessageBox.alert(
    `支付订单详情：
    
支付单号：${payment.paymentNo}
关联订单：${payment.orderNo}
支付状态：${getPaymentStatusText(payment.paymentStatus)}
支付金额：¥${payment.paymentAmount}
支付方式：${getPaymentMethodText(payment.paymentMethod)}
交易流水号：${payment.transactionId || '无'}
支付时间：${payment.payTime || '未支付'}
创建时间：${formatTime(payment.createdAt)}
`,
    '支付订单详情',
    {
      confirmButtonText: '确定'
    }
  )
}

onMounted(() => {
  loadPayments()
})
</script>

<style scoped>
.payment-orders {
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

.payment-list {
  min-height: 400px;
}

.payment-card {
  margin-bottom: 16px;
  transition: all 0.3s;
}

.payment-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.payment-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.payment-no {
  font-weight: bold;
  color: #333;
  font-size: 16px;
}

.order-no {
  font-size: 13px;
  color: #666;
}

.payment-time {
  font-size: 12px;
  color: #999;
}

.payment-body {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.payment-details {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-item .label {
  color: #666;
  font-size: 14px;
}

.detail-item .value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.detail-item .price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.payment-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>