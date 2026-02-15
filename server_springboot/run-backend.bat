@echo off
REM Script pour demarrer le backend Spring Boot
REM Gere automatiquement mvnw.cmd ou mvn

if exist "mvnw.cmd" (
    call mvnw.cmd spring-boot:run
) else (
    REM Verifier si Maven est installe
    mvn --version > nul 2>&1
    if %errorlevel% neq 0 (
        echo [ERREUR] Maven n'est pas installe!
        echo.
        echo Solutions:
        echo 1. Installer Maven: https://maven.apache.org/download.cgi
        echo 2. Ou generer le wrapper: generate-maven-wrapper.bat
        echo.
        pause
        exit /b 1
    )
    mvn spring-boot:run
)
