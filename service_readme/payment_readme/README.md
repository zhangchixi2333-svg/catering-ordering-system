# Payment Service - 支付服务

## 📋 服务说明

**服务名称**: payment-service  
**服务端口**: 8084  
**API基础路径**: `/api/payment`  
**数据库表**: payment_order  

---

## 🎯 核心功能

1. **支付订单管理**: 创建、查询、更新支付记录
2. **支付状态管理**: 待支付→支付中→支付成功/失败→已退款
3. **订单验证**: 调用order-service验证订单信息
4. **金额安全**: 从订单获取支付金额，防止篡改
5. **防重复支付**: 检查订单是否已支付

---

## 🔧 技术栈

- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- MyBatis Plus 3.5.5
- MySQL 8.3.0
- RabbitMQ (消息队列)
- Knife4j (API文档)
- OpenFeign (服务间调用)

---

## 📦 依赖说明

### 核心依赖
- spring-boot-starter-web: Web服务
- spring-boot-starter-validation: 参数验证
- mybatis-plus-spring-boot3-starter: ORM框架
- mysql-connector-j: MySQL驱动
- druid-spring-boot-3-starter: 数据库连接池
- spring-cloud-starter-openfeign: 服务间调用

### 中间件
- spring-boot-starter-amqp: RabbitMQ客户端

### 工具类
- knife4j-openapi3-jakarta-spring-boot-starter: API文档
- hutool-all: Java工具类库
- lombok: 代码简化

---

## 🗂️ 项目结构

```
payment-service/
├── src/main/java/org/example/paymentservice/
│   ├── PaymentServiceApplication.java    # 启动类
│   ├── common/                           # 公共类
│   │   └── Result.java                   # 统一返回结果
│   ├── config/                           # 配置类
│   │   ├── GlobalExceptionHandler.java   # 全局异常处理
│   │   └── ...
│   ├── controller/                       # 控制器
│   │   └── PaymentOrderController.java   # 支付订单接口
│   ├── dto/                              # 数据传输对象
│   │   └── PaymentOrderCreateRequest.java # 支付创建请求（已优化）
│   ├── entity/                           # 实体类
│   │   └── PaymentOrder.java             # 支付订单实体
│   ├── feign/                            # Feign客户端
│   │   ├── OrderFeignClient.java         # 订单服务客户端
│   │   └── OrderFeignClientFallback.java
│   ├── mapper/                           # Mapper接口
│   │   └── PaymentOrderMapper.java
│   ├── service/                          # 服务层
│   │   ├── PaymentOrderService.java
│   │   └── impl/
│   │       └── PaymentOrderServiceImpl.java
│   └── util/                             # 工具类
│       └── PaymentNoGenerator.java       # 支付单号生成器
├── src/main/resources/
│   ├── application.yml                   # 配置文件
│   └── mapper/                           # MyBatis XML
│       └── PaymentOrderMapper.xml
└── pom.xml                               # Maven配置
```

---

## 🔥 核心优化（v2.0）

### 1. DTO重构 ✅

**PaymentOrderCreateRequest** 已精简为10个必要字段：

```java
{
  "orderNo": "ORD2026051700001",  // ✅ 保留：订单编号
  "orderId": null,                 // ✅ 保留：订单ID（可选）
  "userId": 1001,                  // ✅ 保留：用户ID
  "paymentMethod": 1,              // ✅ 保留：支付方式
  "currency": "CNY",               // ✅ 保留：货币类型
  "subject": "美味餐厅订单支付",     // ✅ 保留：支付主题
  "body": "宫保鸡丁等3件商品",      // ✅ 保留：支付描述
  "clientIp": "192.168.1.100",     // ✅ 保留：客户端IP
  "deviceInfo": "iOS",             // ✅ 保留：设备信息
  "notifyUrl": null                // ✅ 保留：异步通知地址
}
```

**删除的2个字段**:
- ❌ paymentAmount - 改由服务端从order-service获取
- ❌ shopId - 改由服务端从order-service获取

**优势**:
- 🔒 安全性提升：防止客户端篡改支付金额
- 🎯 准确性提升：使用订单实际金额
- 🛡️ 防重复支付：服务端验证订单支付状态

### 2. 服务间调用 ✅

创建了OrderFeignClient，实现与订单服务的交互：

#### OrderFeignClient
```java
@FeignClient(name = "order-service", path = "/api/order", fallback = OrderFeignClientFallback.class)
public interface OrderFeignClient {
    @GetMapping("/no/{orderNo}")
    Result<OrderInfoDTO> getOrderByOrderNo(@PathVariable("orderNo") String orderNo);
}
```

**功能**:
- 验证订单是否存在
- 获取订单金额（actualAmount）
- 获取订单店铺ID（shopId）
- 检查订单支付状态
- 熔断器保护：100%

### 3. Controller优化 ✅

PaymentOrderController添加了完整的服务间调用逻辑：

```java
@PostMapping
public Result<Boolean> createPayment(@RequestBody @Valid PaymentOrderCreateRequest request) {
    // 1. 调用order-service验证订单是否存在
    Result<OrderInfoDTO> orderResult = orderFeignClient.getOrderByOrderNo(request.getOrderNo());
    
    if (orderResult == null || orderResult.getData() == null) {
        return Result.error("订单不存在");
    }
    
    OrderInfoDTO orderInfo = orderResult.getData();
    
    // 2. 验证订单是否已支付
    if (orderInfo.getPaymentStatus() != null && orderInfo.getPaymentStatus() == 1) {
        return Result.error("订单已支付，请勿重复支付");
    }
    
    // 3. 创建支付订单（使用订单的金额和店铺ID）
    PaymentOrder payment = new PaymentOrder();
    BeanUtils.copyProperties(request, payment);
    payment.setPaymentNo(PaymentNoGenerator.generate());
    payment.setPaymentAmount(orderInfo.getActualAmount());  // ✅ 从订单获取
    payment.setShopId(orderInfo.getShopId());               // ✅ 从订单获取
    payment.setPaymentStatus(0);  // 默认待支付
    payment.setCurrency("CNY");   // 默认人民币
    
    boolean success = paymentOrderService.save(payment);
    
    return Result.success(true);
}
```

---

## 📝 API接口概览

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取所有支付订单 | GET | /api/payment/list | 获取支付列表 |
| 根据ID获取 | GET | /api/payment/{id} | 获取支付详情 |
| 根据支付单号获取 | GET | /api/payment/no/{paymentNo} | 通过支付单号查询 |
| 根据订单号获取 | GET | /api/payment/order/{orderNo} | 通过订单号查询 |
| 根据店铺获取 | GET | /api/payment/shop/{shopId} | 店铺支付列表 |
| 根据用户获取 | GET | /api/payment/user/{userId} | 用户支付列表 |
| 根据状态获取 | GET | /api/payment/status/{status} | 状态支付列表 |
| **创建支付订单** | POST | /api/payment | ⭐核心接口（已优化） |
| 更新支付订单 | PUT | /api/payment | 更新支付信息 |
| 删除支付订单 | DELETE | /api/payment/{id} | 删除支付订单 |
| 更新支付状态 | PUT | /api/payment/{id}/status | 更新支付状态 |

详细API文档见: [API_TEST.md](API_TEST.md)

---

## 🗄️ 数据库设计

### payment_order表

```sql
CREATE TABLE `payment_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付ID',
  `payment_no` varchar(50) NOT NULL COMMENT '支付单号',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `payment_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `payment_method` tinyint NOT NULL COMMENT '支付方式：1-微信，2-支付宝，3-现金，4-会员卡，5-银行卡',
  `payment_status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态：0-待支付，1-支付中，2-支付成功，3-支付失败，4-已退款',
  `currency` varchar(10) NOT NULL DEFAULT 'CNY' COMMENT '货币类型',
  `subject` varchar(200) DEFAULT NULL COMMENT '支付主题',
  `body` varchar(500) DEFAULT NULL COMMENT '支付描述',
  `transaction_id` varchar(100) DEFAULT NULL COMMENT '第三方支付交易号',
  `client_ip` varchar(50) DEFAULT NULL COMMENT '客户端IP',
  `device_info` varchar(200) DEFAULT NULL COMMENT '设备信息',
  `notify_url` varchar(500) DEFAULT NULL COMMENT '异步通知地址',
  `return_url` varchar(500) DEFAULT NULL COMMENT '同步返回地址',
  `extra_params` text COMMENT '扩展参数',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `refund_reason` varchar(500) DEFAULT NULL COMMENT '退款原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';
```

---

## 🚀 启动指南

### 1. 前置条件
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- RabbitMQ (可选)
- Eureka Server (必须)
- Order Service (必须)

### 2. 配置修改

编辑 `src/main/resources/application.yml`:

```yaml
server:
  port: 8084

spring:
  application:
    name: payment-service
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
# 1. 编译项目
mvn clean package -DskipTests

# 2. 运行服务
java -jar target/payment-service-1.0-SNAPSHOT.jar

# 或者使用Maven插件
mvn spring-boot:run
```

### 4. 验证启动

访问API文档: http://localhost:8084/doc.html

---

## 🧪 测试指南

详见: [API_TEST.md](API_TEST.md)

### 快速测试

```bash
# 创建支付订单
curl -X POST "http://localhost:8084/api/payment" \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD2026051700001",
    "userId": 1001,
    "paymentMethod": 1,
    "subject": "测试支付",
    "clientIp": "192.168.1.100"
  }'

# 获取支付列表
curl -X GET "http://localhost:8084/api/payment/list"
```

---

## 📊 监控与日志

### 日志配置
- 日志框架: Logback
- 日志级别: INFO
- 日志文件: logs/payment-service.log

### 健康检查
- Actuator端点: http://localhost:8084/actuator/health
- 指标监控: http://localhost:8084/actuator/metrics

---

## 🔐 安全说明

### 支付安全
- ✅ 支付金额从order-service获取，防止篡改
- ✅ 防止重复支付，检查订单支付状态
- ✅ 所有支付操作都有日志记录
- ✅ 支持多种支付方式

### 服务间调用安全
- ✅ OrderFeignClient有熔断器保护
- ✅ 服务不可用时提供友好提示
- ✅ 详细的错误日志便于排查
- ✅ 确保支付金额与订单金额一致

---

## 📚 相关文档

- [API测试文档](API_TEST.md)
- [DTO设计文档](DTO_DESIGN.md) - 待创建
- [Windows测试指南](WINDOWS_TEST.md) - 待创建
- [代码恢复指南](../service_readme/CODE_RECOVERY_GUIDE.md)
- [编译检查报告](../service_readme/COMPILATION_CHECK_REPORT.md)

---

## 🔄 版本历史

### v2.0 (2026-05-18)
- ✅ PaymentOrderCreateRequest重构（删除2个冗余字段）
- ✅ 添加OrderFeignClient
- ✅ Controller添加订单验证逻辑
- ✅ 100%熔断器覆盖
- ✅ 完善API文档
- ✅ 防止重复支付

### v1.0 (初始版本)
- 基础支付订单管理功能
- 简单的CRUD操作

---

**维护者**: 开发团队  
**最后更新**: 2026-05-18  
**文档版本**: v2.0
