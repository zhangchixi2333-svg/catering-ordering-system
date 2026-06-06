param(
  [string]$Namespace = "catering-dev",
  [string]$MysqlLabel = "app=mysql",
  [string]$MysqlUser = "root",
  [string]$MysqlPassword = "",
  [string]$InitSqlDir = "deploy/docker/mysql-init",
  [switch]$Force,
  [switch]$RestartServices
)

$ErrorActionPreference = "Stop"

function Write-Step($Message) {
  Write-Host ""
  Write-Host "==> $Message" -ForegroundColor Cyan
}

function Invoke-Kubectl {
  param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Args)
  & kubectl @Args
  if ($LASTEXITCODE -ne 0) {
    throw "kubectl $($Args -join ' ') failed with exit code $LASTEXITCODE"
  }
}

$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path
Set-Location $ProjectRoot
$LocalTempDir = ".tmp/k8s-db-reset"
New-Item -ItemType Directory -Force -Path $LocalTempDir | Out-Null

if (-not (Test-Path $InitSqlDir)) {
  throw "SQL init directory not found: $InitSqlDir. Run this script from the project root or keep the default layout."
}

$SqlFiles = Get-ChildItem -Path $InitSqlDir -Filter "*.sql" | Sort-Object Name
if ($SqlFiles.Count -eq 0) {
  throw "No .sql files found in $InitSqlDir"
}

if ([string]::IsNullOrWhiteSpace($MysqlPassword)) {
  Write-Step "Reading MySQL password from Kubernetes Secret"
  $EncodedPassword = (& kubectl get secret catering-secret -n $Namespace -o jsonpath="{.data.MYSQL_ROOT_PASSWORD}")
  if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($EncodedPassword)) {
    throw "Cannot read MYSQL_ROOT_PASSWORD from secret catering-secret. Pass -MysqlPassword manually."
  }
  $MysqlPassword = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($EncodedPassword))
}

Write-Step "Locating MySQL Pod"
$MysqlPod = (& kubectl get pod -n $Namespace -l $MysqlLabel -o jsonpath="{.items[0].metadata.name}").Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($MysqlPod)) {
  throw "Cannot find MySQL Pod by label '$MysqlLabel' in namespace '$Namespace'."
}
Write-Host "MySQL Pod: $MysqlPod"

Write-Step "SQL files to import"
$SqlFiles | ForEach-Object { Write-Host " - $($_.Name)" }

$Databases = @(
  "catering_auth",
  "shop_service",
  "menu_service",
  "order_service",
  "payment_service",
  "queue_service",
  "notification_service"
)

Write-Host ""
Write-Host "This will DROP and recreate these databases in Kubernetes namespace '$Namespace':" -ForegroundColor Yellow
$Databases | ForEach-Object { Write-Host " - $_" -ForegroundColor Yellow }

if (-not $Force) {
  $Confirm = Read-Host "Type RESET to continue"
  if ($Confirm -ne "RESET") {
    Write-Host "Cancelled."
    exit 0
  }
}

$RemoteDir = "/tmp/catering-db-reset"

Write-Step "Preparing remote temp directory"
Invoke-Kubectl exec $MysqlPod -n $Namespace "--" sh -c "rm -rf '$RemoteDir' && mkdir -p '$RemoteDir'"

Write-Step "Copying init SQL files to MySQL Pod"
foreach ($File in $SqlFiles) {
  $LocalPath = Join-Path $InitSqlDir $File.Name
  Invoke-Kubectl cp -n $Namespace $LocalPath "${MysqlPod}:$RemoteDir/$($File.Name)"
}

Write-Step "Dropping project databases"
$DropSql = "SET FOREIGN_KEY_CHECKS=0; " + (($Databases | ForEach-Object { "DROP DATABASE IF EXISTS $_;" }) -join " ") + " SET FOREIGN_KEY_CHECKS=1;"
$LocalDropSql = Join-Path $LocalTempDir "catering-drop-databases.sql"
[System.IO.File]::WriteAllText($LocalDropSql, $DropSql, [System.Text.Encoding]::UTF8)
Invoke-Kubectl cp -n $Namespace $LocalDropSql "${MysqlPod}:$RemoteDir/00_drop_databases.sql"
Invoke-Kubectl exec $MysqlPod -n $Namespace "--" sh -c "mysql --default-character-set=utf8mb4 -u$MysqlUser -p'$MysqlPassword' < '$RemoteDir/00_drop_databases.sql'"

Write-Step "Importing initial SQL files"
foreach ($File in $SqlFiles) {
  Write-Host "Importing $($File.Name)"
  Invoke-Kubectl exec $MysqlPod -n $Namespace "--" sh -c "mysql --default-character-set=utf8mb4 -u$MysqlUser -p'$MysqlPassword' < '$RemoteDir/$($File.Name)'"
}

Write-Step "Checking database table counts"
$CheckSql = @"
SELECT 'catering_auth.sys_user' AS table_name, COUNT(*) AS rows_count FROM catering_auth.sys_user
UNION ALL SELECT 'shop_service.shop_info', COUNT(*) FROM shop_service.shop_info
UNION ALL SELECT 'menu_service.menu_item', COUNT(*) FROM menu_service.menu_item
UNION ALL SELECT 'order_service.orders', COUNT(*) FROM order_service.orders
UNION ALL SELECT 'payment_service.payment_order', COUNT(*) FROM payment_service.payment_order
UNION ALL SELECT 'queue_service.queue_number', COUNT(*) FROM queue_service.queue_number
UNION ALL SELECT 'notification_service.message_template', COUNT(*) FROM notification_service.message_template;
"@
$CheckSqlOneLine = $CheckSql -replace "`r?`n", " "
$LocalCheckSql = Join-Path $LocalTempDir "catering-check-databases.sql"
[System.IO.File]::WriteAllText($LocalCheckSql, $CheckSqlOneLine, [System.Text.Encoding]::UTF8)
Invoke-Kubectl cp -n $Namespace $LocalCheckSql "${MysqlPod}:$RemoteDir/99_check_databases.sql"
Invoke-Kubectl exec $MysqlPod -n $Namespace "--" sh -c "mysql --default-character-set=utf8mb4 -u$MysqlUser -p'$MysqlPassword' < '$RemoteDir/99_check_databases.sql'"

if ($RestartServices) {
  Write-Step "Restarting business services"
  $Deployments = @(
    "gateway-service",
    "shop-service",
    "menu-service",
    "order-service",
    "payment-service",
    "queue-service",
    "notification-service",
    "user-service"
  )
  foreach ($Deployment in $Deployments) {
    Invoke-Kubectl rollout restart "deployment/$Deployment" -n $Namespace
  }
  foreach ($Deployment in $Deployments) {
    Invoke-Kubectl rollout status "deployment/$Deployment" -n $Namespace --timeout=180s
  }
}

Write-Step "Done"
Write-Host "Cluster MySQL has been reset to the initial project SQL state." -ForegroundColor Green
