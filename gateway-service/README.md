# 🚪 Gateway Service - API 网关服务

## 📋 服务说明

**端口**: 8080  
**服务名**: gateway-service  
**功能**: 统一入口、路由转发、权限验证、日志记录

---

## 🎯 核心功能

### 1. **统一路由转发**
- 所有请求通过网关访问后端微服务
- 基于 Eureka 服务发现自动负载均衡
- 支持路径匹配和动态路由

### 2. **JWT Token 认证**
- 全局认证过滤器验证 Token
- 白名单机制（登录、注册等无需 Token）
- 提取用户信息并传递到下游服务

### 3. **跨域处理（CORS）**
- 全局跨域配置
- 支持所有 HTTP 方法
- 允许携带凭证

### 4. **请求日志记录**
- 记录每个请求的详细信息
- 生成唯一请求ID
- 统计请求耗时

### 5. **全局异常处理**
- 统一错误响应格式
- 友好的错误提示
- 完整的异常日志

---

## 🚀 启动步骤

### 1. 确保依赖服务已启动

```bash
# Eureka Server
cd eureka-server
mvn spring-boot:run

# Redis (可选，用于限流)
redis-server
```

### 2. 启动网关服务

```bash
cd gateway-service
mvn spring-boot:run
```

### 3. 验证启动

访问：**http://localhost:8080**

在 Eureka 控制台查看：**http://localhost:8761**

应该看到 `GATEWAY-SERVICE` 已注册。

---

## 📊 路由配置

### 当前配置的路由规则

| 路由 ID | 路径前缀 | 目标服务 | 说明 |
|---------|----------|----------|------|
| user-service | /api/auth/** | lb://user-service | 用户认证服务 |
| order-service | /api/orders/** | lb://order-service | 订单服务 |
| shop-service | /api/shops/** | lb://shop-service | 店铺服务 |
| queue-service | /api/queues/** | lb://queue-service | 排队服务 |
| payment-service | /api/payments/** | lb://payment-service | 支付服务 |
| notification-service | /api/notifications/** | lb://notification-service | 通知服务 |

### 添加新路由

编辑 `application.yml`：

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 新增路由
        - id: new-service
          uri: lb://new-service
          predicates:
            - Path=/api/new/**
          filters:
            - StripPrefix=0
```

---

## 🔐 认证机制

### 白名单路径（无需 Token）

以下路径不需要 JWT Token 即可访问：

```yaml
security:
  whitelist:
    - /api/auth/login        # 登录接口
    - /api/auth/register     # 注册接口
    - /doc.html              # API 文档
    - /swagger-ui/**         # Swagger UI
    - /v3/api-docs/**        # OpenAPI 文档
    - /actuator/**           # 监控端点
```

### Token 验证流程

```
客户端请求
    ↓
网关接收
    ↓
检查是否在白名单？
    ├─ 是 → 直接转发到下游服务
    └─ 否 → 继续验证
         ↓
    检查 Authorization 头？
         ├─ 无 → 返回 401 Unauthorized
         └─ 有 → 继续验证
              ↓
         验证 Token 有效性？
              ├─ 无效 → 返回 401 Unauthorized
              └─ 有效 → 继续
                   ↓
              提取用户信息
                   ↓
              添加到请求头
              - X-User-Id
              - X-Username
              - X-User-Role
                   ↓
              转发到下游服务
```

---

## 🧪 测试示例

### 1. 登录获取 Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": { ... },
    "roles": [ ... ],
    "menus": [ ... ]
  }
}
```

### 2. 使用 Token 访问受保护接口

```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# 获取用户菜单
curl http://localhost:8080/api/auth/menus \
  -H "Authorization: Bearer $TOKEN"

# 查询订单
curl http://localhost:8080/api/orders/list \
  -H "Authorization: Bearer $TOKEN"
```

### 3. 不带 Token 访问（应返回 401）

```bash
curl http://localhost:8080/api/orders/list
```

**响应**: `401 Unauthorized`

---

## 📁 项目结构

```
gateway-service/
├── src/main/java/org/example/gatewayservice/
│   ├── GatewayServiceApplication.java      # ✨ 启动类
│   ├── config/
│   │   └── GlobalExceptionHandler.java     # ✨ 全局异常处理器
│   ├── filter/
│   │   ├── AuthGlobalFilter.java           # ✨ 认证过滤器（核心）
│   │   └── LoggingGlobalFilter.java        # ✨ 日志过滤器
│   └── util/
│       └── JwtUtil.java                    # ✨ JWT 工具类
├── src/main/resources/
│   └── application.yml                     # ✨ 配置文件
└── pom.xml                                 # ✨ Maven 配置
```

---

## 🔧 技术栈

- **Spring Cloud Gateway** - 响应式 API 网关
- **Eureka Client** - 服务发现与注册
- **JWT (jjwt)** - Token 验证
- **Redis Reactive** - 限流（可选）
- **Project Reactor** - 响应式编程
- **Lombok** - 简化代码

---

## 📊 监控与管理

### Actuator 端点

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 查看所有端点
curl http://localhost:8080/actuator

# 网关路由信息
curl http://localhost:8080/actuator/gateway/routes
```

### 日志输出

网关会记录每个请求的详细信息：

```
========== 请求开始 [REQ-123456] ==========
时间: 2026-05-18 23:12:00.123
方法: GET
路径: /api/orders/list
客户端IP: 127.0.0.1
参数: {page=[1], size=[10]}
✅ Token 验证通过 - 用户ID: 4, 用户名: admin, 角色: ADMIN, 路径: /api/orders/list
========== 请求结束 [REQ-123456] ==========
耗时: 45 ms
状态码: 200 OK
```

---

## ⚙️ 配置说明

### 关键配置项

```yaml
# 网关端口
server:
  port: 8080

# 启用服务发现
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true

# JWT 密钥（生产环境必须修改）
jwt:
  secret: catering-ordering-system-jwt-secret-key-2026

# 白名单配置
security:
  whitelist:
    - /api/auth/login
    - /api/auth/register
```

---

## 🛡️ 安全建议

### 1. 修改 JWT 密钥

生产环境必须使用更复杂的密钥：

```yaml
jwt:
  secret: your-super-secret-key-at-least-32-characters-long
```

### 2. 启用 HTTPS

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: your-password
```

### 3. 配置限流

使用 Redis 实现限流（需安装 Redis）：

```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 10
            redis-rate-limiter.burstCapacity: 20
```

### 4. 隐藏敏感信息

不要在日志中输出 Token 或密码：

```yaml
logging:
  level:
    org.springframework.cloud.gateway: INFO  # 降低日志级别
```

---

## 🔍 常见问题

### Q1: 网关返回 401 Unauthorized

**原因**: 
- Token 未提供或格式错误
- Token 已过期
- Token 签名无效

**解决**: 
- 检查请求头是否包含 `Authorization: Bearer <token>`
- 重新登录获取新 Token
- 确认 JWT 密钥配置正确

### Q2: 无法连接到下游服务

**原因**:
- 下游服务未启动
- Eureka 注册失败
- 服务名配置错误

**解决**:
- 检查 Eureka 控制台确认服务已注册
- 检查 `application.yml` 中的路由配置
- 查看网关日志中的错误信息

### Q3: 跨域问题

**解决**: 
网关已配置全局 CORS，如果仍有问题，检查：

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"  # 改为具体的域名
```

---

## 📈 性能优化

### 1. 启用连接池

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          type: ELASTIC
          max-connections: 1000
```

### 2. 配置超时

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 3000
        response-timeout: 10s
```

### 3. 启用压缩

```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,text/plain
```

---

## 📞 扩展功能

### 添加限流过滤器

```java
@Component
public class RateLimitFilter implements GlobalFilter {
    // 实现限流逻辑
}
```

### 添加灰度发布

```java
@Component
public class CanaryFilter implements GlobalFilter {
    // 根据用户ID或版本分配流量
}
```

### 添加请求缓存

```java
@Component
public class CacheFilter implements GlobalFilter {
    // 缓存 GET 请求响应
}
```

---

## 🎉 总结

✅ **完整的 API 网关**
- 统一路由转发
- JWT Token 认证
- 全局异常处理
- 请求日志记录

✅ **开箱即用**
- 编译成功
- 配置完整
- 6个微服务路由已配置

✅ **生产就绪**
- 跨域处理
- 监控端点
- 安全建议
- 性能优化

---

**最后更新**: 2026-05-18  
**维护者**: Lingma AI Assistant
