# Order Management API Test Script
Write-Host "`n========== Order Management API Test ==========" -ForegroundColor Cyan

$baseUrl = "http://localhost:8083/api/order"

# Test 1: Get order list
Write-Host "`n[Test 1] Get order list" -ForegroundColor Yellow
try {
    $result = Invoke-RestMethod -Uri "$baseUrl/list" -TimeoutSec 5
    Write-Host "PASS - Order count: $($result.data.Count)" -ForegroundColor Green
} catch {
    Write-Host "FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Get single order
Write-Host "`n[Test 2] Get single order" -ForegroundColor Yellow
try {
    $listResult = Invoke-RestMethod -Uri "$baseUrl/list"
    if ($listResult.data.Count -gt 0) {
        $orderId = $listResult.data[0].id
        $result = Invoke-RestMethod -Uri "$baseUrl/$orderId" -TimeoutSec 5
        Write-Host "PASS - Order No: $($result.data.orderNo)" -ForegroundColor Green
    }
} catch {
    Write-Host "FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Update order status
Write-Host "`n[Test 3] Update order status" -ForegroundColor Yellow
try {
    $listResult = Invoke-RestMethod -Uri "$baseUrl/list"
    if ($listResult.data.Count -gt 0) {
        $orderId = $listResult.data[0].id
        $result = Invoke-RestMethod -Uri "$baseUrl/$orderId/status?orderStatus=2" -Method Put -TimeoutSec 5
        Write-Host "PASS - Status updated to 2" -ForegroundColor Green
        
        # Verify
        $verifyResult = Invoke-RestMethod -Uri "$baseUrl/$orderId"
        if ($verifyResult.data.orderStatus -eq 2) {
            Write-Host "  Verified - Database synced" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Cancel order
Write-Host "`n[Test 4] Cancel order" -ForegroundColor Yellow
try {
    $listResult = Invoke-RestMethod -Uri "$baseUrl/list"
    if ($listResult.data.Count -gt 0) {
        $orderId = $listResult.data[0].id
        $result = Invoke-RestMethod -Uri "$baseUrl/$orderId/cancel?cancelReason=Test cancel" -Method Put -TimeoutSec 5
        Write-Host "PASS - Order cancelled" -ForegroundColor Green
        
        # Verify
        $verifyResult = Invoke-RestMethod -Uri "$baseUrl/$orderId"
        if ($verifyResult.data.orderStatus -eq 5) {
            Write-Host "  Verified - Status is 5 (cancelled)" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "FAIL - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n========== Test Complete ==========`n" -ForegroundColor Cyan
