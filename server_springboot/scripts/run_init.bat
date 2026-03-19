@echo off
setlocal
cd /d "%~dp0"

if not exist ".venv\Scripts\python.exe" (
  echo Creating venv...
  python -m venv .venv
  if errorlevel 1 (
    echo Error: unable to create venv. Verify that Python 3.8+ is installed.
    exit /b 1
  )
)

call .venv\Scripts\activate.bat
echo Installing dependencies...
pip install -q -r requirements.txt
if errorlevel 1 (
  echo Error: pip install failed.
  exit /b 1
)

python init_data.py %*
exit /b %errorlevel%
