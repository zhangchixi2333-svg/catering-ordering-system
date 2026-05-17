# Shop Service - 店铺服务

## 📋 服务说明

**端口**: 8081  
**功能**: 管理店铺信息和桌台信息  
**数据库**: shop_service

---

## 🚀 启动步骤

### 1. 前置条件

确保以下服务已启动：
- ✅ MySQL (localhost:3306)
- ✅ Redis (localhost:6379)
- ✅ Nacos (localhost:8848)

### 2. 初始化数据库

执行SQL脚本创建数据库和表：
```bash
mysql -u root -p123456 -h localhost -P 3306 < ../sql/shop-service.sql
```

### 3. 启动服务

**方式一：IDEA启动**
- 打开 `ShopServiceApplication.java`
- 点击运行按钮

**方式二：Maven命令**
```bash
cd shop-service
mvn spring-boot:run
```

### 4. 验证启动

访问API文档：http://localhost:8081/doc.html

看到以下输出表示启动成功：
```
========================================
   Shop Service 启动成功！
   API文档: http://localhost:8081/doc.html
========================================
```

---

## 📡 API接口

### 店铺管理 (/api/shop)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/shop/list | 获取所有店铺列表 |
| GET | /api/shop/open | 获取营业中的店铺 |
| GET | /api/shop/{id} | 根据ID获取店铺详情 |
| GET | /api/shop/code/{shopCode} | 根据编码获取店铺 |
| POST | /api/shop | 创建店铺 |
| PUT | /api/shop | 更新店铺信息 |
| DELETE | /api/shop/{id} | 删除店铺 |
| PUT | /api/shop/{id}/status | 更新店铺状态 |

### 桌台管理 (/api/table)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/table/shop/{shopId} | 获取店铺的所有桌台 |
| GET | /api/table/shop/{shopId}/available | 获取店铺可用桌台 |
| GET | /api/table/{id} | 根据ID获取桌台详情 |
| GET | /api/table/shop/{shopId}/number/{tableNumber} | 根据桌台编号查询 |
| POST | /api/table | 创建桌台 |
| PUT | /api/table | 更新桌台信息 |
| DELETE | /api/table/{id} | 删除桌台 |
| PUT | /api/table/{id}/status | 更新桌台状态 |

### 店铺配置 (/api/config)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/config/{shopId}/{configKey} | 获取配置值 |
| POST | /api/config | 设置配置 |

---

## 🧪 测试示例

### 1. 查询所有店铺

```bash
curl http://localhost:8081/api/shop/list
```

响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "shopName": "美味餐厅旗舰店",
      "shopCode": "SHOP001",
      "address": "北京市朝阳区建国路88号",
      "phone": "010-12345678",
      "businessHours": "09:00-22:00",
      "shopStatus": 1,
      "capacity": 120
    }
  ]
}
```

### 2. 查询店铺可用桌台

```bash
curl http://localhost:8081/api/table/shop/1/available
```

### 3. 更新桌台状态

```bash
curl -X PUT "http://localhost:8081/api/table/1/status?status=1"
```

---

## 📁 项目结构

```
shop-service/
├── src/main/java/org/example/shopservice/
│   ├── ShopServiceApplication.java      # 启动类
│   ├── common/                           # 通用类
│   │   └── Result.java                   # 统一返回结果
│   ├── config/                           # 配置类
│   │   ├── MybatisPlusConfig.java        # MyBatis Plus配置
│   │   └── MyMetaObjectHandler.java      # 自动填充配置
│   ├── controller/                       # 控制器
│   │   ├── ShopInfoController.java       # 店铺管理
│   │   ├── TableInfoController.java      # 桌台管理
│   │   └── ShopConfigController.java     # 配置管理
│   ├── service/                          # 服务接口
│   │   ├── ShopInfoService.java
│   │   ├── TableInfoService.java
│   │   ├── ShopConfigService.java
│   │   └── impl/                         # 服务实现
│   │       ├── ShopInfoServiceImpl.java
│   │       ├── TableInfoServiceImpl.java
│   │       └── ShopConfigServiceImpl.java
│   ├── mapper/                           # Mapper接口
│   │   ├── ShopInfoMapper.java
│   │   ├── TableInfoMapper.java
│   │   └── ShopConfigMapper.java
│   └── entity/                           # 实体类
│       ├── ShopInfo.java
│       ├── TableInfo.java
│       └── ShopConfig.java
├── src/main/resources/
│   └── application.yml                   # 配置文件
└── pom.xml                               # Maven配置
```

---

## 🔧 技术栈

- **Spring Boot** 3.2.5
- **Spring Cloud Alibaba** 2023.0.1.0
- **MyBatis Plus** 3.5.5
- **MySQL** 8.3.0
- **Druid** 1.2.21
- **Redis** 
- **Knife4j** 4.4.0 (API文档)
- **Lombok**
- **Hutool**

---

## ⚙️ 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shop_service
    username: root
    password: 123456
```

### Nacos配置

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848
```

### Redis配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

---

## 📝 开发规范

### 1. 统一返回格式

所有接口返回 `Result<T>` 对象：
```java
return Result.success(data);  // 成功
return Result.error("错误信息");  // 失败
```

### 2. Service层

继承 `IService<T>`，使用MyBatis Plus的CRUD方法：
```java
public interface ShopInfoService extends IService<ShopInfo> {
    // 自定义方法
}
```

### 3. Controller层

使用 `@RequiredArgsConstructor` 注入依赖：
```java
@RestController
@RequiredArgsConstructor
public class ShopInfoController {
    private final ShopInfoService shopInfoService;
}
```

---

## 🐛 常见问题

### 1. 启动失败：无法连接Nacos

**解决**: 确保Nacos已启动
```bash
# Windows
startup.cmd -m standalone

# Linux/Mac
sh startup.sh -m standalone
```

### 2. 数据库连接失败

**解决**: 检查MySQL是否启动，用户名密码是否正确

### 3. Redis连接失败

**解决**: 启动Redis服务
```bash
redis-server
```

---

## 📚 相关文档

- [数据库设计](../sql/readme/shop-service.md)
- [依赖版本说明](../DEPENDENCIES.md)
- [Knife4j文档](https://doc.xiaominfo.com/)
- [MyBatis Plus文档](https://baomidou.com/)

---

**最后更新**: 2026-05-17
