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

$logsDir = Join-Path $root "logs"
New-Item -ItemType Directory -Force -Path $logsDir | Out-Null

$pidFile = Join-Path $logsDir "services.pid"
if (Test-Path $pidFile) {
    Remove-Item $pidFile
}

Add-Type -AssemblyName System.IO.Compression.FileSystem

function Test-ExecutableJar {
    param([string]$JarPath)

    $zip = $null
    try {
        $zip = [System.IO.Compression.ZipFile]::OpenRead($JarPath)
        $manifest = $zip.GetEntry("META-INF/MANIFEST.MF")
        if ($null -eq $manifest) {
            return $false
        }

        $reader = New-Object System.IO.StreamReader($manifest.Open())
        try {
            $content = $reader.ReadToEnd()
            return $content -match "(?m)^Main-Class:\s+"
        }
        finally {
            $reader.Dispose()
        }
    }
    finally {
        if ($null -ne $zip) {
            $zip.Dispose()
        }
    }
}

foreach ($module in $modules) {
    $targetDir = Join-Path $root "$module\target"
    if (-not (Test-Path $targetDir)) {
        Write-Warning "Skip $module because target directory does not exist. Run .\build-only.ps1 first."
        continue
    }

    $jar = Get-ChildItem -Path $targetDir -Filter "*.jar" |
        Where-Object {
            $_.Name -notlike "original-*" -and
            $_.Name -notlike "*sources*" -and
            $_.Name -notlike "*javadoc*"
        } |
        Sort-Object Length -Descending |
        Select-Object -First 1

    if ($null -eq $jar) {
        Write-Warning "Skip $module because no jar was found."
        continue
    }

    if (-not (Test-ExecutableJar $jar.FullName)) {
        Write-Warning "Skip $($jar.FullName) because it has no Main-Class. Run .\build-only.ps1 to create executable Spring Boot jars."
        continue
    }

    $stdout = Join-Path $logsDir "$module.out.log"
    $stderr = Join-Path $logsDir "$module.err.log"

    Write-Host "Starting $module..."
    $process = Start-Process java `
        -ArgumentList @("-jar", $jar.FullName) `
        -WorkingDirectory $root `
        -RedirectStandardOutput $stdout `
        -RedirectStandardError $stderr `
        -PassThru `
        -WindowStyle Hidden

    "$module $($process.Id) $($jar.FullName)" | Add-Content -Path $pidFile
    Write-Host "  PID: $($process.Id)"
    Write-Host "  Log: $stdout"
}

Write-Host "Run finished. PID file: $pidFile"
