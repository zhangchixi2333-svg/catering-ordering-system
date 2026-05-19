# 测试订单创建 API

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  订单创建 API 测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8083/api/order"

# 测试数据
$orderData = @{
    shopId = 1
    userId = 1001
    orderType = 1
    tableId = $null
    queueId = $null
    remark = "测试订单 - 不要辣"
    items = @(
        @{
            itemId = 1
            itemName = "宫保鸡丁"
            price = 38.00
            quantity = 2
            subtotal = 76.00
            remark = ""
        },
        @{
            itemId = 11
            itemName = "酸梅汤"
            price = 15.00
            quantity = 1
            subtotal = 15.00
            remark = ""
        }
    )
    totalAmount = 91.00
    itemCount = 3
} | ConvertTo-Json -Depth 10

Write-Host "[测试] 创建订单..." -ForegroundColor Yellow
Write-Host ""
Write-Host "请求数据:" -ForegroundColor Gray
Write-Host $orderData -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/create" `
        -Method Post `
        -Body $orderData `
        -ContentType "application/json; charset=utf-8"
    
    Write-Host "✅ 响应成功！" -ForegroundColor Green
    Write-Host ""
    Write-Host "响应数据:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 5 | Write-Host
    
    if ($response.code -eq 200) {
        Write-Host ""
        Write-Host "🎉 订单创建成功！" -ForegroundColor Green
        Write-Host "订单号: $($response.data.orderNo)" -ForegroundColor Green
        Write-Host "总金额: ¥$($response.data.totalAmount)" -ForegroundColor Green
        Write-Host "菜品数量: $($response.data.itemCount)" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "❌ 订单创建失败: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 请求失败: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "错误详情: $responseBody" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  测试完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
