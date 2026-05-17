# Shop-Service Windows 测试指南

## 📋 快速测试（推荐方式）

### 方式1：使用 Knife4j 界面（最简单）✅

1. 启动 shop-service
2. 浏览器访问：**http://localhost:8081/doc.html**
3. 在网页中直接点击"调试"按钮测试所有接口
4. 无需编写任何命令！

---

### 方式2：使用 Postman

1. 下载并安装 [Postman](https://www.postman.com/)
2. 新建请求
3. 选择请求方法（GET/POST/PUT/DELETE）
4. 输入 URL
5. 在 Body 中选择 "raw" → "JSON"，填入 JSON 数据
6. 点击 "Send" 发送请求

---

### 方式3：使用 cURL（命令行）

#### Windows CMD 注意事项

⚠️ **重要**：Windows CMD 和 PowerShell 的命令格式不同！

---

## 🔧 Windows CMD 测试示例

### 1. 获取店铺列表（GET）

```cmd
curl http://localhost:8081/api/shop/list
```

### 2. 创建店铺（POST）

```cmd
curl -X POST http://localhost:8081/api/shop -H "Content-Type: application/json" -d "{\"shopName\":\"测试店铺\",\"shopCode\":\"SHOP003\",\"address\":\"上海市浦东新区\",\"phone\":\"021-12345678\",\"businessHours\":\"11:00-22:00\",\"shopStatus\":1,\"capacity\":100}"
```

### 3. 更新店铺（PUT）

```cmd
curl -X PUT http://localhost:8081/api/shop -H "Content-Type: application/json" -d "{\"id\":1,\"shopName\":\"新名称\",\"capacity\":150}"
```

### 4. 删除店铺（DELETE）

```cmd
curl -X DELETE http://localhost:8081/api/shop/3
```

### 5. 创建桌台（POST）

```cmd
curl -X POST http://localhost:8081/api/table -H "Content-Type: application/json" -d "{\"shopId\":1,\"tableNumber\":\"A01\",\"tableName\":\"A区1号桌\",\"seats\":4,\"tableType\":1,\"tableStatus\":0,\"qrCode\":\"QR_A01\",\"location\":\"A区靠窗\",\"isAvailable\":1}"
```

### 6. 查询可用桌台（GET）

```cmd
curl http://localhost:8081/api/table/shop/1/available
```

---

## 💡 PowerShell 测试示例

PowerShell 支持多行命令，更易读：

### 1. 获取店铺列表

```powershell
curl http://localhost:8081/api/shop/list
```

### 2. 创建店铺

```powershell
curl -X POST http://localhost:8081/api/shop `
  -H "Content-Type: application/json" `
  -d '{
    "shopName": "测试店铺",
    "shopCode": "SHOP003",
    "address": "上海市浦东新区",
    "phone": "021-12345678",
    "businessHours": "11:00-22:00",
    "shopStatus": 1,
    "capacity": 100
  }'
```

### 3. 创建桌台

```powershell
curl -X POST http://localhost:8081/api/table `
  -H "Content-Type: application/json" `
  -d '{
    "shopId": 1,
    "tableNumber": "A01",
    "tableName": "A区1号桌",
    "seats": 4,
    "tableType": 1,
    "tableStatus": 0,
    "qrCode": "QR_A01",
    "location": "A区靠窗",
    "isAvailable": 1
  }'
```

---

## ⚠️ 常见错误及解决

### 错误1：CMD 中使用单引号

❌ **错误写法**：
```cmd
curl -X POST http://localhost:8081/api/shop -d '{"shopName":"test"}'
```

✅ **正确写法**：
```cmd
curl -X POST http://localhost:8081/api/shop -d "{\"shopName\":\"test\"}"
```

---

### 错误2：CMD 中使用反斜杠续行

❌ **错误写法**：
```cmd
curl -X POST http://localhost:8081/api/shop \
  -H "Content-Type: application/json"
```

✅ **正确写法（一行）**：
```cmd
curl -X POST http://localhost:8081/api/shop -H "Content-Type: application/json"
```

或使用 `^` 续行：
```cmd
curl -X POST http://localhost:8081/api/shop ^
  -H "Content-Type: application/json"
```

---

### 错误3：PowerShell 中 curl 是别名

在 PowerShell 中，`curl` 实际上是 `Invoke-WebRequest` 的别名。

✅ **解决方法1**：使用 `curl.exe`（调用真正的 curl）
```powershell
curl.exe -X POST http://localhost:8081/api/shop -H "Content-Type: application/json" -d "{}"
```

✅ **解决方法2**：使用完整路径
```powershell
C:\Windows\System32\curl.exe -X POST http://localhost:8081/api/shop ...
```

---

## 🎯 推荐测试流程

### 第一步：验证服务启动

浏览器访问：http://localhost:8081/doc.html  
如果能看到界面，说明服务正常。

### 第二步：查询现有数据

```cmd
curl http://localhost:8081/api/shop/list
```

### 第三步：测试创建功能

使用 Knife4j 界面或 Postman 创建测试数据（避免命令行转义问题）。

### 第四步：测试更新和删除

同样推荐使用图形化工具。

---

## 🛠️ 工具对比

| 工具 | 难度 | 推荐度 | 适用场景 |
|------|------|--------|---------|
| **Knife4j** | ⭐ 简单 | ⭐⭐⭐⭐⭐ | 开发调试、快速测试 |
| **Postman** | ⭐⭐ 中等 | ⭐⭐⭐⭐ | 接口管理、团队协作 |
| **cURL** | ⭐⭐⭐ 较难 | ⭐⭐⭐ | 脚本自动化、CI/CD |

---

## 📝 完整测试脚本（PowerShell）

保存为 `test-shop-service.ps1`：

```powershell
# 测试配置
$baseUrl = "http://localhost:8081"

Write-Host "=== 1. 获取店铺列表 ===" -ForegroundColor Green
curl.exe "$baseUrl/api/shop/list"

Write-Host "`n=== 2. 创建测试店铺 ===" -ForegroundColor Green
curl.exe -X POST "$baseUrl/api/shop" `
  -H "Content-Type: application/json" `
  -d '{
    "shopName": "PowerShell测试店",
    "shopCode": "SHOP_TEST",
    "address": "测试地址",
    "phone": "12345678",
    "businessHours": "09:00-21:00",
    "shopStatus": 1,
    "capacity": 50
  }'

Write-Host "`n=== 3. 获取可用桌台 ===" -ForegroundColor Green
curl.exe "$baseUrl/api/table/shop/1/available"

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
```

运行：
```powershell
.\test-shop-service.ps1
```

---

## 📚 相关文档

- [完整 API 文档](./API_TEST.md)
- [Knife4j 官方文档](https://doc.xiaominfo.com/)
- [cURL 官方文档](https://curl.se/docs/)

---

**提示**：对于日常开发测试，**强烈推荐使用 Knife4j 界面**，简单直观，无需记忆命令格式！
