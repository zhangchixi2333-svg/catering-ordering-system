# 清除前端缓存并重启

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  清除前端缓存并重启" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 停止所有 node 进程
Write-Host "[步骤 1] 停止所有 Node.js 进程..." -ForegroundColor Yellow
Get-Process | Where-Object {$_.ProcessName -like "*node*"} | Stop-Process -Force
Write-Host "✅ Node.js 进程已停止" -ForegroundColor Green
Write-Host ""

# 2. 清除 node_modules/.vite 缓存
Write-Host "[步骤 2] 清除 Vite 缓存..." -ForegroundColor Yellow
if (Test-Path "node_modules/.vite") {
    Remove-Item -Recurse -Force "node_modules/.vite"
    Write-Host "✅ Vite 缓存已清除" -ForegroundColor Green
} else {
    Write-Host "⚠️  Vite 缓存目录不存在" -ForegroundColor Gray
}
Write-Host ""

# 3. 清除 dist 目录
Write-Host "[步骤 3] 清除构建输出..." -ForegroundColor Yellow
if (Test-Path "dist") {
    Remove-Item -Recurse -Force "dist"
    Write-Host "✅ dist 目录已清除" -ForegroundColor Green
} else {
    Write-Host "⚠️  dist 目录不存在" -ForegroundColor Gray
}
Write-Host ""

# 4. 提示清除浏览器缓存
Write-Host "[步骤 4] 请手动清除浏览器缓存：" -ForegroundColor Yellow
Write-Host "  方法 1: 按 Ctrl+Shift+Delete，选择'缓存的图片和文件'" -ForegroundColor Gray
Write-Host "  方法 2: 按 Ctrl+F5 硬刷新页面" -ForegroundColor Gray
Write-Host "  方法 3: 使用无痕/隐私模式打开" -ForegroundColor Gray
Write-Host ""

# 5. 重新启动前端
Write-Host "[步骤 5] 重新启动前端服务..." -ForegroundColor Yellow
Write-Host "正在启动，请稍候..." -ForegroundColor Gray
Write-Host ""

npm run dev
