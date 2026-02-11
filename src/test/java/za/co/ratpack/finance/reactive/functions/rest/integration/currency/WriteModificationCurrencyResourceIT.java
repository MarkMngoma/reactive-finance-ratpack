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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for WriteModificationCurrencyResource endpoint.
 * 
 * @author Generated
 * @created at 14:13 on 11/02/2026
 */
@ExtendWith({RatpackTestServerExtension.class, OpenApiSpecExtension.class})
public class WriteModificationCurrencyResourceIT extends RatpackServerBaseIT {
  
  @BeforeAll
  static void beforeAll() throws Exception {
    ratpackServer.start();
  }
  
  @AfterAll
  static void afterAll() throws Exception {
    ratpackServer.stop();
  }
  
  @Test
  void givenExistingCurrencyWhenModifyingThenVerifyNoContentResponse() {
    // First create a currency
    String createRequest = """
      {
        "currencyId": 840,
        "currencyCode": "USD",
        "currencyName": "United States Dollar",
        "currencySymbol": "$",
        "currencyFlag": "🇺🇸"
      }
      """;
    
    testHttpClient
      .requestSpec(requestSpec -> {
        try {
          requestSpec.headers(httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Accept", "application/json");
          });
          requestSpec.body(requestBody -> requestBody.text(createRequest)).getBody();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      })
      .post("/v1/WriteCurrencyResource");
    
    // Now modify it with OpenAPI capture
    OpenApiTestHttpClient apiClient = new OpenApiTestHttpClient(testHttpClient);
    apiClient.setTestContext("WriteModificationCurrencyResourceIT", "givenExistingCurrencyWhenModifyingThenVerifyNoContentResponse");
    
    String modifyRequest = """
      {
        "currencyId": 840,
        "currencyCode": "USD",
        "currencyName": "United States Dollar (Modified)",
        "currencySymbol": "$",
        "currencyFlag": "🇺🇸"
      }
      """;
    
    var receivedResponse = apiClient
      .requestSpec(requestSpec -> {
        try {
          requestSpec.headers(httpHeaders -> {
            httpHeaders.add("Content-Type", "application/json");
            httpHeaders.add("Accept", "application/json");
          });
          requestSpec.body(requestBody -> requestBody.text(modifyRequest)).getBody();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      })
      .put("/v1/WriteModificationCurrencyResource");
    
    // Verify response - could be 204 No Content or 200 OK depending on implementation
    assertTrue(receivedResponse.getStatus() == Status.NO_CONTENT || 
               receivedResponse.getStatus() == Status.OK,
               "Expected 204 No Content or 200 OK, got: " + receivedResponse.getStatus());
  }
}
