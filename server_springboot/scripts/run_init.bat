@echo off
setlocal
cd /d "%~dp0"

if not exist ".venv\Scripts\python.exe" (
  echo Creation du venv...
  python -m venv .venv
  if errorlevel 1 (
    echo Erreur: impossible de creer le venv. Verifiez que Python 3.8+ est installe.
    exit /b 1
  )
)

call .venv\Scripts\activate.bat
echo Installation des dependances...
pip install -q -r requirements.txt
if errorlevel 1 (
  echo Erreur: pip install a echoue.
  exit /b 1
)

python init_data.py %*
exit /b %errorlevel%
