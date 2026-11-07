@echo off
setlocal

echo ===============================================================================
echo   ALBERO DI REGRESSIONE - AVVIO CLIENT WEB VAADIN [PORTA 8081]
echo ===============================================================================

if not exist "Client\target\Client-1.0-SNAPSHOT.jar" (
    echo [ATTENZIONE] Il fat-JAR del Client non e stato trovato in Client\target\.
    echo Esecuzione automatica della compilazione con build.bat...
    echo.
    call build.bat
    if errorlevel 1 (
        echo [ERRORE] Compilazione fallita. Impossibile avviare il Client.
        pause
        exit /b 1
    )
)

echo.
echo Avvio del frontend Vaadin Flow su porta 8081...
echo   - URL Web Client: http://localhost:8081
echo   - All'avvio dell'applicazione, il browser predefinito verra aperto automaticamente!
echo.
echo Premi CTRL+C in questa finestra per arrestare il Client.
echo ===============================================================================
echo.

java -jar "Client\target\Client-1.0-SNAPSHOT.jar"

endlocal
