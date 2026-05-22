# 订单创建与排队状态更新测试脚本
# 功能：测试创建订单后，排队状态是否自动更新为"已入座"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "订单创建与排队状态更新测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 配置
$GATEWAY_URL = "http://localhost:8080"
$SHOP_ID = 1
$USER_ID = 1001
$QUEUE_NUMBER = "A001"  # 请根据实际数据库中的排队号码修改
$TABLE_ID = 1  # 请根据实际数据库中的桌台ID修改

Write-Host "【步骤1】查询排队初始状态..." -ForegroundColor Yellow
$queueUrl = "$GATEWAY_URL/api/queue/no/$QUEUE_NUMBER"
Write-Host "请求URL: $queueUrl" -ForegroundColor Gray

try {
    $queueResponse = Invoke-RestMethod -Uri $queueUrl -Method Get -ContentType "application/json"
    Write-Host "✅ 排队信息查询成功" -ForegroundColor Green
    Write-Host "排队ID: $($queueResponse.data.id)" -ForegroundColor White
    Write-Host "排队号码: $($queueResponse.data.queueNo)" -ForegroundColor White
    Write-Host "当前状态: $($queueResponse.data.queueStatus) (0-等待中, 1-已叫号, 2-已入座, 3-已取消, 4-已过号)" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "❌ 查询排队信息失败: $_" -ForegroundColor Red
    Write-Host "提示：请确保 queue-service 已启动，且排队号码存在" -ForegroundColor Yellow
    exit 1
}

Write-Host "【步骤2】创建订单（堂食，指定桌台和排队号码）..." -ForegroundColor Yellow

# 构建订单创建请求
$orderRequest = @{
    shopId = $SHOP_ID
    userId = $USER_ID
    orderType = 1  # 1-堂食
    tableId = $TABLE_ID
    queueNumber = $QUEUE_NUMBER
    remark = "测试订单 - 验证排队状态更新"
    items = @(
        @{
            itemId = 1
            itemName = "宫保鸡丁"
            price = 38.00
            quantity = 2
            remark = "微辣"
        },
        @{
            itemId = 5
            itemName = "酸辣汤"
            price = 18.00
            quantity = 1
            remark = ""
        }
    )
} | ConvertTo-Json -Depth 10

$orderUrl = "$GATEWAY_URL/api/order"
Write-Host "请求URL: $orderUrl" -ForegroundColor Gray
Write-Host "请求体:" -ForegroundColor Gray
Write-Host $orderRequest -ForegroundColor DarkGray
Write-Host ""

try {
    $orderResponse = Invoke-RestMethod -Uri $orderUrl -Method Post -Body $orderRequest -ContentType "application/json; charset=utf-8"
    
    if ($orderResponse.code -eq 200) {
        Write-Host "✅ 订单创建成功" -ForegroundColor Green
        Write-Host "响应消息: $($orderResponse.message)" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Host "❌ 订单创建失败" -ForegroundColor Red
        Write-Host "错误代码: $($orderResponse.code)" -ForegroundColor Red
        Write-Host "错误消息: $($orderResponse.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 订单创建请求失败: $_" -ForegroundColor Red
    Write-Host "详细错误: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "等待2秒，让系统完成排队状态更新..." -ForegroundColor Cyan
Start-Sleep -Seconds 2

Write-Host ""
Write-Host "【步骤3】验证排队状态是否已更新为'已入座'..." -ForegroundColor Yellow
Write-Host "请求URL: $queueUrl" -ForegroundColor Gray

try {
    $queueResponseAfter = Invoke-RestMethod -Uri $queueUrl -Method Get -ContentType "application/json"
    Write-Host "✅ 排队信息查询成功" -ForegroundColor Green
    Write-Host "排队ID: $($queueResponseAfter.data.id)" -ForegroundColor White
    Write-Host "排队号码: $($queueResponseAfter.data.queueNo)" -ForegroundColor White
    Write-Host "更新后状态: $($queueResponseAfter.data.queueStatus) (0-等待中, 1-已叫号, 2-已入座, 3-已取消, 4-已过号)" -ForegroundColor White
    Write-Host ""
    
    if ($queueResponseAfter.data.queueStatus -eq 2) {
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "✅ 测试通过！排队状态已成功更新为'已入座'" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
    } else {
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "❌ 测试失败！排队状态未更新为'已入座'" -ForegroundColor Red
        Write-Host "期望值: 2 (已入座)" -ForegroundColor Red
        Write-Host "实际值: $($queueResponseAfter.data.queueStatus)" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 查询排队状态失败: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "测试完成！" -ForegroundColor Cyan
