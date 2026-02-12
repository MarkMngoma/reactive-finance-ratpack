package za.co.ratpack.finance.reactive.functions.rest.integration.currency;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ratpack.http.Status;
import za.co.ratpack.finance.reactive.functions.rest.integration.RatpackServerBaseIT;
import za.co.ratpack.finance.reactive.functions.rest.integration.RatpackTestServerExtension;
import za.co.ratpack.finance.reactive.functions.rest.integration.openapi.DocumentApi;
import za.co.ratpack.finance.reactive.functions.rest.integration.openapi.OpenApiSpecExtension;
import za.co.ratpack.finance.reactive.functions.rest.integration.openapi.OpenApiTestHttpClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for WriteCurrencyResource endpoint.
 * 
 * @author Generated
 * @created at 14:13 on 11/02/2026
 */
@ExtendWith({RatpackTestServerExtension.class, OpenApiSpecExtension.class})
public class WriteCurrencyResourceIT extends RatpackServerBaseIT {
  
  @BeforeAll
  static void beforeAll() throws Exception {
    ratpackServer.start();
  }
  
  @AfterAll
  static void afterAll() throws Exception {
    ratpackServer.stop();
  }
  
  @Test
  @DocumentApi(
    description = "Create a single currency resource with JSON payload",
    path = "/v1/WriteCurrencyResource",
    summary = "Create single currency",
    tags = {"Write", "Currency"}
  )
  void givenValidCurrencyWhenCreatingThenVerifyCreatedResponse() {
    OpenApiTestHttpClient apiClient = new OpenApiTestHttpClient(testHttpClient);
    apiClient.setTestContext("WriteCurrencyResourceIT", "givenValidCurrencyWhenCreatingThenVerifyCreatedResponse");
    
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
      .withBody(currencyRequest)
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
    
    // Verify response
    assertNotNull(receivedResponse);
    assertNotNull(receivedResponse.getBody().getText());
    assertEquals(Status.CREATED, receivedResponse.getStatus());
  }
}
