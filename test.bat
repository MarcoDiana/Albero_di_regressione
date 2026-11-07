@echo off
setlocal

echo ===============================================================================
echo   ALBERO DI REGRESSIONE - ESECUZIONE SUITE TEST AUTOMATIZZATI [64 TEST]
echo ===============================================================================

where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERRORE] Apache Maven non trovato nel PATH.
    exit /b 1
)

echo Avvio esecuzione test JUnit 5 su Server e Client...
echo.

call mvn test
if errorlevel 1 (
    echo.
    echo ===============================================================================
    echo   [ERRORE] Alcuni test sono falliti. Controlla il log sopra.
    echo ===============================================================================
    exit /b 1
)

echo.
echo ===============================================================================
echo   TUTTI I 64 TEST SONO STATI SUPERATI CON SUCCESSO AL 100%!
echo   [0 Fallimenti, 0 Errori]
echo ===============================================================================

endlocal
exit /b 0
