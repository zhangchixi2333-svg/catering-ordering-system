$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$logsDir = Join-Path $root "logs"
$pidFile = Join-Path $logsDir "services.pid"

if (-not (Test-Path $pidFile)) {
    Write-Warning "PID file not found: $pidFile"
    Write-Warning "No services were stopped. Start services with .\run-jars.ps1 first."
    exit 0
}

$entries = Get-Content $pidFile | Where-Object { $_.Trim() -ne "" }
if ($entries.Count -eq 0) {
    Write-Warning "PID file is empty: $pidFile"
    exit 0
}

foreach ($entry in $entries) {
    $parts = $entry -split "\s+", 3
    if ($parts.Count -lt 2) {
        Write-Warning "Skip invalid PID entry: $entry"
        continue
    }

    $module = $parts[0]
    $processId = [int]$parts[1]
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue

    if ($null -eq $process) {
        Write-Host "$module is already stopped. PID: $processId"
        continue
    }

    Write-Host "Stopping $module. PID: $processId"
    Stop-Process -Id $processId -Force
}

Remove-Item $pidFile -Force
Write-Host "All recorded services have been stopped."
