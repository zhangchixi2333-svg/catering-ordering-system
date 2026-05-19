# 验证点餐菜单配置

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  验证点餐菜单配置" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$filesToCheck = @(
    @{Path = "src/router/index.js"; Pattern = "ordering"; Name = "路由配置"},
    @{Path = "src/stores/user.js"; Pattern = "在线点餐"; Name = "菜单配置"},
    @{Path = "src/views/OrderingView.vue"; Pattern = "ordering-view"; Name = "组件文件"}
)

$allPassed = $true

foreach ($file in $filesToCheck) {
    Write-Host "[检查] $($file.Name)..." -ForegroundColor Yellow
    
    if (Test-Path $file.Path) {
        $content = Get-Content $file.Path -Raw -Encoding UTF8
        if ($content -match [regex]::Escape($file.Pattern)) {
            Write-Host "  ✅ 通过 - 找到 '$($file.Pattern)'" -ForegroundColor Green
        } else {
            Write-Host "  ❌ 失败 - 未找到 '$($file.Pattern)'" -ForegroundColor Red
            $allPassed = $false
        }
    } else {
        Write-Host "  ❌ 失败 - 文件不存在: $($file.Path)" -ForegroundColor Red
        $allPassed = $false
    }
    Write-Host ""
}

if ($allPassed) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  ✅ 所有配置检查通过！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "如果左侧菜单仍然没有显示，请：" -ForegroundColor Yellow
    Write-Host "  1. 退出当前登录" -ForegroundColor Gray
    Write-Host "  2. 重新登录系统" -ForegroundColor Gray
    Write-Host "  3. 或者按 Ctrl+F5 硬刷新页面" -ForegroundColor Gray
} else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  ❌ 配置检查失败，请修复上述问题" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
}
