@echo off
setlocal

echo ===============================================================================
echo   ALBERO DI REGRESSIONE - COMPILAZIONE E PACKAGING DEI MODULI (MAVEN)
echo ===============================================================================

REM 1. Verifica presenza di Java
where java >nul 2>&1
if errorlevel 1 (
    echo [ERRORE] Java non trovato nel PATH di sistema.
    echo Assicurati di aver installato Java 21+ e configurato JAVA_HOME.
    exit /b 1
)

REM 2. Verifica presenza di Maven
where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERRORE] Apache Maven non trovato nel PATH di sistema.
    echo Installa Maven o aggiungi la directory bin al PATH.
    exit /b 1
)

echo [1/3] Java e Maven rilevati correttamente.
echo [2/3] Compilazione ed impacchettamento fat-JAR: Server e Client...
echo.

call mvn clean package -DskipTests
if errorlevel 1 (
    echo.
    echo [ERRORE] La compilazione Maven e fallita. Controlla i log sopra riportati.
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
echo   Puoi ora avviare l'applicazione con:
echo     - start-server.bat : avvia il backend REST su porta 8080
echo     - start-client.bat : avvia il client Vaadin Web su porta 8081
echo     - run-local.bat    : avvia entrambi automaticamente
echo ===============================================================================

endlocal
exit /b 0
