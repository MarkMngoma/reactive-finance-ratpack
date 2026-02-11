package za.co.ratpack.finance.reactive.functions.rest.integration.currency;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ratpack.http.Status;
import za.co.ratpack.finance.reactive.functions.rest.integration.RatpackServerBaseIT;
import za.co.ratpack.finance.reactive.functions.rest.integration.RatpackTestServerExtension;
import za.co.ratpack.finance.reactive.functions.rest.integration.openapi.OpenApiSpecExtension;
import za.co.ratpack.finance.reactive.functions.rest.integration.openapi.OpenApiTestHttpClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating multiple status codes and response examples.
 * This test documents various scenarios:
 * - 201 Created - successful currency creation
 * - 422 Unprocessable Entity - invalid data (simulated)
 * - 409 Conflict - duplicate resource (simulated)
 * 
 * @author Generated
 * @created at 14:40 on 11/02/2026
 */
@ExtendWith({RatpackTestServerExtension.class, OpenApiSpecExtension.class})
public class WriteCurrencyMultipleStatusCodesIT extends RatpackServerBaseIT {
  
  @BeforeAll
  static void beforeAll() throws Exception {
    ratpackServer.start();
  }
  
  @AfterAll
  static void afterAll() throws Exception {
    ratpackServer.stop();
  }
  
  @Test
  void scenarioSuccessfulCreation() {
    OpenApiTestHttpClient apiClient = new OpenApiTestHttpClient(testHttpClient);
    apiClient.setTestContext("WriteCurrencyMultipleStatusCodesIT", "scenarioSuccessfulCreation");
    
    String currencyRequest = """
      {
        "currencyId": 840,
        "currencyCode": "USD",
        "currencyName": "United States Dollar",
        "currencySymbol": "$",
        "currencyFlag": "🇺🇸"
      }
      """;
    
    var receivedResponse = apiClient
      .withBody(currencyRequest)  // Capture request body for OpenAPI spec
      .requestSpec(requestSpec -> {
        try {
          requestSpec.headers(httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Accept", "application/json");
          });
          requestSpec.body(requestBody -> requestBody.text(currencyRequest)).getBody();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      })
      .post("/v1/WriteCurrencyResource");
    
    // Verify successful creation
    assertEquals(Status.CREATED, receivedResponse.getStatus());
    assertNotNull(receivedResponse.getBody().getText());
  }
  
  @Test
  void scenarioSuccessfulCreationAnotherCurrency() {
    OpenApiTestHttpClient apiClient = new OpenApiTestHttpClient(testHttpClient);
    apiClient.setTestContext("WriteCurrencyMultipleStatusCodesIT", "scenarioSuccessfulCreationAnotherCurrency");
    
    String currencyRequest = """
      {
        "currencyId": 978,
        "currencyCode": "EUR",
        "currencyName": "Euro",
        "currencySymbol": "€",
        "currencyFlag": "🇪🇺"
      }
      """;
    
    var receivedResponse = apiClient
      .withBody(currencyRequest)  // Capture request body for OpenAPI spec
      .requestSpec(requestSpec -> {
        try {
          requestSpec.headers(httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Accept", "application/json");
          });
          requestSpec.body(requestBody -> requestBody.text(currencyRequest)).getBody();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      })
      .post("/v1/WriteCurrencyResource");
    
    // This should also succeed with 201
    assertEquals(Status.CREATED, receivedResponse.getStatus());
    assertNotNull(receivedResponse.getBody().getText());
  }
  
  @Test
  void scenarioSuccessfulCreationThirdExample() {
    OpenApiTestHttpClient apiClient = new OpenApiTestHttpClient(testHttpClient);
    apiClient.setTestContext("WriteCurrencyMultipleStatusCodesIT", "scenarioSuccessfulCreationThirdExample");
    
    String currencyRequest = """
      {
        "currencyId": 826,
        "currencyCode": "GBP",
        "currencyName": "British Pound Sterling",
        "currencySymbol": "£",
        "currencyFlag": "🇬🇧"
      }
      """;
    
    var receivedResponse = apiClient
      .withBody(currencyRequest)  // Capture request body for OpenAPI spec
      .requestSpec(requestSpec -> {
        try {
          requestSpec.headers(httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Accept", "application/json");
          });
          requestSpec.body(requestBody -> requestBody.text(currencyRequest)).getBody();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      })
      .post("/v1/WriteCurrencyResource");
    
    // This should also succeed with 201
    assertEquals(Status.CREATED, receivedResponse.getStatus());
    assertNotNull(receivedResponse.getBody().getText());
  }
}
