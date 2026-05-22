# 桌台管理功能测试脚本

## 测试环境
- 基础URL: http://localhost:8080/api
- 店铺ID: 1

## 测试用例

### 1. 创建桌台
```powershell
# 创建普通桌
$tableData = @{
    shopId = 1
    tableNumber = "A01"
    tableName = "A区1号桌"
    seats = 4
    tableType = 1
    location = "A区靠窗"
    isAvailable = 1
}

Invoke-RestMethod -Uri "http://localhost:8080/api/table" -Method POST -Body ($tableData | ConvertTo-Json) -ContentType "application/json"

# 创建卡座
$tableData2 = @{
    shopId = 1
    tableNumber = "B01"
    tableName = "B区卡座1"
    seats = 6
    tableType = 2
    location = "B区"
    isAvailable = 1
}

Invoke-RestMethod -Uri "http://localhost:8080/api/table" -Method POST -Body ($tableData2 | ConvertTo-Json) -ContentType "application/json"

# 创建包厢
$tableData3 = @{
    shopId = 1
    tableNumber = "VIP01"
    tableName = "VIP包厢1"
    seats = 10
    tableType = 3
    location = "VIP区"
    isAvailable = 1
}

Invoke-RestMethod -Uri "http://localhost:8080/api/table" -Method POST -Body ($tableData3 | ConvertTo-Json) -ContentType "application/json"
```

### 2. 查询桌台列表
```powershell
# 查询店铺所有桌台
Invoke-RestMethod -Uri "http://localhost:8080/api/table/shop/1" -Method GET

# 查询店铺可用桌台
Invoke-RestMethod -Uri "http://localhost:8080/api/table/shop/1/available" -Method GET
```

### 3. 查询单个桌台
```powershell
# 根据ID查询
Invoke-RestMethod -Uri "http://localhost:8080/api/table/1" -Method GET

# 根据桌台编号查询
Invoke-RestMethod -Uri "http://localhost:8080/api/table/shop/1/number/A01" -Method GET
```

### 4. 更新桌台信息
```powershell
$updateData = @{
    id = 1
    shopId = 1
    tableNumber = "A01"
    tableName = "A区1号桌（已更新）"
    seats = 6
    tableType = 1
    location = "A区靠窗"
    isAvailable = 1
}

Invoke-RestMethod -Uri "http://localhost:8080/api/table" -Method PUT -Body ($updateData | ConvertTo-Json) -ContentType "application/json"
```

### 5. 更新桌台状态
```powershell
# 更新为已占用
$statusData = @{
    tableStatus = 1
}

Invoke-RestMethod -Uri "http://localhost:8080/api/table/1/status" -Method PUT -Body ($statusData | ConvertTo-Json) -ContentType "application/json"

# 更新为空闲
$statusData2 = @{
    tableStatus = 0
}

Invoke-RestMethod -Uri "http://localhost:8080/api/table/1/status" -Method PUT -Body ($statusData2 | ConvertTo-Json) -ContentType "application/json"
```

### 6. 清洁流程测试
```powershell
# 先将桌台设置为已占用
$statusData = @{
    tableStatus = 1
}
Invoke-RestMethod -Uri "http://localhost:8080/api/table/1/status" -Method PUT -Body ($statusData | ConvertTo-Json) -ContentType "application/json"

# 开始清洁
Invoke-RestMethod -Uri "http://localhost:8080/api/table/1/start-cleaning" -Method PUT

# 完成清洁
Invoke-RestMethod -Uri "http://localhost:8080/api/table/1/complete-cleaning" -Method PUT
```

### 7. 删除桌台
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/table/3" -Method DELETE
```

## 完整测试流程

```powershell
# 1. 创建多个桌台
Write-Host "=== 创建桌台 ===" -ForegroundColor Green

$tables = @(
    @{ shopId = 1; tableNumber = "A01"; tableName = "A区1号桌"; seats = 4; tableType = 1; location = "A区靠窗"; isAvailable = 1 },
    @{ shopId = 1; tableNumber = "A02"; tableName = "A区2号桌"; seats = 4; tableType = 1; location = "A区靠窗"; isAvailable = 1 },
    @{ shopId = 1; tableNumber = "B01"; tableName = "B区卡座1"; seats = 6; tableType = 2; location = "B区"; isAvailable = 1 },
    @{ shopId = 1; tableNumber = "B02"; tableName = "B区卡座2"; seats = 6; tableType = 2; location = "B区"; isAvailable = 1 },
    @{ shopId = 1; tableNumber = "VIP01"; tableName = "VIP包厢1"; seats = 10; tableType = 3; location = "VIP区"; isAvailable = 1 },
    @{ shopId = 1; tableNumber = "BAR01"; tableName = "吧台1"; seats = 2; tableType = 4; location = "吧台区"; isAvailable = 1 }
)

foreach ($table in $tables) {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/table" -Method POST -Body ($table | ConvertTo-Json) -ContentType "application/json"
    Write-Host "创建桌台 $($table.tableNumber): $($result.message)" -ForegroundColor Cyan
}

Start-Sleep -Seconds 2

# 2. 查询桌台列表
Write-Host "`n=== 查询桌台列表 ===" -ForegroundColor Green
$result = Invoke-RestMethod -Uri "http://localhost:8080/api/table/shop/1" -Method GET
Write-Host "店铺1共有 $($result.data.Count) 个桌台" -ForegroundColor Cyan
$result.data | ForEach-Object {
    Write-Host "  - $($_.tableNumber): $($_.tableName), $($_.seats)人座, 状态: $($_.tableStatus)" -ForegroundColor Yellow
}

# 3. 模拟清洁流程
Write-Host "`n=== 模拟清洁流程 ===" -ForegroundColor Green

# 获取第一个桌台ID
$tableId = $result.data[0].id
$tableNumber = $result.data[0].tableNumber

Write-Host "桌台: $tableNumber (ID: $tableId)" -ForegroundColor Cyan

# 设置为已占用
Write-Host "1. 设置为已占用状态" -ForegroundColor Yellow
$statusData = @{ tableStatus = 1 }
Invoke-RestMethod -Uri "http://localhost:8080/api/table/$tableId/status" -Method PUT -Body ($statusData | ConvertTo-Json) -ContentType "application/json"
Start-Sleep -Seconds 1

# 开始清洁
Write-Host "2. 开始清洁" -ForegroundColor Yellow
Invoke-RestMethod -Uri "http://localhost:8080/api/table/$tableId/start-cleaning" -Method PUT
Start-Sleep -Seconds 1

# 完成清洁
Write-Host "3. 完成清洁" -ForegroundColor Yellow
Invoke-RestMethod -Uri "http://localhost:8080/api/table/$tableId/complete-cleaning" -Method PUT
Start-Sleep -Seconds 1

# 4. 验证状态
Write-Host "`n=== 验证最终状态 ===" -ForegroundColor Green
$result = Invoke-RestMethod -Uri "http://localhost:8080/api/table/$tableId" -Method GET
Write-Host "桌台 $tableNumber 当前状态: $($result.data.tableStatus) (0=空闲, 1=已占用, 2=清洁中)" -ForegroundColor Cyan

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
```

## 预期结果

### 成功响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "shopId": 1,
    "tableNumber": "A01",
    "tableName": "A区1号桌",
    "seats": 4,
    "tableType": 1,
    "tableStatus": 0,
    "location": "A区靠窗",
    "isAvailable": 1,
    "createdAt": "2025-01-01T10:00:00",
    "updatedAt": "2025-01-01T10:00:00"
  }
}
```

### 错误响应格式
```json
{
  "code": 500,
  "message": "错误信息",
  "data": null
}
```

## 桌台状态说明
- 0: 空闲
- 1: 已占用
- 2: 清洁中
- 3: 预订

## 桌台类型说明
- 1: 普通桌
- 2: 卡座
- 3: 包厢
- 4: 吧台

## 注意事项
1. 确保服务已启动
2. 确保数据库中存在店铺ID为1的店铺
3. 桌台编号在同一店铺内必须唯一
4. 只有"已占用"状态的桌台才能开始清洁
5. 只有"清洁中"状态的桌台才能完成清洁
6. 删除桌台前请确保没有关联的订单