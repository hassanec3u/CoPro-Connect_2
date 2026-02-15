@echo off
REM Script pour demarrer le frontend Angular
REM Installe les dependances si necessaire

if not exist "node_modules" (
    echo Installation des dependances npm...
    call npm install
    if %errorlevel% neq 0 (
        echo [ERREUR] Echec de l'installation des dependances
        pause
        exit /b 1
    )
    echo [OK] Dependances installees
    echo.
)

echo Demarrage du frontend sur http://localhost:4200...
echo.

call npm start
