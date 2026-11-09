@echo off
setlocal
cd /d "%~dp0"

echo.
echo ================================================
echo   ALBERO DI REGRESSIONE - SERVER REST [8080]
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
if not exist "Server\target\Server-1.0-SNAPSHOT.jar" (
    echo [INFO] Prima esecuzione: compilazione del Server in corso...
    call mvn -pl Server -am package -DskipTests -q -B > build.log 2>&1
    if errorlevel 1 (
        echo [ERRORE] Compilazione fallita. Vedi build.log
        echo.
        pause
        exit /b 1
    )
)

echo [AVVIO] Server in avvio, attendi qualche istante...
echo.

java -XX:+UseSerialGC --enable-native-access=ALL-UNNAMED -jar "Server\target\Server-1.0-SNAPSHOT.jar" --spring.main.banner-mode=off --logging.level.root=WARN --logging.level.org.springframework.boot.web.embedded.tomcat.TomcatWebServer=INFO 2>&1 | powershell -NoProfile -Command "$input | ForEach-Object { if ($_ -match 'Tomcat started') { Write-Host '================================================'; Write-Host '  [PRONTO] Server avviato correttamente!'; Write-Host ''; Write-Host '  Server:  http://localhost:8080'; Write-Host '  Swagger: http://localhost:8080/swagger-ui.html'; Write-Host ''; Write-Host '  Premi CTRL+C per arrestarlo.'; Write-Host '================================================'; Write-Host '' } elseif ($_ -match 'Tomcat initialized|^WARNING: ') { } else { $_ } }"

echo.
echo ------------------------------------------------
echo   Server arrestato.
echo.
pause
endlocal