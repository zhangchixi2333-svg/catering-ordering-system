# Notification Service API 测试文档

## 📋 服务说明

**服务名称**: notification-service  
**服务端口**: 8086  
**API基础路径**: `/api/notification`  

---

## 📝 API接口列表

### 1. 获取所有通知列表

**接口**: `GET /api/notification/list`

**请求示例**:
```bash
curl -X GET "http://localhost:8086/api/notification/list"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 1001,
      "title": "订单创建成功",
      "content": "您的订单ORD2026051700001已创建成功",
      "type": 1,
      "channel": 1,
      "status": 0,
      "relatedId": 1,
      "createdAt": "2026-05-18T19:30:00"
    }
  ]
}
```

---

### 2. 根据ID获取通知详情

**接口**: `GET /api/notification/{id}`

**请求示例**:
```bash
curl -X GET "http://localhost:8086/api/notification/1"
```

---

### 3. 根据用户ID获取通知列表

**接口**: `GET /api/notification/user/{userId}`

**请求示例**:
```bash
curl -X GET "http://localhost:8086/api/notification/user/1001"
```

**查询参数**:
- `status`: 筛选状态（0-未读，1-已读）
- `type`: 筛选类型（1-订单，2-支付，3-排队，4-系统）
- `page`: 页码（默认1）
- `size`: 每页数量（默认20）

**示例**:
```bash
# 获取用户的未读通知
curl -X GET "http://localhost:8086/api/notification/user/1001?status=0"

# 获取用户的订单通知
curl -X GET "http://localhost:8086/api/notification/user/1001?type=1"
```

---

### 4. 发送通知 ⭐核心接口

**接口**: `POST /api/notification`

**请求体**:
```json
{
  "userId": 1001,
  "title": "订单创建成功",
  "content": "您的订单ORD2026051700001已创建成功",
  "type": 1,
  "channel": 1,
  "relatedId": 1
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | ✅ | 用户ID |
| title | String | ✅ | 通知标题 |
| content | String | ✅ | 通知内容 |
| type | Integer | ✅ | 类型：1-订单，2-支付，3-排队，4-系统 |
| channel | Integer | ❌ | 渠道：1-WebSocket，2-短信，3-邮件（默认1） |
| relatedId | Long | ❌ | 关联ID（订单ID/排队ID等） |

**请求示例**:
```bash
curl -X POST "http://localhost:8086/api/notification" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1001,
    "title": "支付成功",
    "content": "您的订单ORD2026051700001已支付成功",
    "type": 2,
    "relatedId": 1
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

### 5. 标记通知为已读

**接口**: `PUT /api/notification/{id}/read`

**请求示例**:
```bash
curl -X PUT "http://localhost:8086/api/notification/1/read"
```

**业务规则**:
- 只能标记自己的通知
- 标记后status变为1
- 记录read_at时间

---

### 6. 批量标记已读

**接口**: `PUT /api/notification/user/{userId}/read-all`

**请求示例**:
```bash
curl -X PUT "http://localhost:8086/api/notification/user/1001/read-all"
```

**业务规则**:
- 标记该用户所有未读通知为已读
- 返回标记的数量

---

### 7. 删除通知

**接口**: `DELETE /api/notification/{id}`

**请求示例**:
```bash
curl -X DELETE "http://localhost:8086/api/notification/1"
```

**业务规则**:
- 只能删除自己的通知
- 物理删除，不可恢复

---

### 8. 批量删除通知

**接口**: `DELETE /api/notification/user/{userId}/clear`

**请求示例**:
```bash
curl -X DELETE "http://localhost:8086/api/notification/user/1001/clear"
```

**业务规则**:
- 删除该用户所有已读通知
- 保留未读通知

---

## 🧪 完整测试流程

### 测试场景1：发送和查看通知

```bash
# 1. 发送通知
curl -X POST "http://localhost:8086/api/notification" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1001,
    "title": "订单创建成功",
    "content": "您的订单ORD2026051700001已创建成功",
    "type": 1,
    "relatedId": 1
  }'

# 2. 查看用户通知列表
curl -X GET "http://localhost:8086/api/notification/user/1001"

# 3. 查看详情
curl -X GET "http://localhost:8086/api/notification/1"
```

### 测试场景2：标记已读

```bash
# 1. 标记单个通知为已读
curl -X PUT "http://localhost:8086/api/notification/1/read"

# 2. 验证状态
curl -X GET "http://localhost:8086/api/notification/1"
# 预期: status = 1 (已读)

# 3. 批量标记已读
curl -X PUT "http://localhost:8086/api/notification/user/1001/read-all"
```

### 测试场景3：清理通知

```bash
# 1. 先标记一些通知为已读
curl -X PUT "http://localhost:8086/api/notification/user/1001/read-all"

# 2. 删除所有已读通知
curl -X DELETE "http://localhost:8086/api/notification/user/1001/clear"

# 3. 验证只剩未读通知
curl -X GET "http://localhost:8086/api/notification/user/1001?status=0"
```

---

## 🔔 消息模板

### 订单相关通知

#### 订单创建成功
```json
{
  "title": "订单创建成功",
  "content": "您的订单{orderNo}已创建成功，等待支付"
}
```

#### 订单支付成功
```json
{
  "title": "支付成功",
  "content": "您的订单{orderNo}已支付成功，商家正在准备"
}
```

#### 订单完成
```json
{
  "title": "订单完成",
  "content": "您的订单{orderNo}已完成，欢迎再次光临"
}
```

### 排队相关通知

#### 取号成功
```json
{
  "title": "取号成功",
  "content": "您的排队号码是{queueNo}，预计等待{waitTime}分钟"
}
```

#### 叫号通知
```json
{
  "title": "叫号通知",
  "content": "请{queueNo}号顾客到{shopName}就餐"
}
```

---

## 📊 RabbitMQ异步处理

### 消息队列配置

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    
notification:
  queue:
    name: notification.queue
    exchange: notification.exchange
    routing-key: notification.send
```

### 发送消息到队列

```java
// 异步发送通知
rabbitTemplate.convertAndSend(
    "notification.exchange",
    "notification.send",
    notificationMessage
);
```

### 消费者处理

```java
@RabbitListener(queues = "notification.queue")
public void handleNotification(NotificationMessage message) {
    // 根据channel选择发送方式
    if (message.getChannel() == 1) {
        sendWebSocket(message);
    } else if (message.getChannel() == 2) {
        sendSMS(message);
    }
}
```

---

## 📊 WebSocket实时推送

### 连接WebSocket

```javascript
const ws = new WebSocket('ws://localhost:8086/ws/notification/1001');

ws.onmessage = (event) => {
    const notification = JSON.parse(event.data);
    console.log('收到通知:', notification);
    
    // 显示通知
    showNotification(notification.title, notification.content);
};
```

### 通知格式

```json
{
  "id": 1,
  "title": "订单创建成功",
  "content": "您的订单ORD2026051700001已创建成功",
  "type": 1,
  "relatedId": 1,
  "createdAt": "2026-05-18T19:30:00"
}
```

---

## 📊 性能测试建议

### 并发测试
```bash
# 模拟批量发送通知
ab -n 1000 -c 10 -p notification.json -T application/json http://localhost:8086/api/notification
```

### 监控指标
- 通知发送平均响应时间
- WebSocket连接数
- RabbitMQ队列长度
- 消息处理成功率

---

## 🎯 注意事项

1. **用户关联**: 所有通知都必须关联到具体的用户ID
2. **渠道选择**: 优先使用WebSocket，离线用户使用短信
3. **异步处理**: 大量通知使用RabbitMQ异步发送
4. **消息模板**: 建议使用预定义的消息模板
5. **批量操作**: 提供批量标记已读和批量删除功能

---

## 📚 相关文档

- [README.md](README.md) - 服务说明文档
- [代码恢复指南](../service_readme/CODE_RECOVERY_GUIDE.md)

---

**文档版本**: v1.0  
**最后更新**: 2026-05-18  
**维护者**: 开发团队
