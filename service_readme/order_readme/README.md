# Order Service - 订单服务

## 📋 服务说明

**服务名称**: order-service  
**服务端口**: 8083  
**API基础路径**: `/api/order`  
**数据库表**: orders  

---

## 🎯 核心功能

1. **订单管理**: 创建、查询、更新、删除订单
2. **订单状态管理**: 待支付→待接单→制作中→待取餐→已完成
3. **店铺验证**: 调用shop-service验证店铺状态
4. **排队关联**: 可选关联queue-service的排队号
5. **金额计算**: 服务端自动计算订单总金额和菜品数量

---

## 🔧 技术栈

- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- MyBatis Plus 3.5.5
- MySQL 8.3.0
- Redis (缓存)
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
- spring-boot-starter-data-redis: Redis客户端
- spring-boot-starter-amqp: RabbitMQ客户端

### 工具类
- knife4j-openapi3-jakarta-spring-boot-starter: API文档
- hutool-all: Java工具类库
- lombok: 代码简化

---

## 🗂️ 项目结构

```
order-service/
├── src/main/java/org/example/orderservice/
│   ├── OrderServiceApplication.java    # 启动类
│   ├── common/                         # 公共类
│   │   └── Result.java                 # 统一返回结果
│   ├── config/                         # 配置类
│   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   └── ...
│   ├── controller/                     # 控制器
│   │   └── OrdersController.java       # 订单管理接口
│   ├── dto/                            # 数据传输对象
│   │   └── OrderCreateRequest.java     # 订单创建请求（已优化）
│   ├── entity/                         # 实体类
│   │   └── Orders.java                 # 订单实体
│   ├── feign/                          # Feign客户端
│   │   ├── ShopFeignClient.java        # 店铺服务客户端
│   │   ├── ShopFeignClientFallback.java
│   │   ├── MenuFeignClient.java        # 菜单服务客户端
│   │   ├── MenuFeignClientFallback.java
│   │   ├── QueueFeignClient.java       # 排队服务客户端
│   │   └── QueueFeignClientFallback.java
│   ├── mapper/                         # Mapper接口
│   │   └── OrdersMapper.java
│   ├── service/                        # 服务层
│   │   ├── OrdersService.java
│   │   └── impl/
│   │       └── OrdersServiceImpl.java
│   └── util/                           # 工具类
│       └── OrderNoGenerator.java       # 订单号生成器
├── src/main/resources/
│   ├── application.yml                 # 配置文件
│   └── mapper/                         # MyBatis XML
│       └── OrdersMapper.xml
└── pom.xml                             # Maven配置
```

---

## 🔥 核心优化（v2.0）

### 1. DTO重构 ✅

**OrderCreateRequest** 已精简为6个必要字段：

```java
{
  "shopId": 1,           // ✅ 保留：店铺ID
  "tableId": 5,          // ✅ 保留：桌台ID
  "userId": 1001,        // ✅ 保留：用户ID
  "orderType": 1,        // ✅ 保留：订单类型
  "queueId": null,       // ✅ 保留：排队ID（可选）
  "remark": "不要辣"      // ✅ 保留：订单备注
}
```

**删除的9个字段**:
- ❌ totalAmount - 改由服务端计算
- ❌ actualAmount - 改由服务端计算
- ❌ itemCount - 改由服务端统计
- ❌ tableNumber - 改由服务端查询
- ❌ queueNumber - 改由服务端查询
- ❌ priority - 改由服务端设置默认值
- ❌ estimatedTime - 改由服务端计算
- ❌ discountAmount - 暂不使用
- ❌ paymentMethod - 在支付时设置

**优势**:
- 🔒 安全性提升：防止客户端篡改金额
- 🎯 准确性提升：使用服务端最新价格
- 📊 一致性提升：服务端统一计算逻辑

### 2. 服务间调用 ✅

创建了3个FeignClient，实现完整的服务间调用：

#### ShopFeignClient
```java
@FeignClient(name = "shop-service", path = "/api/shop", fallback = ShopFeignClientFallback.class)
```
- 验证店铺是否存在
- 验证店铺是否营业中
- 熔断器保护：100%

#### MenuFeignClient
```java
@FeignClient(name = "menu-service", fallback = MenuFeignClientFallback.class)
```
- 获取菜品信息
- 更新菜品库存
- 熔断器保护：100%

#### QueueFeignClient
```java
@FeignClient(name = "queue-service", fallback = QueueFeignClientFallback.class)
```
- 验证排队记录
- 获取排队信息
- 熔断器保护：100%

### 3. Controller优化 ✅

OrdersController添加了完整的服务间调用逻辑：

```java
@PostMapping
public Result<Boolean> createOrder(@RequestBody @Valid OrderCreateRequest request) {
    // 1. 验证店铺是否存在且营业中
    Result<ShopInfoDTO> shopResult = shopFeignClient.getShopById(request.getShopId());
    if (!shop.isOpen()) {
        return Result.error("店铺当前未营业，无法下单");
    }
    
    // 2. 验证排队记录（如果提供queueId）
    if (request.getQueueId() != null) {
        Result<QueueInfoDTO> queueResult = queueFeignClient.getQueueById(request.getQueueId());
        // 验证逻辑...
    }
    
    // 3. 创建订单（金额由服务端计算）
    order.setTotalAmount(calculatedTotal);  // ✅ 服务端计算
    order.setActualAmount(calculatedTotal); // ✅ 服务端计算
    order.setItemCount(totalQuantity);      // ✅ 服务端统计
    
    // 4. 保存订单
    boolean success = ordersService.save(order);
    
    return Result.success(true);
}
```

---

## 📝 API接口概览

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取所有订单 | GET | /api/order/list | 获取订单列表 |
| 根据ID获取订单 | GET | /api/order/{id} | 获取订单详情 |
| 根据订单号获取 | GET | /api/order/no/{orderNo} | 通过订单号查询 |
| 根据店铺获取 | GET | /api/order/shop/{shopId} | 店铺订单列表 |
| 根据用户获取 | GET | /api/order/user/{userId} | 用户订单列表 |
| 根据状态获取 | GET | /api/order/status/{status} | 状态订单列表 |
| **创建订单** | POST | /api/order | ⭐核心接口（已优化） |
| 更新订单 | PUT | /api/order | 更新订单信息 |
| 删除订单 | DELETE | /api/order/{id} | 删除订单 |
| 更新状态 | PUT | /api/order/{id}/status | 更新订单状态 |
| 取消订单 | PUT | /api/order/{id}/cancel | 取消订单 |

详细API文档见: [API_TEST.md](API_TEST.md)

---

## 🗄️ 数据库设计

### orders表

```sql
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `table_id` bigint DEFAULT NULL COMMENT '桌台ID',
  `table_number` varchar(20) DEFAULT NULL COMMENT '桌台编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_type` tinyint NOT NULL COMMENT '订单类型：1-堂食，2-外带，3-外卖',
  `order_status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态：0-待支付，1-待接单，2-制作中，3-待取餐，4-已完成，5-已取消',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠金额',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `item_count` int NOT NULL COMMENT '菜品总数量',
  `remark` varchar(500) DEFAULT NULL COMMENT '订单备注',
  `payment_method` tinyint DEFAULT NULL COMMENT '支付方式',
  `payment_status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态：0-未支付，1-已支付',
  `queue_number` varchar(20) DEFAULT NULL COMMENT '排队号码',
  `priority` tinyint DEFAULT '0' COMMENT '优先级：0-普通，1-加急',
  `estimated_time` int DEFAULT NULL COMMENT '预计等待时间（分钟）',
  `is_evaluated` tinyint NOT NULL DEFAULT '0' COMMENT '是否已评价：0-否，1-是',
  `cancel_reason` varchar(500) DEFAULT NULL COMMENT '取消原因',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `accept_time` datetime DEFAULT NULL COMMENT '接单时间',
  `prepare_time` datetime DEFAULT NULL COMMENT '开始制作时间',
  `ready_time` datetime DEFAULT NULL COMMENT '制作完成时间',
  `complete_time` datetime DEFAULT NULL COMMENT '订单完成时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';
```

---

## 🚀 启动指南

### 1. 前置条件
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis (可选)
- RabbitMQ (可选)
- Eureka Server (必须)

### 2. 配置修改

编辑 `src/main/resources/application.yml`:

```yaml
server:
  port: 8083

spring:
  application:
    name: order-service
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
java -jar target/order-service-1.0-SNAPSHOT.jar

# 或者使用Maven插件
mvn spring-boot:run
```

### 4. 验证启动

访问API文档: http://localhost:8083/doc.html

---

## 🧪 测试指南

详见: [API_TEST.md](API_TEST.md)

### 快速测试

```bash
# 创建订单
curl -X POST "http://localhost:8083/api/order" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "tableId": 5,
    "userId": 1001,
    "orderType": 1,
    "remark": "测试订单"
  }'

# 获取订单列表
curl -X GET "http://localhost:8083/api/order/list"
```

---

## 📊 监控与日志

### 日志配置
- 日志框架: Logback
- 日志级别: INFO
- 日志文件: logs/order-service.log

### 健康检查
- Actuator端点: http://localhost:8083/actuator/health
- 指标监控: http://localhost:8083/actuator/metrics

---

## 🔐 安全说明

### 数据安全
- ✅ 订单金额由服务端计算，防止篡改
- ✅ 店铺状态实时验证，防止未营业店铺接单
- ✅ 所有敏感操作都有日志记录

### 服务间调用安全
- ✅ 所有FeignClient都有熔断器保护
- ✅ 服务不可用时提供友好提示
- ✅ 详细的错误日志便于排查

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
- ✅ OrderCreateRequest重构（删除9个冗余字段）
- ✅ 添加3个FeignClient（Shop, Menu, Queue）
- ✅ Controller添加服务间调用逻辑
- ✅ 100%熔断器覆盖
- ✅ 完善API文档

### v1.0 (初始版本)
- 基础订单管理功能
- 简单的CRUD操作

---

**维护者**: 开发团队  
**最后更新**: 2026-05-18  
**文档版本**: v2.0
