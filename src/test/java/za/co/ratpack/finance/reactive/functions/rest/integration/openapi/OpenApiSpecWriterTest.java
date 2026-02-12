package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OpenApiSpecWriter to verify generic schema introspection.
 */
class OpenApiSpecWriterTest {
    
    private OpenApiSpecWriter writer;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        writer = new OpenApiSpecWriter();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void testIntrospectCurrencyRequest() {
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
        
        assertTrue(schema.isPresent(), "Should introspect CurrencyRequest");
        assertTrue(schema.get().containsKey("$ref"), "Should return schema reference");
        assertEquals("#/components/schemas/CurrencyRequest", schema.get().get("$ref"));
        
        // Verify schema was registered in components
        Map<String, Map<String, Object>> components = writer.getComponentSchemas();
        assertTrue(components.containsKey("CurrencyRequest"), "Should register schema in components");
    }
    
    @Test
    void testIntrospectCurrencyEntityModel() {
        String json = """
            {
                "id": 1,
                "currencyId": 840,
                "currencyCode": "USD",
                "currencyName": "US Dollar",
                "currencySymbol": "$",
                "currencyFlag": "🇺🇸"
            }
            """;
        
        Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass(json);
        
        assertTrue(schema.isPresent(), "Should introspect CurrencyEntityModel");
        assertTrue(schema.get().containsKey("$ref"));
        assertEquals("#/components/schemas/CurrencyEntityModel", schema.get().get("$ref"));
    }
    
    @Test
    void testIntrospectArrayOfCurrencyRequests() {
        String json = """
            [
                {
                    "currencyId": 840,
                    "currencyCode": "USD",
                    "currencyName": "US Dollar",
                    "currencySymbol": "$",
                    "currencyFlag": "🇺🇸"
                },
                {
                    "currencyId": 978,
                    "currencyCode": "EUR",
                    "currencyName": "Euro",
                    "currencySymbol": "€",
                    "currencyFlag": "🇪🇺"
                }
            ]
            """;
        
        Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass(json);
        
        assertTrue(schema.isPresent(), "Should introspect array");
        assertEquals("array", schema.get().get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> items = (Map<String, Object>) schema.get().get("items");
        assertNotNull(items);
        assertEquals("#/components/schemas/CurrencyRequest", items.get("$ref"));
    }
    
    @Test
    void testIntrospectBatchCurrencyRequest() {
        String json = """
            {
                "batchCurrencies": [
                    {
                        "currencyId": 840,
                        "currencyCode": "USD",
                        "currencyName": "US Dollar",
                        "currencySymbol": "$",
                        "currencyFlag": "🇺🇸"
                    }
                ]
            }
            """;
        
        Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass(json);
        
        assertTrue(schema.isPresent(), "Should introspect BatchCurrencyRequest");
        assertTrue(schema.get().containsKey("$ref"));
        assertEquals("#/components/schemas/BatchCurrencyRequest", schema.get().get("$ref"));
    }
    
    @Test
    void testInferSchemaFromUnknownJson() {
        String json = """
            {
                "unknownField1": "value1",
                "unknownField2": 123,
                "unknownField3": true
            }
            """;
        
        Map<String, Object> schema = writer.inferSchemaFromJson(json);
        
        assertNotNull(schema);
        assertEquals("object", schema.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("unknownField1"));
        assertTrue(properties.containsKey("unknownField2"));
        assertTrue(properties.containsKey("unknownField3"));
    }
    
    @Test
    void testIntrospectEmptyString() {
        Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass("");
        
        assertFalse(schema.isPresent(), "Should not introspect empty string");
    }
    
    @Test
    void testIntrospectNull() {
        Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass((String) null);
        
        assertFalse(schema.isPresent(), "Should not introspect null");
    }
    
    @Test
    void testIntrospectInvalidJson() {
        String invalidJson = "{ this is not valid json }";
        
        Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass(invalidJson);
        
        assertFalse(schema.isPresent(), "Should not introspect invalid JSON");
    }
    
    @Test
    void testIntrospectEmptyArray() {
        String json = "[]";
        
        Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass(json);
        
        assertTrue(schema.isPresent(), "Should handle empty array");
        assertEquals("array", schema.get().get("type"));
    }
    
    @Test
    void testMultipleIntrospectionsShareSchemas() {
        String json1 = """
            {
                "currencyId": 840,
                "currencyCode": "USD",
                "currencyName": "US Dollar",
                "currencySymbol": "$",
                "currencyFlag": "🇺🇸"
            }
            """;
        
        String json2 = """
            {
                "currencyId": 978,
                "currencyCode": "EUR",
                "currencyName": "Euro",
                "currencySymbol": "€",
                "currencyFlag": "🇪🇺"
            }
            """;
        
        writer.tryIntrospectKnownClass(json1);
        writer.tryIntrospectKnownClass(json2);
        
        // Should only create one schema for CurrencyRequest
        Map<String, Map<String, Object>> components = writer.getComponentSchemas();
        assertEquals(1, components.size(), "Should reuse schema for same type");
        assertTrue(components.containsKey("CurrencyRequest"));
    }
    
    @Test
    void testFallbackToInferenceForUnknownClass() {
        String json = """
            {
                "totallyNewField": "value",
                "anotherNewField": 999
            }
            """;
        
        Optional<Map<String, Object>> introspected = writer.tryIntrospectKnownClass(json);
        
        // Should not match any known class
        assertFalse(introspected.isPresent());
        
        // But inference should work
        Map<String, Object> inferred = writer.inferSchemaFromJson(json);
        assertNotNull(inferred);
        assertEquals("object", inferred.get("type"));
    }
    
    @Test
    void testIntrospectorGeneratesProperSchema() {
        String json = """
            {
                "currencyId": 840,
                "currencyCode": "USD",
                "currencyName": "US Dollar",
                "currencySymbol": "$",
                "currencyFlag": "🇺🇸"
            }
            """;
        
        writer.tryIntrospectKnownClass(json);
        
        Map<String, Map<String, Object>> schemas = writer.getComponentSchemas();
        Map<String, Object> currencyRequestSchema = schemas.get("CurrencyRequest");
        
        assertNotNull(currencyRequestSchema);
        assertEquals("object", currencyRequestSchema.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) currencyRequestSchema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("currencyId"));
        assertTrue(properties.containsKey("currencyCode"));
        assertTrue(properties.containsKey("currencyName"));
        assertTrue(properties.containsKey("currencySymbol"));
        assertTrue(properties.containsKey("currencyFlag"));
    }
}
