@echo off
echo ========================================
echo   CoPro Connect - Demarrage Complet
echo ========================================
echo.

echo Verification de MongoDB...
mongosh --eval "db.version()" > nul 2>&1
if %errorlevel% neq 0 (
    echo [ATTENTION] MongoDB n'est pas accessible!
    echo Tentative de demarrage de MongoDB...
    net start MongoDB > nul 2>&1
    if %errorlevel% neq 0 (
        echo [ERREUR] Impossible de demarrer MongoDB automatiquement.
        echo Veuillez le demarrer manuellement avec: mongod --dbpath C:\data\db
        pause
        exit /b 1
    )
    echo [OK] MongoDB demarre
) else (
    echo [OK] MongoDB est accessible
)
echo.

echo Demarrage du Backend dans un nouveau terminal...
start "CoPro Backend (Port 4000)" cmd /k "cd server_springboot && run-backend.bat"

echo Attente du demarrage du backend (15 secondes)...
timeout /t 15 /nobreak > nul

echo Demarrage du Frontend dans un nouveau terminal...
start "CoPro Frontend (Port 4200)" cmd /k "cd frontend_Angular && run-frontend.bat"

echo.
echo ========================================
echo   Les deux serveurs sont en cours de demarrage
echo ========================================
echo.
echo Backend:  http://localhost:4000
echo Frontend: http://localhost:4200
echo.
echo Appuyez sur une touche pour fermer cette fenetre...
echo (Les serveurs continueront a fonctionner dans leurs fenetres respectives)
pause > nul
