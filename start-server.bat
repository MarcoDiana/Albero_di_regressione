@echo off
setlocal

echo ===============================================================================
echo   ALBERO DI REGRESSIONE - AVVIO SERVER BACKEND REST [PORTA 8080]
echo ===============================================================================

if not exist "Server\target\Server-1.0-SNAPSHOT.jar" (
    echo [ATTENZIONE] Il fat-JAR del Server non e stato trovato in Server\target\.
    echo Esecuzione automatica della compilazione con build.bat...
    echo.
    call build.bat
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
