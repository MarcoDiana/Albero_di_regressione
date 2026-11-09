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

echo [AVVIO] Client in avvio, attendi qualche istante...
echo.

call mvn -pl Client spring-boot:run -q -B -Dspring-boot.run.jvmArguments="--enable-native-access=ALL-UNNAMED" -Dspring-boot.run.arguments="--spring.main.banner-mode=off --logging.level.root=WARN --logging.level.org.springframework.boot.web.embedded.tomcat.TomcatWebServer=INFO" 2>&1 | powershell -NoProfile -Command "$input | ForEach-Object { if ($_ -match 'Tomcat started') { Write-Host '================================================'; Write-Host '  [PRONTO] Client avviato correttamente!'; Write-Host ''; Write-Host '  Client:  http://localhost:8081'; Write-Host '  Il browser si apre automaticamente.'; Write-Host ''; Write-Host '  Premi CTRL+C per arrestarlo.'; Write-Host '================================================'; Write-Host '' } elseif ($_ -match 'Tomcat initialized|^WARNING: ') { } else { $_ } }"

echo.
echo ------------------------------------------------
echo   Client arrestato.
echo.
pause
endlocal