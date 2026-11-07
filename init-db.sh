#!/usr/bin/env bash
set -e

echo "==============================================================================="
echo "  ALBERO DI REGRESSIONE - INIZIALIZZAZIONE DATABASE MYSQL (MapDB)"
echo "==============================================================================="

FLAG_PROVA=0
for arg in "$@"; do
    if [ "$arg" == "-prova" ] || [ "$arg" == "--sample" ] || [ "$arg" == "-p" ]; then
        FLAG_PROVA=1
    fi
done

if ! command -v mysql &> /dev/null; then
    echo "[ATTENZIONE] Il comando 'mysql' non e' installato o non e' presente nel PATH."
    echo "Esegui manualmente i seguenti script SQL in MySQL:"
    echo "  1. sql/01-init-schema.sql"
    if [ "$FLAG_PROVA" -eq 1 ]; then
        echo "  2. sql/02-sample-data.sql"
    fi
    exit 1
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_ADMIN_USER="${DB_ADMIN_USER:-root}"

echo "Connessione a MySQL in corso su ${DB_HOST}:${DB_PORT} come '${DB_ADMIN_USER}'..."
echo "(Inserisci la password di root di MySQL se richiesta)"
echo ""

echo "[1/2] Creazione database MapDB ed utente MapUser..."
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_ADMIN_USER" -p < "sql/01-init-schema.sql"
echo "      Database MapDB ed utente MapUser configurati con successo."

if [ "$FLAG_PROVA" -eq 1 ]; then
    echo ""
    echo "[2/2] Flag '-prova' rilevato: popolamento tabelle dimostrative (provaC e servo)..."
    if mysql -h "$DB_HOST" -P "$DB_PORT" -u MapUser -pmap MapDB < "sql/02-sample-data.sql" 2>/dev/null; then
        echo "      Tabelle 'provaC' e 'servo' inserite con successo tramite utente MapUser."
    else
        mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_ADMIN_USER" -p MapDB < "sql/02-sample-data.sql"
        echo "      Tabelle 'provaC' e 'servo' inserite con successo tramite utente amministratore."
    fi
else
    echo ""
    echo "[2/2] Nessun flag '-prova' specificato: database MapDB pronto e pulito."
fi

echo ""
echo "==============================================================================="
echo "  INIZIALIZZAZIONE COMPLETATA CON SUCCESSO!"
echo "==============================================================================="
