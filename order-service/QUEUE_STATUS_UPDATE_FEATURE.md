# 订单创建与排队状态自动更新功能

## 📋 功能概述

在创建订单时，如果指定了排队号码（queueNumber），系统会自动将该排队记录的状态更新为"已入座"（状态码2）。

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
10. **更新桌台状态** - 如果有桌台ID，将桌台状态更新为'已占用'（状态码1）
11. **✨ 更新排队状态** - 如果有排队号码，将排队状态更新为'已入座'（状态码2）

## 🎯 使用场景

- 用户被叫号后点击"前往点菜"按钮
- 用户在点餐页面选择菜品并提交订单
- 订单自动关联排队号码，实现排队与订单的绑定
- 系统自动为用户分配空闲桌子（堂食场景）
- **订单创建成功后自动将桌台状态更新为"已占用"**
- **订单创建成功后自动将排队状态更新为"已入座"**

## ⚙️ 技术实现

### 1. QueueFeignClient 新增接口

```java
/**
 * 更新排队状态
 * @param id 排队ID
 * @param queueStatus 排队状态：0-等待中，1-已叫号，2-已入座，3-已取消，4-已过号
 * @return 更新结果
 */
@PutMapping("/api/queue/{id}/status")
Result<Boolean> updateQueueStatus(@PathVariable("id") Long id, @RequestParam("queueStatus") Integer queueStatus);
```

### 2. OrdersController 核心逻辑

```java
// 10. 如果有排队号码，将排队状态更新为"已入座"（状态码2）
if (request.getQueueNumber() != null) {
    System.out.println("\n步骤10: 更新排队状态为已入座...");
    try {
        // 先根据排队号码获取排队信息
        Result<QueueFeignClient.QueueInfoDTO> queueResult = queueFeignClient.getQueueByNo(request.getQueueNumber());
        
        if (queueResult != null && queueResult.getData() != null) {
            Long queueId = queueResult.getData().getId();
            System.out.println("获取到排队ID: " + queueId);
            
            // 更新排队状态为已入座（2）
            Result<Boolean> updateResult = queueFeignClient.updateQueueStatus(queueId, 2);
            
            if (updateResult != null && Boolean.TRUE.equals(updateResult.getData())) {
                System.out.println("✅ 排队状态更新成功 - 排队号码: " + request.getQueueNumber() + ", 状态: 已入座");
            } else {
                String errorMsg = updateResult != null ? updateResult.getMessage() : "返回结果为null";
                System.err.println("⚠️ 排队状态更新失败 - 排队号码: " + request.getQueueNumber() + ", 原因: " + errorMsg);
            }
        } else {
            System.err.println("⚠️ 无法获取排队信息 - 排队号码: " + request.getQueueNumber());
        }
    } catch (Exception e) {
        // 排队状态更新失败不影响订单创建主流程（降级策略）
        System.err.println("❌ 排队状态更新异常 - 排队号码: " + request.getQueueNumber() + ", 错误: " + e.getMessage());
        System.out.println("⚠️ 排队状态更新失败，但不影响订单创建，可稍后手动更新");
    }
}
```

## 🛡️ 容错机制

- **降级策略**：排队状态更新失败不影响订单创建主流程
- **日志记录**：详细的控制台日志，便于调试和追踪
- **异常处理**：捕获所有异常，确保订单创建不受影响
- **手动补偿**：如果自动更新失败，可以稍后手动更新排队状态

## 🧪 测试方法

### 方法一：使用 PowerShell 测试脚本

```powershell
cd order-service
.\test_order_with_queue_status_update.ps1
```

测试脚本会：
1. 查询排队初始状态
2. 创建订单（堂食，指定桌台和排队号码）
3. 等待2秒让系统完成更新
4. 验证排队状态是否已更新为"已入座"

### 方法二：使用 Swagger UI

1. 访问 Gateway 的 Swagger UI：`http://localhost:8080/swagger-ui.html`
2. 找到"订单管理" -> "创建订单"接口
3. 填写请求参数（必须包含 queueNumber）
4. 点击"Execute"执行
5. 查看控制台日志，确认排队状态更新成功

### 方法三：使用 curl 命令

```bash
# 1. 查询排队初始状态
curl http://localhost:8080/api/queue/no/A001

# 2. 创建订单
curl -X POST http://localhost:8080/api/order \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "userId": 1001,
    "orderType": 1,
    "tableId": 1,
    "queueNumber": "A001",
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

# 3. 再次查询排队状态，验证是否变为 2（已入座）
curl http://localhost:8080/api/queue/no/A001
```

## 📊 排队状态说明

| 状态码 | 状态名称 | 说明 |
|--------|----------|------|
| 0 | 等待中 | 用户已取号，正在排队等待 |
| 1 | 已叫号 | 店员已叫号，等待用户入座 |
| 2 | 已入座 | 用户已入座，开始点餐 |
| 3 | 已取消 | 用户取消排队 |
| 4 | 已过号 | 用户未及时入座，已过号 |

## 📝 注意事项

1. **仅当提供queueNumber时更新**：如果订单没有关联排队号码，不会触发状态更新
2. **降级策略**：如果 queue-service 不可用，排队状态更新会失败，但不影响订单创建
3. **手动补偿**：如果自动更新失败，可以通过 queue-service 的接口手动更新排队状态
4. **状态流转**：排队状态应按照 0(等待中) → 1(已叫号) → 2(已入座) 的顺序流转
5. **并发控制**：当前实现没有并发锁，高并发场景下可能需要优化

## 🔗 相关文件

- `order-service/src/main/java/org/example/orderservice/controller/OrdersController.java`
- `order-service/src/main/java/org/example/orderservice/feign/QueueFeignClient.java`
- `order-service/src/main/java/org/example/orderservice/feign/QueueFeignClientFallback.java`
- `queue-service/src/main/java/org/example/queueservice/controller/QueueNumberController.java`
- `order-service/test_order_with_queue_status_update.ps1`

## 📅 更新日期

2026-05-20
