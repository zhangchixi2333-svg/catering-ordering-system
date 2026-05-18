# 前端菜单跳转验证脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  前端菜单跳转验证测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api"

# 测试用户列表
$testUsers = @(
    @{ username = "user"; password = "123456"; role = "普通用户" },
    @{ username = "staff"; password = "123456"; role = "店员" },
    @{ username = "manager"; password = "123456"; role = "店长" },
    @{ username = "admin"; password = "123456"; role = "超级管理员" }
)

foreach ($testUser in $testUsers) {
    Write-Host "`n[$($testUser.role)] 登录测试..." -ForegroundColor Yellow
    
    try {
        # 登录
        $loginData = @{
            username = $testUser.username
            password = $testUser.password
        } | ConvertTo-Json
        
        $loginResult = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
            -Method Post `
            -Body $loginData `
            -ContentType "application/json"
        
        Write-Host "✅ 登录成功" -ForegroundColor Green
        
        # 获取菜单
        $menus = $loginResult.data.menus | Where-Object { $_.menuType -eq 2 }
        
        if ($menus) {
            Write-Host "`n   菜单列表:" -ForegroundColor Cyan
            foreach ($menu in $menus) {
                Write-Host "   $($menu.icon) $($menu.menuName) -> $($menu.path)" -ForegroundColor White
            }
        } else {
            Write-Host "   ⚠️  没有可用菜单" -ForegroundColor Yellow
        }
        
    } catch {
        Write-Host "❌ 登录失败: $_" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  验证完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`n请检查以上菜单路径是否正确：" -ForegroundColor Yellow
Write-Host "✓ 所有路径应该以 / 开头" -ForegroundColor Green
Write-Host "✓ 路径应该与前端路由配置一致" -ForegroundColor Green
Write-Host "✓ 不应该出现 /queue/take, /queue/call 等旧路径" -ForegroundColor Green
Write-Host "✓ 用户管理和角色管理应该被隐藏（visible=0）" -ForegroundColor Green
