# Queue Service API 测试文档

## 📋 服务说明

**服务名称**: queue-service  
**服务端口**: 8085  
**API基础路径**: `/api/queue`  

---

## 📝 API接口列表

### 1. 获取所有排队记录列表

**接口**: `GET /api/queue/list`

**请求示例**:
```bash
curl -X GET "http://localhost:8085/api/queue/list"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "queueNo": "A001",
      "shopId": 1,
      "userId": 1001,
      "userName": "张三",
      "phone": "13800138000",
      "partySize": 2,
      "queueStatus": 0,
      "priority": 0,
      "estimatedWaitTime": 15
    }
  ]
}
```

---

### 2. 根据ID获取排队记录详情

**接口**: `GET /api/queue/{id}`

**请求示例**:
```bash
curl -X GET "http://localhost:8085/api/queue/1"
```

---

### 3. 根据店铺ID获取排队记录列表

**接口**: `GET /api/queue/shop/{shopId}`

**请求示例**:
```bash
curl -X GET "http://localhost:8085/api/queue/shop/1"
```

---

### 4. 根据用户ID获取排队记录列表

**接口**: `GET /api/queue/user/{userId}`

**请求示例**:
```bash
curl -X GET "http://localhost:8085/api/queue/user/1001"
```

---

### 5. 取号 ⭐核心接口

**接口**: `POST /api/queue`

**请求体**:
```json
{
  "shopId": 1,
  "userId": 1001,
  "userName": "张三",
  "phone": "13800138000",
  "partySize": 2,
  "priority": 0
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| shopId | Long | ✅ | 店铺ID |
| userId | Long | ✅ | 用户ID |
| userName | String | ❌ | 用户姓名 |
| phone | String | ❌ | 联系电话 |
| partySize | Integer | ✅ | 就餐人数 |
| priority | Integer | ❌ | 优先级：0-普通，1-VIP（默认0） |

**请求示例**:
```bash
curl -X POST "http://localhost:8085/api/queue" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "userId": 1001,
    "userName": "张三",
    "phone": "13800138000",
    "partySize": 2
  }'
```

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "queueNo": "A001",
    "estimatedWaitTime": 15
  }
}
```

**业务规则**:
1. ✅ 验证店铺是否存在且营业中
2. ✅ 自动生成排队号码（格式：A001, A002...）
3. ✅ 计算预计等待时间
4. ✅ 检查用户是否已有等待中的排队记录

---

### 6. 取消排队

**接口**: `PUT /api/queue/{id}/cancel`

**请求示例**:
```bash
curl -X PUT "http://localhost:8085/api/queue/1/cancel"
```

**业务规则**:
- 只能取消等待中的排队记录
- 取消后状态变为"已取消"
- WebSocket推送状态更新

---

### 7. 叫号

**接口**: `PUT /api/queue/{id}/call`

**请求示例**:
```bash
curl -X PUT "http://localhost:8085/api/queue/1/call"
```

**业务规则**:
- 只能叫号等待中的记录
- 叫号后状态变为"已叫号"
- 记录叫号时间
- WebSocket推送叫号通知

---

### 8. 完成排队

**接口**: `PUT /api/queue/{id}/complete`

**请求示例**:
```bash
curl -X PUT "http://localhost:8085/api/queue/1/complete"
```

**业务规则**:
- 只能完成已叫号的记录
- 完成后状态变为"已完成"
- 记录完成时间和实际等待时间

---

### 9. 跳过

**接口**: `PUT /api/queue/{id}/skip`

**请求示例**:
```bash
curl -X PUT "http://localhost:8085/api/queue/1/skip"
```

**业务规则**:
- 跳过当前号码
- 将排队记录移到队列末尾
- 重新分配排队号码

---

## 🧪 完整测试流程

### 测试场景1：正常取号流程

```bash
# 1. 取号
curl -X POST "http://localhost:8085/api/queue" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "userId": 1001,
    "userName": "张三",
    "partySize": 2
  }'

# 2. 查询排队状态
curl -X GET "http://localhost:8085/api/queue/1"

# 3. 商家叫号
curl -X PUT "http://localhost:8085/api/queue/1/call"

# 4. 完成排队
curl -X PUT "http://localhost:8085/api/queue/1/complete"
```

### 测试场景2：取消排队

```bash
# 1. 取号
curl -X POST "http://localhost:8085/api/queue" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "userId": 1002,
    "partySize": 4
  }'

# 2. 取消排队
curl -X PUT "http://localhost:8085/api/queue/2/cancel"

# 3. 验证状态
curl -X GET "http://localhost:8085/api/queue/2"
# 预期: queueStatus = 3 (已取消)
```

### 测试场景3：查看店铺排队情况

```bash
# 查看某店铺的所有排队记录
curl -X GET "http://localhost:8085/api/queue/shop/1"

# 查看等待中的排队
curl -X GET "http://localhost:8085/api/queue/shop/1?status=0"
```

---

## 🔍 服务间调用验证

### 验证ShopFeignClient

当取号时，系统会调用shop-service验证店铺：

```java
// Queue Service内部调用
Result<ShopInfoDTO> shopResult = shopFeignClient.getShopById(request.getShopId());
if (!shop.isOpen()) {
    return Result.error("店铺当前未营业，无法取号");
}
```

**测试方法**:
1. 停止shop-service
2. 尝试取号
3. 应该收到："店铺服务暂时不可用，请稍后重试"

---

## 📊 WebSocket实时通知

### 架构说明

本系统采用微服务架构实现 WebSocket 实时推送：
- **notification-service** (端口 8086): WebSocket 服务端
- **queue-service** (端口 8085): 调用 notification-service 触发推送
- **前端**: 连接 `ws://localhost:8086/ws/notification/{userId}` 接收通知

### 连接WebSocket

```javascript
// 1. 先连接 WebSocket（替换 userId 为实际用户ID）
const ws = new WebSocket('ws://localhost:8086/ws/notification/1001');

ws.onopen = function() {
    console.log('✅ WebSocket 连接成功');
};

ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log('📨 收到通知:', data);
    
    // 根据通知类型处理
    if (data.type === 'QUEUE_CREATED') {
        alert('取号成功！排队号码: ' + data.data.queueNo);
    }
};

ws.onerror = (error) => {
    console.error('❌ WebSocket 错误:', error);
};
```

### 通知类型

| 类型 | 说明 | 触发时机 |
|------|------|----------|
| CONNECTED | 连接成功 | WebSocket 连接建立时 |
| QUEUE_CREATED | 取号成功 | 用户成功取号后（已实现） |
| QUEUE_CALLED | 叫号通知 | 商家叫号时（待实现） |
| QUEUE_COMPLETED | 排队完成 | 排队完成时（待实现） |
| QUEUE_CANCELLED | 排队取消 | 用户取消排队时（待实现） |

### 完整测试流程

#### 步骤 1: 连接 WebSocket
在浏览器控制台执行上述 JavaScript 代码

#### 步骤 2: 调用取号接口
使用 Knife4j 或 Postman 调用：`POST http://localhost:8085/api/queue`

请求体：
```json
{
  "shopId": 1,
  "userId": 1001,
  "phone": "13800138001",
  "partySize": 3,
  "queueType": 1,
  "tableType": 1,
  "remark": "测试WebSocket推送"
}
```

#### 步骤 3: 验证实时通知
查看浏览器控制台，应该收到：
```json
{
  "type": "QUEUE_CREATED",
  "title": "取号成功",
  "data": {
    "id": 1,
    "queueNo": "A001",
    "shopId": 1,
    "userId": 1001,
    ...
  },
  "timestamp": 1716000000000
}
```

### 调试技巧

#### 1. 查看在线用户数
```bash
curl http://localhost:8086/api/notification/ws/online/count
```

#### 2. 检查日志
- **queue-service**: 查看推送日志
- **notification-service**: 查看连接和推送日志

#### 3. 常见问题
- **收不到通知**: 确保 userId 一致，WebSocket 先连接再取号
- **推送失败**: 检查 notification-service 是否启动

> 📖 **详细文档**: [WebSocket 测试指南](../WEBSOCKET_TEST.md)

---

## 📊 性能测试建议

### 并发测试
```bash
# 模拟多人同时取号
ab -n 100 -c 10 -p queue.json -T application/json http://localhost:8085/api/queue
```

### 监控指标
- 取号平均响应时间
- WebSocket连接数
- 排队号码分配准确性

---

## 🎯 注意事项

1. **店铺验证**: 取号前确保店铺存在且营业中
2. **重复取号**: 同一用户在同一个店铺只能有一个等待中的排队
3. **号码分配**: 排队号码按顺序分配，格式为"A001"
4. **熔断器**: ShopFeignClient和NotificationFeignClient都有fallback保护
5. **WebSocket推送**:
   - ✅ 取号成功后自动推送通知给用户
   - ⚠️ 前端必须先连接 WebSocket，再调用取号接口
   - 🔧 推送失败不影响取号主流程（有容错机制）
   - 📖 详细测试指南: [WEBSOCKET_TEST.md](../WEBSOCKET_TEST.md)
6. **用户ID一致性**: 取号请求的 userId 必须与 WebSocket 连接时的用户ID一致

---

## 📚 相关文档

- [README.md](README.md) - 服务说明文档
- [代码恢复指南](../service_readme/CODE_RECOVERY_GUIDE.md)

---

**文档版本**: v2.0  
**最后更新**: 2026-05-18  
**维护者**: 开发团队

### 版本历史
- **v2.0** (2026-05-18): 新增 WebSocket 实时推送功能
  - ✅ 取号成功后自动推送通知
  - ✅ 通过 notification-service 实现微服务架构
  - ✅ 添加熔断降级保护
  - 📖 新增 WebSocket 测试指南
