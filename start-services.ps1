# G-Nex Services Startup Script
# Run this in PowerShell to start all services

Write-Host "=== Starting G-Nex Services ===" -ForegroundColor Cyan
Write-Host ""

# Check if infrastructure is running
Write-Host "[1/4] Checking infrastructure..." -ForegroundColor Yellow
$infraCheck = docker-compose ps --format json | ConvertFrom-Json
if ($infraCheck.Count -lt 5) {
    Write-Host "⚠️  Infrastructure not running. Starting..." -ForegroundColor Yellow
    docker-compose up -d postgres redis kafka zookeeper minio
    Write-Host "Waiting 15 seconds for services to stabilize..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
} else {
    Write-Host "✅ Infrastructure is running" -ForegroundColor Green
}

# Build common-lib
Write-Host ""
Write-Host "[2/4] Building common-lib..." -ForegroundColor Yellow
mvn clean install -pl common-lib -DskipTests -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Common-lib built successfully" -ForegroundColor Green
} else {
    Write-Host "❌ Failed to build common-lib" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[3/4] Starting services..." -ForegroundColor Yellow
Write-Host "You need to open 3 separate PowerShell terminals and run:" -ForegroundColor Cyan
Write-Host ""
Write-Host "Terminal 1 - API Gateway:" -ForegroundColor Yellow
Write-Host "  cd api-gateway; mvn spring-boot:run" -ForegroundColor White
Write-Host ""
Write-Host "Terminal 2 - File Service:" -ForegroundColor Yellow  
Write-Host "  cd file-service; mvn spring-boot:run" -ForegroundColor White
Write-Host ""
Write-Host "Terminal 3 - Metadata Service:" -ForegroundColor Yellow
Write-Host "  cd metadata-service; mvn spring-boot:run" -ForegroundColor White
Write-Host ""
Write-Host "[4/4] After all services start, run the test script:" -ForegroundColor Yellow
Write-Host "  .\run-test.ps1" -ForegroundColor White
Write-Host ""
Write-Host "=== Setup Complete ===" -ForegroundColor Green
