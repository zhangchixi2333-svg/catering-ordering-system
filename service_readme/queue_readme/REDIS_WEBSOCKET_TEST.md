# Redis 排队和 WebSocket 通知功能测试指南

## 📋 测试准备

### 1. 确保服务已启动
```bash
# 检查所有服务是否运行
netstat -ano | findstr "8761 8080 8081 8085 8086 8087 3000"
```

### 2. 确保 Redis 已启动
```bash
# Windows: 检查 Redis 服务
redis-cli ping
# 应该返回: PONG
```

---

## 🔧 后端测试步骤

### 测试 1: 取号并验证 Redis 队列

#### 1.1 取号（创建排队记录）
```powershell
# 通过 Gateway 取号（需要登录 Token）
$token = "your_jwt_token_here"
$body = @{
    shopId = 1
    userId = 4
    phone = "13800138004"
    partySize = 2
    queueType = 1
    tableType = 1
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/queue" `
    -Body $body `
    -Method POST `
    -ContentType "application/json" `
    -Headers @{
        "Authorization" = "Bearer $token"
    }
```

#### 1.2 直接访问 Queue Service（无需 Token）
```powershell
$body = @{
    shopId = 1
    userId = 4
    phone = "13800138004"
    partySize = 2
    queueType = 1
    tableType = 1
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8085/api/queue" `
    -Body $body `
    -Method POST `
    -ContentType "application/json"

Write-Host "取号响应:" -ForegroundColor Green
$response | ConvertTo-Json -Depth 5
```

#### 1.3 验证 Redis 等待队列
```bash
# 连接 Redis
redis-cli

# 查看店铺 1 的等待队列
ZREVRANGE queue:waiting:1 0 -1 WITHSCORES

# 查看等待人数
ZCARD queue:waiting:1
```

**预期结果：**
- 返回排队 ID 列表，按时间戳排序
- ZCARD 返回等待人数

---

### 测试 2: 叫号并验证 Redis 队列移动

#### 2.1 获取排队列表，找到排队 ID
```powershell
# 获取店铺 1 的排队列表
$response = Invoke-RestMethod -Uri "http://localhost:8085/api/queue/shop/1"
Write-Host "排队列表:" -ForegroundColor Green
$response.data | Format-Table id, queueNo, queueStatus, shopId
```

#### 2.2 执行叫号
```powershell
# 假设排队 ID 为 1
$queueId = 1

$response = Invoke-RestMethod -Uri "http://localhost:8085/api/queue/$queueId/call" `
    -Method PUT

Write-Host "叫号响应:" -ForegroundColor Green
$response | ConvertTo-Json -Depth 5
```

#### 2.3 验证 Redis 队列移动
```bash
redis-cli

# 检查等待队列（应该减少）
ZREVRANGE queue:waiting:1 0 -1 WITHSCORES
ZCARD queue:waiting:1

# 检查叫号队列（应该增加）
ZREVRANGE queue:calling:1 0 -1 WITHSCORES
ZCARD queue:calling:1
```

**预期结果：**
- 等待队列中不再包含该排队 ID
- 叫号队列中包含该排队 ID，score 为叫号时间戳

---

### 测试 3: 完成排队

#### 3.1 完成排队
```powershell
$queueId = 1

$response = Invoke-RestMethod -Uri "http://localhost:8085/api/queue/$queueId/complete" `
    -Method PUT

Write-Host "完成排队响应:" -ForegroundColor Green
$response | ConvertTo-Json -Depth 5
```

#### 3.2 验证 Redis 队列移动
```bash
redis-cli

# 检查叫号队列（应该减少）
ZREVRANGE queue:calling:1 0 -1 WITHSCORES
ZCARD queue:calling:1

# 检查完成队列（应该增加）
ZREVRANGE queue:completed:1 0 -1 WITHSCORES
ZCARD queue:completed:1
```

---

### 测试 4: 取消排队

#### 4.1 先取一个新号
```powershell
$body = @{
    shopId = 1
    userId = 4
    phone = "13800138004"
    partySize = 3
    queueType = 1
    tableType = 2
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8085/api/queue" `
    -Body $body `
    -Method POST `
    -ContentType "application/json"

$newQueueId = $response.data.id
Write-Host "新排队 ID: $newQueueId" -ForegroundColor Cyan
```

#### 4.2 取消排队
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8085/api/queue/$newQueueId/cancel?cancelReason=用户主动取消" `
    -Method PUT

Write-Host "取消排队响应:" -ForegroundColor Green
$response | ConvertTo-Json -Depth 5
```

#### 4.3 验证 Redis 队列移除
```bash
redis-cli

# 检查等待队列（应该不包含该排队 ID）
ZRANK queue:waiting:1 $newQueueId
# 应该返回 (nil)
```

---

### 测试 5: 查询实时队列信息

#### 5.1 获取等待队列
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8085/api/queue/redis/waiting/1"
Write-Host "等待队列:" -ForegroundColor Green
$response | ConvertTo-Json -Depth 5
```

**预期响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "shopId": 1,
    "waitingCount": 3,
    "queueIds": ["12", "15", "18"]
  }
}
```

#### 5.2 获取叫号队列
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8085/api/queue/redis/calling/1"
Write-Host "叫号队列:" -ForegroundColor Green
$response | ConvertTo-Json -Depth 5
```

**预期响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "shopId": 1,
    "callingCount": 2,
    "queueIds": ["10", "11"]
  }
}
```

#### 5.3 获取排队位置
```powershell
$queueId = 12
$response = Invoke-RestMethod -Uri "http://localhost:8085/api/queue/redis/position/1/$queueId"
Write-Host "排队位置:" -ForegroundColor Green
$response | ConvertTo-Json -Depth 5
```

**预期响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "shopId": 1,
    "queueId": 12,
    "position": 1,
    "message": "前面还有1人"
  }
}
```

---

## 🌐 WebSocket 通知测试

### 测试 6: WebSocket 连接和通知

#### 6.1 使用浏览器测试 WebSocket

打开浏览器控制台（F12），执行以下 JavaScript：

```javascript
// 连接 WebSocket
const userId = 4; // 使用实际的用户 ID
const ws = new WebSocket(`ws://localhost:8086/ws/notification/${userId}`);

// 监听消息
ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log('📨 收到通知:', data);
    
    if (data.type === 'QUEUE_CREATED') {
        console.log('✅ 取号成功:', data.data.queueNo);
    } else if (data.type === 'QUEUE_CALLED') {
        console.log('🔔 叫号通知:', data.data.queueNo);
        alert(`叫号通知：${data.data.queueNo} 请前往就餐！`);
    }
};

ws.onopen = () => {
    console.log('✅ WebSocket 连接成功');
};

ws.onerror = (error) => {
    console.error('❌ WebSocket 错误:', error);
};
```

#### 6.2 触发通知

在另一个终端窗口执行取号或叫号操作，观察浏览器控制台是否收到通知。

**取号通知示例：**
```json
{
  "type": "QUEUE_CREATED",
  "title": "取号成功",
  "data": {
    "id": 20,
    "queueNo": "A20260519001",
    "shopId": 1,
    "userId": 4,
    "partySize": 2,
    "queueType": 1
  },
  "timestamp": 1779123456789
}
```

**叫号通知示例：**
```json
{
  "type": "QUEUE_CALLED",
  "title": "叫号通知",
  "data": {
    "id": 15,
    "queueNo": "A20260519005",
    "shopId": 1,
    "userId": 4,
    "partySize": 3,
    "queueType": 1
  },
  "timestamp": 1779123456789
}
```

#### 6.3 检查在线用户数
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8086/api/notification/ws/online/count"
Write-Host "在线用户数:" -ForegroundColor Green
$response | ConvertTo-Json -Depth 5
```

---

## 📊 完整测试流程

### 场景：用户取号 → 等待 → 叫号 → 完成

```powershell
# 1. 用户取号
Write-Host "=== 步骤 1: 用户取号 ===" -ForegroundColor Cyan
$body = @{
    shopId = 1
    userId = 4
    phone = "13800138004"
    partySize = 2
    queueType = 1
    tableType = 1
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8085/api/queue" `
    -Body $body `
    -Method POST `
    -ContentType "application/json"

$queueId = $response.data.id
Write-Host "排队 ID: $queueId" -ForegroundColor Green

# 2. 查看 Redis 等待队列
Write-Host "`n=== 步骤 2: 查看等待队列 ===" -ForegroundColor Cyan
redis-cli ZREVRANGE queue:waiting:1 0 -1 WITHSCORES

# 3. 等待几秒后叫号
Write-Host "`n=== 步骤 3: 叫号 ===" -ForegroundColor Cyan
Start-Sleep -Seconds 2
Invoke-RestMethod -Uri "http://localhost:8085/api/queue/$queueId/call" -Method PUT

# 4. 查看 Redis 叫号队列
Write-Host "`n=== 步骤 4: 查看叫号队列 ===" -ForegroundColor Cyan
redis-cli ZREVRANGE queue:calling:1 0 -1 WITHSCORES

# 5. 完成排队
Write-Host "`n=== 步骤 5: 完成排队 ===" -ForegroundColor Cyan
Start-Sleep -Seconds 2
Invoke-RestMethod -Uri "http://localhost:8085/api/queue/$queueId/complete" -Method PUT

# 6. 查看 Redis 完成队列
Write-Host "`n=== 步骤 6: 查看完成队列 ===" -ForegroundColor Cyan
redis-cli ZREVRANGE queue:completed:1 0 -1 WITHSCORES

Write-Host "`n✅ 完整流程测试完成！" -ForegroundColor Green
```

---

## 🔍 常见问题排查

### 问题 1: Redis 连接失败
```bash
# 检查 Redis 是否运行
redis-cli ping

# 如果没有运行，启动 Redis
# Windows: 启动 Redis 服务
net start Redis

# Linux/Mac: 
redis-server
```

### 问题 2: WebSocket 连接失败
- 检查 Notification Service 是否运行在 8086 端口
- 检查防火墙是否阻止 WebSocket 连接
- 查看后端日志是否有错误信息

### 问题 3: 通知未推送
- 检查用户是否在线（WebSocket 是否连接）
- 查看 Notification Service 日志
- 检查 Feign 调用是否成功

### 问题 4: Redis 队列为空
- 确认取号时是否正确添加到 Redis
- 检查 Queue Service 日志
- 验证 Redis Key 格式是否正确：`queue:waiting:{shopId}`

---

## 📝 测试检查清单

- [ ] Redis 服务正常运行
- [ ] 所有微服务已启动并注册到 Eureka
- [ ] 前端 Vite 开发服务器运行在 3000 端口
- [ ] 取号成功并添加到 Redis 等待队列
- [ ] 叫号成功并从等待队列移动到叫号队列
- [ ] 完成排队并从叫号队列移动到完成队列
- [ ] 取消排队并从等待队列移除
- [ ] WebSocket 连接成功
- [ ] 取号通知推送到前端
- [ ] 叫号通知推送到前端
- [ ] 前端实时显示等待人数和叫号列表
- [ ] 提示音播放正常

---

## 🎯 性能测试建议

### 并发取号测试
```powershell
# 模拟 10 个用户同时取号
for ($i = 1; $i -le 10; $i++) {
    Start-Job -ScriptBlock {
        param($userId, $phone)
        $body = @{
            shopId = 1
            userId = $userId
            phone = $phone
            partySize = 2
            queueType = 1
            tableType = 1
        } | ConvertTo-Json
        
        Invoke-RestMethod -Uri "http://localhost:8085/api/queue" `
            -Body $body `
            -Method POST `
            -ContentType "application/json"
    } -ArgumentList $i, "1380013800$i"
}

# 等待所有任务完成
Get-Job | Wait-Job | Receive-Job
```

### Redis 性能监控
```bash
# 监控 Redis 命令执行情况
redis-cli MONITOR

# 查看 Redis 内存使用
redis-cli INFO memory

# 查看 Keyspace 信息
redis-cli INFO keyspace
```

---

## ✅ 测试完成标志

当以上所有测试都通过时，说明：
1. ✅ Redis Sorted Set 排队功能正常工作
2. ✅ WebSocket 实时通知功能正常工作
3. ✅ 前后端集成完成
4. ✅ 可以投入使用

祝测试顺利！🎉
