# WebSocket 实时通知测试指南

## 📋 概述

本系统使用 WebSocket 实现排队状态的实时推送，当用户取号成功后会立即收到通知。

**架构说明**：
- **notification-service** (端口 8086): WebSocket 服务端，负责推送通知
- **queue-service** (端口 8085): 调用 notification-service 触发推送
- **前端**: 连接 WebSocket 接收实时通知

---

## 🔧 前置条件

### 1. 启动必需服务

按以下顺序启动服务：

```bash
# 1. Eureka Server
cd eureka-server
mvn spring-boot:run

# 2. Shop Service
cd shop-service
mvn spring-boot:run

# 3. Notification Service
cd notification-service
mvn spring-boot:run

# 4. Queue Service
cd queue-service
mvn spring-boot:run
```

### 2. 验证服务状态

访问 Eureka 控制台: http://localhost:8761

确认以下服务都已注册且状态为 **UP**：
- ✅ eureka-server
- ✅ shop-service
- ✅ notification-service
- ✅ queue-service

---

## 🧪 WebSocket 连接测试

### 方法一：使用浏览器控制台测试

#### 1. 打开浏览器开发者工具

按 `F12` 打开开发者工具，切换到 **Console** 标签

#### 2. 连接 WebSocket

```javascript
// 替换 {userId} 为实际的用户ID，例如 1001
const ws = new WebSocket('ws://localhost:8086/ws/notification/1001');

// 监听连接成功
ws.onopen = function() {
    console.log('✅ WebSocket 连接成功');
};

// 监听消息
ws.onmessage = function(event) {
    const data = JSON.parse(event.data);
    console.log('📨 收到通知:', data);
    
    // 根据通知类型处理
    switch(data.type) {
        case 'CONNECTED':
            console.log('欢迎连接！');
            break;
        case 'QUEUE_CREATED':
            console.log('取号成功！排队号码:', data.data.queueNo);
            alert('取号成功！您的排队号码是: ' + data.data.queueNo);
            break;
        case 'QUEUE_CALLED':
            console.log('叫号通知！');
            alert('请前往店铺，您的号码 ' + data.data.queueNo + ' 已被叫到');
            break;
        case 'QUEUE_COMPLETED':
            console.log('排队完成');
            break;
        case 'QUEUE_CANCELLED':
            console.log('排队已取消');
            break;
    }
};

// 监听错误
ws.onerror = function(error) {
    console.error('❌ WebSocket 错误:', error);
};

// 监听关闭
ws.onclose = function() {
    console.log('🔒 WebSocket 连接已关闭');
};
```

#### 3. 测试取号推送

在另一个浏览器标签页或使用 Postman/Knife4j 调用取号接口：

**请求地址**: `POST http://localhost:8085/api/queue`

**请求体**:
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

**预期结果**:
1. 取号接口返回成功
2. WebSocket 控制台收到通知：
```json
{
  "type": "QUEUE_CREATED",
  "title": "取号成功",
  "data": {
    "id": 1,
    "queueNo": "A001",
    "shopId": 1,
    "userId": 1001,
    "phone": "13800138001",
    "partySize": 3,
    "queueType": 1,
    "tableType": 1,
    "queueStatus": 0,
    ...
  },
  "timestamp": 1716000000000
}
```

---

### 方法二：使用 HTML 页面测试

创建测试文件 `websocket-test.html`:

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WebSocket 通知测试</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
        }
        .status {
            padding: 10px;
            margin: 10px 0;
            border-radius: 5px;
        }
        .connected {
            background-color: #d4edda;
            color: #155724;
        }
        .disconnected {
            background-color: #f8d7da;
            color: #721c24;
        }
        .message {
            background-color: #e7f3ff;
            padding: 10px;
            margin: 10px 0;
            border-left: 4px solid #2196F3;
        }
        button {
            padding: 10px 20px;
            margin: 5px;
            cursor: pointer;
        }
        input {
            padding: 8px;
            width: 200px;
        }
    </style>
</head>
<body>
    <h1>WebSocket 实时通知测试</h1>
    
    <div>
        <label>用户ID: </label>
        <input type="number" id="userId" value="1001" placeholder="输入用户ID">
        <button onclick="connect()">连接</button>
        <button onclick="disconnect()">断开</button>
    </div>
    
    <div id="status" class="status disconnected">
        状态: 未连接
    </div>
    
    <div id="messages"></div>
    
    <script>
        let ws = null;
        
        function connect() {
            const userId = document.getElementById('userId').value;
            if (!userId) {
                alert('请输入用户ID');
                return;
            }
            
            const url = `ws://localhost:8086/ws/notification/${userId}`;
            ws = new WebSocket(url);
            
            ws.onopen = function() {
                updateStatus('已连接', true);
                addMessage('系统', 'WebSocket 连接成功', 'CONNECTED');
            };
            
            ws.onmessage = function(event) {
                const data = JSON.parse(event.data);
                addMessage(data.title, JSON.stringify(data.data, null, 2), data.type);
                
                // 弹窗提示
                if (data.type === 'QUEUE_CREATED') {
                    alert(`取号成功！\n排队号码: ${data.data.queueNo}`);
                }
            };
            
            ws.onerror = function(error) {
                console.error('WebSocket 错误:', error);
                updateStatus('连接错误', false);
            };
            
            ws.onclose = function() {
                updateStatus('已断开', false);
                addMessage('系统', 'WebSocket 连接已关闭', 'CLOSED');
            };
        }
        
        function disconnect() {
            if (ws) {
                ws.close();
            }
        }
        
        function updateStatus(text, connected) {
            const statusDiv = document.getElementById('status');
            statusDiv.textContent = `状态: ${text}`;
            statusDiv.className = `status ${connected ? 'connected' : 'disconnected'}`;
        }
        
        function addMessage(title, content, type) {
            const messagesDiv = document.getElementById('messages');
            const messageDiv = document.createElement('div');
            messageDiv.className = 'message';
            messageDiv.innerHTML = `
                <strong>[${type}] ${title}</strong><br>
                <pre>${content}</pre>
                <small>${new Date().toLocaleTimeString()}</small>
            `;
            messagesDiv.insertBefore(messageDiv, messagesDiv.firstChild);
        }
    </script>
</body>
</html>
```

**使用方法**:
1. 将文件保存到桌面
2. 双击用浏览器打开
3. 输入用户ID（如 1001）
4. 点击"连接"按钮
5. 调用取号接口测试推送

---

## 📊 通知类型说明

| 通知类型 | 说明 | 触发时机 | 数据结构 |
|---------|------|---------|---------|
| CONNECTED | 连接成功 | WebSocket 连接建立时 | null |
| QUEUE_CREATED | 取号成功 | 用户成功取号后 | QueueNumber 对象 |
| QUEUE_CALLED | 叫号通知 | 商家叫号时（待实现） | QueueNumber 对象 |
| QUEUE_COMPLETED | 排队完成 | 排队完成时（待实现） | QueueNumber 对象 |
| QUEUE_CANCELLED | 排队取消 | 用户取消排队时（待实现） | QueueNumber 对象 |

---

## 🔍 调试技巧

### 1. 查看 WebSocket 连接状态

在 notification-service 控制台查看日志：
```
INFO  - WebSocket连接建立 - 用户ID: 1001, 当前在线用户数: 1
INFO  - 消息推送成功 - 用户ID: 1001, 消息类型: QUEUE_CREATED
```

### 2. 检查在线用户数

调用接口查看当前在线用户数：
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

### 3. 排查推送失败

如果取号成功但没有收到 WebSocket 推送，检查：

#### queue-service 日志
```
WARN  - WebSocket推送失败或不在线 - 用户ID: 1001, 排队号码: A001
ERROR - 调用notification-service失败，用户ID: 1001, 通知类型: QUEUE_CREATED
```

**可能原因**：
1. notification-service 未启动
2. 用户未连接 WebSocket
3. 网络连接问题

#### notification-service 日志
```
WARN  - 用户不在线，无法推送消息 - 用户ID: 1001
```

**解决方案**：确保前端先连接 WebSocket，再调用取号接口

---

## 🎯 完整测试流程

### 场景：用户在线取号并接收通知

1. **准备阶段**
   - ✅ 启动所有服务
   - ✅ 确认 Eureka 中服务正常

2. **连接 WebSocket**
   ```javascript
   const ws = new WebSocket('ws://localhost:8086/ws/notification/1001');
   ```

3. **调用取号接口**
   - 使用 Knife4j: http://localhost:8085/doc.html
   - 或 Postman/curl

4. **验证实时通知**
   - 查看浏览器控制台
   - 检查是否收到 QUEUE_CREATED 通知

5. **检查日志**
   - queue-service: 查看推送日志
   - notification-service: 查看连接和推送日志

---

## 💡 注意事项

1. **用户ID 必须一致**
   - 取号请求中的 `userId` 必须与 WebSocket 连接时的用户ID一致

2. **WebSocket 先连接**
   - 建议前端页面加载时就建立 WebSocket 连接
   - 避免取号后才连接导致错过通知

3. **容错机制**
   - WebSocket 推送失败不影响取号主流程
   - queue-service 有熔断保护，notification-service 不可用时不会报错

4. **重连机制**
   - 前端应实现 WebSocket 断线重连
   - 建议使用 exponential backoff 策略

5. **生产环境优化**
   - 使用 wss:// (WebSocket over SSL)
   - 添加认证机制（Token）
   - 实现心跳检测

---

## 📚 相关文档

- [Queue Service API 测试](../queue_readme/API_TEST.md)
- [Notification Service API 测试](../notification_readme/API_TEST.md)
- [服务间调用流程图](../SERVICE_CALL_DIAGRAM.html)

---

**文档版本**: v1.0  
**最后更新**: 2026-05-18  
**维护者**: 开发团队
