package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.ratpack.finance.reactive.domain.mybatis.model.CurrencyEntityModel;
import za.co.ratpack.finance.reactive.rest.v1.dto.BatchCurrencyRequest;
import za.co.ratpack.finance.reactive.rest.v1.dto.CurrencyRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ClasspathSchemaRegistry to verify generic class discovery and matching.
 */
class ClasspathSchemaRegistryTest {
    
    private ClasspathSchemaRegistry registry;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        registry = new ClasspathSchemaRegistry();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void testRegistryInitialization() {
        // Registry should discover classes from default packages
        Collection<Class<?>> classes = registry.getAllClasses();
        
        assertNotNull(classes);
        assertFalse(classes.isEmpty(), "Should discover at least some classes");
        
        // Verify known classes are discovered
        Optional<Class<?>> currencyRequest = registry.getClassByName("CurrencyRequest");
        assertTrue(currencyRequest.isPresent(), "Should find CurrencyRequest");
        assertEquals(CurrencyRequest.class, currencyRequest.get());
        
        Optional<Class<?>> currencyEntity = registry.getClassByName("CurrencyEntityModel");
        assertTrue(currencyEntity.isPresent(), "Should find CurrencyEntityModel");
        assertEquals(CurrencyEntityModel.class, currencyEntity.get());
        
        Optional<Class<?>> batchRequest = registry.getClassByName("BatchCurrencyRequest");
        assertTrue(batchRequest.isPresent(), "Should find BatchCurrencyRequest");
        assertEquals(BatchCurrencyRequest.class, batchRequest.get());
    }
    
    @Test
    void testExactFieldMatch() {
        // Create JSON matching CurrencyRequest exactly
        Set<String> fields = Set.of("currencyId", "currencyCode", "currencyName", "currencySymbol", "currencyFlag");
        
        Optional<Class<?>> matched = registry.findMatchingClass(fields);
        
        assertTrue(matched.isPresent(), "Should match CurrencyRequest");
        assertEquals(CurrencyRequest.class, matched.get());
    }
    
    @Test
    void testSubsetFieldMatch() {
        // Create JSON with subset of CurrencyEntityModel fields (missing some inherited fields)
        Set<String> fields = Set.of("id", "currencyId", "currencyCode", "currencyName");
        
        Optional<Class<?>> matched = registry.findMatchingClass(fields);
        
        assertTrue(matched.isPresent(), "Should match CurrencyEntityModel as subset");
        assertEquals(CurrencyEntityModel.class, matched.get());
    }
    
    @Test
    void testFieldNamesExtraction() {
        Set<String> currencyRequestFields = registry.getFieldNames(CurrencyRequest.class);
        
        assertNotNull(currencyRequestFields);
        assertTrue(currencyRequestFields.contains("currencyId"));
        assertTrue(currencyRequestFields.contains("currencyCode"));
        assertTrue(currencyRequestFields.contains("currencyName"));
        assertTrue(currencyRequestFields.contains("currencySymbol"));
        assertTrue(currencyRequestFields.contains("currencyFlag"));
    }
    
    @Test
    void testFieldNamesWithInheritance() {
        Set<String> entityFields = registry.getFieldNames(CurrencyEntityModel.class);
        
        assertNotNull(entityFields);
        // Should include own fields
        assertTrue(entityFields.contains("currencyId"));
        assertTrue(entityFields.contains("currencyCode"));
        // Should include inherited fields from AbstractAuditEntityModel
        assertTrue(entityFields.contains("id"));
        assertTrue(entityFields.contains("createdBy"));
        assertTrue(entityFields.contains("createdAt"));
    }
    
    @Test
    void testNoMatchForUnknownFields() {
        // Create JSON with completely unknown fields
        Set<String> fields = Set.of("unknownField1", "unknownField2", "unknownField3");
        
        Optional<Class<?>> matched = registry.findMatchingClass(fields);
        
        assertFalse(matched.isPresent(), "Should not match unknown fields");
    }
    
    @Test
    void testEmptyFieldsReturnsEmpty() {
        Optional<Class<?>> matched = registry.findMatchingClass(Collections.emptySet());
        
        assertFalse(matched.isPresent(), "Should not match empty field set");
    }
    
    @Test
    void testCustomPackageScanning() {
        List<String> customPackages = Collections.singletonList("za.co.ratpack.finance.reactive.rest.v1.dto");
        ClasspathSchemaRegistry customRegistry = new ClasspathSchemaRegistry(customPackages);
        
        // Should find DTOs
        Optional<Class<?>> currencyRequest = customRegistry.getClassByName("CurrencyRequest");
        assertTrue(currencyRequest.isPresent());
        
        // Should find nested DTO in BatchCurrencyRequest package
        Optional<Class<?>> batchRequest = customRegistry.getClassByName("BatchCurrencyRequest");
        assertTrue(batchRequest.isPresent());
    }
    
    @Test
    void testSystemPropertyConfiguration() {
        // Set system property
        String originalProp = System.getProperty("openapi.spec.scan.packages");
        try {
            System.setProperty("openapi.spec.scan.packages", 
                "za.co.ratpack.finance.reactive.rest.v1.dto");
            
            ClasspathSchemaRegistry propRegistry = new ClasspathSchemaRegistry();
            
            Optional<Class<?>> currencyRequest = propRegistry.getClassByName("CurrencyRequest");
            assertTrue(currencyRequest.isPresent(), "Should respect system property");
        } finally {
            // Restore original property
            if (originalProp != null) {
                System.setProperty("openapi.spec.scan.packages", originalProp);
            } else {
                System.clearProperty("openapi.spec.scan.packages");
            }
        }
    }
    
    @Test
    void testBatchCurrencyRequestHasNestedFields() {
        Set<String> fields = registry.getFieldNames(BatchCurrencyRequest.class);
        
        assertNotNull(fields);
        assertTrue(fields.contains("batchCurrencies"), "Should have batchCurrencies field");
    }
    
    @Test
    void testMatchingWithJsonLikeFields() throws Exception {
        // Simulate extracting fields from actual JSON
        String json = """
            {
                "currencyId": 840,
                "currencyCode": "USD",
                "currencyName": "US Dollar",
                "currencySymbol": "$",
                "currencyFlag": "🇺🇸"
            }
            """;
        
        JsonNode node = objectMapper.readTree(json);
        Set<String> fields = new HashSet<>();
        node.fieldNames().forEachRemaining(fields::add);
        
        Optional<Class<?>> matched = registry.findMatchingClass(fields);
        
        assertTrue(matched.isPresent());
        assertEquals(CurrencyRequest.class, matched.get());
    }
    
    @Test
    void testMatchingEntityWithId() throws Exception {
        // CurrencyEntityModel has 'id' field from parent, CurrencyRequest doesn't
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
        
        JsonNode node = objectMapper.readTree(json);
        Set<String> fields = new HashSet<>();
        node.fieldNames().forEachRemaining(fields::add);
        
        Optional<Class<?>> matched = registry.findMatchingClass(fields);
        
        assertTrue(matched.isPresent());
        // Should match CurrencyEntityModel because it has the 'id' field
        assertEquals(CurrencyEntityModel.class, matched.get());
    }
}
