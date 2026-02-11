# API Documentation with Scalar UI

This project includes Docker Compose setup to serve the auto-generated OpenAPI specification through a beautiful Scalar UI interface.

## Prerequisites

- Docker and Docker Compose installed
- OpenAPI spec generated (run tests to generate it)

## Quick Start

### Option 1: Using the convenience script

```bash
./start-api-docs.sh
```

This will:
1. Check if Docker is running
2. Verify the OpenAPI spec exists
3. Start the documentation server
4. Display the URL to access the docs

### Option 2: Using Docker Compose directly

```bash
docker compose -f docker-compose-docs.yml up -d
```

## Accessing the Documentation

Once started, open your browser and navigate to:

**👉 http://localhost:8080**

You'll see a beautiful Scalar UI interface displaying your API documentation with:
- Interactive API explorer
- Request/response examples
- Multiple response scenarios
- Complete header documentation
- Path parameters
- Schema definitions

## Stopping the Documentation Server

### Using the convenience script:

```bash
./stop-api-docs.sh
```

### Using Docker Compose:

```bash
docker compose -f docker-compose-docs.yml down
```

## Generating/Updating the OpenAPI Spec

The API documentation is auto-generated from integration tests. To regenerate the spec:

```bash
# Run all currency resource integration tests
./gradlew test --tests "*CurrencyResourceIT"

# Or run specific tests
./gradlew test --tests "QueryCurrencyByCodeMultipleResponsesIT"
./gradlew test --tests "WriteCurrencyMultipleStatusCodesIT"
```

The OpenAPI spec will be updated at: `src/test/resources/spec/openapi.yaml`

## Architecture

The documentation setup consists of:

- **docker-compose-docs.yml**: Defines the nginx service to serve static files
- **src/test/resources/spec/index.html**: HTML page that loads Scalar UI from CDN
- **src/test/resources/spec/openapi.yaml**: Auto-generated OpenAPI 3.0.3 specification
- **start-api-docs.sh**: Convenience script to start the documentation server
- **stop-api-docs.sh**: Convenience script to stop the documentation server

## Features

The Scalar UI provides:

- ✅ Beautiful, modern interface
- ✅ Dark/light mode support
- ✅ Interactive API testing
- ✅ Multiple example responses per endpoint
- ✅ Complete request/response documentation
- ✅ Header examples
- ✅ Path parameter documentation
- ✅ Schema inference and visualization

## Troubleshooting

### Port Already in Use

If port 8080 is already in use, you can modify the port mapping in `docker-compose-docs.yml`:

```yaml
ports:
  - "8081:80"  # Change 8080 to another port
```

### OpenAPI Spec Not Found

If you see a warning about the missing OpenAPI spec, run the tests first:

```bash
./gradlew test --tests "*CurrencyResourceIT"
```

### Container Won't Start

Check Docker logs:

```bash
docker compose -f docker-compose-docs.yml logs
```
