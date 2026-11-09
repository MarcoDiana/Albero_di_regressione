@echo off
setlocal
cd /d "%~dp0"

echo.
echo ================================================
echo   ALBERO DI REGRESSIONE - BUILD
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

echo Compilazione in corso, attendi qualche istante...
echo.

call mvn clean package -DskipTests -B -q
if errorlevel 1 (
    echo.
    echo ================================================
    echo   [ERRORE] COMPILAZIONE FALLITA
    echo ================================================
    echo.
    pause
    exit /b 1
)

echo   [OK] Compilazione completata.
if exist "Server\target\Server-1.0-SNAPSHOT.jar" echo   [OK] Server\target\Server-1.0-SNAPSHOT.jar
if exist "Client\target\Client-1.0-SNAPSHOT.jar" echo   [OK] Client\target\Client-1.0-SNAPSHOT.jar
echo.
echo ================================================
echo   BUILD COMPLETATA CON SUCCESSO
echo ================================================
echo.
pause
endlocal
exit /b 0
