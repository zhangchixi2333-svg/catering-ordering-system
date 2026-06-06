# Notification Service - 通知服务

## 📋 服务说明

**服务名称**: notification-service  
**服务端口**: 8086  
**API基础路径**: `/api/notification`  

---

## 🎯 核心功能

1. **消息推送**: 向用户推送各种通知
2. **多渠道通知**: 支持WebSocket、短信、邮件等
3. **消息模板**: 预定义消息模板
4. **异步处理**: 使用RabbitMQ异步发送消息
5. **消息记录**: 保存所有发送的消息历史

---

## 🔧 技术栈

- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- MySQL 8.3.0
- RabbitMQ (消息队列)
- WebSocket (实时推送)
- Knife4j (API文档)

---

## 🗂️ 项目结构

```
notification-service/
├── src/main/java/org/example/notificationservice/
│   ├── NotificationServiceApplication.java  # 启动类
│   ├── common/                              # 公共类
│   ├── config/                              # 配置类
│   ├── controller/                          # 控制器
│   │   └── NotificationController.java      # 通知接口
│   ├── dto/                                 # 数据传输对象
│   ├── entity/                              # 实体类
│   │   └── Notification.java                # 通知实体
│   ├── mapper/                              # Mapper接口
│   ├── service/                             # 服务层
│   └── consumer/                            # MQ消费者
└── pom.xml                                  # Maven配置
```

---

## 🔥 核心特性

### 1. 消息队列集成 ✅

- 使用RabbitMQ异步处理消息
- 解耦消息生产和消费
- 提高系统吞吐量

### 2. 多渠道通知 ✅

- **WebSocket**: 实时推送（在线用户）
- **短信**: 重要通知（离线用户）
- **邮件**: 营销信息、账单等

### 3. 消息模板 ✅

- 订单创建通知模板
- 支付成功通知模板
- 排队叫号通知模板
- 订单完成通知模板

---

## 📝 API接口概览

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取所有通知 | GET | /api/notification/list | 获取通知列表 |
| 根据ID获取通知 | GET | /api/notification/{id} | 获取详情 |
| 根据用户获取通知 | GET | /api/notification/user/{userId} | 用户通知列表 |
| 发送通知 | POST | /api/notification | 发送新通知 |
| 标记已读 | PUT | /api/notification/{id}/read | 标记为已读 |
| 删除通知 | DELETE | /api/notification/{id} | 删除通知 |

---

## 🗄️ 数据库设计

### notification表

```sql
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `type` tinyint NOT NULL COMMENT '类型：1-订单，2-支付，3-排队，4-系统',
  `channel` tinyint NOT NULL DEFAULT '1' COMMENT '渠道：1-WebSocket，2-短信，3-邮件',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-未读，1-已读',
  `related_id` bigint DEFAULT NULL COMMENT '关联ID（订单ID/排队ID等）',
  `sent_at` datetime DEFAULT NULL COMMENT '发送时间',
  `read_at` datetime DEFAULT NULL COMMENT '阅读时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录表';
```

---

## 🚀 启动指南

### 1. 前置条件
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- RabbitMQ (必须)

### 2. 配置修改

编辑 `src/main/resources/application.yml`:

```yaml
server:
  port: 8086

spring:
  application:
    name: notification-service
  datasource:
    url: jdbc:mysql://localhost:3306/catering_order?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### 3. 启动步骤

```bash
mvn clean package -DskipTests
java -jar target/notification-service-1.0-SNAPSHOT.jar
```

### 4. 验证启动

访问API文档: http://localhost:8086/doc.html

---

## 📊 监控与日志

### 日志配置
- 日志框架: Logback
- 日志级别: INFO
- 日志文件: logs/notification-service.log

### 健康检查
- Actuator端点: http://localhost:8086/actuator/health

---

## 🔐 安全说明

### 数据安全
- ✅ 所有通知都有用户ID关联
- ✅ 敏感信息脱敏处理
- ✅ 消息发送失败重试机制

---

## 📚 相关文档

- [API测试文档](API_TEST.md) - 待创建
- [代码恢复指南](../service_readme/CODE_RECOVERY_GUIDE.md)

---

## 🔄 版本历史

### v1.0 (当前版本)
- 基础通知管理功能
- RabbitMQ集成
- WebSocket实时推送

---

**维护者**: 开发团队  
**最后更新**: 2026-05-18  
**文档版本**: v1.0
