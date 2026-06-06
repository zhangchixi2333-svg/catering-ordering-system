# Menu Service API 测试文档

## 📋 服务说明

**服务名称**: menu-service  
**服务端口**: 8082  
**API基础路径**: `/api/menu`  

---

## 📝 API接口列表

### 分类管理

#### 1. 获取所有分类列表

**接口**: `GET /api/category/list`

**请求示例**:
```bash
curl -X GET "http://localhost:8082/api/category/list"
```

---

#### 2. 根据ID获取分类详情

**接口**: `GET /api/category/{id}`

**请求示例**:
```bash
curl -X GET "http://localhost:8082/api/category/1"
```

---

#### 3. 创建分类

**接口**: `POST /api/category`

**请求体**:
```json
{
  "shopId": 1,
  "categoryName": "热菜",
  "parentId": 0,
  "sortOrder": 1,
  "status": 1
}
```

**请求示例**:
```bash
curl -X POST "http://localhost:8082/api/category" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryName": "热菜",
    "sortOrder": 1
  }'
```

---

#### 4. 更新分类

**接口**: `PUT /api/category`

**请求示例**:
```bash
curl -X PUT "http://localhost:8082/api/category" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "categoryName": "热门热菜",
    "sortOrder": 2
  }'
```

---

#### 5. 删除分类

**接口**: `DELETE /api/category/{id}`

**请求示例**:
```bash
curl -X DELETE "http://localhost:8082/api/category/1"
```

---

### 菜品管理

#### 6. 获取所有菜品列表

**接口**: `GET /api/menu/list`

**请求示例**:
```bash
curl -X GET "http://localhost:8082/api/menu/list"
```

---

#### 7. 根据ID获取菜品详情

**接口**: `GET /api/menu/{id}`

**请求示例**:
```bash
curl -X GET "http://localhost:8082/api/menu/1"
```

---

#### 8. 根据店铺ID获取菜品列表

**接口**: `GET /api/menu/shop/{shopId}`

**请求示例**:
```bash
curl -X GET "http://localhost:8082/api/menu/shop/1"
```

---

#### 9. 根据分类ID获取菜品列表

**接口**: `GET /api/menu/category/{categoryId}`

**请求示例**:
```bash
curl -X GET "http://localhost:8082/api/menu/category/1"
```

---

#### 10. 创建菜品 ⭐核心接口

**接口**: `POST /api/menu`

**请求体**:
```json
{
  "shopId": 1,
  "categoryId": 1,
  "itemName": "宫保鸡丁",
  "description": "经典川菜，口味鲜美",
  "price": 38.00,
  "originalPrice": 45.00,
  "imageUrl": "http://example.com/image.jpg",
  "stock": 100,
  "status": 1,
  "isRecommended": 1,
  "preparationTime": 15
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| shopId | Long | ✅ | 店铺ID |
| categoryId | Long | ✅ | 分类ID |
| itemName | String | ✅ | 菜品名称 |
| description | String | ❌ | 菜品描述 |
| price | BigDecimal | ✅ | 价格 |
| originalPrice | BigDecimal | ❌ | 原价 |
| imageUrl | String | ❌ | 图片URL |
| stock | Integer | ✅ | 库存 |
| status | Integer | ❌ | 状态：0-下架，1-上架（默认1） |
| isRecommended | Integer | ❌ | 是否推荐：0-否，1-是（默认0） |
| preparationTime | Integer | ❌ | 制作时间（分钟） |

**请求示例**:
```bash
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

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

---

#### 11. 更新菜品

**接口**: `PUT /api/menu`

**请求示例**:
```bash
curl -X PUT "http://localhost:8082/api/menu" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "price": 42.00,
    "stock": 150
  }'
```

---

#### 12. 删除菜品

**接口**: `DELETE /api/menu/{id}`

**请求示例**:
```bash
curl -X DELETE "http://localhost:8082/api/menu/1"
```

---

#### 13. 上架/下架菜品

**接口**: `PUT /api/menu/{id}/status?status={status}`

**参数说明**:
- status: 0-下架，1-上架

**请求示例**:
```bash
# 上架
curl -X PUT "http://localhost:8082/api/menu/1/status?status=1"

# 下架
curl -X PUT "http://localhost:8082/api/menu/1/status?status=0"
```

---

#### 14. 扣减库存

**接口**: `PUT /api/menu/{id}/stock?quantity={quantity}`

**参数说明**:
- quantity: 扣减数量（正整数）

**请求示例**:
```bash
curl -X PUT "http://localhost:8082/api/menu/1/stock?quantity=2"
```

**业务规则**:
- 库存不足时返回错误
- 使用原子操作防止超卖
- 扣减成功后返回新的库存量

---

## 🧪 完整测试流程

### 测试场景1：创建完整的菜单体系

```bash
# 1. 创建分类
curl -X POST "http://localhost:8082/api/category" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryName": "热菜",
    "sortOrder": 1
  }'

# 2. 创建菜品
curl -X POST "http://localhost:8082/api/menu" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "categoryId": 1,
    "itemName": "宫保鸡丁",
    "price": 38.00,
    "stock": 100
  }'

# 3. 查询菜品列表验证
curl -X GET "http://localhost:8082/api/menu/shop/1"
```

### 测试场景2：库存管理

```bash
# 1. 查看当前库存
curl -X GET "http://localhost:8082/api/menu/1"

# 2. 扣减库存
curl -X PUT "http://localhost:8082/api/menu/1/stock?quantity=5"

# 3. 再次查看库存验证
curl -X GET "http://localhost:8082/api/menu/1"
```

### 测试场景3：菜品上下架

```bash
# 1. 下架菜品
curl -X PUT "http://localhost:8082/api/menu/1/status?status=0"

# 2. 查询店铺菜品（应该看不到下架的菜品）
curl -X GET "http://localhost:8082/api/menu/shop/1"

# 3. 重新上架
curl -X PUT "http://localhost:8082/api/menu/1/status?status=1"
```

---

## 🔍 服务间调用验证

### 验证ShopFeignClient

当创建菜品或分类时，系统会调用shop-service验证店铺：

```java
// Menu Service内部调用
Result<ShopInfoDTO> shopResult = shopFeignClient.getShopById(request.getShopId());
```

**测试方法**:
1. 停止shop-service
2. 尝试创建菜品
3. 应该收到："店铺服务暂时不可用，请稍后重试"

---

## 📊 性能测试建议

### 并发测试
```bash
# 使用ab工具进行压力测试
ab -n 1000 -c 10 -p menu.json -T application/json http://localhost:8082/api/menu
```

### 监控指标
- 菜品创建平均响应时间
- 库存扣减成功率
- 服务间调用成功率

---

## 🎯 注意事项

1. **店铺关联**: 创建菜品和分类时必须提供有效的shopId
2. **库存管理**: 库存扣减使用原子操作，防止超卖
3. **熔断器**: ShopFeignClient有fallback保护
4. **状态管理**: 菜品下架后不会出现在列表中，但数据仍保留

---

## 📚 相关文档

- [README.md](README.md) - 服务说明文档
- [代码恢复指南](../service_readme/CODE_RECOVERY_GUIDE.md)

---

**文档版本**: v1.0  
**最后更新**: 2026-05-18  
**维护者**: 开发团队
