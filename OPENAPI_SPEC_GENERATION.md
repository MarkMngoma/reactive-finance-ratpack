# OpenAPI Spec Generation - How It Works

## Overview

This document explains how the OpenAPI specification is automatically generated from integration tests, with specific focus on how DTOs and entities are detected and documented.

## Architecture

### Key Components

1. **OpenApiTestHttpClient** - Wraps test HTTP client to capture requests/responses
2. **OpenApiCapture** - Thread-safe singleton that collects captured interactions
3. **CapturedInteraction** - POJO storing a single HTTP interaction with metadata
4. **OpenApiSpecExtension** - JUnit 5 extension that triggers spec generation after tests
5. **OpenApiSpecWriter** - Generates OpenAPI YAML from captured interactions
6. **SchemaIntrospector** - Uses reflection to introspect Java classes and generate schemas

## How Schema Detection Works

### Request DTO Detection (CurrencyRequest, BatchCurrencyRequest)

When a test makes a request with a JSON body, the `OpenApiSpecWriter` attempts to detect known DTO patterns:

**CurrencyRequest Detection** (lines 648-654):
```java
if (node.isObject() && !node.has("id") && node.has("currencyId") && 
    node.has("currencyCode") && node.has("currencyName") && 
    node.has("currencySymbol") && node.has("currencyFlag")) {
  // Detected CurrencyRequest (no 'id' field = request DTO)
  Class<?> currencyRequestClass = Class.forName(
    "za.co.ratpack.finance.reactive.rest.v1.dto.CurrencyRequest");
  Schema<?> schema = SchemaIntrospector.introspectClass(
    currencyRequestClass, "CurrencyRequest");
  return new SchemaWithName(schema, "CurrencyRequest");
}
```

**Key distinguisher**: CurrencyRequest does NOT have an `id` field (it's a creation request).

**BatchCurrencyRequest Detection** (lines 657-671):
```java
if (node.isObject() && node.has("batchCurrencies") && 
    node.get("batchCurrencies").isArray()) {
  // Detected BatchCurrencyRequest
  // Registers CurrencyRequest first (it's referenced)
  // Then registers BatchCurrencyRequest
}
```

### Response Entity Detection (CurrencyEntityModel)

When a test receives a response with a JSON body, the same detection logic applies:

**Single CurrencyEntityModel Detection** (lines 638-645):
```java
if (node.isObject() && node.has("id") && node.has("currencyId") && 
    node.has("currencyCode") && node.has("currencyName") && 
    node.has("currencySymbol") && node.has("currencyFlag")) {
  // Detected CurrencyEntityModel (has 'id' field = entity response)
  Class<?> entityClass = Class.forName(
    "za.co.ratpack.finance.reactive.domain.mybatis.model.CurrencyEntityModel");
  Schema<?> schema = SchemaIntrospector.introspectClass(
    entityClass, "CurrencyEntityModel");
  return new SchemaWithName(schema, "CurrencyEntityModel");
}
```

**Key distinguisher**: CurrencyEntityModel HAS an `id` field (it's a persisted entity).

**Array of CurrencyEntityModel Detection** (lines 674-694):
```java
if (node.isArray() && node.size() > 0) {
  JsonNode firstItem = node.get(0);
  if (firstItem.isObject() && firstItem.has("id") && 
      firstItem.has("currencyId") && firstItem.has("currencyCode") && 
      firstItem.has("currencyName")) {
    // Detected array of CurrencyEntityModel
    // Register CurrencyEntityModel schema
    // Return array schema with $ref
  }
}
```

### SchemaIntrospector

Once a DTO/entity is detected, `SchemaIntrospector` uses reflection to:

1. Load the class
2. Read field annotations (@Schema, @NotNull)
3. Detect field types (String, Integer, LocalDateTime, etc.)
4. Generate OpenAPI Schema objects with:
   - Field descriptions from @Schema annotations
   - Examples from @Schema annotations
   - Required fields from @NotNull annotations
   - Proper formats (int32, int64, date-time, etc.)
   - Inherited fields from parent classes

## Schema Registration

### Request Body Processing (lines 398-470)

```java
private RequestBody createRequestBody(List<CapturedInteraction> interactions) {
  // Get first interaction as sample
  CapturedInteraction sample = interactions.get(0);
  
  // Try to detect known DTO classes
  SchemaWithName schemaWithName = tryIntrospectKnownClass(sample.getRequestBody());
  
  if (schemaWithName != null && schemaWithName.name != null) {
    // Register schema in components/schemas if not already there
    if (!openAPI.getComponents().getSchemas().containsKey(schemaWithName.name)) {
      openAPI.getComponents().addSchemas(schemaWithName.name, schemaWithName.schema);
    }
    // Create $ref to the schema
    Schema<?> refSchema = new Schema<>();
    refSchema.set$ref("#/components/schemas/" + schemaWithName.name);
    mediaType.setSchema(refSchema);
  }
  // ... handle examples
}
```

### Response Body Processing (lines 477-603)

```java
private ApiResponses createResponses(List<CapturedInteraction> interactions) {
  // Group by status code
  for (Map.Entry<Integer, List<CapturedInteraction>> entry : byStatus.entrySet()) {
    // Get sample with body
    CapturedInteraction sampleWithBody = statusInteractions.stream()
      .filter(i -> i.getResponseBody() != null && !i.getResponseBody().isEmpty())
      .findFirst()
      .orElse(null);
    
    if (sampleWithBody != null) {
      // Try to detect known DTO/entity classes
      SchemaWithName schemaWithName = tryIntrospectKnownClass(
        sampleWithBody.getResponseBody());
      
      if (schemaWithName != null && schemaWithName.name != null) {
        // Register schema in components/schemas if not already there
        if (!openAPI.getComponents().getSchemas().containsKey(schemaWithName.name)) {
          openAPI.getComponents().addSchemas(schemaWithName.name, schemaWithName.schema);
        }
        // Create $ref to the schema
        Schema<?> refSchema = new Schema<>();
        refSchema.set$ref("#/components/schemas/" + schemaWithName.name);
        mediaType.setSchema(refSchema);
      }
    }
    // ... handle examples and headers
  }
}
```

## Requirements for CurrencyEntityModel to Appear

For CurrencyEntityModel to be automatically detected and included in the generated spec:

1. **Database must be running** - Tests need to successfully query the database
2. **Tests must return entities** - GET requests must return actual CurrencyEntityModel objects
3. **Response must have 'id' field** - This distinguishes entities from request DTOs
4. **Response must be JSON** - Content-Type: application/json

### Example Test Flow:

```java
@Test
@DocumentApi(
  description = "Query all currencies",
  path = "/v1/QueryCurrencyResource",
  summary = "Get all currencies",
  tags = {"QueryBatch"}
)
void testQueryAllCurrencies() {
  OpenApiTestHttpClient apiClient = new OpenApiTestHttpClient(testHttpClient);
  apiClient.setTestContext(this.getClass().getName(), "testQueryAllCurrencies");
  
  // This GET request returns an array of CurrencyEntityModel objects
  var response = apiClient.get("/v1/QueryCurrencyResource");
  
  // Response body example:
  // [
  //   {
  //     "id": 1,                    ← 'id' field present = entity
  //     "currencyId": 840,
  //     "currencyCode": "USD",
  //     "currencyName": "US Dollar",
  //     "currencySymbol": "$",
  //     "currencyFlag": "🇺🇸",
  //     "createdBy": 1,
  //     "createdAt": "2024-12-23T00:34:00"
  //   }
  // ]
  
  // OpenApiSpecWriter will:
  // 1. Parse response JSON
  // 2. Detect array of CurrencyEntityModel pattern (has 'id' field)
  // 3. Use SchemaIntrospector to introspect CurrencyEntityModel class
  // 4. Register schema in components/schemas
  // 5. Create array schema with $ref to CurrencyEntityModel
}
```

## Generated OpenAPI Spec Structure

### components/schemas section:

```yaml
components:
  schemas:
    CurrencyRequest:              # Request DTO (no id)
      type: object
      required: [currencyId, currencyCode, currencyName, currencySymbol, currencyFlag]
      properties:
        currencyId: {...}
        currencyCode: {...}
        # ... 5 fields total
    
    BatchCurrencyRequest:         # Batch request wrapper
      type: object
      required: [batchCurrencies]
      properties:
        batchCurrencies:
          type: array
          items:
            $ref: '#/components/schemas/CurrencyRequest'
    
    CurrencyEntityModel:          # Response entity (has id)
      type: object
      required: [id, createdBy, createdAt]
      properties:
        id: {...}               # Primary key
        currencyId: {...}
        currencyCode: {...}
        # ... 8 fields total including audit fields
```

### paths section with $ref:

```yaml
paths:
  /v1/WriteCurrencyResource:
    post:
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CurrencyRequest'  # References request DTO
  
  /v1/QueryCurrencyResource:
    get:
      responses:
        "200":
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/CurrencyEntityModel'  # References response entity
```

## Adding Support for New DTOs/Entities

To add auto-detection for new DTOs or entities:

1. **Add detection logic** in `tryIntrospectKnownClass()` method
2. **Identify unique field pattern** that distinguishes your DTO
3. **Use fully qualified class name** in Class.forName()
4. **Add @Schema annotations** to your DTO/entity fields
5. **Run tests** that exercise the endpoints
6. **Verify schema** appears in generated openapi.yaml

### Example for ExchangeRateRequest:

```java
// In OpenApiSpecWriter.tryIntrospectKnownClass():
if (node.isObject() && node.has("fromCurrency") && node.has("toCurrency") && 
    node.has("rate")) {
  LOG.info("Detected ExchangeRateRequest pattern, using SchemaIntrospector");
  Class<?> exchangeRateClass = Class.forName(
    "za.co.ratpack.finance.reactive.rest.v1.dto.ExchangeRateRequest");
  Schema<?> schema = SchemaIntrospector.introspectClass(
    exchangeRateClass, "ExchangeRateRequest");
  return new SchemaWithName(schema, "ExchangeRateRequest");
}
```

## Debugging

### Enable Debug Logging:

The code logs when it detects DTOs/entities:

```java
LOG.info("Detected CurrencyEntityModel pattern (response entity), using SchemaIntrospector");
LOG.info("Detected CurrencyRequest pattern, using SchemaIntrospector");
LOG.info("Detected BatchCurrencyRequest pattern, using SchemaIntrospector");
LOG.info("Detected array of CurrencyEntityModel, using SchemaIntrospector");
```

### Check if detection is working:

1. Run tests with database connection
2. Check logs for "Detected" messages
3. Check generated spec has schemas in components/schemas
4. Verify $ref links point to correct schemas

### Common Issues:

**Issue**: CurrencyEntityModel not appearing in spec
- **Cause**: Database not running, so GET queries return empty/error
- **Solution**: Start database before running tests

**Issue**: Schema has inline properties instead of $ref
- **Cause**: Detection pattern didn't match, fell back to JSON inference
- **Solution**: Verify response JSON has all required fields (id, currencyId, etc.)

**Issue**: @Schema annotations not being used
- **Cause**: Class not found or reflection failed
- **Solution**: Check fully qualified class name, verify class is on classpath

## Summary

The code DOES automatically generate CurrencyEntityModel schemas:
- ✅ Detection logic exists (lines 638-645, 674-694)
- ✅ SchemaIntrospector introspects the class
- ✅ Schema registered in components/schemas
- ✅ $ref created in responses
- ✅ All @Schema annotations used

The spec is NOT manually created - it's genuinely auto-generated from test execution!
