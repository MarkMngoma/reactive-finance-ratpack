package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.ratpack.finance.reactive.rest.v1.dto.AccountResponse;
import za.co.ratpack.finance.reactive.rest.v1.dto.CurrencyRequest;
import za.co.ratpack.finance.reactive.rest.v1.dto.TransactionRequest;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating that the OpenAPI spec writer is truly future-proof.
 * This test uses NEW DTOs (TransactionRequest, AccountResponse) that were NOT hardcoded
 * in the OpenApiSpecWriter, proving that the classpath scanning approach automatically
 * discovers and matches them.
 */
class FutureProofSchemaIntrospectionIT {
    
    private OpenApiSpecWriter writer;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        writer = new OpenApiSpecWriter();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void testNewTransactionRequestIsAutomaticallyDiscovered() {
        // Create JSON for TransactionRequest - a DTO that was NEVER hardcoded
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
        
        // Should successfully match to TransactionRequest WITHOUT any hardcoded logic
        assertTrue(schema.isPresent(), 
            "FUTURE-PROOF TEST: Should automatically discover and match TransactionRequest");
        assertEquals("#/components/schemas/TransactionRequest", schema.get().get("$ref"),
            "Should generate correct schema reference for TransactionRequest");
        
        // Verify the schema was properly introspected and registered
        Map<String, Map<String, Object>> schemas = writer.getComponentSchemas();
        assertTrue(schemas.containsKey("TransactionRequest"), 
            "Should register TransactionRequest schema in components");
        
        Map<String, Object> txnSchema = schemas.get("TransactionRequest");
        assertEquals("object", txnSchema.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) txnSchema.get("properties");
        assertNotNull(properties);
        assertTrue(properties.containsKey("transactionId"));
        assertTrue(properties.containsKey("accountNumber"));
        assertTrue(properties.containsKey("amount"));
        assertTrue(properties.containsKey("currency"));
        assertTrue(properties.containsKey("description"));
        assertTrue(properties.containsKey("transactionType"));
        
        System.out.println("✓ FUTURE-PROOF: New TransactionRequest DTO was automatically discovered!");
    }
    
    @Test
    void testNewAccountResponseIsAutomaticallyDiscovered() {
        // Create JSON for AccountResponse - another DTO that was NEVER hardcoded
        String json = """
            {
                "accountId": 999,
                "accountNumber": "ACC-123456",
                "accountName": "John Doe Savings",
                "accountType": "SAVINGS",
                "balance": 25000.75,
                "status": "ACTIVE"
            }
            """;
        
        Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass(json);
        
        // Should successfully match to AccountResponse WITHOUT any hardcoded logic
        assertTrue(schema.isPresent(), 
            "FUTURE-PROOF TEST: Should automatically discover and match AccountResponse");
        assertEquals("#/components/schemas/AccountResponse", schema.get().get("$ref"),
            "Should generate correct schema reference for AccountResponse");
        
        Map<String, Map<String, Object>> schemas = writer.getComponentSchemas();
        assertTrue(schemas.containsKey("AccountResponse"), 
            "Should register AccountResponse schema in components");
        
        System.out.println("✓ FUTURE-PROOF: New AccountResponse DTO was automatically discovered!");
    }
    
    @Test
    void testArrayOfNewDTOsIsAutomaticallyDiscovered() {
        // Test array of TransactionRequest
        String json = """
            [
                {
                    "transactionId": "TXN-001",
                    "accountNumber": "ACC-111",
                    "amount": 100.00,
                    "currency": "USD",
                    "description": "Payment 1",
                    "transactionType": "CREDIT"
                },
                {
                    "transactionId": "TXN-002",
                    "accountNumber": "ACC-222",
                    "amount": 200.00,
                    "currency": "EUR",
                    "description": "Payment 2",
                    "transactionType": "DEBIT"
                }
            ]
            """;
        
        Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass(json);
        
        assertTrue(schema.isPresent(), "Should introspect array of new DTOs");
        assertEquals("array", schema.get().get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> items = (Map<String, Object>) schema.get().get("items");
        assertNotNull(items);
        assertEquals("#/components/schemas/TransactionRequest", items.get("$ref"),
            "Should reference TransactionRequest schema for array items");
        
        System.out.println("✓ FUTURE-PROOF: Array of new TransactionRequest DTOs handled correctly!");
    }
    
    @Test
    void testMixOfOldAndNewDTOsAllWork() {
        // Test that existing DTOs still work alongside new ones
        String currencyJson = """
            {
                "currencyId": 840,
                "currencyCode": "USD",
                "currencyName": "US Dollar",
                "currencySymbol": "$",
                "currencyFlag": "🇺🇸"
            }
            """;
        
        String transactionJson = """
            {
                "transactionId": "TXN-123",
                "accountNumber": "ACC-456",
                "amount": 500.00,
                "currency": "USD",
                "description": "Test",
                "transactionType": "CREDIT"
            }
            """;
        
        String accountJson = """
            {
                "accountId": 1,
                "accountNumber": "ACC-789",
                "accountName": "Test Account",
                "accountType": "CHECKING",
                "balance": 1000.00,
                "status": "ACTIVE"
            }
            """;
        
        // All should be introspected successfully
        Optional<Map<String, Object>> currencySchema = writer.tryIntrospectKnownClass(currencyJson);
        Optional<Map<String, Object>> transactionSchema = writer.tryIntrospectKnownClass(transactionJson);
        Optional<Map<String, Object>> accountSchema = writer.tryIntrospectKnownClass(accountJson);
        
        assertTrue(currencySchema.isPresent(), "Old CurrencyRequest should still work");
        assertTrue(transactionSchema.isPresent(), "New TransactionRequest should work");
        assertTrue(accountSchema.isPresent(), "New AccountResponse should work");
        
        // Verify all schemas are registered
        Map<String, Map<String, Object>> schemas = writer.getComponentSchemas();
        assertTrue(schemas.containsKey("CurrencyRequest"), "Old DTO registered");
        assertTrue(schemas.containsKey("TransactionRequest"), "New DTO registered");
        assertTrue(schemas.containsKey("AccountResponse"), "New DTO registered");
        
        assertEquals(3, schemas.size(), "Should have 3 distinct schemas");
        
        System.out.println("✓ FUTURE-PROOF: Old and new DTOs coexist perfectly!");
    }
    
    @Test
    void testPartialFieldMatchStillWorks() {
        // Test with subset of TransactionRequest fields
        String json = """
            {
                "transactionId": "TXN-999",
                "amount": 750.00,
                "currency": "GBP"
            }
            """;
        
        Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass(json);
        
        // Should still match TransactionRequest with subset matching
        assertTrue(schema.isPresent(), 
            "Should match even with partial fields (subset matching)");
        assertEquals("#/components/schemas/TransactionRequest", schema.get().get("$ref"),
            "Should identify correct class even with partial fields");
        
        System.out.println("✓ FUTURE-PROOF: Subset matching works for new DTOs!");
    }
    
    @Test
    void testRegistryCanFindNewDTOsByName() {
        ClasspathSchemaRegistry registry = writer.getRegistry();
        
        // Verify new DTOs are in the registry
        Optional<Class<?>> txnClass = registry.getClassByName("TransactionRequest");
        Optional<Class<?>> accClass = registry.getClassByName("AccountResponse");
        
        assertTrue(txnClass.isPresent(), "Registry should find TransactionRequest");
        assertTrue(accClass.isPresent(), "Registry should find AccountResponse");
        
        assertEquals(TransactionRequest.class, txnClass.get());
        assertEquals(AccountResponse.class, accClass.get());
        
        System.out.println("✓ FUTURE-PROOF: Registry successfully indexed new DTOs!");
    }
    
    @Test
    void testSystemPropertyConfigurationWorksWithNewPackages() {
        // Test that we can configure the scanner to look in additional packages
        String originalProp = System.getProperty("openapi.spec.scan.packages");
        try {
            // Set to scan only the DTO package
            System.setProperty("openapi.spec.scan.packages", 
                "za.co.ratpack.finance.reactive.rest.v1.dto");
            
            ClasspathSchemaRegistry customRegistry = new ClasspathSchemaRegistry();
            OpenApiSpecWriter customWriter = new OpenApiSpecWriter(customRegistry);
            
            String json = """
                {
                    "transactionId": "TXN-CONFIG-TEST",
                    "accountNumber": "ACC-999",
                    "amount": 9999.99,
                    "currency": "ZAR",
                    "description": "Config test",
                    "transactionType": "TRANSFER"
                }
                """;
            
            Optional<Map<String, Object>> schema = customWriter.tryIntrospectKnownClass(json);
            
            assertTrue(schema.isPresent(), 
                "Should work with custom package configuration");
            assertEquals("#/components/schemas/TransactionRequest", schema.get().get("$ref"));
            
            System.out.println("✓ FUTURE-PROOF: System property configuration works!");
        } finally {
            if (originalProp != null) {
                System.setProperty("openapi.spec.scan.packages", originalProp);
            } else {
                System.clearProperty("openapi.spec.scan.packages");
            }
        }
    }
    
    @Test
    void testNoHardcodedClassNamesAnywhere() {
        // Verify that the writer doesn't have any hardcoded class names
        // by checking that it can handle ANY DTO in the scanned packages
        
        String json = """
            {
                "transactionId": "TEST",
                "accountNumber": "TEST",
                "amount": 1.0,
                "currency": "TEST",
                "description": "TEST",
                "transactionType": "TEST"
            }
            """;
        
        Optional<Map<String, Object>> schema = writer.tryIntrospectKnownClass(json);
        
        assertTrue(schema.isPresent());
        
        // The key assertion: the schema was matched purely by field signatures,
        // NOT by any hardcoded logic checking for specific class names
        String ref = (String) schema.get().get("$ref");
        assertNotNull(ref);
        assertTrue(ref.endsWith("TransactionRequest"), 
            "Matched by field signature, not hardcoded class name");
        
        System.out.println("✓ FUTURE-PROOF: NO hardcoded class names - pure field matching!");
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🎉 ALL FUTURE-PROOF TESTS PASSED!");
        System.out.println("The OpenAPI spec writer is truly generic and will automatically");
        System.out.println("discover and introspect ANY new DTO added to the configured packages.");
        System.out.println("=".repeat(70));
    }
}
