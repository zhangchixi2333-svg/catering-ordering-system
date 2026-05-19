# 前后端联调测试脚本
# 测试点餐页面完整流程

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   前后端联调测试 - 点餐功能" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:3000"

Write-Host "📋 测试步骤:" -ForegroundColor Yellow
Write-Host "1. 访问登录页面" -ForegroundColor White
Write-Host "2. 使用测试账号登录" -ForegroundColor White
Write-Host "3. 进入在线点餐页面" -ForegroundColor White
Write-Host "4. 选择店铺" -ForegroundColor White
Write-Host "5. 浏览菜品并添加到购物车" -ForegroundColor White
Write-Host "6. 提交订单" -ForegroundColor White
Write-Host "7. 查看订单列表`n" -ForegroundColor White

Write-Host "🔗 访问地址:" -ForegroundColor Green
Write-Host "  前端首页: $baseUrl" -ForegroundColor Cyan
Write-Host "  登录页面: $baseUrl/login" -ForegroundColor Cyan
Write-Host "  点餐页面: $baseUrl/ordering`n" -ForegroundColor Cyan

Write-Host "👤 测试账号:" -ForegroundColor Green
Write-Host "  用户名: user" -ForegroundColor Cyan
Write-Host "  密码: 123456`n" -ForegroundColor Cyan

Write-Host "⚙️  服务状态检查..." -ForegroundColor Yellow

# 检查后端服务
$services = @(
    @{name="Eureka Server"; port=8761},
    @{name="Shop Service"; port=8081},
    @{name="Menu Service"; port=8181},
    @{name="Order Service"; port=8083}
)

foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.port)" -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "  ✅ $($service.name) (端口 $($service.port)) - 运行中" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  $($service.name) (端口 $($service.port)) - 响应异常" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  ❌ $($service.name) (端口 $($service.port)) - 未启动" -ForegroundColor Red
    }
}

# 检查前端服务
try {
    $response = Invoke-WebRequest -Uri $baseUrl -TimeoutSec 2 -ErrorAction SilentlyContinue
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✅ Frontend (端口 3000) - 运行中`n" -ForegroundColor Green
    }
} catch {
    Write-Host "  ❌ Frontend (端口 3000) - 未启动`n" -ForegroundColor Red
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🚀 开始手动测试" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "请在浏览器中执行以下操作:`n" -ForegroundColor Yellow

Write-Host "步骤1: 登录系统" -ForegroundColor Green
Write-Host "  1. 打开浏览器访问: $baseUrl/login" -ForegroundColor White
Write-Host "  2. 输入用户名: user" -ForegroundColor White
Write-Host "  3. 输入密码: 123456" -ForegroundColor White
Write-Host "  4. 点击登录按钮`n" -ForegroundColor White

Write-Host "步骤2: 进入点餐页面" -ForegroundColor Green
Write-Host "  1. 登录后，查看左侧菜单" -ForegroundColor White
Write-Host "  2. 确认是否显示'在线点餐'菜单项（带🍽️图标）" -ForegroundColor White
Write-Host "  3. 点击'在线点餐'进入点餐页面`n" -ForegroundColor White

Write-Host "步骤3: 选择店铺" -ForegroundColor Green
Write-Host "  1. 页面顶部应显示店铺下拉选择框" -ForegroundColor White
Write-Host "  2. 默认应自动选择第一个店铺" -ForegroundColor White
Write-Host "  3. 如果没有自动选择，手动选择一个店铺`n" -ForegroundColor White

Write-Host "步骤4: 浏览菜品" -ForegroundColor Green
Write-Host "  1. 左侧应显示菜品分类导航" -ForegroundColor White
Write-Host "  2. 右侧应显示该店铺的菜品列表" -ForegroundColor White
Write-Host "  3. 每个菜品应显示：图片、名称、价格、库存等`n" -ForegroundColor White

Write-Host "步骤5: 添加菜品到购物车" -ForegroundColor Green
Write-Host "  1. 点击菜品卡片上的 '+' 按钮" -ForegroundColor White
Write-Host "  2. 观察数量是否增加" -ForegroundColor White
Write-Host "  3. 添加2-3个不同的菜品" -ForegroundColor White
Write-Host "  4. 底部应出现购物车栏，显示总价和总数量`n" -ForegroundColor White

Write-Host "步骤6: 查看购物车" -ForegroundColor Green
Write-Host "  1. 点击底部购物车栏" -ForegroundColor White
Write-Host "  2. 弹出购物车对话框" -ForegroundColor White
Write-Host "  3. 确认购物车中的菜品和数量正确" -ForegroundColor White
Write-Host "  4. 可以调整数量或删除菜品`n" -ForegroundColor White

Write-Host "步骤7: 提交订单" -ForegroundColor Green
Write-Host "  1. 点击购物车对话框中的'提交订单'按钮" -ForegroundColor White
Write-Host "  2. 或点击底部购物车栏的'提交订单'按钮" -ForegroundColor White
Write-Host "  3. 等待请求完成" -ForegroundColor White
Write-Host "  4. 应弹出成功提示，显示订单号`n" -ForegroundColor White

Write-Host "步骤8: 查看订单" -ForegroundColor Green
Write-Host "  1. 订单提交成功后，应自动跳转到订单页面" -ForegroundColor White
Write-Host "  2. 或在左侧菜单点击'我的订单'" -ForegroundColor White
Write-Host "  3. 确认刚才创建的订单在列表中显示`n" -ForegroundColor White

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ 测试检查清单" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$checklist = @(
    "左侧菜单显示'在线点餐'选项",
    "点餐页面正常加载",
    "店铺下拉列表有数据",
    "菜品分类正常显示",
    "菜品列表正常显示",
    "可以添加菜品到购物车",
    "购物车数量计算正确",
    "购物车总价计算正确",
    "提交订单成功",
    "订单号生成正确",
    "订单创建后跳转到订单页",
    "订单列表显示新创建的订单"
)

for ($i = 0; $i -lt $checklist.Count; $i++) {
    Write-Host "  [ ] $($i+1). $($checklist[$i])" -ForegroundColor White
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "🐛 问题排查" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "如果遇到问题，请检查:" -ForegroundColor Yellow
Write-Host "  1. 浏览器控制台是否有错误信息 (F12)" -ForegroundColor White
Write-Host "  2. Network标签中API请求是否成功" -ForegroundColor White
Write-Host "  3. 后端服务日志是否有异常" -ForegroundColor White
Write-Host "  4. 数据库是否有对应数据`n" -ForegroundColor White

Write-Host "常见错误及解决方案:" -ForegroundColor Yellow
Write-Host "  ❌ CORS错误 → 检查vite.config.js代理配置" -ForegroundColor White
Write-Host "  ❌ 404错误 → 检查API路径是否正确" -ForegroundColor White
Write-Host "  ❌ 500错误 → 检查后端服务日志" -ForegroundColor White
Write-Host "  ❌ 菜单不显示 → 清除浏览器缓存并重新登录`n" -ForegroundColor White

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📊 API调用预期" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "点餐页面会调用以下API:" -ForegroundColor Yellow
Write-Host "  GET  /api/shop/list              - 获取店铺列表" -ForegroundColor Cyan
Write-Host "  GET  /api/menu/category/shop/:id - 获取菜品分类" -ForegroundColor Cyan
Write-Host "  GET  /api/menu/item/available/:id - 获取可用菜品" -ForegroundColor Cyan
Write-Host "  POST /api/order                  - 创建订单`n" -ForegroundColor Cyan

Write-Host "所有请求应通过Vite代理转发到对应的后端服务`n" -ForegroundColor White

Write-Host "按任意键打开浏览器..." -ForegroundColor Green
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# 打开浏览器
Start-Process "http://localhost:3000/login"

Write-Host "`n✅ 浏览器已打开，开始测试！`n" -ForegroundColor Green
