# Shop Management API Test Script
Write-Host "`n========== Shop Management API Test ==========" -ForegroundColor Cyan

$baseUrl = "http://localhost:8082/api/shop"
$testPassed = 0
$testFailed = 0

# Test 1: Get shop list
Write-Host "`n[Test 1] Get shop list" -ForegroundColor Yellow
try {
    $result = Invoke-RestMethod -Uri "$baseUrl/list" -TimeoutSec 5
    Write-Host "PASS - Shop count: $($result.data.Count)" -ForegroundColor Green
    $testPassed++
    
    if ($result.data.Count -gt 0) {
        $firstShop = $result.data[0]
        Write-Host "  First shop: $($firstShop.shopName) (Code: $($firstShop.shopCode))" -ForegroundColor Gray
    }
} catch {
    Write-Host "FAIL - $($_.Exception.Message)" -ForegroundColor Red
    $testFailed++
}

# Test 2: Create a test shop
Write-Host "`n[Test 2] Create shop" -ForegroundColor Yellow
try {
    $shopData = @{
        shopName = "Test Shop $(Get-Date -Format 'HHmmss')"
        shopCode = "TEST$(Get-Date -Format 'HHmmss')"
        shopType = 1
        shopStatus = 1
        phone = "13800138000"
        address = "Test Address"
        businessHours = "09:00-22:00"
        tableCount = 10
        description = "Test shop for API testing"
    } | ConvertTo-Json
    
    $result = Invoke-RestMethod -Uri "$baseUrl" -Method Post -Body $shopData -ContentType "application/json" -TimeoutSec 5
    Write-Host "PASS - Shop created" -ForegroundColor Green
    $testPassed++
    
    # Get the created shop ID
    $listResult = Invoke-RestMethod -Uri "$baseUrl/list"
    $newShop = $listResult.data | Where-Object { $_.shopCode -like "TEST*" } | Select-Object -First 1
    
    if ($newShop) {
        Write-Host "  Created shop ID: $($newShop.id), Code: $($newShop.shopCode)" -ForegroundColor Gray
        
        # Test 3: Update shop
        Write-Host "`n[Test 3] Update shop" -ForegroundColor Yellow
        try {
            $updateData = @{
                id = $newShop.id
                shopName = $newShop.shopName
                shopCode = $newShop.shopCode
                shopType = 2
                shopStatus = 1
                phone = "13800138001"
                address = "Updated Address"
                businessHours = "10:00-23:00"
                tableCount = 15
                description = "Updated description"
            } | ConvertTo-Json
            
            $updateResult = Invoke-RestMethod -Uri "$baseUrl" -Method Put -Body $updateData -ContentType "application/json" -TimeoutSec 5
            Write-Host "PASS - Shop updated" -ForegroundColor Green
            $testPassed++
            
            # Verify update
            $verifyResult = Invoke-RestMethod -Uri "$baseUrl/$($newShop.id)"
            if ($verifyResult.data.phone -eq "13800138001") {
                Write-Host "  Verified - Phone updated successfully" -ForegroundColor Green
            }
        } catch {
            Write-Host "FAIL - $($_.Exception.Message)" -ForegroundColor Red
            $testFailed++
        }
        
        # Test 4: Toggle shop status
        Write-Host "`n[Test 4] Toggle shop status" -ForegroundColor Yellow
        try {
            $toggleData = @{
                id = $newShop.id
                shopName = $newShop.shopName
                shopCode = $newShop.shopCode
                shopType = 2
                shopStatus = 0  # Change to closed
                phone = $newShop.phone
                address = $newShop.address
                businessHours = $newShop.businessHours
                tableCount = $newShop.tableCount
                description = $newShop.description
            } | ConvertTo-Json
            
            $toggleResult = Invoke-RestMethod -Uri "$baseUrl" -Method Put -Body $toggleData -ContentType "application/json" -TimeoutSec 5
            Write-Host "PASS - Status toggled to closed" -ForegroundColor Green
            $testPassed++
            
            # Verify toggle
            $verifyResult = Invoke-RestMethod -Uri "$baseUrl/$($newShop.id)"
            if ($verifyResult.data.shopStatus -eq 0) {
                Write-Host "  Verified - Status is 0 (closed)" -ForegroundColor Green
            }
        } catch {
            Write-Host "FAIL - $($_.Exception.Message)" -ForegroundColor Red
            $testFailed++
        }
        
        # Test 5: Delete test shop
        Write-Host "`n[Test 5] Delete test shop" -ForegroundColor Yellow
        try {
            $deleteResult = Invoke-RestMethod -Uri "$baseUrl/$($newShop.id)" -Method Delete -TimeoutSec 5
            Write-Host "PASS - Shop deleted" -ForegroundColor Green
            $testPassed++
        } catch {
            Write-Host "FAIL - $($_.Exception.Message)" -ForegroundColor Red
            $testFailed++
        }
    }
} catch {
    Write-Host "FAIL - $($_.Exception.Message)" -ForegroundColor Red
    $testFailed++
}

# Test Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total: $($testPassed + $testFailed)" -ForegroundColor White
Write-Host "Passed: $testPassed" -ForegroundColor Green
Write-Host "Failed: $testFailed" -ForegroundColor $(if ($testFailed -eq 0) { "Green" } else { "Red" })
Write-Host ""

if ($testFailed -eq 0) {
    Write-Host "All tests passed!" -ForegroundColor Green
} else {
    Write-Host "Some tests failed, please check errors above" -ForegroundColor Yellow
}

Write-Host ""
