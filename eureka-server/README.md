# Eureka Server - 服务注册中心

## 📋 服务说明

**端口**: 8761  
**功能**: 服务注册与发现中心  
**访问地址**: http://localhost:8761

---

## 🚀 启动步骤

### 1. 启动 Eureka Server

```cmd
cd eureka-server
mvn spring-boot:run
```

或在 IDEA 中运行 `EurekaServerApplication.java`

### 2. 验证启动

访问：http://localhost:8761

看到 Eureka 管理界面表示启动成功。

---

## 📊 Eureka 管理界面

启动后访问 http://localhost:8761 可以看到：

- **Instances currently registered with Eureka**: 已注册的服务列表
- **General Info**: Eureka 服务器信息
- **Instance Info**: 实例信息

---

## 🔧 配置说明

### 核心配置

```yaml
eureka:
  client:
    # 不注册自己
    register-with-eureka: false
    # 不获取服务列表
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
  server:
    # 关闭自我保护（开发环境）
    enable-self-preservation: false
```

---

## 📡 客户端注册

其他微服务需要添加以下配置才能注册到 Eureka：

### 1. 添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### 2. 配置文件

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${server.port}
```

### 3. 启动类注解

```java
@SpringBootApplication
@EnableDiscoveryClient
public class Application {
    // ...
}
```

---

## 🎯 已注册服务

当前已配置的服务：

| 服务名称 | 端口 | 说明 |
|---------|------|------|
| eureka-server | 8761 | 服务注册中心 |
| shop-service | 8081 | 店铺服务 |

---

## 🐛 常见问题

### 1. 服务未显示在 Eureka 界面

**原因**: 
- 客户端未正确配置
- Eureka Server 未启动

**解决**:
- 检查 `eureka.client.service-url.defaultZone` 配置
- 确认 Eureka Server 已启动

### 2. 服务状态为 DOWN

**原因**: 
- 健康检查失败
- 服务启动异常

**解决**:
- 检查服务日志
- 确认服务正常运行

### 3. 服务注册延迟

**说明**: Eureka 默认有 30 秒的注册延迟，这是正常现象。

**调整**:
```yaml
eureka:
  instance:
    lease-renewal-interval-in-seconds: 10  # 心跳间隔
  client:
    registry-fetch-interval-seconds: 5     # 获取注册表间隔
```

---

## 📚 相关文档

- [Eureka 官方文档](https://github.com/Netflix/eureka)
- [Spring Cloud Netflix](https://spring.io/projects/spring-cloud-netflix)

---

**最后更新**: 2026-05-17
