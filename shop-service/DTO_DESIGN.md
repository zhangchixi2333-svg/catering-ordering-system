# Shop-Service DTO 设计说明

## 📋 设计原则

### 1. 创建与更新分离
- **CreateRequest**: 创建时不包含 ID 和时间字段
- **UpdateRequest**: 更新时必须包含 ID，不包含不可修改的字段

### 2. 时间字段自动化
- `createdAt`: 创建时由 MyBatis Plus 自动填充
- `updatedAt`: 创建和更新时由 MyBatis Plus 自动填充
- **客户端无需传递**

### 3. 业务字段合理性
- 创建时必须提供业务必填字段
- 更新时只提供需要修改的字段
- 不可修改的字段（如编码）不在 UpdateRequest 中

---

## 🎯 DTO 列表

### 店铺相关

#### 1. ShopCreateRequest - 创建店铺

**使用场景**: POST /api/shop

**字段说明**:
```json
{
  "shopName": "美味餐厅旗舰店",      // ✅ 必填
  "shopCode": "SHOP001",             // ✅ 必填
  "address": "北京市朝阳区...",       // ⭕ 可选
  "phone": "010-12345678",           // ⭕ 可选
  "businessHours": "09:00-22:00",    // ⭕ 可选
  "shopStatus": 1,                   // ⭕ 可选，默认1
  "capacity": 120,                   // ⭕ 可选
  "description": "主营川菜、湘菜",    // ⭕ 可选
  "logoUrl": "https://..."           // ⭕ 可选
}
```

**不包含**:
- ❌ `id` - 由数据库自动生成
- ❌ `createdAt` - 系统自动填充
- ❌ `updatedAt` - 系统自动填充

---

#### 2. ShopUpdateRequest - 更新店铺

**使用场景**: PUT /api/shop

**字段说明**:
```json
{
  "id": 1,                           // ✅ 必填，指定更新的店铺
  "shopName": "新名称",               // ⭕ 可选
  "address": "新地址",                // ⭕ 可选
  "phone": "新电话",                  // ⭕ 可选
  "businessHours": "新营业时间",      // ⭕ 可选
  "shopStatus": 1,                   // ⭕ 可选
  "capacity": 150,                   // ⭕ 可选
  "description": "新描述",            // ⭕ 可选
  "logoUrl": "新Logo"                // ⭕ 可选
}
```

**不包含**:
- ❌ `shopCode` - 店铺编码不可修改
- ❌ `createdAt` - 系统维护
- ❌ `updatedAt` - 系统自动更新

---

#### 3. ShopStatusUpdateRequest - 更新店铺状态

**使用场景**: PUT /api/shop/{id}/status

**字段说明**:
```json
{
  "shopStatus": 0                    // ✅ 必填，0-停业, 1-营业, 2-装修
}
```

**优势**:
- ✅ 使用 RequestBody 而非 QueryParam
- ✅ 更符合 RESTful 规范
- ✅ 便于扩展更多状态相关字段

---

### 桌台相关

#### 4. TableCreateRequest - 创建桌台

**使用场景**: POST /api/table

**字段说明**:
```json
{
  "shopId": 1,                       // ✅ 必填
  "tableNumber": "A01",              // ✅ 必填
  "tableName": "A区1号桌",            // ⭕ 可选
  "seats": 4,                        // ⭕ 可选
  "tableType": 1,                    // ⭕ 可选
  "tableStatus": 0,                  // ⭕ 可选，默认0
  "qrCode": "QR_A01_SHOP001",        // ⭕ 可选
  "location": "A区靠窗",              // ⭕ 可选
  "isAvailable": 1                   // ⭕ 可选，默认1
}
```

**不包含**:
- ❌ `id` - 由数据库自动生成
- ❌ `createdAt` - 系统自动填充
- ❌ `updatedAt` - 系统自动填充

---

#### 5. TableUpdateRequest - 更新桌台

**使用场景**: PUT /api/table

**字段说明**:
```json
{
  "id": 1,                           // ✅ 必填，指定更新的桌台
  "tableName": "A区1号桌（VIP）",     // ⭕ 可选
  "seats": 4,                        // ⭕ 可选
  "tableType": 3,                    // ⭕ 可选
  "tableStatus": 0,                  // ⭕ 可选
  "qrCode": "QR_A01_VIP",            // ⭕ 可选
  "location": "A区靠窗（景观位）",    // ⭕ 可选
  "isAvailable": 1                   // ⭕ 可选
}
```

**不包含**:
- ❌ `shopId` - 店铺关联不可修改
- ❌ `tableNumber` - 桌台编号不可修改
- ❌ `createdAt` - 系统维护
- ❌ `updatedAt` - 系统自动更新

---

#### 6. TableStatusUpdateRequest - 更新桌台状态

**使用场景**: PUT /api/table/{id}/status

**字段说明**:
```json
{
  "tableStatus": 1                   // ✅ 必填，0-空闲, 1-使用中, 2-预订, 3-清洁
}
```

---

## 🔄 数据流转

### 创建流程

```
Client Request (DTO)
    ↓
Controller (@Valid 验证)
    ↓
BeanUtils.copyProperties(dto, entity)
    ↓
设置默认值（如果需要）
    ↓
Service.save(entity)
    ↓
MyBatis Plus 自动填充 createdAt, updatedAt
    ↓
Database INSERT
```

### 更新流程

```
Client Request (DTO with id)
    ↓
Controller (@Valid 验证)
    ↓
BeanUtils.copyProperties(dto, entity)
    ↓
Service.updateById(entity)
    ↓
MyBatis Plus 自动填充 updatedAt
    ↓
Database UPDATE
```

---

## 💡 最佳实践

### 1. 必填字段标注

在 DTO 中使用 `@NotNull` 或 `@NotBlank`：

```java
@Schema(description = "店铺名称", example = "美味餐厅", required = true)
@NotBlank(message = "店铺名称不能为空")
private String shopName;
```

### 2. 合理示例值

为每个字段提供有意义的示例：

```java
@Schema(description = "桌台编号", example = "A01")
private String tableNumber;
```

### 3. 默认值处理

在 Controller 中设置合理的默认值：

```java
if (tableInfo.getTableStatus() == null) {
    tableInfo.setTableStatus(0); // 默认空闲
}
```

### 4. 不可修改字段

不在 UpdateRequest 中包含不可修改的字段：
- 主键 ID（创建时）
- 业务唯一标识（如 shopCode, tableNumber）
- 系统维护字段（如 createdAt, updatedAt）

---

## 📊 对比表

| 操作 | 需要 ID | 需要时间字段 | 可修改编码 | 默认值处理 |
|------|---------|-------------|-----------|-----------|
| **Create** | ❌ | ❌ | ✅ 必须提供 | ✅ Controller 设置 |
| **Update** | ✅ 必须 | ❌ | ❌ 不允许 | ⭕ 可选 |
| **Status Update** | ✅ PathVariable | ❌ | ❌ | ❌ 必须提供 |

---

## 🎯 API 文档效果

使用 DTO 后，Knife4j/Swagger 会显示：

### 创建桌台示例（优化后）
```json
{
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
```

### 更新桌台示例（优化后）
```json
{
  "id": 1,
  "tableName": "A区1号桌（VIP）",
  "seats": 4,
  "tableType": 3,
  "tableStatus": 0,
  "qrCode": "QR_A01_VIP",
  "location": "A区靠窗（景观位）",
  "isAvailable": 1
}
```

✅ 清晰明了  
✅ 符合业务逻辑  
✅ 易于理解和使用

---

**最后更新**: 2026-05-17
