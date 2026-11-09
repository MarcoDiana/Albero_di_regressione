@echo off
setlocal

echo ===============================================================================
echo   ALBERO DI REGRESSIONE - AVVIO CLIENT WEB VAADIN [PORTA 8081]
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

echo.
echo Avvio del frontend Vaadin Flow su porta 8081...
echo   - URL Web Client: http://localhost:8081
echo   - All'avvio dell'applicazione, il browser predefinito verra aperto automaticamente!
echo.
echo Premi CTRL+C in questa finestra per arrestare il Client.
echo ===============================================================================
echo.

call mvn -pl Client spring-boot:run

endlocal
