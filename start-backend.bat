@echo off
echo ========================================
echo   Demarrage du Backend Spring Boot
echo ========================================
echo.

cd server_springboot

echo Verification de MongoDB...
mongosh --eval "db.version()" > nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] MongoDB n'est pas accessible!
    echo Veuillez demarrer MongoDB d'abord avec: net start MongoDB
    echo Ou lancer manuellement: mongod --dbpath C:\data\db
    pause
    exit /b 1
)

echo [OK] MongoDB est accessible
echo.
echo Demarrage du backend sur http://localhost:4000...
echo.

REM Verifier si mvnw.cmd existe
if exist "mvnw.cmd" (
    echo Utilisation du Maven Wrapper...
    call mvnw.cmd spring-boot:run
) else (
    echo Maven Wrapper non trouve, utilisation de Maven directement...
    REM Verifier si Maven est installe
    mvn --version > nul 2>&1
    if %errorlevel% neq 0 (
        echo [ERREUR] Maven n'est pas installe ou pas dans le PATH!
        echo.
        echo Solutions:
        echo 1. Installer Maven: https://maven.apache.org/download.cgi
        echo 2. Ou generer le Maven Wrapper avec: mvn wrapper:wrapper
        echo.
        pause
        exit /b 1
    )
    echo [OK] Maven detecte
    echo.
    mvn spring-boot:run
)

pause
