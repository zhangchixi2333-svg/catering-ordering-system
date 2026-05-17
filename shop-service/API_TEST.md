# Shop-Service 接口测试文档

## 📋 服务信息

- **服务名称**: shop-service
- **端口**: 8081
- **基础URL**: http://localhost:8081
- **API文档**: http://localhost:8081/doc.html

---

## 🔧 前置条件

### 1. 启动服务

确保以下服务已启动：
- ✅ MySQL (localhost:3306)
- ✅ Redis (localhost:6379) - 可选
- ✅ shop-service (localhost:8081)

### 2. 初始化数据库

```bash
mysql -u root -p123456 -h localhost -P 3306 < ../sql/shop-service.sql
```

### 3. 验证服务状态

访问：http://localhost:8081/doc.html  
如果能看到 Knife4j 界面，说明服务启动成功。

---

## 🧪 接口测试

### 一、店铺管理接口 (/api/shop)

#### 1. 获取所有店铺列表

**请求**
```http
GET http://localhost:8081/api/shop/list
```

**预期响应**
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
      "capacity": 120,
      "description": "主营川菜、湘菜",
      "logoUrl": "https://example.com/logo1.jpg"
    },
    {
      "id": 2,
      "shopName": "美味餐厅分店",
      "shopCode": "SHOP002",
      "address": "北京市海淀区中关村大街1号",
      "phone": "010-87654321",
      "businessHours": "10:00-21:00",
      "shopStatus": 1,
      "capacity": 80,
      "description": "主营粤菜、海鲜",
      "logoUrl": "https://example.com/logo2.jpg"
    }
  ]
}
```

**cURL 命令**
```bash
curl -X GET "http://localhost:8081/api/shop/list"
```

---

#### 2. 获取营业中的店铺

**请求**
```http
GET http://localhost:8081/api/shop/open
```

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "shopName": "美味餐厅旗舰店",
      "shopStatus": 1
    }
  ]
}
```

**cURL 命令**
```bash
curl -X GET "http://localhost:8081/api/shop/open"
```

---

#### 3. 根据ID获取店铺详情

**请求**
```http
GET http://localhost:8081/api/shop/1
```

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "shopName": "美味餐厅旗舰店",
    "shopCode": "SHOP001",
    "address": "北京市朝阳区建国路88号",
    "phone": "010-12345678",
    "businessHours": "09:00-22:00",
    "shopStatus": 1,
    "capacity": 120,
    "description": "主营川菜、湘菜",
    "logoUrl": "https://example.com/logo1.jpg"
  }
}
```

**cURL 命令**
```bash
curl -X GET "http://localhost:8081/api/shop/1"
```

---

#### 4. 根据编码获取店铺

**请求**
```http
GET http://localhost:8081/api/shop/code/SHOP001
```

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "shopName": "美味餐厅旗舰店",
    "shopCode": "SHOP001"
  }
}
```

**cURL 命令**
```bash
curl -X GET "http://localhost:8081/api/shop/code/SHOP001"
```

---

#### 5. 创建店铺

**请求**
```http
POST http://localhost:8081/api/shop
Content-Type: application/json

{
  "shopName": "美味餐厅旗舰店",
  "shopCode": "SHOP003",
  "address": "上海市浦东新区陆家嘴环路1000号",
  "phone": "021-12345678",
  "businessHours": "11:00-22:00",
  "shopStatus": 1,
  "capacity": 100,
  "description": "主营川菜、湘菜",
  "logoUrl": "https://example.com/logo.jpg"
}
```

**说明**
- ✅ 不需要传 `id`、`createdAt`、`updatedAt`（系统自动生成）
- ✅ `shopName` 和 `shopCode` 为必填项
- ✅ `shopStatus` 默认为 1（营业中）

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**cURL 命令（Windows CMD）**
```cmd
curl -X POST "http://localhost:8081/api/shop" -H "Content-Type: application/json" -d "{\"shopName\": \"测试店铺\", \"shopCode\": \"SHOP003\", \"address\": \"上海市浦东新区陆家嘴环路1000号\", \"phone\": \"021-12345678\", \"businessHours\": \"11:00-22:00\", \"shopStatus\": 1, \"capacity\": 100, \"description\": \"测试店铺描述\", \"logoUrl\": \"https://example.com/logo3.jpg\"}"
```

**cURL 命令（PowerShell）**
```powershell
curl -X POST "http://localhost:8081/api/shop" `
  -H "Content-Type: application/json" `
  -d '{
    "shopName": "测试店铺",
    "shopCode": "SHOP003",
    "address": "上海市浦东新区陆家嘴环路1000号",
    "phone": "021-12345678",
    "businessHours": "11:00-22:00",
    "shopStatus": 1,
    "capacity": 100,
    "description": "测试店铺描述",
    "logoUrl": "https://example.com/logo3.jpg"
  }'
```

**cURL 命令（Linux/Mac）**
```bash
curl -X POST "http://localhost:8081/api/shop" \
  -H "Content-Type: application/json" \
  -d '{
    "shopName": "测试店铺",
    "shopCode": "SHOP003",
    "address": "上海市浦东新区陆家嘴环路1000号",
    "phone": "021-12345678",
    "businessHours": "11:00-22:00",
    "shopStatus": 1,
    "capacity": 100,
    "description": "测试店铺描述",
    "logoUrl": "https://example.com/logo3.jpg"
  }'
```

---

#### 6. 更新店铺信息

**请求**
```http
PUT http://localhost:8081/api/shop
Content-Type: application/json

{
  "id": 1,
  "shopName": "美味餐厅旗舰店（已装修）",
  "address": "北京市朝阳区建国路88号",
  "phone": "010-12345678",
  "businessHours": "09:00-23:00",
  "shopStatus": 1,
  "capacity": 150,
  "description": "装修升级，环境更好",
  "logoUrl": "https://example.com/logo-new.jpg"
}
```

**说明**
- ✅ `id` 为必填项（指定要更新的店铺）
- ✅ 不需要传 `shopCode`（店铺编码不可修改）
- ✅ 不需要传 `createdAt`、`updatedAt`（系统自动更新）
- ✅ 只需传需要修改的字段

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**cURL 命令**
```bash
curl -X PUT "http://localhost:8081/api/shop" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "shopName": "美味餐厅旗舰店（已装修）",
    "capacity": 150
  }'
```

---

#### 7. 更新店铺状态

**请求**
```http
PUT http://localhost:8081/api/shop/1/status
Content-Type: application/json

{
  "shopStatus": 0
}
```

**参数说明**
- `shopStatus`: 0-停业, 1-营业中, 2-装修中

**说明**
- ✅ 使用 RequestBody 传递状态，而非 QueryParam
- ✅ 更符合 RESTful 规范

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**cURL 命令**
```bash
curl -X PUT "http://localhost:8081/api/shop/1/status?status=0"
```

---

#### 8. 删除店铺

**请求**
```http
DELETE http://localhost:8081/api/shop/3
```

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**cURL 命令**
```bash
curl -X DELETE "http://localhost:8081/api/shop/3"
```

---

### 二、桌台管理接口 (/api/table)

#### 1. 获取店铺的所有桌台

**请求**
```http
GET http://localhost:8081/api/table/shop/1
```

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "shopId": 1,
      "tableNumber": "A01",
      "tableName": "A区1号桌",
      "seats": 4,
      "tableType": 1,
      "tableStatus": 0,
      "qrCode": "QR_A01_SHOP001",
      "location": "A区靠窗",
      "isAvailable": 1
    },
    {
      "id": 2,
      "shopId": 1,
      "tableNumber": "A02",
      "tableName": "A区2号桌",
      "seats": 6,
      "tableType": 2,
      "tableStatus": 0,
      "qrCode": "QR_A02_SHOP001",
      "location": "A区中间",
      "isAvailable": 1
    }
  ]
}
```

**cURL 命令**
```bash
curl -X GET "http://localhost:8081/api/table/shop/1"
```

---

#### 2. 获取店铺可用桌台

**请求**
```http
GET http://localhost:8081/api/table/shop/1/available
```

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "tableNumber": "A01",
      "tableStatus": 0,
      "isAvailable": 1
    }
  ]
}
```

**cURL 命令**
```bash
curl -X GET "http://localhost:8081/api/table/shop/1/available"
```

---

#### 3. 根据ID获取桌台详情

**请求**
```http
GET http://localhost:8081/api/table/1
```

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "shopId": 1,
    "tableNumber": "A01",
    "tableName": "A区1号桌",
    "seats": 4,
    "tableType": 1,
    "tableStatus": 0,
    "qrCode": "QR_A01_SHOP001",
    "location": "A区靠窗",
    "isAvailable": 1
  }
}
```

**cURL 命令**
```bash
curl -X GET "http://localhost:8081/api/table/1"
```

---

#### 4. 根据桌台编号查询

**请求**
```http
GET http://localhost:8081/api/table/shop/1/number/A01
```

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "tableNumber": "A01",
    "tableName": "A区1号桌"
  }
}
```

**cURL 命令**
```bash
curl -X GET "http://localhost:8081/api/table/shop/1/number/A01"
```

---

#### 5. 创建桌台

**请求**
```http
POST http://localhost:8081/api/table
Content-Type: application/json

{
  "shopId": 1,
  "tableNumber": "B01",
  "tableName": "B区1号桌",
  "seats": 2,
  "tableType": 1,
  "tableStatus": 0,
  "qrCode": "QR_B01_SHOP001",
  "location": "B区角落",
  "isAvailable": 1
}
```

**说明**
- ✅ 不需要传 `id`、`createdAt`、`updatedAt`（系统自动生成）
- ✅ `shopId` 和 `tableNumber` 为必填项
- ✅ `tableStatus` 默认为 0（空闲）
- ✅ `isAvailable` 默认为 1（可用）

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**cURL 命令**
```bash
curl -X POST "http://localhost:8081/api/table" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "tableNumber": "B01",
    "tableName": "B区1号桌",
    "seats": 2,
    "tableType": 1,
    "tableStatus": 0,
    "qrCode": "QR_B01_SHOP001",
    "location": "B区角落",
    "isAvailable": 1
  }'
```

---

#### 6. 更新桌台信息

**请求**
```http
PUT http://localhost:8081/api/table
Content-Type: application/json

{
  "id": 1,
  "tableName": "A区1号桌（VIP）",
  "seats": 4,
  "tableType": 3,
  "tableStatus": 0,
  "qrCode": "QR_A01_SHOP001_VIP",
  "location": "A区靠窗（景观位）",
  "isAvailable": 1
}
```

**说明**
- ✅ `id` 为必填项（指定要更新的桌台）
- ✅ 不需要传 `shopId`、`tableNumber`（不可修改）
- ✅ 不需要传 `createdAt`、`updatedAt`（系统自动更新）
- ✅ 只需传需要修改的字段

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**cURL 命令**
```bash
curl -X PUT "http://localhost:8081/api/table" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "tableName": "A区1号桌（VIP）",
    "tableType": 3
  }'
```

---

#### 7. 更新桌台状态

**请求**
```http
PUT http://localhost:8081/api/table/1/status
Content-Type: application/json

{
  "tableStatus": 1
}
```

**参数说明**
- `tableStatus`: 0-空闲, 1-使用中, 2-已预订, 3-维护中

**说明**
- ✅ 使用 RequestBody 传递状态，而非 QueryParam
- ✅ 更符合 RESTful 规范

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**cURL 命令**
```bash
curl -X PUT "http://localhost:8081/api/table/1/status?status=1"
```

---

#### 8. 删除桌台

**请求**
```http
DELETE http://localhost:8081/api/table/5
```

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**cURL 命令**
```bash
curl -X DELETE "http://localhost:8081/api/table/5"
```

---

### 三、店铺配置接口 (/api/config)

#### 1. 获取配置值

**请求**
```http
GET http://localhost:8081/api/config/1/min_consumption
```

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "100"
}
```

**cURL 命令**
```bash
curl -X GET "http://localhost:8081/api/config/1/min_consumption"
```

---

#### 2. 设置配置

**请求**
```http
POST http://localhost:8081/api/config?shopId=1&configKey=min_consumption&configValue=150&configDesc=最低消费金额
```

**预期响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

**cURL 命令**
```bash
curl -X POST "http://localhost:8081/api/config?shopId=1&configKey=min_consumption&configValue=150&configDesc=最低消费金额"
```

---

## 📊 测试场景

### 场景1：完整业务流程测试

1. **查询店铺列表** → 获取所有店铺
2. **查询可用桌台** → 查看店铺1的可用桌台
3. **更新桌台状态** → 将桌台1设置为"使用中"
4. **再次查询可用桌台** → 确认桌台1不再出现在可用列表中
5. **恢复桌台状态** → 将桌台1恢复为"空闲"

### 场景2：店铺管理测试

1. **创建新店铺** → 添加测试店铺
2. **查询店铺详情** → 验证创建成功
3. **更新店铺信息** → 修改店铺名称和容量
4. **查询更新结果** → 验证修改生效
5. **删除测试店铺** → 清理测试数据

### 场景3：配置管理测试

1. **读取配置** → 获取最低消费配置
2. **修改配置** → 更新最低消费金额
3. **再次读取** → 验证配置已更新

---

## 🔍 常见错误及处理

### 1. 服务未启动

**错误信息**
```
Connection refused: connect
```

**解决方法**
```bash
# 启动 shop-service
cd shop-service
mvn spring-boot:run
```

---

### 2. 数据库未初始化

**错误信息**
```
Table 'shop_service.shop_info' doesn't exist
```

**解决方法**
```bash
mysql -u root -p123456 -h localhost -P 3306 < ../sql/shop-service.sql
```

---

### 3. 资源不存在

**错误信息**
```json
{
  "code": 500,
  "message": "店铺不存在"
}
```

**解决方法**
- 检查ID是否正确
- 先查询列表确认资源存在

---

### 4. 参数错误

**错误信息**
```json
{
  "code": 500,
  "message": "请求参数错误"
}
```

**解决方法**
- 检查必填参数是否完整
- 检查参数类型是否正确
- 查看 API 文档确认参数格式

---

## 📝 测试清单

### 店铺管理
- [ ] 获取所有店铺列表
- [ ] 获取营业中的店铺
- [ ] 根据ID查询店铺
- [ ] 根据编码查询店铺
- [ ] 创建新店铺
- [ ] 更新店铺信息
- [ ] 更新店铺状态
- [ ] 删除店铺

### 桌台管理
- [ ] 获取店铺所有桌台
- [ ] 获取可用桌台
- [ ] 根据ID查询桌台
- [ ] 根据桌台编号查询
- [ ] 创建新桌台
- [ ] 更新桌台信息
- [ ] 更新桌台状态
- [ ] 删除桌台

### 配置管理
- [ ] 获取配置值
- [ ] 设置配置

---

## 🛠️ 测试工具推荐

### 1. Knife4j (推荐)
- 地址: http://localhost:8081/doc.html
- 优点: 可视化界面，自动生成请求示例

### 2. Postman
- 导入 cURL 命令即可测试
- 支持保存测试集合

### 3. cURL (命令行)
- 适合快速测试
- 可编写脚本批量测试

### 4. Apifox / ApiPost
- 国产API测试工具
- 支持团队协作

---

## 📚 相关文档

- [Shop-Service README](../shop-service/README.md)
- [数据库设计文档](../sql/readme/shop-service.md)
- [Knife4j官方文档](https://doc.xiaominfo.com/)

---

**最后更新**: 2026-05-17  
**版本**: v1.0
