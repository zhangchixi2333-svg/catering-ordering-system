# 👤 User Service - 用户服务

## 📋 服务说明

**端口**: 8087  
**服务名**: user-service  
**功能**: 用户认证授权、JWT Token 管理、权限查询

---

## 🚀 启动步骤

### 1. 确保依赖服务已启动

- ✅ Eureka Server (8761)
- ✅ MySQL (3306) - 数据库 `catering_auth`

### 2. 执行数据库脚本

```bash
mysql -u root -p < sql/auth_system.sql
```

### 3. 启动服务

```bash
cd user-service
mvn spring-boot:run
```

### 4. 访问 OpenAPI 文档

浏览器打开：**http://localhost:8087/doc.html**

---

## 🎯 核心接口

### 1. 用户登录

**接口**: `POST /api/auth/login`

**请求体**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 4,
      "username": "admin",
      "nickname": "管理员",
      "phone": "13800138004",
      "email": "admin@example.com",
      "avatar": "https://ui-avatars.com/api/?name=admin"
    },
    "roles": [
      {
        "id": 4,
        "roleCode": "ADMIN",
        "roleName": "超级管理员",
        "description": "拥有系统所有权限"
      }
    ],
    "menus": [
      {
        "id": 1,
        "parentId": 0,
        "menuName": "首页",
        "menuCode": "dashboard",
        "menuType": 2,
        "path": "/dashboard",
        "component": "DashboardView.vue",
        "icon": "🏠",
        "permission": "dashboard:view"
      }
      // ... 更多菜单
    ]
  }
}
```

---

### 2. 验证 Token

**接口**: `GET /api/auth/validate`

**请求头**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "userId": 4,
    "username": "admin",
    "role": "ADMIN",
    "valid": true
  }
}
```

---

### 3. 获取用户菜单

**接口**: `GET /api/auth/menus`

**请求头**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "menuName": "首页",
      "path": "/dashboard",
      "component": "DashboardView.vue",
      "icon": "🏠",
      "permission": "dashboard:view"
    }
    // ... 更多菜单
  ]
}
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

## 📁 项目结构

```
user-service/
├── src/main/java/org/example/userservice/
│   ├── UserServiceApplication.java      # 启动类
│   ├── common/
│   │   └── Result.java                  # 统一返回结果
│   ├── controller/
│   │   └── AuthController.java          # 认证控制器
│   ├── dto/
│   │   ├── LoginRequest.java            # 登录请求
│   │   └── LoginVO.java                 # 登录响应
│   └── util/
│       └── JwtUtil.java                 # JWT 工具类
├── src/main/resources/
│   └── application.yml                  # 配置文件
└── pom.xml                              # Maven 配置
```

---

## 🔧 技术栈

- Spring Boot 3.1.5
- Spring Cloud (Eureka Client)
- MyBatis Plus 3.5.5
- JWT (jjwt 0.11.5)
- BCrypt 密码加密
- Knife4j OpenAPI 3

---

## 📊 Eureka 注册

启动成功后，在 Eureka 控制台（http://localhost:8761）可以看到：

```
USER-SERVICE  UP (1)
  - user-service:8087
```

---

## ⚠️ 注意事项

1. **密码加密**: 当前为演示版本使用明文，生产环境必须使用 BCrypt 加密
2. **Token 过期**: 默认 2 小时，可在 `application.yml` 中修改
3. **JWT Secret**: 生产环境必须修改为更复杂的密钥
4. **数据库连接**: 确保 MySQL 已启动且数据库 `catering_auth` 存在

---

## 🔍 调试技巧

### 查看日志

```bash
# 控制台会输出详细日志
========== 用户登录 ==========
用户名: admin
✅ 登录成功 - 用户ID: 4, 角色: ADMIN
==========================================
```

### 测试 API

```bash
# 登录
curl -X POST http://localhost:8087/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 保存 Token
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# 验证 Token
curl http://localhost:8087/api/auth/validate \
  -H "Authorization: Bearer $TOKEN"

# 获取菜单
curl http://localhost:8087/api/auth/menus \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📞 常见问题

### Q1: Token 无效怎么办？
A: 检查是否添加了 `Bearer ` 前缀，确认 Token 未过期。

### Q2: 如何修改 Token 过期时间？
A: 编辑 `application.yml` 中的 `jwt.expiration`（单位：毫秒）。

### Q3: 登录失败提示"用户名或密码错误"？
A: 确认数据库中已有测试数据，执行 `auth_system.sql` 脚本。

---

**最后更新**: 2026-05-18  
**维护者**: Lingma AI Assistant
