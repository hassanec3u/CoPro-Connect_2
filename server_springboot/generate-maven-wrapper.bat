@echo off
echo ========================================
echo   Generation du Maven Wrapper
echo ========================================
echo.

REM Verifier si Maven est installe
mvn --version > nul 2>&1
if %errorlevel% neq 0 (
    echo [ERREUR] Maven n'est pas installe!
    echo.
    echo Veuillez installer Maven d'abord:
    echo https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

echo [OK] Maven detecte
echo.
echo Generation du Maven Wrapper...
echo.

mvn wrapper:wrapper

if %errorlevel% equ 0 (
    echo.
    echo [OK] Maven Wrapper genere avec succes!
    echo Vous pouvez maintenant utiliser mvnw.cmd au lieu de mvn
) else (
    echo.
    echo [ERREUR] Echec de la generation du wrapper
)

echo.
pause
