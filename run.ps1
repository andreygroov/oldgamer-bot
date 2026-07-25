# Old Gamer Bot - Local Run Script (PowerShell)

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "   OLD GAMER BOT - LOCAL RUN" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Check Java
Write-Host "Checking Java..." -NoNewline
try {
    $version = java -version 2>&1
    Write-Host " [OK]" -ForegroundColor Green
} catch {
    Write-Host " [ERROR]" -ForegroundColor Red
    Write-Host "Java not found! Install from https://www.oracle.com/java/technologies/downloads/"
    exit 1
}

# Check PostgreSQL
Write-Host "Checking PostgreSQL..." -NoNewline
try {
    $test = psql -U postgres -tc "SELECT 1" 2>$null
    Write-Host " [OK]" -ForegroundColor Green
} catch {
    Write-Host " [ERROR]" -ForegroundColor Red
    Write-Host "PostgreSQL not running! Start the service or create database:"
    Write-Host "  createdb oldgamer_bot"
    exit 1
}

# Create database if needed
Write-Host "Checking database..." -NoNewline
$dbExists = psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'oldgamer_bot'" 2>$null
if (-not $dbExists) {
    Write-Host " Creating..."
    createdb oldgamer_bot
    Write-Host " [OK]" -ForegroundColor Green
} else {
    Write-Host " [OK]" -ForegroundColor Green
}

# Build
Write-Host ""
Write-Host "Building project..." -NoNewline
$build = mvn clean package -DskipTests -q 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host " [ERROR]" -ForegroundColor Red
    Write-Host $build
    exit 1
}
Write-Host " [OK]" -ForegroundColor Green

# Run
Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "   STARTING BOT..." -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

java -jar target/oldgamer-bot-1.0-SNAPSHOT.jar
