# 🚀 角色权限系统 - 快速开始

## 📦 执行步骤

### 1. 执行 SQL 脚本

```bash
# 方法1: 命令行执行
mysql -u root -p < sql/auth_system.sql

# 方法2: MySQL Workbench
# 打开 auth_system.sql 文件，点击执行
```

### 2. 验证数据

```sql
-- 切换到数据库
USE catering_auth;

-- 查看角色
SELECT * FROM sys_role;

-- 查看用户
SELECT id, username, nickname FROM sys_user;

-- 查看用户角色关联
SELECT u.username, r.role_name 
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id;
```

**预期输出**:

```
+----------+-----------+
| username | role_name |
+----------+-----------+
| user     | 普通用户   |
| staff    | 店员      |
| manager  | 店长      |
| admin    | 超级管理员 |
+----------+-----------+
```

---

## 🔐 测试账号

| 用户名 | 密码 | 角色 | 权限范围 |
|--------|------|------|----------|
| user | 123456 | 普通用户 | 取号、我的订单 |
| staff | 123456 | 店员 | + 叫号管理、全部订单 |
| manager | 123456 | 店长 | + 店铺管理 |
| admin | 123456 | 超级管理员 | 所有权限 |

---

## 💻 后端集成示例

### Spring Boot 配置

#### 1. 添加依赖 (pom.xml)

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

#### 2. 实体类

```java
@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private Integer status;
}

@Data
@TableName("sys_role")
public class SysRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
}

@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String menuName;
    private String path;
    private String component;
    private String icon;
    private String permission;
}
```

#### 3. Mapper

```java
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    @Select("SELECT r.* FROM sys_role r " +
            "JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<SysRole> findRolesByUserId(@Param("userId") Long userId);
    
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1")
    List<SysMenu> findMenusByUserId(@Param("userId") Long userId);
}
```

#### 4. Service

```java
@Service
public class AuthService {
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public LoginVO login(String username, String password) {
        // 1. 查询用户
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
        );
        
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 2. 查询角色和菜单
        List<SysRole> roles = userMapper.findRolesByUserId(user.getId());
        List<SysMenu> menus = userMapper.findMenusByUserId(user.getId());
        
        // 3. 生成 Token
        String token = jwtUtil.generateToken(user.getId(), username);
        
        // 4. 返回结果
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(user);
        vo.setRoles(roles);
        vo.setMenus(menus);
        
        return vo;
    }
}
```

#### 5. Controller

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequest request) {
        try {
            LoginVO vo = authService.login(request.getUsername(), request.getPassword());
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/menus")
    public Result<List<SysMenu>> getMenus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<SysMenu> menus = authService.getUserMenus(userId);
        return Result.success(menus);
    }
}
```

---

## 🌐 前端集成示例

### Vue 3 + Pinia

#### 1. 用户 Store

```javascript
// stores/user.js
import { defineStore } from 'pinia'
import axios from 'axios'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    menus: []
  }),
  
  actions: {
    async login(username, password) {
      const res = await axios.post('/api/auth/login', { username, password })
      
      this.token = res.data.token
      this.user = res.data.user
      this.menus = res.data.menus
      
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      
      return res.data
    },
    
    logout() {
      this.token = ''
      this.user = null
      this.menus = []
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
```

#### 2. 动态路由

```javascript
// router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('../views/LoginView.vue') }
  ]
})

// 动态添加路由
export function addDynamicRoutes(menus) {
  menus.forEach(menu => {
    if (menu.component) {
      router.addRoute({
        path: menu.path,
        name: menu.menuCode,
        component: () => import(`../views/${menu.component}`),
        meta: { title: menu.menuName }
      })
    }
  })
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  if (to.path === '/login') {
    next()
    return
  }
  
  if (!userStore.token) {
    next('/login')
    return
  }
  
  // 如果还没有加载菜单，先加载
  if (userStore.menus.length === 0) {
    const res = await axios.get('/api/auth/menus')
    userStore.menus = res.data
    addDynamicRoutes(res.data)
    next(to.path)
  } else {
    next()
  }
})

export default router
```

#### 3. 登录页面

```vue
<template>
  <div class="login">
    <h1>登录</h1>
    <form @submit.prevent="handleLogin">
      <input v-model="form.username" placeholder="用户名" />
      <input v-model="form.password" type="password" placeholder="密码" />
      <button type="submit">登录</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    await userStore.login(form.value.username, form.value.password)
    router.push('/dashboard')
  } catch (error) {
    alert('登录失败: ' + error.message)
  }
}
</script>
```

---

## 🔍 调试技巧

### 1. 查看用户权限

```sql
SELECT 
    u.username,
    r.role_name,
    m.menu_name,
    m.permission
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id
JOIN sys_role r ON ur.role_id = r.id
JOIN sys_role_menu rm ON r.id = rm.role_id
JOIN sys_menu m ON rm.menu_id = m.id
WHERE u.username = 'manager'
ORDER BY m.sort_order;
```

### 2. 检查 Token

```javascript
// 浏览器控制台
console.log('Token:', localStorage.getItem('token'))
console.log('User:', JSON.parse(localStorage.getItem('user')))
```

### 3. 测试 API

```bash
# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 获取菜单（替换 YOUR_TOKEN）
curl http://localhost:8080/api/auth/menus \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## ⚠️ 注意事项

1. **密码加密**: 生产环境必须使用 BCrypt 加密
2. **HTTPS**: 传输 Token 时必须使用 HTTPS
3. **Token 过期**: 设置合理的过期时间（建议 2 小时）
4. **权限缓存**: 使用 Redis 缓存用户权限，提升性能
5. **日志记录**: 关键操作必须记录到 sys_operation_log

---

## 📞 需要帮助？

查看详细文档: `sql/readme/AUTH_SYSTEM_README.md`

---

**祝使用愉快！** 🎉
