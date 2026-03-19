@echo off
echo ========================================
echo   Maven Wrapper Generation
echo ========================================
echo.

REM Check if Maven is installed
mvn --version > nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven is not installed!
    echo.
    echo Please install Maven first:
    echo https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

echo [OK] Maven detected
echo.
echo Generating Maven Wrapper...
echo.

mvn wrapper:wrapper

if %errorlevel% equ 0 (
    echo.
    echo [OK] Maven Wrapper generated successfully!
    echo You can now use mvnw.cmd instead of mvn
) else (
    echo.
    echo [ERROR] Wrapper generation failed
)

echo.
pause
