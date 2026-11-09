@echo off
setlocal

echo ===============================================================================
echo   ALBERO DI REGRESSIONE - COMPILAZIONE E PACKAGING DEI MODULI [MAVEN]
echo ===============================================================================

where java >nul 2>&1
if errorlevel 1 (
    echo [ERRORE] Java non trovato nel PATH di sistema.
    echo Assicurati di aver installato Java 21+ e configurato JAVA_HOME.
    pause
    exit /b 1
)

where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERRORE] Apache Maven non trovato nel PATH di sistema.
    echo Assicurati di aver installato Maven e configurato il PATH.
    pause
    exit /b 1
)

echo [1/3] Java e Maven rilevati correttamente.
echo [2/3] Compilazione ed impacchettamento fat-JAR: Server e Client...
echo.

call mvn clean package -DskipTests
if errorlevel 1 (
    echo.
    echo [ERRORE] La compilazione Maven e fallita. Controlla i log sopra riportati.
    pause
    exit /b 1
)

echo.
echo [3/3] Verifica artefatti generati:
if exist "Server\target\Server-1.0-SNAPSHOT.jar" (
    echo   - Server JAR: OK [Server\target\Server-1.0-SNAPSHOT.jar]
) else (
    echo   [ATTENZIONE] Server JAR non trovato!
)

if exist "Client\target\Client-1.0-SNAPSHOT.jar" (
    echo   - Client JAR: OK [Client\target\Client-1.0-SNAPSHOT.jar]
) else (
    echo   [ATTENZIONE] Client JAR non trovato!
)

echo.
echo ===============================================================================
echo   BUILD COMPLETATA CON SUCCESSO!
echo ===============================================================================

endlocal
exit /b 0
