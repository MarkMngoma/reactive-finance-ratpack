# API Documentation

This project includes **integrated API documentation** served directly by the Ratpack application on port 5051. The documentation features a beautiful Scalar UI interface with **classic theme** and **dark mode toggle**.

## Quick Start

### Running the Application

Start the Ratpack application:

```bash
./gradlew run
```

### Accessing the Documentation

Once the application is running, the API documentation is available at:

**👉 http://localhost:5051/api-docs**

The OpenAPI specification is also available at:

**👉 http://localhost:5051/api-docs/openapi.yaml**

## Features

The Scalar UI provides:

- ✅ **Integrated with Application** - Runs on same port as API (5051)
- ✅ **Classic theme as default** - Clean, professional appearance
- ✅ **Dark mode toggle** - Switch between light and dark themes
- ✅ **Interactive API explorer** - Test endpoints directly from the UI
- ✅ **Multiple response examples** - See all possible response scenarios
- ✅ **Complete documentation** - Headers, parameters, schemas
- ✅ **Auto-synced** - Updates when OpenAPI spec regenerates

## Architecture

The documentation is served by the Ratpack application:

```
Ratpack Application (Port 5051)
├── /v1/* → API Endpoints
└── /api-docs/* → API Documentation
    ├── /api-docs → Scalar UI (index.html)
    └── /api-docs/openapi.yaml → OpenAPI 3.0.3 Specification
```

Files are served from `src/main/resources/api-docs/`:
- `index.html` - Scalar UI loader with theme configuration
- `openapi.yaml` - Auto-generated OpenAPI specification

## Generating/Updating the OpenAPI Spec

The API documentation is auto-generated from integration tests. To regenerate the spec:

```bash
# Run all currency resource integration tests
./gradlew test --tests "*CurrencyResourceIT"

# Or run specific tests
./gradlew test --tests "QueryCurrencyByCodeMultipleResponsesIT"
./gradlew test --tests "WriteCurrencyMultipleStatusCodesIT"
```

After running tests:
1. The OpenAPI spec is generated at `src/test/resources/spec/openapi.yaml`
2. Copy it to `src/main/resources/api-docs/openapi.yaml`
3. Restart the application to see updates

## UI Theme Configuration

The Scalar UI is configured with:
- ✅ **Classic theme** as the default (professional, clean look)
- ✅ **Dark mode toggle** available in the top-right corner
- ✅ **Classic layout** for optimal readability

To customize the theme, edit `src/main/resources/api-docs/index.html`:

```javascript
var configuration = {
  spec: { url: '/openapi.yaml' },
  theme: 'default',  // Classic theme (options: 'default', 'alternate', 'moon', 'purple', 'solarized')
  darkMode: true,    // Enable dark mode toggle (true/false)
  layout: 'classic'  // Use classic layout (options: 'classic', 'modern')
}
```

## Alternative: Docker Compose (Standalone)

If you prefer to run the documentation independently on port 8080:

```bash
./start-api-docs.sh
# or
docker compose -f docker-compose-docs.yml up -d
```

Access at: http://localhost:8080

Stop with:
```bash
./stop-api-docs.sh
# or
docker compose -f docker-compose-docs.yml down
```

## Benefits of Integrated Approach

✅ **Same Port**: Documentation and API on one port (5051)  
✅ **No Separate Container**: Simpler deployment  
✅ **Easy Testing**: Test API and view docs simultaneously  
✅ **Integrated**: Bundled in application JAR  
✅ **Production Ready**: Deploy documentation with your application

## Troubleshooting

### "Document 'api-1' could not be loaded" Error

If you see this error in Scalar UI:

**Cause**: The OpenAPI spec URL in `index.html` doesn't match the serving context.

**Solution**: 
- For Ratpack integration (`/api-docs`): Ensure `src/main/resources/api-docs/index.html` uses `url: '/api-docs/openapi.yaml'`
- For Docker Compose (root): Ensure `src/test/resources/spec/index.html` uses `url: '/openapi.yaml'`

Verify the spec is accessible:
```bash
# For Ratpack
curl http://localhost:5051/api-docs/openapi.yaml

# For Docker Compose
curl http://localhost:8080/openapi.yaml
```

### Documentation Not Loading

If the documentation doesn't load:

1. **Check application is running**:
   ```bash
   curl http://localhost:5051/api-docs
   ```

2. **Verify OpenAPI spec exists**:
   ```bash
   ls -la src/main/resources/api-docs/openapi.yaml
   ```

3. **Check application logs** for any errors

### OpenAPI Spec Not Found

If you see a 404 for the OpenAPI spec:

1. Run tests to generate it:
   ```bash
   ./gradlew test --tests "*CurrencyResourceIT"
   ```

2. Copy to main resources:
   ```bash
   cp src/test/resources/spec/openapi.yaml src/main/resources/api-docs/
   ```

3. Restart the application

### Port Already in Use

If port 5051 is already in use, update `src/main/resources/application-localhost.yml`:

```yaml
server:
  defaultPort: 8080  # Change to another port
```

## Implementation Details

The API documentation is served by:

- **ApiDocsHandler** (`src/main/java/.../handlers/ApiDocsHandler.java`)
  - Serves static files from classpath resources
  - Handles content type detection (HTML, YAML, JSON)
  - Returns 404 for missing resources

- **ServerCommand** (`src/main/java/.../ServerCommand.java`)
  - Routes `/api-docs` prefix to ApiDocsHandler
  - Integrated into main application routing chain

- **FunctionHandlerModule** (`src/main/java/.../guice/modules/FunctionHandlerModule.java`)
  - Registers ApiDocsHandler with Guice for dependency injection

