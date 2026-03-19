@echo off
REM Script to start the Spring Boot backend
REM Automatically manages mvnw.cmd or mvn

if exist "mvnw.cmd" (
    call mvnw.cmd spring-boot:run
) else (
    REM Check if Maven is installed
    mvn --version > nul 2>&1
    if %errorlevel% neq 0 (
        echo [ERROR] Maven is not installed!
        echo.
        echo Solutions:
        echo 1. Install Maven: https://maven.apache.org/download.cgi
        echo 2. Or generate the wrapper: generate-maven-wrapper.bat
        echo.
        pause
        exit /b 1
    )
    mvn spring-boot:run
)
