#!/bin/bash

# Script to stop the Scalar API Documentation UI
# Usage: ./stop-api-docs.sh

set -e

echo "🛑 Stopping Reactive Finance Ratpack API Documentation..."

docker compose -f docker-compose-docs.yml down

echo "✅ API Documentation server stopped"
