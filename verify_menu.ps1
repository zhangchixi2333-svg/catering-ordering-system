# =============================================
# 前端菜单验证脚本
# 版本: 1.0
# 日期: 2026-05-21
# 说明: 验证数据库菜单配置是否正确
# =============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  餐饮系统 - 菜单验证脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# MySQL 配置
$mysqlUser = "root"
$mysqlPassword = "123456"
$mysqlHost = "localhost"
$mysqlPort = "3306"
$database = "catering_auth"

Write-Host "🔍 正在检查数据库菜单配置..." -ForegroundColor Yellow
Write-Host ""

# 1. 检查新菜单是否存在
Write-Host "1️⃣  检查新增菜单项..." -ForegroundColor Cyan
$query1 = "SELECT id, menu_name, path FROM sys_menu WHERE menu_code IN ('order:my', 'order:payment', 'order:all', 'shop:table') ORDER BY id;"
$result1 = mysql -u $mysqlUser -p$mysqlPassword -h $mysqlHost -P $mysqlPort $database --default-character-set=utf8mb4 -e $query1 2>$null

if ($result1) {
    Write-Host "✅ 新增菜单项：" -ForegroundColor Green
    $result1 | ForEach-Object { Write-Host "   $_" -ForegroundColor White }
} else {
    Write-Host "❌ 未找到新增菜单项" -ForegroundColor Red
}
Write-Host ""

# 2. 检查各角色菜单数量
Write-Host "2️⃣  检查角色菜单权限..." -ForegroundColor Cyan
$query2 = "SELECT r.role_name, COUNT(rm.menu_id) as menu_count FROM sys_role r LEFT JOIN sys_role_menu rm ON r.id = rm.role_id GROUP BY r.id ORDER BY r.sort_order;"
$result2 = mysql -u $mysqlUser -p$mysqlPassword -h $mysqlHost -P $mysqlPort $database --default-character-set=utf8mb4 -e $query2 2>$null

if ($result2) {
    Write-Host "✅ 角色菜单数量：" -ForegroundColor Green
    $result2 | Select-Object -Skip 1 | ForEach-Object { 
        Write-Host "   $_" -ForegroundColor White 
    }
} else {
    Write-Host "❌ 角色权限查询失败" -ForegroundColor Red
}
Write-Host ""

# 3. 检查普通用户菜单
Write-Host "3️⃣  检查普通用户菜单..." -ForegroundColor Cyan
$query3 = "SELECT m.menu_name, m.path FROM sys_menu m JOIN sys_role_menu rm ON m.id = rm.menu_id JOIN sys_user_role ur ON rm.role_id = ur.role_id WHERE ur.user_id = 1 AND m.status = 1 ORDER BY m.sort_order;"
$result3 = mysql -u $mysqlUser -p$mysqlPassword -h $mysqlHost -P $mysqlPort $database --default-character-set=utf8mb4 -e $query3 2>$null

if ($result3) {
    Write-Host "✅ 普通用户菜单列表：" -ForegroundColor Green
    $result3 | Select-Object -Skip 1 | ForEach-Object { 
        Write-Host "   $_" -ForegroundColor White 
    }
    
    # 检查是否包含新菜单
    $hasMyOrders = $result3 | Select-String "我的订单"
    $hasPaymentOrders = $result3 | Select-String "支付订单"
    
    Write-Host ""
    if ($hasMyOrders) {
        Write-Host "   ✅ 包含'我的订单'" -ForegroundColor Green
    } else {
        Write-Host "   ❌ 缺少'我的订单'" -ForegroundColor Red
    }
    
    if ($hasPaymentOrders) {
        Write-Host "   ✅ 包含'支付订单'" -ForegroundColor Green
    } else {
        Write-Host "   ❌ 缺少'支付订单'" -ForegroundColor Red
    }
} else {
    Write-Host "❌ 普通用户菜单查询失败" -ForegroundColor Red
}
Write-Host ""

# 4. 检查店员菜单
Write-Host "4️⃣  检查店员菜单..." -ForegroundColor Cyan
$query4 = "SELECT m.menu_name, m.path FROM sys_menu m JOIN sys_role_menu rm ON m.id = rm.menu_id JOIN sys_user_role ur ON rm.role_id = ur.role_id WHERE ur.user_id = 2 AND m.status = 1 ORDER BY m.sort_order;"
$result4 = mysql -u $mysqlUser -p$mysqlPassword -h $mysqlHost -P $mysqlPort $database --default-character-set=utf8mb4 -e $query4 2>$null

if ($result4) {
    Write-Host "✅ 店员菜单列表：" -ForegroundColor Green
    $result4 | Select-Object -Skip 1 | ForEach-Object { 
        Write-Host "   $_" -ForegroundColor White 
    }
    
    # 检查是否包含桌台管理
    $hasTableManagement = $result4 | Select-String "桌台管理"
    
    Write-Host ""
    if ($hasTableManagement) {
        Write-Host "   ✅ 包含'桌台管理'" -ForegroundColor Green
    } else {
        Write-Host "   ❌ 缺少'桌台管理'" -ForegroundColor Red
    }
} else {
    Write-Host "❌ 店员菜单查询失败" -ForegroundColor Red
}
Write-Host ""

# 总结
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  验证完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📝 下一步操作：" -ForegroundColor Yellow
Write-Host "   1. 重启前端服务: cd frontend && npm run dev" -ForegroundColor White
Write-Host "   2. 清除浏览器缓存: Ctrl + Shift + Delete" -ForegroundColor White
Write-Host "   3. 重新登录系统查看菜单" -ForegroundColor White
Write-Host ""

Write-Host "💡 提示：" -ForegroundColor Yellow
Write-Host "   - 如果菜单仍不显示，请检查浏览器控制台是否有错误" -ForegroundColor Gray
Write-Host "   - 确认前端路由配置正确（router/index.js）" -ForegroundColor Gray
Write-Host "   - 确认组件文件存在（views/目录下）" -ForegroundColor Gray
Write-Host ""
