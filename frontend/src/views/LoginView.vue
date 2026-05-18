<template>
  <div class="login-container">
    <div class="login-box">
      <h1>🍽️ 餐饮点餐排队系统</h1>
      <h2>用户登录</h2>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input 
            v-model="form.username" 
            type="text" 
            placeholder="请输入用户名" 
            required 
          />
        </div>
        
        <div class="form-group">
          <label>密码</label>
          <input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            required 
          />
        </div>
        
        <div class="form-group">
          <label>角色（测试用）</label>
          <select v-model="form.role">
            <option value="USER">普通用户</option>
            <option value="STAFF">店员</option>
            <option value="MANAGER">店长</option>
            <option value="ADMIN">超级管理员</option>
          </select>
        </div>
        
        <button type="submit" class="btn-login">登录</button>
      </form>
      
      <div class="test-accounts">
        <h3>测试账号（点击快速填充）</h3>
        <div @click="fillAccount('user', 'USER')">
          👤 普通用户: user / 123456
        </div>
        <div @click="fillAccount('staff', 'STAFF')">
          🧑‍💼 店员: staff / 123456
        </div>
        <div @click="fillAccount('manager', 'MANAGER')">
          👨‍💼 店长: manager / 123456
        </div>
        <div @click="fillAccount('admin', 'ADMIN')">
          ⚙️ 管理员: admin / 123456
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { authApi } from '../api'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: '',
  role: 'USER'
})

// 真实登录（调用后端 API）
const handleLogin = async () => {
  try {
    // 调用后端登录API
    const res = await authApi.login({
      username: form.value.username,
      password: form.value.password
    })
    
    if (res.code === 200) {
      const loginData = res.data
      
      // 构建用户数据
      const userData = {
        id: loginData.user.id,
        username: loginData.user.username,
        nickname: loginData.user.nickname,
        phone: loginData.user.phone,
        email: loginData.user.email,
        avatar: loginData.user.avatar,
        roles: loginData.roles.map(role => role.roleCode),
        menus: loginData.menus
      }
      
      // 存储用户信息和token
      userStore.login(userData, loginData.token)
      
      alert(`✅ 登录成功！\n欢迎，${userStore.getRoleName(loginData.roles[0]?.roleCode || 'USER')}`)
      
      // 跳转到首页
      router.push('/dashboard')
    } else {
      alert(res.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    alert(error.response?.data?.message || '网络错误，请稍后重试')
  }
}

// 快速填充测试账号
const fillAccount = (username, role) => {
  form.value.username = username
  form.value.password = '123456'
  form.value.role = role
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
  width: 100%;
  max-width: 450px;
}

.login-box h1 {
  text-align: center;
  color: #667eea;
  margin-bottom: 10px;
  font-size: 28px;
}

.login-box h2 {
  text-align: center;
  color: #666;
  margin-bottom: 30px;
  font-size: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: bold;
}

input, select {
  width: 100%;
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

.btn-login {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.2s;
}

.btn-login:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.test-accounts {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.test-accounts h3 {
  font-size: 14px;
  color: #999;
  margin-bottom: 10px;
}

.test-accounts div {
  padding: 8px 12px;
  margin: 5px 0;
  background: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
  transition: background 0.2s;
}

.test-accounts div:hover {
  background: #e0e0e0;
}
</style>
