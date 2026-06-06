$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$modules = @(
    "eureka-server",
    "shop-service",
    "menu-service",
    "order-service",
    "payment-service",
    "queue-service",
    "notification-service",
    "user-service",
    "gateway-service"
)

Write-Host "Building executable Spring Boot jars..."
foreach ($module in $modules) {
    $modulePath = Join-Path $root $module
    if (-not (Test-Path (Join-Path $modulePath "pom.xml"))) {
        Write-Warning "Skip missing module: $module"
        continue
    }

    Write-Host "Building $module..."
    Push-Location $modulePath
    try {
        mvn clean package spring-boot:repackage -DskipTests
    }
    finally {
        Pop-Location
    }
}

Write-Host "Build finished."
