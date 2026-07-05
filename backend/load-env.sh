#!/bin/bash
# Loads backend/.env into the current shell session
# Usage: source backend/load-env.sh

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

if [ -f "$ENV_FILE" ]; then
    set -a   # automatically export all variables
    source "$ENV_FILE"
    set +a   # stop auto-exporting
    echo "✓ Environment variables loaded from .env"
    echo "  DATABASE_URL: $DATABASE_URL"
    echo "  DB_USERNAME:  $DB_USERNAME"
    echo "  JWT_SECRET:   [set, ${#JWT_SECRET} chars]"
else
    echo "✗ .env file not found at $ENV_FILE"
fi
