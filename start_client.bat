@echo off
setlocal
cd /d "%~dp0"

echo.
echo ================================================
echo   ALBERO DI REGRESSIONE - CLIENT WEB [8081]
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
if not exist "Client\target\client.args" (
    echo [INFO] Prima esecuzione: preparazione del Client in corso...
    call mvn -pl Client compile dependency:build-classpath -Dmdep.outputFile=target\cp.txt -q -B
    if errorlevel 1 (
        echo [ERRORE] Preparazione fallita.
        echo.
        pause
        exit /b 1
    )
    powershell -NoProfile -Command "$q=[char]34; $cp=(Get-Content 'Client\target\cp.txt' -Raw).Trim().Replace('\','/'); Set-Content -Path 'Client\target\client.args' -Value @('-cp', ($q+'target/classes;'+$cp+$q)) -Encoding ascii"
)

echo [AVVIO] Client in avvio, attendi qualche istante...
echo Il browser si aprira automaticamente.
echo.

pushd Client
java -XX:+UseSerialGC --enable-native-access=ALL-UNNAMED @target\client.args Client.ClientApplication --spring.main.banner-mode=off --logging.level.root=WARN --logging.level.org.springframework.boot.web.embedded.tomcat.TomcatWebServer=INFO 2>&1 | powershell -NoProfile -Command "$input | ForEach-Object { if ($_ -match 'Tomcat started') { Write-Host '================================================'; Write-Host '  [PRONTO] Client avviato correttamente!'; Write-Host ''; Write-Host '  Client:  http://localhost:8081'; Write-Host '  Il browser si apre automaticamente.'; Write-Host ''; Write-Host '  Premi CTRL+C per arrestarlo.'; Write-Host '================================================'; Write-Host '' } elseif ($_ -match 'Tomcat initialized|^WARNING: ') { } else { $_ } }"
popd
echo.
echo ------------------------------------------------
echo   Client arrestato.
echo.
pause
endlocal
