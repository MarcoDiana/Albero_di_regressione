#!/usr/bin/env bash
set -e

echo "==============================================================================="
echo "  ALBERO DI REGRESSIONE - AVVIO COMPLETO AMBIENTE LOCALE (ALL-IN-ONE)"
echo "==============================================================================="

FLAG_PROVA=0
for arg in "$@"; do
    if [ "$arg" == "-prova" ] || [ "$arg" == "--sample" ] || [ "$arg" == "-p" ]; then
        FLAG_PROVA=1
    fi
done

# 1. Inizializzazione DB se richiesto -prova
if [ "$FLAG_PROVA" -eq 1 ]; then
    echo "[Fase 1/4] Flag '-prova' rilevato: inizializzazione MapDB con tabelle di test..."
    ./init-db.sh -prova || echo "[ATTENZIONE] Inizializzazione DB non riuscita."
else
    echo "[Fase 1/4] Nessun flag '-prova': avvio con database esistente."
fi

# 2. Compilazione se necessario
if [ ! -f "Server/target/Server-1.0-SNAPSHOT.jar" ] || [ ! -f "Client/target/Client-1.0-SNAPSHOT.jar" ]; then
    echo ""
    echo "[Fase 2/4] Artefatti non trovati. Compilazione con build.sh..."
    ./build.sh
else
    echo ""
    echo "[Fase 2/4] Artefatti JAR presenti e pronti."
fi

# 3. Avvio Server in background
echo ""
echo "[Fase 3/4] Avvio Server REST (porta 8080) in background..."
java -jar "Server/target/Server-1.0-SNAPSHOT.jar" > server.log 2>&1 &
SERVER_PID=$!
echo "  Server avviato con PID: $SERVER_PID (log in server.log)"

echo "In attesa che il Server sia operativo su http://localhost:8080..."
for i in {1..30}; do
    if curl -s http://localhost:8080/v3/api-docs > /dev/null 2>&1; then
        echo "  Server pronto e operativo!"
        break
    fi
    sleep 1
done

# 4. Avvio Client
echo ""
echo "[Fase 4/4] Avvio Client Web Vaadin (porta 8081)..."
echo "  - URL Web Client: http://localhost:8081"
echo "  - Swagger UI:     http://localhost:8080/swagger-ui.html"
echo ""

trap "echo 'Arresto dei servizi in corso...'; kill $SERVER_PID 2>/dev/null || true; exit" INT TERM EXIT

java -jar "Client/target/Client-1.0-SNAPSHOT.jar"
