# 依赖版本管理说明

## 📋 版本兼容性说明

### Spring Boot & Spring Cloud 兼容版本

本项目采用以下兼容的版本组合：

| 组件 | 版本 | 说明 |
|------|------|------|
| **Spring Boot** | 3.2.5 | 最新稳定版 |
| **Spring Cloud** | 2023.0.1 (Leyton) | 与Spring Boot 3.2.x兼容 |
| **Spring Cloud Alibaba** | 2023.0.1.0 | 与Spring Cloud 2023.0.x兼容 |
| **Java** | 17 | LTS长期支持版本 |

### 版本对应关系

```
Spring Boot 3.2.x  ←→  Spring Cloud 2023.0.x (Leyton)
                                    ↓
                    Spring Cloud Alibaba 2023.0.x
```

**参考官方兼容性矩阵**：
- [Spring Cloud Release Train](https://spring.io/projects/spring-cloud)
- [Spring Cloud Alibaba Version](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)

---

## 📦 核心依赖清单

### 1. Spring生态

| 依赖 | 版本 | 用途 |
|------|------|------|
| spring-boot-dependencies | 3.2.5 | Spring Boot核心框架 |
| spring-cloud-dependencies | 2023.0.1 | Spring Cloud微服务框架 |
| spring-cloud-alibaba-dependencies | 2023.0.1.0 | 阿里云微服务组件 |

### 2. 数据库相关

| 依赖 | 版本 | 用途 |
|------|------|------|
| mysql-connector-j | 8.3.0 | MySQL数据库驱动 |
| mybatis-plus-spring-boot3-starter | 3.5.5 | MyBatis增强工具（适配Spring Boot 3） |
| druid-spring-boot-3-starter | 1.2.21 | 阿里巴巴数据库连接池 |

### 3. 中间件

| 依赖 | 版本 | 用途 |
|------|------|------|
| spring-boot-starter-data-redis | 3.2.5 | Redis缓存 |
| spring-boot-starter-amqp | 3.2.5 | RabbitMQ消息队列 |

### 4. 工具类

| 依赖 | 版本 | 用途 |
|------|------|------|
| lombok | 1.18.30 | 简化Java代码 |
| hutool-all | 5.8.25 | Java工具类库 |
| fastjson2 | 2.0.47 | JSON处理（FastJSON升级版） |
| commons-lang3 | 3.14.0 | Apache通用工具 |

### 5. API文档

| 依赖 | 版本 | 用途 |
|------|------|------|
| knife4j-openapi3-jakarta-spring-boot-starter | 4.4.0 | Swagger增强版API文档 |

---

## 🔧 使用说明

### 1. 父项目管理版本

在父项目 `pom.xml` 中，通过 `<dependencyManagement>` 统一管理所有依赖版本：

```xml
<dependencyManagement>
    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        
        <!-- 其他依赖... -->
    </dependencies>
</dependencyManagement>
```

### 2. 子模块引用依赖

子模块只需指定 `groupId` 和 `artifactId`，无需指定版本：

```xml
<dependencies>
    <!-- Web starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    
    <!-- MyBatis Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>
</dependencies>
```

### 3. 添加新子模块

在父项目 `pom.xml` 的 `<modules>` 中添加：

```xml
<modules>
    <module>shop-service</module>
    <module>menu-service</module>
    <!-- 更多模块... -->
</modules>
```

---

## 📝 各微服务推荐依赖

### shop-service / menu-service（基础服务）

```xml
<dependencies>
    <!-- Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Nacos 注册发现 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    
    <!-- Nacos 配置中心 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    </dependency>
    
    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    
    <!-- MyBatis Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>
    
    <!-- Druid -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-3-starter</artifactId>
    </dependency>
    
    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- Knife4j -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### order-service（订单服务）

```xml
<dependencies>
    <!-- 基础依赖同上 -->
    
    <!-- RabbitMQ -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    
    <!-- Sentinel 流控 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    </dependency>
</dependencies>
```

### payment-service（支付服务）

```xml
<dependencies>
    <!-- 基础依赖同上 -->
    
    <!-- Sentinel 流控熔断 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    </dependency>
</dependencies>
```

### queue-service（排队服务）

```xml
<dependencies>
    <!-- 基础依赖同上 -->
    
    <!-- Redis（核心依赖） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
</dependencies>
```

### notification-service（通知服务）

```xml
<dependencies>
    <!-- 基础依赖同上 -->
    
    <!-- WebSocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    
    <!-- RabbitMQ -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
</dependencies>
```

### gateway-service（网关服务）

```xml
<dependencies>
    <!-- Gateway -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    
    <!-- Nacos 注册发现 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    
    <!-- Sentinel 网关流控 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    </dependency>
    
    <!-- LoadBalancer 负载均衡 -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
</dependencies>
```

---

## ⚠️ 注意事项

### 1. Spring Boot 3 变化

- **Jakarta EE**: Spring Boot 3 使用 `jakarta.*` 包名，替代了 `javax.*`
- **Java 17+**: 必须使用 Java 17 或更高版本
- **MyBatis Plus**: 需要使用 `mybatis-plus-spring-boot3-starter`

### 2. 版本升级建议

- 升级前先查看[官方发行说明](https://github.com/spring-projects/spring-boot/releases)
- 保持 Spring Boot、Spring Cloud、Spring Cloud Alibaba 版本兼容
- 在测试环境充分测试后再升级到生产环境

### 3. 依赖冲突解决

如果遇到依赖冲突，可以：

```xml
<dependency>
    <groupId>xxx</groupId>
    <artifactId>xxx</artifactId>
    <exclusions>
        <exclusion>
            <groupId>冲突的groupId</groupId>
            <artifactId>冲突的artifactId</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 4. Maven命令

```bash
# 清理并编译
mvn clean compile

# 打包
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests

# 安装到本地仓库
mvn clean install

# 查看依赖树
mvn dependency:tree

# 更新依赖
mvn clean install -U
```

---

## 📚 参考资料

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Cloud 官方文档](https://spring.io/projects/spring-cloud)
- [Spring Cloud Alibaba Wiki](https://github.com/alibaba/spring-cloud-alibaba/wiki)
- [MyBatis Plus 文档](https://baomidou.com/)
- [Knife4j 文档](https://doc.xiaominfo.com/)

---

**最后更新**: 2026-05-17  
**维护者**: 开发团队
