# Menu Service - 菜单服务

## 📋 服务说明

**服务名称**: menu-service  
**服务端口**: 8082  
**API基础路径**: `/api/menu`  
**数据库表**: category, menu_item  

---

## 🎯 核心功能

1. **分类管理**: 菜品分类的增删改查
2. **菜品管理**: 菜品的增删改查、上下架
3. **库存管理**: 菜品库存查询和扣减
4. **店铺关联**: 每个菜品关联到特定店铺
5. **服务间调用**: 调用shop-service验证店铺状态

---

## 🔧 技术栈

- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- MyBatis Plus 3.5.5
- MySQL 8.3.0
- Redis (缓存)
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

### 工具类
- knife4j-openapi3-jakarta-spring-boot-starter: API文档
- hutool-all: Java工具类库
- lombok: 代码简化

---

## 🗂️ 项目结构

```
menu-service/
├── src/main/java/org/example/menuservice/
│   ├── MenuServiceApplication.java    # 启动类
│   ├── common/                        # 公共类
│   │   └── Result.java                # 统一返回结果
│   ├── config/                        # 配置类
│   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   └── ...
│   ├── controller/                    # 控制器
│   │   ├── CategoryController.java    # 分类管理接口
│   │   └── MenuItemController.java    # 菜品管理接口
│   ├── dto/                           # 数据传输对象
│   │   ├── CategoryCreateRequest.java
│   │   └── MenuItemCreateRequest.java
│   ├── entity/                        # 实体类
│   │   ├── Category.java              # 分类实体
│   │   └── MenuItem.java              # 菜品实体
│   ├── feign/                         # Feign客户端
│   │   ├── ShopFeignClient.java       # 店铺服务客户端
│   │   └── ShopFeignClientFallback.java
│   ├── mapper/                        # Mapper接口
│   │   ├── CategoryMapper.java
│   │   └── MenuItemMapper.java
│   ├── service/                       # 服务层
│   │   ├── CategoryService.java
│   │   ├── MenuItemService.java
│   │   └── impl/
│   └── util/                          # 工具类
├── src/main/resources/
│   ├── application.yml                # 配置文件
│   └── mapper/                        # MyBatis XML
└── pom.xml                            # Maven配置
```

---

## 🔥 核心特性

### 1. 服务间调用 ✅

创建了ShopFeignClient，实现与店铺服务的交互：

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

### 2. 库存管理 ✅

- 菜品库存实时查询
- 下单时原子扣减库存
- 防止超卖

### 3. 分类管理 ✅

- 支持多级分类
- 分类排序
- 分类状态管理

---

## 📝 API接口概览

### 分类管理

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取所有分类 | GET | /api/category/list | 获取分类列表 |
| 根据ID获取分类 | GET | /api/category/{id} | 获取分类详情 |
| 创建分类 | POST | /api/category | 创建新分类 |
| 更新分类 | PUT | /api/category | 更新分类信息 |
| 删除分类 | DELETE | /api/category/{id} | 删除分类 |

### 菜品管理

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取所有菜品 | GET | /api/menu/list | 获取菜品列表 |
| 根据ID获取菜品 | GET | /api/menu/{id} | 获取菜品详情 |
| 根据店铺获取菜品 | GET | /api/menu/shop/{shopId} | 店铺菜品列表 |
| 根据分类获取菜品 | GET | /api/menu/category/{categoryId} | 分类菜品列表 |
| 创建菜品 | POST | /api/menu | 创建新菜品 |
| 更新菜品 | PUT | /api/menu | 更新菜品信息 |
| 删除菜品 | DELETE | /api/menu/{id} | 删除菜品 |
| 上架/下架菜品 | PUT | /api/menu/{id}/status | 更新菜品状态 |
| 扣减库存 | PUT | /api/menu/{id}/stock | 扣减菜品库存 |

---

## 🗄️ 数据库设计

### category表

```sql
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID',
  `sort_order` int DEFAULT '0' COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';
```

### menu_item表

```sql
CREATE TABLE `menu_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜品ID',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `item_name` varchar(200) NOT NULL COMMENT '菜品名称',
  `description` varchar(1000) DEFAULT NULL COMMENT '菜品描述',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图片URL',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
  `sales` int NOT NULL DEFAULT '0' COMMENT '销量',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-下架，1-上架',
  `is_recommended` tinyint NOT NULL DEFAULT '0' COMMENT '是否推荐：0-否，1-是',
  `preparation_time` int DEFAULT NULL COMMENT '制作时间（分钟）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';
```

---

## 🚀 启动指南

### 1. 前置条件
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis (可选)
- Eureka Server (必须)
- Shop Service (可选，用于店铺验证)

### 2. 配置修改

编辑 `src/main/resources/application.yml`:

```yaml
server:
  port: 8082

spring:
  application:
    name: menu-service
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
java -jar target/menu-service-1.0-SNAPSHOT.jar

# 或者使用Maven插件
mvn spring-boot:run
```

### 4. 验证启动

访问API文档: http://localhost:8082/doc.html

---

## 🧪 测试指南

详见: [API_TEST.md](API_TEST.md)

### 快速测试

```bash
# 创建分类
curl -X POST "http://localhost:8082/api/category" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryName": "热菜",
    "sortOrder": 1
  }'

# 创建菜品
curl -X POST "http://localhost:8082/api/menu" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 1,
    "itemName": "宫保鸡丁",
    "price": 38.00,
    "stock": 100
  }'
```

---

## 📊 监控与日志

### 日志配置
- 日志框架: Logback
- 日志级别: INFO
- 日志文件: logs/menu-service.log

### 健康检查
- Actuator端点: http://localhost:8082/actuator/health
- 指标监控: http://localhost:8082/actuator/metrics

---

## 🔐 安全说明

### 数据安全
- ✅ 菜品价格由服务端管理
- ✅ 库存扣减使用原子操作
- ✅ 所有敏感操作都有日志记录

### 服务间调用安全
- ✅ ShopFeignClient有熔断器保护
- ✅ 服务不可用时提供友好提示
- ✅ 详细的错误日志便于排查

---

## 📚 相关文档

- [API测试文档](API_TEST.md) - 待创建
- [DTO设计文档](DTO_DESIGN.md) - 待创建
- [Windows测试指南](WINDOWS_TEST.md) - 待创建
- [代码恢复指南](../service_readme/CODE_RECOVERY_GUIDE.md)

---

## 🔄 版本历史

### v1.0 (当前版本)
- 基础菜品管理功能
- 分类管理功能
- 库存管理功能
- ShopFeignClient集成

---

**维护者**: 开发团队  
**最后更新**: 2026-05-18  
**文档版本**: v1.0
