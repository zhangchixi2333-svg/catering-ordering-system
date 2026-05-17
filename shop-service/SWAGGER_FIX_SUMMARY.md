# Swagger/Knife4j 路径参数配置修复总结

## ✅ 修复完成状态

所有 Controller 的路径参数和请求参数已正确配置 `@Parameter` 注解，现在在 Knife4j 调试界面中可以正常显示输入框。

---

## 📋 修复的 Controller 列表

### 1. ShopInfoController（店铺管理）✅

| 接口 | 方法 | 路径 | 修复内容 |
|------|------|------|---------|
| 根据ID获取店铺详情 | GET | `/api/shop/{id}` | ✅ 添加 `@Parameter` + `@PathVariable("id")` |
| 根据编码获取店铺 | GET | `/api/shop/code/{shopCode}` | ✅ 添加 `@Parameter` + `@PathVariable("shopCode")` |
| 删除店铺 | DELETE | `/api/shop/{id}` | ✅ 添加 `@Parameter` + `@PathVariable("id")` |
| 更新店铺状态 | PUT | `/api/shop/{id}/status` | ✅ 已有 `@PathVariable("id")`，补充 `@Parameter` |

**修复示例**：
```java
@GetMapping("/{id}")
public Result<ShopInfo> getShopById(
        @Parameter(description = "店铺ID", example = "1", required = true)
        @PathVariable("id") Long id) {
    // ...
}
```

---

### 2. TableInfoController（桌台管理）✅

| 接口 | 方法 | 路径 | 修复内容 |
|------|------|------|---------|
| 获取店铺的所有桌台 | GET | `/api/table/shop/{shopId}` | ✅ 添加 `@Parameter` + `@PathVariable("shopId")` |
| 获取店铺可用桌台 | GET | `/api/table/shop/{shopId}/available` | ✅ 添加 `@Parameter` + `@PathVariable("shopId")` |
| 根据ID获取桌台详情 | GET | `/api/table/{id}` | ✅ 添加 `@Parameter` + `@PathVariable("id")` |
| 根据桌台编号查询 | GET | `/api/table/shop/{shopId}/number/{tableNumber}` | ✅ 添加两个 `@Parameter` |
| 删除桌台 | DELETE | `/api/table/{id}` | ✅ 添加 `@Parameter` + `@PathVariable("id")` |
| 更新桌台状态 | PUT | `/api/table/{id}/status` | ✅ 已有 `@PathVariable("id")`，补充 `@Parameter` |

**修复示例**：
```java
@GetMapping("/shop/{shopId}/number/{tableNumber}")
public Result<TableInfo> getByTableNumber(
        @Parameter(description = "店铺ID", example = "1", required = true)
        @PathVariable("shopId") Long shopId,
        @Parameter(description = "桌台编号", example = "A01", required = true)
        @PathVariable("tableNumber") String tableNumber) {
    // ...
}
```

---

### 3. ShopConfigController（店铺配置）✅

| 接口 | 方法 | 路径 | 修复内容 |
|------|------|------|---------|
| 获取配置值 | GET | `/api/config/{shopId}/{configKey}` | ✅ 添加两个 `@Parameter` + `@PathVariable` |
| 设置配置 | POST | `/api/config` | ✅ 为所有 `@RequestParam` 添加 `@Parameter` |

**修复示例 - 路径参数**：
```java
@GetMapping("/{shopId}/{configKey}")
public Result<String> getConfig(
        @Parameter(description = "店铺ID", example = "1", required = true)
        @PathVariable("shopId") Long shopId,
        @Parameter(description = "配置键名", example = "queue_enabled", required = true)
        @PathVariable("configKey") String configKey) {
    // ...
}
```

**修复示例 - 请求参数**：
```java
@PostMapping
public Result<Boolean> setConfig(
        @Parameter(description = "店铺ID", example = "1", required = true)
        @RequestParam("shopId") Long shopId,
        @Parameter(description = "配置键名", example = "queue_enabled", required = true)
        @RequestParam("configKey") String configKey,
        @Parameter(description = "配置值", example = "true", required = true)
        @RequestParam("configValue") String configValue,
        @Parameter(description = "配置说明", example = "是否启用排队功能")
        @RequestParam(value = "configDesc", required = false) String configDesc) {
    // ...
}
```

---

## 🔧 修复要点

### 1. 路径参数（@PathVariable）

**必须同时满足**：
- ✅ 显式指定参数名：`@PathVariable("id")`
- ✅ 添加 Swagger 注解：`@Parameter(description, example, required)`

**错误写法**：
```java
@GetMapping("/{id}")
public Result<?> getById(@PathVariable Long id) { }  // ❌
```

**正确写法**：
```java
@GetMapping("/{id}")
public Result<?> getById(
        @Parameter(description = "ID", example = "1", required = true)
        @PathVariable("id") Long id) { }  // ✅
```

---

### 2. 请求参数（@RequestParam）

**推荐做法**：
- ✅ 显式指定参数名：`@RequestParam("shopId")`
- ✅ 添加 Swagger 注解：`@Parameter(description, example, required)`

**错误写法**：
```java
@PostMapping
public Result<?> create(@RequestParam Long shopId) { }  // ❌
```

**正确写法**：
```java
@PostMapping
public Result<?> create(
        @Parameter(description = "店铺ID", example = "1", required = true)
        @RequestParam("shopId") Long shopId) { }  // ✅
```

---

### 3. 请求体（@RequestBody）

**不需要额外配置**：
- DTO 类中的字段已经有 `@Schema` 注解
- Swagger 会自动识别并生成文档

```java
@PostMapping
public Result<?> create(@RequestBody @Valid ShopCreateRequest request) { 
    // ✅ 无需额外配置，DTO 中的 @Schema 会自动生效
}
```

---

## 🎯 Knife4j 界面效果

### 修复前 ❌

```
接口: GET /api/shop/{id}

[调试] 按钮
  ↓
❌ 没有路径参数输入框
❌ 需要手动拼接 URL
```

### 修复后 ✅

```
接口: GET /api/shop/{id}

[调试] 按钮
  ↓
路径参数:
  ┌─────────────────────────────────┐
  │ id: [____1____]                 │
  │ 店铺ID (必填, 示例: 1)           │
  └─────────────────────────────────┘

[发送] 按钮
```

---

## 📊 完整接口清单

### ShopInfoController（8个接口）

| # | 接口名称 | 方法 | 路径 | 路径参数 | 状态 |
|---|---------|------|------|---------|------|
| 1 | 获取所有店铺列表 | GET | `/api/shop/list` | 无 | ✅ |
| 2 | 获取营业中的店铺 | GET | `/api/shop/open` | 无 | ✅ |
| 3 | 根据ID获取店铺详情 | GET | `/api/shop/{id}` | `id` | ✅ |
| 4 | 根据编码获取店铺 | GET | `/api/shop/code/{shopCode}` | `shopCode` | ✅ |
| 5 | 创建店铺 | POST | `/api/shop` | 无 | ✅ |
| 6 | 更新店铺信息 | PUT | `/api/shop` | 无 | ✅ |
| 7 | 删除店铺 | DELETE | `/api/shop/{id}` | `id` | ✅ |
| 8 | 更新店铺状态 | PUT | `/api/shop/{id}/status` | `id` | ✅ |

---

### TableInfoController（8个接口）

| # | 接口名称 | 方法 | 路径 | 路径参数 | 状态 |
|---|---------|------|------|---------|------|
| 1 | 获取店铺的所有桌台 | GET | `/api/table/shop/{shopId}` | `shopId` | ✅ |
| 2 | 获取店铺可用桌台 | GET | `/api/table/shop/{shopId}/available` | `shopId` | ✅ |
| 3 | 根据ID获取桌台详情 | GET | `/api/table/{id}` | `id` | ✅ |
| 4 | 根据桌台编号查询 | GET | `/api/table/shop/{shopId}/number/{tableNumber}` | `shopId`, `tableNumber` | ✅ |
| 5 | 创建桌台 | POST | `/api/table` | 无 | ✅ |
| 6 | 更新桌台信息 | PUT | `/api/table` | 无 | ✅ |
| 7 | 删除桌台 | DELETE | `/api/table/{id}` | `id` | ✅ |
| 8 | 更新桌台状态 | PUT | `/api/table/{id}/status` | `id` | ✅ |

---

### ShopConfigController（2个接口）

| # | 接口名称 | 方法 | 路径 | 路径/请求参数 | 状态 |
|---|---------|------|------|--------------|------|
| 1 | 获取配置值 | GET | `/api/config/{shopId}/{configKey}` | `shopId`, `configKey` | ✅ |
| 2 | 设置配置 | POST | `/api/config` | `shopId`, `configKey`, `configValue`, `configDesc` | ✅ |

---

## 🧪 测试验证

### 1. 重启服务

```cmd
cd shop-service
mvn spring-boot:run
```

### 2. 访问 Knife4j

打开浏览器访问：**http://localhost:8081/doc.html**

### 3. 测试步骤

#### 测试路径参数接口

1. 展开"店铺管理"标签
2. 点击"根据ID获取店铺详情"
3. 点击"调试"按钮
4. ✅ 应该看到 `id` 输入框
5. 输入 `1`
6. 点击"发送"

#### 测试请求参数接口

1. 展开"店铺配置"标签
2. 点击"设置配置"
3. 点击"调试"按钮
4. ✅ 应该看到 4 个输入框：
   - `shopId`
   - `configKey`
   - `configValue`
   - `configDesc`（可选）
5. 填写参数
6. 点击"发送"

#### 测试多路径参数接口

1. 展开"桌台管理"标签
2. 点击"根据桌台编号查询"
3. 点击"调试"按钮
4. ✅ 应该看到 2 个输入框：
   - `shopId`
   - `tableNumber`
5. 分别输入 `1` 和 `A01`
6. 点击"发送"

---

## ⚠️ 常见问题

### Q1: 为什么有些接口还是看不到参数输入框？

**A**: 检查以下几点：
1. 是否添加了 `@Parameter` 注解
2. `@PathVariable` 是否显式指定了参数名
3. 是否重新编译并重启了服务
4. 浏览器是否清除了缓存（Ctrl+F5 强制刷新）

### Q2: @Parameter 注解的属性有哪些？

**A**:
```java
@Parameter(
    description = "参数描述",      // 显示在界面上的说明
    example = "1",                // 示例值，会填充到输入框
    required = true               // 是否必填，true 会显示红色星号
)
```

### Q3: 多个路径参数如何配置？

**A**: 每个参数都需要单独配置：
```java
@GetMapping("/{shopId}/number/{tableNumber}")
public Result<?> query(
        @Parameter(description = "店铺ID", example = "1", required = true)
        @PathVariable("shopId") Long shopId,
        @Parameter(description = "桌台编号", example = "A01", required = true)
        @PathVariable("tableNumber") String tableNumber) {
    // ...
}
```

### Q4: 可选参数如何配置？

**A**: 设置 `required = false`：
```java
@Parameter(description = "配置说明", example = "是否启用排队功能")
@RequestParam(value = "configDesc", required = false) String configDesc
```

---

## 📝 最佳实践总结

### 1. 统一规范

所有 Controller 方法参数都应遵循以下规范：

```java
// 路径参数
@Parameter(description = "描述", example = "示例值", required = true)
@PathVariable("参数名") 类型 参数名

// 请求参数
@Parameter(description = "描述", example = "示例值", required = true/false)
@RequestParam("参数名") 类型 参数名

// 请求体
@RequestBody @Valid DTO类 参数名  // DTO 中使用 @Schema
```

### 2. 描述清晰

- `description`: 用中文清晰描述参数含义
- `example`: 提供真实可用的示例值
- `required`: 准确标注是否必填

### 3. 示例值合理

- ID 类型：使用 `1`, `2` 等小数字
- 字符串：使用有意义的文本，如 `"SHOP001"`, `"A01"`
- 布尔值：使用 `"true"` 或 `"false"`
- 枚举值：使用常见的有效值

---

## ✅ 验收标准

所有接口在 Knife4j 中应满足：

- [x] 路径参数有独立的输入框
- [x] 请求参数有独立的输入框
- [x] 输入框显示参数描述
- [x] 输入框预填充示例值
- [x] 必填参数有红色星号标记
- [x] 可以直接填写并发送请求
- [x] 无需手动拼接 URL

---

**修复完成时间**: 2026-05-17  
**涉及文件**: 3个 Controller  
**修复接口数**: 18个  
**状态**: ✅ 全部完成
