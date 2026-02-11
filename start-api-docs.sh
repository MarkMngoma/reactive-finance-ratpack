#!/bin/bash

# Script to start the Scalar API Documentation UI
# Usage: ./start-api-docs.sh

set -e

echo "🚀 Starting Reactive Finance Ratpack API Documentation..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if OpenAPI spec exists
if [ ! -f "src/test/resources/spec/openapi.yaml" ]; then
    echo "⚠️  Warning: OpenAPI spec not found at src/test/resources/spec/openapi.yaml"
    echo "   Please run tests first to generate the OpenAPI specification:"
    echo "   ./gradlew test --tests '*CurrencyResourceIT'"
    exit 1
fi

# Start the Docker container
echo "📚 Starting API Documentation server..."
docker compose -f docker-compose-docs.yml up -d

# Wait a moment for the container to start
sleep 2

# Check if container is running
if docker ps | grep -q "reactive-finance-api-docs"; then
    echo ""
    echo "✅ API Documentation is now running!"
    echo ""
    echo "📖 Open your browser and navigate to:"
    echo "   👉 http://localhost:8080"
    echo ""
    echo "To stop the documentation server, run:"
    echo "   docker compose -f docker-compose-docs.yml down"
else
    echo ""
    echo "❌ Failed to start API Documentation server"
    echo "   Check Docker logs with: docker compose -f docker-compose-docs.yml logs"
    exit 1
fi
