@echo off
setlocal

echo ===============================================================================
echo   ALBERO DI REGRESSIONE - AVVIO SERVER BACKEND REST [PORTA 8080]
echo ===============================================================================

where java >nul 2>&1
if errorlevel 1 (
    echo [ERRORE] Java non trovato nel PATH di sistema.
    echo Assicurati di aver installato Java 21+ e configurato JAVA_HOME.
    pause
    exit /b 1
)

if not exist "Server\target\Server-1.0-SNAPSHOT.jar" (
    echo [INFO] JAR del Server non trovato in Server\target\.
    echo Avvio compilazione automatica con Maven...
    echo.
    where mvn >nul 2>&1
    if errorlevel 1 (
        echo [ERRORE] Apache Maven non trovato nel PATH di sistema.
        pause
        exit /b 1
    )
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo [ERRORE] Compilazione fallita. Impossibile avviare il Server.
        pause
        exit /b 1
    )
)

echo.
echo Avvio del backend Spring Boot su porta 8080...
echo   - Swagger UI:   http://localhost:8080/swagger-ui.html
echo   - OpenAPI Docs: http://localhost:8080/v3/api-docs
echo.
echo Premi CTRL+C in questa finestra per arrestare il Server.
echo ===============================================================================
echo.

java -jar "Server\target\Server-1.0-SNAPSHOT.jar"

endlocal
