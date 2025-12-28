#!/bin/bash
# ===========================================
# Script wait-for-db.sh
# Attend que MySQL soit prêt avant de lancer l'application
# TP26 - Microservice Observable & Résilient
# ===========================================

set -e

# Configuration
DB_HOST="${DB_HOST:-mysql}"
DB_PORT="${DB_PORT:-3306}"
MAX_RETRIES="${MAX_RETRIES:-30}"
RETRY_INTERVAL="${RETRY_INTERVAL:-2}"

echo "============================================="
echo "  Wait-for-DB Script - Book Service"
echo "============================================="
echo "Host: $DB_HOST"
echo "Port: $DB_PORT"
echo "Max retries: $MAX_RETRIES"
echo "Retry interval: ${RETRY_INTERVAL}s"
echo "============================================="

# Fonction de vérification de la connexion
check_db() {
    # Utiliser nc (netcat) pour tester la connexion TCP
    if command -v nc &> /dev/null; then
        nc -z $DB_HOST $DB_PORT 2>/dev/null
        return $?
    # Alternative: utiliser bash TCP
    elif command -v bash &> /dev/null; then
        (echo > /dev/tcp/$DB_HOST/$DB_PORT) 2>/dev/null
        return $?
    # Alternative: utiliser timeout avec cat
    else
        timeout 1 bash -c "cat < /dev/null > /dev/tcp/$DB_HOST/$DB_PORT" 2>/dev/null
        return $?
    fi
}

# Boucle d'attente
retry_count=0
echo "Waiting for database at $DB_HOST:$DB_PORT..."

while ! check_db; do
    retry_count=$((retry_count + 1))
    
    if [ $retry_count -ge $MAX_RETRIES ]; then
        echo "ERROR: Database not available after $MAX_RETRIES attempts. Exiting."
        exit 1
    fi
    
    echo "Attempt $retry_count/$MAX_RETRIES - Database not ready yet. Retrying in ${RETRY_INTERVAL}s..."
    sleep $RETRY_INTERVAL
done

echo "============================================="
echo "Database is ready! Starting application..."
echo "============================================="

# Lancer l'application Java
exec java $JAVA_OPTS -jar /app/app.jar "$@"
