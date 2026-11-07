#!/usr/bin/env bash
set -e

echo "==============================================================================="
echo "  ALBERO DI REGRESSIONE - AVVIO CLIENT WEB VAADIN (PORTA 8081)"
echo "==============================================================================="

if [ ! -f "Client/target/Client-1.0-SNAPSHOT.jar" ]; then
    echo "[ATTENZIONE] Il fat-JAR del Client non e' presente. Avvio build.sh..."
    ./build.sh
fi

echo ""
echo "Avvio frontend Vaadin Flow su porta 8081..."
echo "  - URL Web Client: http://localhost:8081"
echo "  - All'avvio dell'applicazione, il browser predefinito verra' aperto automaticamente!"
echo ""
echo "Premi CTRL+C per arrestare il Client."
echo "==============================================================================="
echo ""

java -jar "Client/target/Client-1.0-SNAPSHOT.jar"
