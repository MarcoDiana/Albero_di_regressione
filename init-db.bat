@echo off
setlocal

echo ===============================================================================
echo   ALBERO DI REGRESSIONE - INIZIALIZZAZIONE DATABASE MYSQL [MapDB]
echo ===============================================================================

set FLAG_PROVA=0
for %%A in (%*) do (
    if /I "%%A"=="-prova" set FLAG_PROVA=1
    if /I "%%A"=="--sample" set FLAG_PROVA=1
    if /I "%%A"=="-p" set FLAG_PROVA=1
)

REM 1. Ricerca del client mysql
set MYSQL_CMD=
where mysql >nul 2>&1
if not errorlevel 1 (
    set MYSQL_CMD=mysql
) else (
    if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" (
        set MYSQL_CMD=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe
    ) else (
        if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
            set MYSQL_CMD=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
        )
    )
)

if "%MYSQL_CMD%"=="" (
    echo [ATTENZIONE] Il client a riga di comando mysql non e stato trovato nel PATH.
    echo Per inizializzare manualmente il database tramite MySQL Workbench o terminale:
    echo   1. Esegui il file: sql\01-init-schema.sql
    if "%FLAG_PROVA%"=="1" (
        echo   2. Esegui il file: sql\02-sample-data.sql
    )
    echo.
    echo Se MySQL e installato in un'altra cartella, aggiungi la sottocartella bin al PATH.
    pause
    exit /b 1
)

REM 2. Parametri di connessione predefiniti
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%DB_PORT%"=="" set DB_PORT=3306
if "%DB_ADMIN_USER%"=="" set DB_ADMIN_USER=root

echo Connessione a MySQL su %DB_HOST%:%DB_PORT% con utente %DB_ADMIN_USER%...
echo Se richiesta la password di root di MySQL, digitala a video.
echo.

REM 3. Esecuzione 01-init-schema.sql
echo [1/2] Creazione database MapDB ed utente MapUser...
"%MYSQL_CMD%" -h %DB_HOST% -P %DB_PORT% -u %DB_ADMIN_USER% -p < "sql\01-init-schema.sql"
if errorlevel 1 (
    echo.
    echo [ERRORE] Esecuzione di sql\01-init-schema.sql fallita.
    exit /b 1
)
echo       Database MapDB ed utente MapUser configurati con successo.

REM 4. Esecuzione 02-sample-data.sql solo se specificato il flag -prova
if "%FLAG_PROVA%"=="1" (
    echo.
    echo [2/2] Flag -prova rilevato: popolamento tabelle dimostrative provaC e servo...
    "%MYSQL_CMD%" -h %DB_HOST% -P %DB_PORT% -u MapUser -pmap MapDB < "sql\02-sample-data.sql"
    if errorlevel 1 (
        echo.
        echo [ATTENZIONE] Inserimento dati di test tramite MapUser non riuscito, tento con %DB_ADMIN_USER%...
        "%MYSQL_CMD%" -h %DB_HOST% -P %DB_PORT% -u %DB_ADMIN_USER% -p MapDB < "sql\02-sample-data.sql"
    )
    echo       Tabelle di test provaC e servo inserite con successo in MapDB!
) else (
    echo.
    echo [2/2] Nessun flag -prova specificato: MapDB inizializzato vuoto.
    echo       Se desideri popolare le tabelle di prova, esegui: init-db.bat -prova
)

echo.
echo ===============================================================================
echo   INIZIALIZZAZIONE DEL DATABASE COMPLETATA CON SUCCESSO!
echo ===============================================================================

endlocal
exit /b 0
