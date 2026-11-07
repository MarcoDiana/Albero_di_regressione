#!/usr/bin/env bash
set -e

echo "==============================================================================="
echo "  ALBERO DI REGRESSIONE - ESECUZIONE SUITE COMPLETA TEST AUTOMATIZZATI (64 TEST)"
echo "==============================================================================="

if ! command -v mvn &> /dev/null; then
    echo "[ERRORE] Apache Maven non trovato nel PATH."
    exit 1
fi

echo "Avvio esecuzione test JUnit 5 (Server e Client)..."
echo ""

mvn test

echo ""
echo "==============================================================================="
echo "  TUTTI I 64 TEST SONO STATI SUPERATI CON SUCCESSO AL 100%!"
echo "==============================================================================="
