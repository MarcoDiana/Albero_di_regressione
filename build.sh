#!/usr/bin/env bash
set -e

echo "==============================================================================="
echo "  ALBERO DI REGRESSIONE - COMPILAZIONE E PACKAGING DEI MODULI (MAVEN)"
echo "==============================================================================="

# 1. Verifica Java
if ! command -v java &> /dev/null; then
    echo "[ERRORE] Java non trovato nel PATH. Installa Java 21+ e imposta JAVA_HOME."
    exit 1
fi

# 2. Verifica Maven
if ! command -v mvn &> /dev/null; then
    echo "[ERRORE] Apache Maven non trovato nel PATH."
    exit 1
fi

echo "[1/3] Java e Maven rilevati."
echo "[2/3] Compilazione ed impacchettamento fat-JAR..."
echo ""

mvn clean package -DskipTests

echo ""
echo "[3/3] Verifica artefatti generati:"
if [ -f "Server/target/Server-1.0-SNAPSHOT.jar" ]; then
    echo "  - Server JAR: OK (Server/target/Server-1.0-SNAPSHOT.jar)"
fi

if [ -f "Client/target/Client-1.0-SNAPSHOT.jar" ]; then
    echo "  - Client JAR: OK (Client/target/Client-1.0-SNAPSHOT.jar)"
fi

echo ""
echo "==============================================================================="
echo "  BUILD COMPLETATA CON SUCCESSO!"
echo "==============================================================================="
