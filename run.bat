@echo off
REM Old Gamer Bot - Local Run Script

echo.
echo ======================================
echo   OLD GAMER BOT - LOCAL RUN
echo ======================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found!
    echo Please install Java 17+ from https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo [OK] Java found
echo.

REM Check if PostgreSQL is running
echo Checking PostgreSQL connection...
psql -U postgres -d postgres -c "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo ERROR: PostgreSQL not running!
    echo Please start PostgreSQL service or create the database:
    echo   createdb oldgamer_bot
    pause
    exit /b 1
)

echo [OK] PostgreSQL is running
echo.

REM Create database if not exists
echo Creating database if needed...
psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'oldgamer_bot'" | grep -q 1
if errorlevel 1 (
    echo Creating database 'oldgamer_bot'...
    createdb oldgamer_bot
    echo [OK] Database created
)

echo.
echo Building project...
call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)

echo [OK] Build successful
echo.
echo ======================================
echo   STARTING BOT...
echo ======================================
echo.

java -jar target/oldgamer-bot-1.0-SNAPSHOT.jar

pause
