# 📚 角色权限系统数据库文档

## 📋 目录结构

```
sql/
├── auth_system.sql          # 角色权限系统建表脚本
└── readme/
    └── AUTH_SYSTEM_README.md # 本文档
```

---

## 🎯 设计目标

实现基于 **RBAC (Role-Based Access Control)** 的角色权限管理系统，支持：

- ✅ 多角色管理（用户、店员、店长、管理员）
- ✅ 灵活的权限配置
- ✅ 菜单动态加载
- ✅ 操作日志记录
- ✅ 用户软删除

---

## 🗄️ 数据库表设计

### 1. sys_user - 用户表

**用途**: 存储系统所有用户的基本信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 用户ID（主键） |
| username | VARCHAR(50) | 用户名（唯一，登录账号） |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| nickname | VARCHAR(50) | 昵称/显示名称 |
| real_name | VARCHAR(50) | 真实姓名 |
| phone | VARCHAR(20) | 手机号码 |
| email | VARCHAR(100) | 电子邮箱 |
| avatar | VARCHAR(255) | 头像URL |
| gender | TINYINT | 性别：0-未知，1-男，2-女 |
| status | TINYINT | 状态：0-禁用，1-启用 |
| last_login_time | DATETIME | 最后登录时间 |
| last_login_ip | VARCHAR(50) | 最后登录IP |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted_at | DATETIME | 删除时间（软删除） |

**索引**:
- `idx_username`: 用户名索引
- `idx_phone`: 手机号索引
- `idx_status`: 状态索引

---

### 2. sys_role - 角色表

**用途**: 定义系统中的角色类型

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 角色ID（主键） |
| role_code | VARCHAR(50) | 角色编码（唯一标识） |
| role_name | VARCHAR(50) | 角色名称 |
| description | VARCHAR(255) | 角色描述 |
| sort_order | INT | 排序号 |
| status | TINYINT | 状态：0-禁用，1-启用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

**预置角色**:

| role_code | role_name | 说明 |
|-----------|-----------|------|
| USER | 普通用户 | 可以取号排队、查看自己的订单 |
| STAFF | 店员 | 可以管理叫号、处理订单 |
| MANAGER | 店长 | 可以管理店铺、查看所有数据 |
| ADMIN | 超级管理员 | 拥有系统所有权限 |

---

### 3. sys_user_role - 用户角色关联表

**用途**: 实现用户与角色的多对多关系

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 关联ID（主键） |
| user_id | BIGINT | 用户ID（外键） |
| role_id | BIGINT | 角色ID（外键） |
| created_at | DATETIME | 创建时间 |

**约束**:
- 唯一约束: `(user_id, role_id)` - 防止重复分配
- 级联删除: 用户或角色删除时自动清理关联

---

### 4. sys_menu - 菜单表

**用途**: 定义系统菜单和权限点

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 菜单ID（主键） |
| parent_id | BIGINT | 父菜单ID（0表示顶级） |
| menu_name | VARCHAR(50) | 菜单名称 |
| menu_code | VARCHAR(50) | 菜单编码 |
| menu_type | TINYINT | 类型：1-目录，2-菜单，3-按钮 |
| path | VARCHAR(200) | 路由路径 |
| component | VARCHAR(200) | 组件路径 |
| icon | VARCHAR(50) | 图标 |
| sort_order | INT | 排序号 |
| permission | VARCHAR(100) | 权限标识（如：queue:call） |
| visible | TINYINT | 是否可见：0-隐藏，1-显示 |
| status | TINYINT | 状态：0-禁用，1-启用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

**菜单层级示例**:
```
🏠 首页 (目录)
🎫 排队管理 (目录)
  ├─ 🎫 取号排队 (菜单)
  └─ 🔔 叫号管理 (菜单)
📦 订单管理 (目录)
  ├─ 📋 我的订单 (菜单)
  └─ 📊 全部订单 (菜单)
```

---

### 5. sys_role_menu - 角色菜单关联表

**用途**: 实现角色与菜单的多对多关系

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 关联ID（主键） |
| role_id | BIGINT | 角色ID（外键） |
| menu_id | BIGINT | 菜单ID（外键） |
| created_at | DATETIME | 创建时间 |

**约束**:
- 唯一约束: `(role_id, menu_id)`
- 级联删除: 角色或菜单删除时自动清理

---

### 6. sys_operation_log - 操作日志表

**用途**: 记录用户的关键操作

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 日志ID（主键） |
| user_id | BIGINT | 操作用户ID |
| username | VARCHAR(50) | 操作用户名 |
| operation | VARCHAR(100) | 操作描述 |
| module | VARCHAR(50) | 操作模块 |
| method | VARCHAR(200) | 请求方法 |
| params | TEXT | 请求参数 |
| ip | VARCHAR(50) | 操作IP |
| location | VARCHAR(100) | 操作地点 |
| result | TINYINT | 结果：0-失败，1-成功 |
| error_msg | TEXT | 错误信息 |
| duration | BIGINT | 执行时长（毫秒） |
| created_at | DATETIME | 操作时间 |

---

## 🔄 关系图

```
sys_user (用户)
    │
    ├── sys_user_role (用户角色关联)
    │       │
    │       └── sys_role (角色)
    │               │
    │               └── sys_role_menu (角色菜单关联)
    │                       │
    │                       └── sys_menu (菜单)
    │
    └── sys_operation_log (操作日志)
```

---

## 📝 初始化数据

### 1. 角色数据

```sql
INSERT INTO sys_role (role_code, role_name, description) VALUES
('USER', '普通用户', '可以取号排队、查看自己的订单'),
('STAFF', '店员', '可以管理叫号、处理订单'),
('MANAGER', '店长', '可以管理店铺、查看所有数据'),
('ADMIN', '超级管理员', '拥有系统所有权限');
```

### 2. 测试用户（密码: 123456）

⚠️ **注意**: 生产环境必须使用 BCrypt 加密密码！

```sql
INSERT INTO sys_user (username, password, nickname, phone) VALUES
('user', '123456', '普通用户', '13800138001'),
('staff', '123456', '店员', '13800138002'),
('manager', '123456', '店长', '13800138003'),
('admin', '123456', '管理员', '13800138004');
```

### 3. 用户角色分配

```sql
-- user -> USER
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
-- staff -> STAFF
INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 2);
-- manager -> MANAGER
INSERT INTO sys_user_role (user_id, role_id) VALUES (3, 3);
-- admin -> ADMIN
INSERT INTO sys_user_role (user_id, role_id) VALUES (4, 4);
```

---

## 🔍 常用查询

### 1. 查询用户的角色

```sql
SELECT r.role_code, r.role_name, r.description
FROM sys_role r
JOIN sys_user_role ur ON r.id = ur.role_id
WHERE ur.user_id = 1;
```

**预期结果**:
```
role_code | role_name | description
----------|-----------|------------------
USER      | 普通用户   | 可以取号排队...
```

---

### 2. 查询角色的菜单

```sql
SELECT m.menu_name, m.path, m.permission, m.icon
FROM sys_menu m
JOIN sys_role_menu rm ON m.id = rm.menu_id
WHERE rm.role_id = 1  -- USER 角色
ORDER BY m.sort_order;
```

**预期结果** (USER 角色):
```
menu_name | path        | permission  | icon
----------|-------------|-------------|-----
首页      | /dashboard  | dashboard:view | 🏠
取号排队  | /queue/take | queue:take  | 🎫
我的订单  | /orders/my  | order:my    | 📋
```

---

### 3. 查询用户的完整权限

```sql
SELECT DISTINCT m.menu_name, m.path, m.permission
FROM sys_menu m
JOIN sys_role_menu rm ON m.id = rm.menu_id
JOIN sys_user_role ur ON rm.role_id = ur.role_id
WHERE ur.user_id = 1 AND m.status = 1
ORDER BY m.sort_order;
```

---

### 4. 查询某个菜单被哪些角色拥有

```sql
SELECT r.role_code, r.role_name
FROM sys_role r
JOIN sys_role_menu rm ON r.id = rm.role_id
WHERE rm.menu_id = 8;  -- 叫号管理菜单
```

**预期结果**:
```
role_code | role_name
----------|----------
STAFF     | 店员
MANAGER   | 店长
ADMIN     | 超级管理员
```

---

## 🛠️ 使用流程

### 前端集成步骤

#### 1. 用户登录

```javascript
// 调用后端登录接口
const login = async (username, password) => {
  const res = await axios.post('/api/auth/login', { username, password })
  
  // 保存 token 和用户信息
  localStorage.setItem('token', res.data.token)
  localStorage.setItem('user', JSON.stringify(res.data.user))
  
  return res.data
}
```

#### 2. 获取用户菜单

```javascript
// 登录后获取菜单
const getUserMenus = async () => {
  const res = await axios.get('/api/auth/menus')
  return res.data.menus
}
```

#### 3. 动态生成路由

```javascript
// 根据菜单生成路由
const menus = await getUserMenus()
menus.forEach(menu => {
  router.addRoute({
    path: menu.path,
    component: () => import(`../views/${menu.component}`),
    meta: { title: menu.menu_name }
  })
})
```

#### 4. 权限检查

```javascript
// 检查是否有某个权限
const hasPermission = (permission) => {
  const user = JSON.parse(localStorage.getItem('user'))
  return user.permissions.includes(permission)
}

// 在组件中使用
if (hasPermission('queue:call')) {
  // 显示叫号按钮
}
```

---

### 后端集成步骤

#### 1. 登录验证

```java
@PostMapping("/login")
public Result<LoginVO> login(@RequestBody LoginRequest request) {
    // 1. 验证用户名密码
    SysUser user = userService.findByUsername(request.getUsername());
    if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        return Result.error("用户名或密码错误");
    }
    
    // 2. 查询用户角色
    List<SysRole> roles = roleService.findByUserId(user.getId());
    
    // 3. 查询用户菜单权限
    List<SysMenu> menus = menuService.findByUserId(user.getId());
    
    // 4. 生成 JWT Token
    String token = jwtUtil.generateToken(user.getId(), user.getUsername());
    
    // 5. 返回登录信息
    LoginVO vo = new LoginVO();
    vo.setToken(token);
    vo.setUser(user);
    vo.setRoles(roles);
    vo.setMenus(menus);
    
    return Result.success(vo);
}
```

#### 2. 权限拦截器

```java
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 获取 Token
        String token = request.getHeader("Authorization");
        
        // 2. 验证 Token
        Long userId = jwtUtil.getUserId(token);
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }
        
        // 3. 检查权限
        String permission = getRequiredPermission(handler);
        if (permission != null && !hasPermission(userId, permission)) {
            throw new ForbiddenException("没有权限");
        }
        
        return true;
    }
}
```

---

## 🔒 安全建议

### 1. 密码加密

**必须使用 BCrypt 加密**，禁止明文存储！

```java
// Spring Security BCrypt
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// 加密密码
String encodedPassword = passwordEncoder.encode("123456");

// 验证密码
boolean matches = passwordEncoder.matches("123456", encodedPassword);
```

### 2. SQL 注入防护

使用参数化查询：

```java
// ❌ 错误：字符串拼接
String sql = "SELECT * FROM sys_user WHERE username = '" + username + "'";

// ✅ 正确：参数化查询
@Select("SELECT * FROM sys_user WHERE username = #{username}")
SysUser findByUsername(@Param("username") String username);
```

### 3. XSS 防护

对用户输入进行转义：

```java
import org.springframework.web.util.HtmlUtils;

String safeInput = HtmlUtils.htmlEscape(userInput);
```

### 4. JWT Token 安全

- 设置合理的过期时间（建议 2 小时）
- 使用 HTTPS 传输
- Token 中包含用户ID而非敏感信息
- 实现 Token 刷新机制

---

## 📊 性能优化

### 1. 添加索引

已为以下字段添加索引：
- `sys_user.username` - 登录查询
- `sys_user.phone` - 手机号查询
- `sys_user_role.user_id` - 用户角色查询
- `sys_role_menu.role_id` - 角色菜单查询

### 2. 缓存策略

```java
// 使用 Redis 缓存用户权限
@Cacheable(value = "user:permissions", key = "#userId")
public List<SysMenu> getUserPermissions(Long userId) {
    return menuMapper.findByUserId(userId);
}

// 权限变更时清除缓存
@CacheEvict(value = "user:permissions", key = "#userId")
public void updateUserPermissions(Long userId) {
    // ...
}
```

### 3. 分页查询

对于大量数据的查询，务必使用分页：

```java
// 查询操作日志（分页）
Page<SysOperationLog> page = new Page<>(pageNum, pageSize);
operationLogMapper.selectPage(page, wrapper);
```

---

## 🧪 测试数据

### 快速重置测试数据

```sql
-- 清空所有数据
DELETE FROM sys_operation_log;
DELETE FROM sys_role_menu;
DELETE FROM sys_user_role;
DELETE FROM sys_menu;
DELETE FROM sys_role;
DELETE FROM sys_user;

-- 重新插入初始数据
-- （执行 auth_system.sql 中的 INSERT 语句）
```

---

## 📞 常见问题

### Q1: 如何添加新角色？

```sql
-- 1. 添加角色
INSERT INTO sys_role (role_code, role_name, description) 
VALUES ('SUPERVISOR', '督导', '可以查看所有店铺数据');

-- 2. 分配菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES 
(5, 1), (5, 2), (5, 7), (5, 9), (5, 10);

-- 3. 分配给用户
INSERT INTO sys_user_role (user_id, role_id) VALUES (5, 5);
```

### Q2: 如何修改用户角色？

```sql
-- 删除旧角色
DELETE FROM sys_user_role WHERE user_id = 1 AND role_id = 1;

-- 添加新角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 2);
```

### Q3: 如何禁用某个菜单？

```sql
UPDATE sys_menu SET status = 0 WHERE menu_code = 'queue:call';
```

### Q4: 如何记录操作日志？

```java
@Aspect
@Component
public class OperationLogAspect {
    
    @AfterReturning("@annotation(operationLog)")
    public void logOperation(JoinPoint joinPoint, OperationLog operationLog) {
        // 记录日志到 sys_operation_log 表
    }
}
```

---

## 📚 参考资料

- [RBAC 权限模型详解](https://en.wikipedia.org/wiki/Role-based_access_control)
- [Spring Security 官方文档](https://spring.io/projects/spring-security)
- [JWT 最佳实践](https://jwt.io/introduction)
- [MySQL 索引优化](https://dev.mysql.com/doc/refman/8.0/en/optimization-indexes.html)

---

**最后更新**: 2026-05-18  
**维护者**: Lingma AI Assistant

