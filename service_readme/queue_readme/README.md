# Queue Service - 排队服务

## 📋 服务说明

**服务名称**: queue-service  
**服务端口**: 8085  
**API基础路径**: `/api/queue`  
**数据库表**: queue_record  

---

## 🎯 核心功能

1. **排队取号**: 用户在线取号
2. **排队管理**: 查看排队状态、取消排队
3. **叫号管理**: 商家叫号、跳过、完成
4. **店铺关联**: 调用shop-service验证店铺
5. **实时通知**: WebSocket推送排队状态更新

---

## 🔧 技术栈

- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- MyBatis Plus 3.5.5
- MySQL 8.3.0
- Redis (缓存)
- WebSocket (实时通知)
- Knife4j (API文档)
- OpenFeign (服务间调用)

---

## 🗂️ 项目结构

```
queue-service/
├── src/main/java/org/example/queueservice/
│   ├── QueueServiceApplication.java    # 启动类
│   ├── common/                         # 公共类
│   ├── config/                         # 配置类
│   ├── controller/                     # 控制器
│   │   └── QueueRecordController.java  # 排队记录接口
│   ├── dto/                            # 数据传输对象
│   ├── entity/                         # 实体类
│   │   └── QueueRecord.java            # 排队记录实体
│   ├── feign/                          # Feign客户端
│   │   ├── ShopFeignClient.java        # 店铺服务客户端
│   │   └── ShopFeignClientFallback.java
│   ├── mapper/                         # Mapper接口
│   ├── service/                        # 服务层
│   └── util/                           # 工具类
└── pom.xml                             # Maven配置
```

---

## 🔥 核心特性

### 1. 服务间调用 ✅

#### ShopFeignClient
```java
@FeignClient(name = "shop-service", path = "/api/shop", fallback = ShopFeignClientFallback.class)
public interface ShopFeignClient {
    @GetMapping("/{id}")
    Result<ShopInfoDTO> getShopById(@PathVariable("id") Long id);
}
```

**功能**:
- 验证店铺是否存在
- 验证店铺是否营业中
- 熔断器保护：100%

### 2. 排队算法 ✅

- 自动分配排队号码
- 支持优先级排队
- 实时计算预计等待时间

### 3. WebSocket实时通知 ✅

- 排队状态变更推送
- 叫号通知
- 无需前端轮询

---

## 📝 API接口概览

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取所有排队记录 | GET | /api/queue/list | 获取排队列表 |
| 根据ID获取排队记录 | GET | /api/queue/{id} | 获取详情 |
| 根据店铺获取排队记录 | GET | /api/queue/shop/{shopId} | 店铺排队列表 |
| 根据用户获取排队记录 | GET | /api/queue/user/{userId} | 用户排队列表 |
| **取号** | POST | /api/queue | ⭐核心接口 |
| 取消排队 | PUT | /api/queue/{id}/cancel | 取消排队 |
| 叫号 | PUT | /api/queue/{id}/call | 商家叫号 |
| 完成排队 | PUT | /api/queue/{id}/complete | 完成排队 |
| 跳过 | PUT | /api/queue/{id}/skip | 跳过当前号码 |

---

## 🗄️ 数据库设计

### queue_record表

```sql
CREATE TABLE `queue_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '排队ID',
  `queue_no` varchar(50) NOT NULL COMMENT '排队号码',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) DEFAULT NULL COMMENT '用户姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `party_size` int NOT NULL COMMENT '就餐人数',
  `queue_status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-等待中，1-已叫号，2-已完成，3-已取消',
  `priority` tinyint DEFAULT '0' COMMENT '优先级：0-普通，1-VIP',
  `estimated_wait_time` int DEFAULT NULL COMMENT '预计等待时间（分钟）',
  `actual_wait_time` int DEFAULT NULL COMMENT '实际等待时间（分钟）',
  `call_time` datetime DEFAULT NULL COMMENT '叫号时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_queue_no` (`queue_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_queue_status` (`queue_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排队记录表';
```

---

## 🚀 启动指南

### 1. 前置条件
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Eureka Server (必须)
- Shop Service (可选)

### 2. 配置修改

编辑 `src/main/resources/application.yml`:

```yaml
server:
  port: 8085

spring:
  application:
    name: queue-service
  datasource:
    url: jdbc:mysql://localhost:3306/catering_order?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### 3. 启动步骤

```bash
mvn clean package -DskipTests
java -jar target/queue-service-1.0-SNAPSHOT.jar
```

### 4. 验证启动

访问API文档: http://localhost:8085/doc.html

---

## 📊 监控与日志

### 日志配置
- 日志框架: Logback
- 日志级别: INFO
- 日志文件: logs/queue-service.log

### 健康检查
- Actuator端点: http://localhost:8085/actuator/health

---

## 🔐 安全说明

### 数据安全
- ✅ 排队号码唯一性保证
- ✅ 防止重复取号
- ✅ 所有操作都有日志记录

### 服务间调用安全
- ✅ ShopFeignClient有熔断器保护
- ✅ 服务不可用时提供友好提示

---

## 📚 相关文档

- [API测试文档](API_TEST.md) - 待创建
- [代码恢复指南](../service_readme/CODE_RECOVERY_GUIDE.md)

---

## 🔄 版本历史

### v1.0 (当前版本)
- 基础排队管理功能
- ShopFeignClient集成
- WebSocket实时通知

---

**维护者**: 开发团队  
**最后更新**: 2026-05-18  
**文档版本**: v1.0
