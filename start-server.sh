#!/usr/bin/env bash
set -e

echo "==============================================================================="
echo "  ALBERO DI REGRESSIONE - AVVIO SERVER BACKEND REST (PORTA 8080)"
echo "==============================================================================="

if [ ! -f "Server/target/Server-1.0-SNAPSHOT.jar" ]; then
    echo "[ATTENZIONE] Il fat-JAR del Server non e' presente. Avvio build.sh..."
    ./build.sh
fi

echo ""
echo "Avvio backend Spring Boot su porta 8080..."
echo "  - Swagger UI:   http://localhost:8080/swagger-ui.html"
echo "  - OpenAPI Docs: http://localhost:8080/v3/api-docs"
echo ""
echo "Premi CTRL+C per arrestare il Server."
echo "==============================================================================="
echo ""

java -jar "Server/target/Server-1.0-SNAPSHOT.jar"
