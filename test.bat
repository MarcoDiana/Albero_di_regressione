@echo off
setlocal
cd /d "%~dp0"

echo.
echo ================================================
echo   ALBERO DI REGRESSIONE - TEST AUTOMATIZZATI
echo ================================================
echo.

where java >nul 2>&1
if errorlevel 1 (
    echo [ERRORE] Java non trovato nel PATH. Installa Java 21+.
    echo.
    pause
    exit /b 1
)

where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERRORE] Maven non trovato nel PATH. Installa Maven.
    echo.
    pause
    exit /b 1
)

echo Esecuzione dei test in corso, attendi qualche istante...
echo.

set "TEMP_TEST_LOG=%TEMP%\albero_test_%RANDOM%.tmp"
call mvn test -B > "%TEMP_TEST_LOG%" 2>&1
set "RESULT=%errorlevel%"

echo Risultato per modulo:
for /f "tokens=1,* delims= " %%a in ('findstr /R /C:"Tests run:.*Skipped: [0-9]*$" "%TEMP_TEST_LOG%"') do echo   %%b
echo.

if not "%RESULT%"=="0" (
    echo ================================================
    echo   [ERRORE] ALCUNI TEST SONO FALLITI
    echo ================================================
    echo.
    powershell -NoProfile -Command "Select-String -Path '%TEMP_TEST_LOG%' -Pattern 'FAIL|ERROR\]' | Select-Object -First 15 | ForEach-Object { $_.Line }"
    echo.
    if exist "%TEMP_TEST_LOG%" del "%TEMP_TEST_LOG%" >nul 2>&1
    pause
    exit /b 1
)

if exist "%TEMP_TEST_LOG%" del "%TEMP_TEST_LOG%" >nul 2>&1

echo ================================================
echo   TUTTI I TEST SUPERATI
echo ================================================
echo.
pause
endlocal
exit /b 0
