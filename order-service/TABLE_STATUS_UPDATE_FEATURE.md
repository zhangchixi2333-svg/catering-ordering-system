# 订单创建与桌台状态自动更新功能

## 📋 功能概述

在创建订单时，如果指定了桌台ID（tableId），系统会自动将该桌台的状态更新为"已占用"（状态码1）。

## 🔄 业务流程

### 完整的订单创建流程

1. **验证店铺** - 通过 Feign 调用 shop-service 验证店铺是否存在且营业中
2. **验证排队** - 如果提供 queueNumber，验证排队记录是否存在且状态为'已叫号'
3. **自动分配桌子** - 如果是堂食且未指定 tableId，自动查询并分配空闲桌子
4. **获取桌台编号** - 如果已指定 tableId，从 shop-service 查询 table_info 表获取 tableNumber 并填充
5. **验证订单明细** - 检查 items 列表不为空
6. **服务端计算** - 自动计算订单总金额、菜品总数量和预计制作时间
7. **保存订单** - 先保存 orders 主表，再批量保存 order_item 明细表
8. **发送通知** - 通过 notification-service 推送 WebSocket 通知给用户
9. **更新排队** - 如果有关联排队，从 Redis 叫号队列移除
10. **✨ 更新桌台状态** - 如果有桌台ID，将桌台状态更新为'已占用'（状态码1）

## 🎯 使用场景

- 用户被叫号后点击"前往点菜"按钮
- 用户在点餐页面选择菜品并提交订单
- 订单自动关联排队号码，实现排队与订单的绑定
- 系统自动为用户分配空闲桌子（堂食场景）
- **订单创建成功后自动将桌台状态更新为"已占用"**

## ⚙️ 技术实现

### 1. TableFeignClient 新增接口

```java
/**
 * 更新桌台状态
 * @param id 桌台ID
 * @param request 桌台状态更新请求（包含tableStatus字段：0-空闲，1-已占用，2-预订，3-清洁中）
 * @return 更新结果
 */
@PutMapping("/{id}/status")
Result<Boolean> updateTableStatus(@PathVariable("id") Long id, @RequestBody TableStatusUpdateRequest request);
```

### 2. TableStatusUpdateRequest DTO

```java
@Data
class TableStatusUpdateRequest {
    private Integer tableStatus;  // 桌台状态：0-空闲，1-已占用，2-预订，3-清洁中
}
```

### 3. OrdersController 核心逻辑

```java
// 9. 如果有桌台ID，将桌台状态更新为"已占用"（状态码1）
if (request.getTableId() != null) {
    System.out.println("\n步骤9: 更新桌台状态为已占用...");
    try {
        TableFeignClient.TableStatusUpdateRequest statusRequest = new TableFeignClient.TableStatusUpdateRequest();
        statusRequest.setTableStatus(1); // 1-已占用
        
        Result<Boolean> updateResult = tableFeignClient.updateTableStatus(request.getTableId(), statusRequest);
        
        if (updateResult != null && Boolean.TRUE.equals(updateResult.getData())) {
            System.out.println("✅ 桌台状态更新成功 - 桌台ID: " + request.getTableId() + ", 状态: 已占用");
        } else {
            String errorMsg = updateResult != null ? updateResult.getMessage() : "返回结果为null";
            System.err.println("⚠️ 桌台状态更新失败 - 桌台ID: " + request.getTableId() + ", 原因: " + errorMsg);
        }
    } catch (Exception e) {
        // 桌台状态更新失败不影响订单创建主流程（降级策略）
        System.err.println("❌ 桌台状态更新异常 - 桌台ID: " + request.getTableId() + ", 错误: " + e.getMessage());
        System.out.println("⚠️ 桌台状态更新失败，但不影响订单创建，可稍后手动更新");
    }
}
```

## 🛡️ 容错机制

- **降级策略**：桌台状态更新失败不影响订单创建主流程
- **日志记录**：详细的控制台日志，便于调试和追踪
- **异常处理**：捕获所有异常，确保订单创建不受影响
- **手动补偿**：如果自动更新失败，可以稍后手动更新桌台状态

## 🧪 测试方法

### 方法一：使用 PowerShell 测试脚本

```powershell
cd order-service
.\test_order_with_table_status_update.ps1
```

测试脚本会：
1. 查询桌台初始状态
2. 创建订单（堂食，指定桌台）
3. 等待2秒让系统完成更新
4. 验证桌台状态是否已更新为"已占用"

### 方法二：使用 Swagger UI

1. 访问 Gateway 的 Swagger UI：`http://localhost:8080/swagger-ui.html`
2. 找到"订单管理" -> "创建订单"接口
3. 填写请求参数（必须包含 tableId）
4. 点击"Execute"执行
5. 查看控制台日志，确认桌台状态更新成功

### 方法三：使用 curl 命令

```bash
# 1. 查询桌台初始状态
curl http://localhost:8080/api/table/1

# 2. 创建订单
curl -X POST http://localhost:8080/api/order \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "userId": 1001,
    "orderType": 1,
    "tableId": 1,
    "remark": "测试订单",
    "items": [
      {
        "itemId": 1,
        "itemName": "宫保鸡丁",
        "price": 38.00,
        "quantity": 2
      }
    ]
  }'

# 3. 再次查询桌台状态，验证是否变为 1（已占用）
curl http://localhost:8080/api/table/1
```

## 📊 桌台状态说明

| 状态码 | 状态名称 | 说明 |
|--------|----------|------|
| 0 | 空闲 | 桌台可用，可以分配 |
| 1 | 已占用 | 桌台已被订单占用 |
| 2 | 预订 | 桌台已被预订 |
| 3 | 清洁中 | 桌台正在清洁 |

## 📝 注意事项

1. **仅堂食订单更新桌台状态**：外带和外卖订单不需要桌台，不会触发状态更新
2. **降级策略**：如果 shop-service 不可用，桌台状态更新会失败，但不影响订单创建
3. **手动补偿**：如果自动更新失败，可以通过 shop-service 的接口手动更新桌台状态
4. **并发控制**：当前实现没有并发锁，高并发场景下可能需要优化
5. **状态恢复**：订单完成后，需要将桌台状态恢复为"空闲"（后续可实现）

## 🔗 相关文件

- `order-service/src/main/java/org/example/orderservice/controller/OrdersController.java`
- `order-service/src/main/java/org/example/orderservice/feign/TableFeignClient.java`
- `order-service/src/main/java/org/example/orderservice/feign/TableFeignClientFallback.java`
- `shop-service/src/main/java/org/example/shopservice/controller/TableInfoController.java`
- `order-service/test_order_with_table_status_update.ps1`

## 📅 更新日期

2026-05-20
