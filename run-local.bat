@echo off
setlocal

echo ===============================================================================
echo   ALBERO DI REGRESSIONE - AVVIO COMPLETO AMBIENTE LOCALE [ALL-IN-ONE]
echo ===============================================================================

set FLAG_PROVA=0
for %%A in (%*) do (
    if /I "%%A"=="-prova" set FLAG_PROVA=1
    if /I "%%A"=="--sample" set FLAG_PROVA=1
    if /I "%%A"=="-p" set FLAG_PROVA=1
)

REM 1. Se richiesto il flag -prova, inizializza il DB con dati di test
if "%FLAG_PROVA%"=="1" (
    echo [Fase 1/4] Flag -prova rilevato: inizializzazione MapDB con tabelle di test...
    call init-db.bat -prova
    if errorlevel 1 (
        echo [ATTENZIONE] Inizializzazione DB non riuscita automaticamente.
        echo Assicurati che MySQL sia attivo prima di procedere.
    )
) else (
    echo [Fase 1/4] Nessun flag -prova: avvio con database esistente.
)

REM 2. Verifica e compilazione JAR se mancanti
echo.
echo [Fase 2/4] Verifica artefatti compilati...
set NEED_BUILD=0
if not exist "Server\target\Server-1.0-SNAPSHOT.jar" set NEED_BUILD=1
if not exist "Client\target\Client-1.0-SNAPSHOT.jar" set NEED_BUILD=1

if "%NEED_BUILD%"=="1" (
    echo   Artefatti non trovati. Compilazione in corso con build.bat...
    call build.bat
    if errorlevel 1 (
        echo [ERRORE] Compilazione fallita. Arresto.
        pause
        exit /b 1
    )
) else (
    echo   Artefatti JAR presenti e pronti.
)

REM 3. Avvio Server in finestra console separata
echo.
echo [Fase 3/4] Avvio del Server REST [porta 8080] in una finestra separata...
start "Albero di Regressione - Server Backend 8080" cmd /k "start-server.bat"

echo In attesa che il Server sia operativo su http://localhost:8080...
powershell -NoProfile -Command "$ready = $false; for ($i=0; $i -lt 30; $i++) { try { $res = Invoke-WebRequest -Uri 'http://localhost:8080/v3/api-docs' -UseBasicParsing -TimeoutSec 1; if ($res.StatusCode -eq 200) { $ready = $true; break } } catch { Start-Sleep -Milliseconds 1000 } }; if (-not $ready) { exit 1 }"
if errorlevel 1 (
    echo [ATTENZIONE] Il server sta impiegando piu tempo del previsto per avviarsi.
    echo Proseguo con l'avvio del Client...
) else (
    echo   Server pronto e operativo!
)

REM 4. Avvio Client in finestra console separata
echo.
echo [Fase 4/4] Avvio del Client Web Vaadin [porta 8081]...
start "Albero di Regressione - Client Web 8081" cmd /k "start-client.bat"

echo.
echo ===============================================================================
echo   TUTTI I SERVIZI SONO STATI AVVIATI!
echo   - Backend REST e Swagger UI: http://localhost:8080/swagger-ui.html
echo   - Client Web Vaadin:         http://localhost:8081
echo   Il browser predefinito si aprira automaticamente appena il client e pronto.
echo ===============================================================================

endlocal
exit /b 0
