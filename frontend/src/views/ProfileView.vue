<template>
  <div class="profile-container">
    <div class="profile-card">
      <h2>👤 个人信息</h2>
      
      <div v-if="loading" class="loading">加载中...</div>
      
      <form v-else @submit.prevent="handleUpdate" class="profile-form">
        <!-- 头像 -->
        <div class="form-group avatar-group">
          <label>头像</label>
          <div class="avatar-preview">
            <img :src="formData.avatar || 'https://ui-avatars.com/api/?name=' + formData.username" alt="avatar" />
          </div>
          <input 
            v-model="formData.avatar" 
            type="text" 
            placeholder="输入头像URL" 
          />
        </div>

        <!-- 用户名（不可编辑） -->
        <div class="form-group">
          <label>用户名</label>
          <input 
            :value="formData.username" 
            type="text" 
            disabled 
            class="disabled-input"
          />
          <small>用户名不可修改</small>
        </div>

        <!-- 昵称 -->
        <div class="form-group">
          <label>昵称</label>
          <input 
            v-model="formData.nickname" 
            type="text" 
            placeholder="请输入昵称" 
            required
          />
        </div>

        <!-- 真实姓名 -->
        <div class="form-group">
          <label>真实姓名</label>
          <input 
            v-model="formData.realName" 
            type="text" 
            placeholder="请输入真实姓名" 
          />
        </div>

        <!-- 手机号 -->
        <div class="form-group">
          <label>手机号</label>
          <input 
            v-model="formData.phone" 
            type="tel" 
            placeholder="请输入手机号" 
          />
        </div>

        <!-- 邮箱 -->
        <div class="form-group">
          <label>邮箱</label>
          <input 
            v-model="formData.email" 
            type="email" 
            placeholder="请输入邮箱" 
          />
        </div>

        <!-- 性别 -->
        <div class="form-group">
          <label>性别</label>
          <select v-model="formData.gender">
            <option :value="0">未知</option>
            <option :value="1">男</option>
            <option :value="2">女</option>
          </select>
        </div>

        <!-- 角色（不可编辑） -->
        <div class="form-group">
          <label>角色</label>
          <input 
            :value="userStore.getRoleName(userStore.currentRole)" 
            type="text" 
            disabled 
            class="disabled-input"
          />
        </div>

        <!-- 按钮组 -->
        <div class="button-group">
          <button type="button" @click="handleCancel" class="btn-cancel">取消</button>
          <button type="submit" class="btn-save" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { authApi } from '../api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)

const formData = ref({
  username: '',
  nickname: '',
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  gender: 0
})

// 获取用户信息
const fetchProfile = async () => {
  loading.value = true
  try {
    const res = await authApi.getProfile(userStore.token)
    if (res.code === 200) {
      const user = res.data
      formData.value = {
        username: user.username,
        nickname: user.nickname || '',
        realName: user.realName || '',
        phone: user.phone || '',
        email: user.email || '',
        avatar: user.avatar || '',
        gender: user.gender || 0
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    alert('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 更新用户信息
const handleUpdate = async () => {
  saving.value = true
  try {
    const res = await authApi.updateProfile(userStore.token, {
      nickname: formData.value.nickname,
      realName: formData.value.realName,
      phone: formData.value.phone,
      email: formData.value.email,
      avatar: formData.value.avatar,
      gender: formData.value.gender
    })
    
    if (res.code === 200) {
      alert('✅ 用户信息更新成功')
      
      // 更新本地存储的用户信息
      const updatedUser = {
        ...userStore.user,
        nickname: formData.value.nickname,
        avatar: formData.value.avatar
      }
      userStore.login(updatedUser, userStore.token)
      
      // 返回上一页或首页
      router.push('/dashboard')
    } else {
      alert(res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    alert(error.response?.data?.message || '网络错误，请稍后重试')
  } finally {
    saving.value = false
  }
}

// 取消
const handleCancel = () => {
  router.back()
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.profile-card h2 {
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  border-bottom: 2px solid #667eea;
  padding-bottom: 15px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 16px;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: bold;
  color: #555;
  font-size: 14px;
}

.form-group small {
  color: #999;
  font-size: 12px;
}

input, select {
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

input:focus, select:focus {
  outline: none;
  border-color: #667eea;
}

.disabled-input {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.avatar-group {
  align-items: center;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  margin: 10px 0;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #667eea;
}

.button-group {
  display: flex;
  gap: 15px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn-cancel, .btn-save {
  flex: 1;
  padding: 14px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-save {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-save:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
