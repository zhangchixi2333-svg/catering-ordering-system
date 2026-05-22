# =============================================
# 更新菜单数据脚本
# 版本: 1.0
# 日期: 2026-05-21
# 说明: 执行数据库菜单更新，添加新功能页面
# =============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  餐饮系统 - 菜单数据更新脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# MySQL 配置
$mysqlUser = "root"
$mysqlPassword = "123456"  # 请根据实际情况修改
$mysqlHost = "localhost"
$mysqlPort = "3306"
$sqlFile = ".\sql\update_menu_add_new_features.sql"

Write-Host "📋 检查 SQL 文件..." -ForegroundColor Yellow
if (-not (Test-Path $sqlFile)) {
    Write-Host "❌ SQL 文件不存在: $sqlFile" -ForegroundColor Red
    exit 1
}
Write-Host "✅ SQL 文件存在" -ForegroundColor Green
Write-Host ""

Write-Host "🔧 准备执行 SQL 更新..." -ForegroundColor Yellow
Write-Host "   数据库: catering_auth" -ForegroundColor Gray
Write-Host "   主机: $mysqlHost:$mysqlPort" -ForegroundColor Gray
Write-Host "   用户: $mysqlUser" -ForegroundColor Gray
Write-Host ""

# 提示用户确认
Write-Host "⚠️  此操作将更新数据库菜单数据，是否继续？" -ForegroundColor Yellow
$confirmation = Read-Host "请输入 'yes' 确认继续"

if ($confirmation -ne "yes") {
    Write-Host "❌ 操作已取消" -ForegroundColor Red
    exit 0
}
Write-Host ""

# 执行 SQL 脚本
Write-Host "🚀 正在执行 SQL 更新..." -ForegroundColor Cyan
try {
    $command = "mysql -u $mysqlUser -p$mysqlPassword -h $mysqlHost -P $mysqlPort < `"$sqlFile`""
    
    Write-Host "执行命令: $command" -ForegroundColor Gray
    Invoke-Expression $command
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  ✅ 菜单更新成功！" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "📝 下一步操作：" -ForegroundColor Cyan
        Write-Host "   1. 重启前端服务 (npm run dev)" -ForegroundColor White
        Write-Host "   2. 清除浏览器缓存 (Ctrl+Shift+Delete)" -ForegroundColor White
        Write-Host "   3. 重新登录系统查看新菜单" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "❌ SQL 执行失败，退出码: $LASTEXITCODE" -ForegroundColor Red
        Write-Host "请检查 MySQL 连接信息和 SQL 语法" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host ""
    Write-Host "❌ 执行出错: $_" -ForegroundColor Red
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  脚本执行完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
