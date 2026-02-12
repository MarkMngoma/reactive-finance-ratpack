# OpenAPI Schema Introspection Implementation

## Overview

This implementation provides a **generic, future-proof OpenAPI schema introspection system** that automatically discovers and matches DTO/model classes without hardcoding. The system uses classpath scanning and field-signature matching to identify the correct Java class for any JSON payload.

## Architecture

### Core Components

#### 1. ClasspathSchemaRegistry
**Location:** `src/test/java/za/co/ratpack/finance/reactive/functions/rest/integration/openapi/ClasspathSchemaRegistry.java`

**Purpose:** Discovers and indexes DTO/model classes from configured packages.

**Key Features:**
- Scans configured base packages at startup (lazy initialization)
- Builds two registries:
  - `classNameRegistry`: Simple name → Class<?> mapping
  - `fieldSignatureRegistry`: Set<String> field names → Class<?> mapping
- Extracts field names including inherited fields from parent classes
- Matches JSON payloads using exact or subset field matching with scoring
- Configurable via constructor parameter or system property `openapi.spec.scan.packages`

**Default Packages:**
- `za.co.ratpack.finance.reactive.rest.v1.dto`
- `za.co.ratpack.finance.reactive.domain.mybatis.model`

**Matching Algorithm:**
1. Extract field names from JSON payload
2. Check for exact match (JSON fields == class fields)
3. If no exact match, find best subset match (JSON fields ⊂ class fields)
4. Score subset matches by overlap percentage
5. Return class with highest score

#### 2. SchemaIntrospector
**Location:** `src/test/java/za/co/ratpack/finance/reactive/functions/rest/integration/openapi/SchemaIntrospector.java`

**Purpose:** Uses reflection to generate OpenAPI schema definitions from Java classes.

**Key Features:**
- Introspects any Java class using reflection
- Handles primitives, collections, nested objects, enums
- Respects Jackson annotations (@JsonProperty, @JsonIgnore)
- Generates OpenAPI 3.0 compatible schemas
- Registers schemas in components/schemas section
- Creates $ref references for reusable schemas

**Supported Types:**
- Primitives: String, Integer, Long, Double, Float, Boolean
- Date/Time: LocalDate, LocalDateTime
- Collections: List, Set, Array
- Maps: Map<K, V>
- Enums: Automatically extracts enum values
- Nested Objects: Recursively introspects complex types

#### 3. OpenApiSpecWriter
**Location:** `src/test/java/za/co/ratpack/finance/reactive/functions/rest/integration/openapi/OpenApiSpecWriter.java`

**Purpose:** Main entry point for schema generation. Coordinates between registry and introspector.

**Key Methods:**
- `tryIntrospectKnownClass(String jsonBody)`: Attempts to match JSON to a known class
- `inferSchemaFromJson(String jsonBody)`: Fallback for unknown structures
- `getComponentSchemas()`: Returns all generated schemas

**Workflow:**
1. Parse JSON body to JsonNode
2. Extract field names from JSON
3. Query ClasspathSchemaRegistry for matching class
4. If match found:
   - Use SchemaIntrospector to generate schema
   - Register in components/schemas
   - Return $ref to schema
5. If no match:
   - Fall back to JSON structure inference
   - Generate ad-hoc schema from JSON shape

#### 4. Supporting Classes

**CapturedInteraction:** Value object for HTTP request/response pairs
**OpenApiCapture:** Thread-local storage for test interactions
**OpenApiTestHttpClient:** HTTP client wrapper that captures interactions
**OpenApiSpecExtension:** JUnit 5 extension for automatic spec generation

## Usage Examples

### Basic Usage

```java
// Create writer with default packages
OpenApiSpecWriter writer = new OpenApiSpecWriter();

// Introspect a JSON payload
String json = """
    {
        "currencyId": 840,
        "currencyCode": "USD",
        "currencyName": "US Dollar",
        "currencySymbol": "$",
        "currencyFlag": "🇺🇸"
    }
    """;

Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass(json);
// Returns: {"$ref": "#/components/schemas/CurrencyRequest"}

// Get all generated schemas
Map<String, Map<String, Object>> schemas = writer.getComponentSchemas();
// Contains: CurrencyRequest schema definition
```

### Custom Package Configuration

```java
// Via constructor
List<String> packages = List.of("com.example.dto", "com.example.models");
ClasspathSchemaRegistry registry = new ClasspathSchemaRegistry(packages);
OpenApiSpecWriter writer = new OpenApiSpecWriter(registry);

// Via system property
System.setProperty("openapi.spec.scan.packages", "com.example.dto,com.example.models");
OpenApiSpecWriter writer = new OpenApiSpecWriter();
```

### Array Handling

```java
String jsonArray = """
    [
        {"currencyId": 840, "currencyCode": "USD", ...},
        {"currencyId": 978, "currencyCode": "EUR", ...}
    ]
    """;

Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass(jsonArray);
// Returns: {
//   "type": "array",
//   "items": {"$ref": "#/components/schemas/CurrencyRequest"}
// }
```

### Fallback to Inference

```java
String unknownJson = """
    {
        "unknownField1": "value",
        "unknownField2": 123
    }
    """;

Optional<Map<String, Object>> introspected = writer.tryIntrospectKnownClass(unknownJson);
// Returns: Optional.empty() (no class matched)

Map<String, Object> inferred = writer.inferSchemaFromJson(unknownJson);
// Returns: {
//   "type": "object",
//   "properties": {
//     "unknownField1": {"type": "string"},
//     "unknownField2": {"type": "integer"}
//   }
// }
```

## Testing

### Test Coverage

- **ClasspathSchemaRegistryTest** (12 tests): Tests class discovery, field extraction, and matching
- **OpenApiSpecWriterTest** (12 tests): Tests schema generation and introspection
- **FutureProofSchemaIntrospectionIT** (8 tests): Proves automatic discovery of new DTOs

**Total: 32 passing tests**

### Future-Proof Demonstration

The `FutureProofSchemaIntrospectionIT` test creates two NEW DTOs (`TransactionRequest` and `AccountResponse`) that were NEVER hardcoded in the system, and proves they are automatically discovered and matched:

```java
// NEW DTO - never hardcoded anywhere
String json = """
    {
        "transactionId": "TXN-123456",
        "accountNumber": "ACC-789012",
        "amount": 1500.50,
        "currency": "USD",
        "description": "Payment for services",
        "transactionType": "DEBIT"
    }
    """;

Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass(json);
// ✅ Successfully matches to TransactionRequest
// ✅ Generates proper schema reference
// ✅ NO hardcoded logic needed!
```

## Benefits

### 1. Future-Proof
- New DTOs are automatically discovered
- No code changes needed when adding new models
- Works with any class in configured packages

### 2. Flexible
- Configurable package scanning
- Supports exact and partial field matching
- Graceful fallback to JSON inference

### 3. Maintainable
- Single source of truth (the DTO classes themselves)
- No duplicate schema definitions
- Respects Jackson annotations automatically

### 4. Robust
- Handles inheritance hierarchies
- Supports collections and nested objects
- Thread-safe with lazy initialization

## Configuration

### System Property

```bash
-Dopenapi.spec.scan.packages=com.example.dto,com.example.models
```

### Environment Variable

```bash
export OPENAPI_SPEC_SCAN_PACKAGES=com.example.dto,com.example.models
```

### Programmatic

```java
List<String> packages = Arrays.asList(
    "za.co.ratpack.finance.reactive.rest.v1.dto",
    "za.co.ratpack.finance.reactive.domain.mybatis.model"
);
ClasspathSchemaRegistry registry = new ClasspathSchemaRegistry(packages);
```

## Migration from Hardcoded Approach

### Before (Hardcoded)
```java
if (node.has("id") && node.has("currencyId") && node.has("currencyCode") && ...) {
    Class<?> entityClass = Class.forName("za.co.ratpack.finance.reactive.domain.mybatis.model.CurrencyEntityModel");
    // ... introspect
}
```

### After (Generic)
```java
Set<String> fieldNames = extractFieldNames(node);
Optional<Class<?>> matchedClass = registry.findMatchingClass(fieldNames);
if (matchedClass.isPresent()) {
    introspector.introspectClass(matchedClass.get());
}
```

## Performance Considerations

- **Initialization:** Classpath scanning happens once per ClasspathSchemaRegistry instance
- **Lazy Loading:** Scanning occurs on first use, not at construction time
- **Caching:** Field signatures and schemas are cached for the lifetime of the registry
- **Thread Safety:** Registry initialization is synchronized; matching is thread-safe

## Limitations

- Only scans classes on the filesystem (not from jars by default)
- Requires classes to have accessible fields (respects access modifiers)
- Field name extraction uses reflection (slight performance overhead)
- Jackson annotation support is basic (can be extended)

## Future Enhancements

1. **JAR Scanning:** Add support for scanning classes inside JAR files
2. **Annotation-based Hints:** Allow DTOs to provide matching hints via custom annotations
3. **Validation Integration:** Integrate with Jakarta Validation annotations for required fields
4. **Swagger Annotations:** Support Swagger/OpenAPI annotations for enhanced schemas
5. **Performance Optimization:** Add field signature hashing for faster lookups

## Files Created

### Core Implementation
- `ClasspathSchemaRegistry.java` - Class discovery and matching
- `SchemaIntrospector.java` - Reflection-based schema generation
- `OpenApiSpecWriter.java` - Main coordination class
- `CapturedInteraction.java` - HTTP interaction value object
- `OpenApiCapture.java` - Thread-local capture storage
- `OpenApiTestHttpClient.java` - Test HTTP client with capture
- `OpenApiSpecExtension.java` - JUnit 5 extension

### Tests
- `ClasspathSchemaRegistryTest.java` - Registry tests
- `OpenApiSpecWriterTest.java` - Writer tests
- `FutureProofSchemaIntrospectionIT.java` - Integration test

### Mock DTOs (for testing)
- `TransactionRequest.java` - Demo DTO for future-proof testing
- `AccountResponse.java` - Demo DTO for future-proof testing

## Conclusion

This implementation successfully replaces hardcoded class detection with a generic, scalable, and maintainable solution. Any new DTO added to the configured packages will be automatically discovered and properly introspected, making the system truly future-proof.
