# WebSocket 推送问题排查指南

## ❓ 问题现象

取号成功后没有收到 WebSocket 推送通知，也没有调用 notification-service。

---

## 🔍 排查步骤

### 第一步：检查请求参数

**关键问题**: 取号请求中必须包含 `userId` 字段！

#### ❌ 错误示例（缺少 userId）
```json
{
  "shopId": 1,
  "phone": "13800138001",
  "partySize": 3,
  "queueType": 1,
  "tableType": 1
}
```

**结果**: 取号成功，但不会推送 WebSocket 通知，日志显示：
```
WARN  - 取号成功但未提供用户ID，无法推送WebSocket通知 - 排队号码: A001
```

#### ✅ 正确示例（包含 userId）
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

**结果**: 取号成功并推送 WebSocket 通知。

---

### 第二步：检查服务启动状态

确保以下服务都已启动且正常运行：

```bash
# 1. Eureka Server (端口 8761)
# 访问: http://localhost:8761

# 2. Shop Service (端口 8081)
# 验证: curl http://localhost:8081/api/shop/1

# 3. Notification Service (端口 8086)
# 验证: curl http://localhost:8086/api/notification/ws/online/count

# 4. Queue Service (端口 8085)
# 验证: curl http://localhost:8085/api/queue/list
```

在 Eureka 控制台确认所有服务状态为 **UP**。

---

### 第三步：查看 queue-service 日志

取号时观察 queue-service 的控制台输出：

#### 场景 1: 没有 userId
```
WARN  - 取号成功但未提供用户ID，无法推送WebSocket通知 - 排队号码: A001
```
**解决方案**: 在请求中添加 `userId` 字段

#### 场景 2: 开始推送
```
INFO  - 开始推送WebSocket通知 - 用户ID: 1001, 排队号码: A001
```
继续观察后续日志...

#### 场景 3: 推送成功
```
INFO  - ✅ WebSocket推送成功 - 用户ID: 1001, 排队号码: A001
```

#### 场景 4: 推送失败（用户不在线）
```
WARN  - ⚠️ WebSocket推送失败或不在线 - 用户ID: 1001, 排队号码: A001, 原因: 用户不在线，无法推送通知
```
**说明**: 这是正常的，因为前端还没有连接 WebSocket

#### 场景 5: 推送异常（notification-service 未启动）
```
ERROR - ❌ WebSocket推送异常 - 用户ID: 1001, 排队号码: A001, 错误: ...
ERROR - 调用notification-service失败，用户ID: 1001, 通知类型: QUEUE_CREATED
```
**解决方案**: 启动 notification-service

---

### 第四步：查看 notification-service 日志

如果 Feign 调用成功，notification-service 应该显示：

```
INFO  - WebSocket连接建立 - 用户ID: 1001, 当前在线用户数: 1
DEBUG - 消息推送成功 - 用户ID: 1001, 消息类型: QUEUE_CREATED
```

如果没有看到这些日志，说明：
1. 前端没有连接 WebSocket
2. 或者 Feign 调用失败了

---

### 第五步：测试 WebSocket 连接

#### 方法 1: 使用浏览器控制台

1. 打开浏览器，按 F12 打开开发者工具
2. 在 Console 标签执行：

```javascript
// 连接 WebSocket（userId 必须与取号请求一致）
const ws = new WebSocket('ws://localhost:8086/ws/notification/1001');

ws.onopen = function() {
    console.log('✅ WebSocket 连接成功');
};

ws.onmessage = function(event) {
    const data = JSON.parse(event.data);
    console.log('📨 收到通知:', data);
    
    if (data.type === 'QUEUE_CREATED') {
        alert('取号成功！排队号码: ' + data.data.queueNo);
    }
};

ws.onerror = function(error) {
    console.error('❌ WebSocket 错误:', error);
};
```

3. 保持 WebSocket 连接，然后调用取号接口
4. 应该在控制台看到通知

#### 方法 2: 使用 HTML 测试页面

创建文件 `test-websocket.html`：

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>WebSocket 测试</title>
</head>
<body>
    <h1>WebSocket 通知测试</h1>
    <button onclick="connect()">连接 WebSocket</button>
    <div id="status"></div>
    <div id="messages"></div>
    
    <script>
        let ws;
        
        function connect() {
            ws = new WebSocket('ws://localhost:8086/ws/notification/1001');
            
            ws.onopen = function() {
                document.getElementById('status').innerHTML = '✅ 已连接';
                console.log('WebSocket 连接成功');
            };
            
            ws.onmessage = function(event) {
                const data = JSON.parse(event.data);
                const msg = document.createElement('div');
                msg.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
                document.getElementById('messages').appendChild(msg);
                
                if (data.type === 'QUEUE_CREATED') {
                    alert('取号成功！号码: ' + data.data.queueNo);
                }
            };
            
            ws.onerror = function(error) {
                document.getElementById('status').innerHTML = '❌ 连接错误';
                console.error('WebSocket 错误:', error);
            };
        }
    </script>
</body>
</html>
```

双击打开，点击"连接 WebSocket"按钮，然后调用取号接口。

---

## 🧪 完整测试流程

### 1. 启动所有服务
```bash
# 按顺序启动
eureka-server → shop-service → notification-service → queue-service
```

### 2. 连接 WebSocket
在浏览器中执行 JavaScript 代码连接 WebSocket

### 3. 调用取号接口

**方式 1: 使用 Knife4j**
- 访问: http://localhost:8085/doc.html
- 找到 "排队管理" -> "排队取号（验证店铺 + WebSocket推送）"
- 填入参数（**必须包含 userId**）
- 点击 "发送"

**方式 2: 使用 curl**
```bash
curl -X POST "http://localhost:8085/api/queue" \
  -H "Content-Type: application/json" \
  -d '{
    "shopId": 1,
    "userId": 1001,
    "phone": "13800138001",
    "partySize": 3,
    "queueType": 1,
    "tableType": 1,
    "remark": "测试WebSocket推送"
  }'
```

**方式 3: 使用 Postman**
- Method: POST
- URL: http://localhost:8085/api/queue
- Body (raw JSON):
```json
{
  "shopId": 1,
  "userId": 1001,
  "phone": "13800138001",
  "partySize": 3,
  "queueType": 1,
  "tableType": 1
}
```

### 4. 验证结果

#### 预期结果 1: 浏览器收到通知
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

#### 预期结果 2: queue-service 日志
```
INFO  - 开始推送WebSocket通知 - 用户ID: 1001, 排队号码: A001
INFO  - ✅ WebSocket推送成功 - 用户ID: 1001, 排队号码: A001
```

#### 预期结果 3: notification-service 日志
```
INFO  - WebSocket连接建立 - 用户ID: 1001, 当前在线用户数: 1
DEBUG - 消息推送成功 - 用户ID: 1001, 消息类型: QUEUE_CREATED
```

---

## 🐛 常见问题

### Q1: 日志显示 "取号成功但未提供用户ID"
**原因**: 请求中没有传入 `userId` 字段  
**解决**: 在取号请求中添加 `"userId": 1001`

### Q2: 日志显示 "调用notification-service失败"
**原因**: notification-service 未启动或不可用  
**解决**: 
1. 启动 notification-service
2. 检查 Eureka 中服务状态
3. 验证接口: `curl http://localhost:8086/api/notification/ws/online/count`

### Q3: 日志显示 "用户不在线，无法推送通知"
**原因**: 前端没有连接 WebSocket  
**解决**: 先连接 WebSocket，再调用取号接口

### Q4: Feign 调用触发 Fallback
**原因**: 
- notification-service 未注册到 Eureka
- 网络超时
- 服务不可用

**解决**:
1. 检查 Eureka 控制台
2. 查看 notification-service 启动日志
3. 检查防火墙/网络配置

### Q5: WebSocket 连接失败
**原因**:
- notification-service 未启动
- 端口被占用
- WebSocket 配置错误

**解决**:
1. 确认服务启动: `netstat -ano | findstr :8086`
2. 检查启动日志是否有 WebSocket 相关错误
3. 尝试重新连接

---

## 📊 调试技巧

### 1. 启用 Feign 详细日志

在 `queue-service/application.yml` 中已配置：
```yaml
logging:
  level:
    org.example.queueservice.feign.NotificationFeignClient: DEBUG
    feign: DEBUG
```

### 2. 查看在线用户数
```bash
curl http://localhost:8086/api/notification/ws/online/count
```

响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

### 3. 手动测试 notification-service 接口
```bash
curl -X POST "http://localhost:8086/api/notification/ws/push/queue" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1001,
    "notificationType": "QUEUE_CREATED",
    "data": {
      "queueNo": "TEST001",
      "shopId": 1
    }
  }'
```

---

## ✅ 验证清单

- [ ] 所有服务已启动（Eureka, shop, notification, queue）
- [ ] Eureka 控制台显示所有服务状态为 UP
- [ ] 取号请求中包含 `userId` 字段
- [ ] 前端已连接 WebSocket（`ws://localhost:8086/ws/notification/{userId}`）
- [ ] queue-service 日志显示 "开始推送WebSocket通知"
- [ ] notification-service 日志显示 "消息推送成功"
- [ ] 浏览器控制台收到 QUEUE_CREATED 通知

---

## 📝 总结

**最常见的原因**:
1. ❌ **忘记传 userId** - 检查请求参数
2. ❌ **notification-service 未启动** - 启动服务
3. ❌ **前端未连接 WebSocket** - 先连接再取号
4. ❌ **userId 不一致** - 确保取号请求和 WebSocket 连接的 userId 相同

按照上述步骤逐一排查，一定能找到问题所在！

---

**文档版本**: v1.0  
**最后更新**: 2026-05-18  
**维护者**: 开发团队
