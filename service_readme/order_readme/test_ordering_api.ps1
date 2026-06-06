# 测试点餐功能后端 API

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  点餐功能 API 测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8181/api/menu"

# 测试 1: 获取店铺分类列表
Write-Host "[测试 1] 获取店铺 1 的分类列表..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/category/shop/1" -Method Get
    Write-Host "✅ 成功！返回 $($response.data.Count) 个分类" -ForegroundColor Green
    $response.data | ForEach-Object {
        Write-Host "   - $($_.categoryName) (ID: $($_.id))" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试 2: 获取可用菜品列表
Write-Host "[测试 2] 获取店铺 1 的可用菜品..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/item/available/1" -Method Get
    Write-Host "✅ 成功！返回 $($response.data.Count) 个菜品" -ForegroundColor Green
    $response.data | Select-Object -First 5 | ForEach-Object {
        Write-Host "   - $($_.itemName): ¥$($_.price) (库存: $($_.stock))" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试 3: 根据分类获取菜品
Write-Host "[测试 3] 获取分类 1 下的菜品..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/item/category/1" -Method Get
    Write-Host "✅ 成功！返回 $($response.data.Count) 个菜品" -ForegroundColor Green
    $response.data | ForEach-Object {
        Write-Host "   - $($_.itemName)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 测试 4: 获取推荐菜品
Write-Host "[测试 4] 获取店铺 1 的推荐菜品..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/item/recommended/1" -Method Get
    Write-Host "✅ 成功！返回 $($response.data.Count) 个推荐菜品" -ForegroundColor Green
    $response.data | ForEach-Object {
        Write-Host "   - $($_.itemName) (评分: $($_.rating))" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 失败: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  测试完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
