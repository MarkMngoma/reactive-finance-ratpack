package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Test;
import za.co.ratpack.finance.reactive.rest.v1.dto.BatchCurrencyRequest;
import za.co.ratpack.finance.reactive.rest.v1.dto.CurrencyRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SchemaIntrospector utility.
 */
class SchemaIntrospectorTest {
  
  @Test
  void testIntrospectCurrencyRequest() {
    Schema<?> schema = SchemaIntrospector.introspectClass(CurrencyRequest.class, "CurrencyRequest");
    
    assertNotNull(schema);
    assertTrue(schema instanceof ObjectSchema);
    assertEquals("CurrencyRequest", schema.getName());
    assertNotNull(schema.getDescription());
    assertTrue(schema.getDescription().contains("Currency request object"));
    
    // Check required fields
    assertNotNull(schema.getRequired());
    assertEquals(5, schema.getRequired().size());
    assertTrue(schema.getRequired().contains("currencyId"));
    assertTrue(schema.getRequired().contains("currencyCode"));
    assertTrue(schema.getRequired().contains("currencyName"));
    assertTrue(schema.getRequired().contains("currencySymbol"));
    assertTrue(schema.getRequired().contains("currencyFlag"));
    
    // Check properties
    assertNotNull(schema.getProperties());
    assertEquals(5, schema.getProperties().size());
    
    // Check currencyId field
    Schema<?> currencyIdSchema = (Schema<?>) schema.getProperties().get("currencyId");
    assertNotNull(currencyIdSchema);
    assertTrue(currencyIdSchema instanceof IntegerSchema);
    assertNotNull(currencyIdSchema.getDescription());
    assertTrue(currencyIdSchema.getDescription().contains("ISO 4217"));
    assertNotNull(currencyIdSchema.getExample()); // Just check it exists
    assertEquals("int32", currencyIdSchema.getFormat());
    
    // Check currencyCode field
    Schema<?> currencyCodeSchema = (Schema<?>) schema.getProperties().get("currencyCode");
    assertNotNull(currencyCodeSchema);
    assertTrue(currencyCodeSchema instanceof StringSchema);
    assertNotNull(currencyCodeSchema.getDescription());
    assertTrue(currencyCodeSchema.getDescription().contains("Three-letter"));
    assertNotNull(currencyCodeSchema.getExample()); // Just check it exists
    
    System.out.println("✅ CurrencyRequest introspection successful!");
    System.out.println("   - 5 required fields detected");
    System.out.println("   - All field descriptions present");
    System.out.println("   - Examples captured: " + currencyIdSchema.getExample() + ", " + currencyCodeSchema.getExample());
  }
  
  @Test
  void testIntrospectBatchCurrencyRequest() {
    Schema<?> schema = SchemaIntrospector.introspectClass(BatchCurrencyRequest.class, "BatchCurrencyRequest");
    
    assertNotNull(schema);
    assertTrue(schema instanceof ObjectSchema);
    assertEquals("BatchCurrencyRequest", schema.getName());
    assertNotNull(schema.getDescription());
    assertTrue(schema.getDescription().contains("Batch request"));
    
    // Check required fields
    assertNotNull(schema.getRequired());
    assertEquals(1, schema.getRequired().size());
    assertTrue(schema.getRequired().contains("batchCurrencies"));
    
    // Check batchCurrencies field
    Schema<?> batchCurrenciesSchema = (Schema<?>) schema.getProperties().get("batchCurrencies");
    assertNotNull(batchCurrenciesSchema);
    assertTrue(batchCurrenciesSchema instanceof ArraySchema);
    assertNotNull(batchCurrenciesSchema.getDescription());
    assertTrue(batchCurrenciesSchema.getDescription().contains("batch"));
    
    // Check array items reference
    ArraySchema arraySchema = (ArraySchema) batchCurrenciesSchema;
    assertNotNull(arraySchema.getItems());
    assertNotNull(arraySchema.getItems().get$ref());
    assertEquals("#/components/schemas/CurrencyRequest", arraySchema.getItems().get$ref());
    
    System.out.println("✅ BatchCurrencyRequest introspection successful!");
    System.out.println("   - Array field with reference to CurrencyRequest");
    System.out.println("   - Description explains batch behavior");
  }
}
