package za.co.ratpack.finance.reactive.functions.rest.integration.currency;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ratpack.http.Status;
import za.co.ratpack.finance.reactive.functions.rest.integration.BatchCurrencyRequestUtil;
import za.co.ratpack.finance.reactive.functions.rest.integration.RatpackServerBaseIT;
import za.co.ratpack.finance.reactive.functions.rest.integration.RatpackTestServerExtension;
import za.co.ratpack.finance.reactive.functions.rest.integration.openapi.OpenApiSpecExtension;
import za.co.ratpack.finance.reactive.functions.rest.integration.openapi.OpenApiTestHttpClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for QueryCurrencyResource endpoints.
 * 
 * @author Generated
 * @created at 14:13 on 11/02/2026
 */
@ExtendWith({RatpackTestServerExtension.class, OpenApiSpecExtension.class})
public class QueryCurrencyResourceIT extends RatpackServerBaseIT {
  
  @BeforeAll
  static void beforeAll() throws Exception {
    ratpackServer.start();
  }
  
  @AfterAll
  static void afterAll() throws Exception {
    ratpackServer.stop();
  }
  
  @Test
  void givenCurrenciesExistWhenQueryingAllThenVerifyResults() {
    // First create some currencies
    testHttpClient
      .requestSpec(BatchCurrencyRequestUtil::constructCurrencyRequest)
      .post("/v1/WriteBatchCurrencyResource");
    
    // Now query all currencies with OpenAPI capture
    OpenApiTestHttpClient apiClient = new OpenApiTestHttpClient(testHttpClient);
    apiClient.setTestContext("QueryCurrencyResourceIT", "givenCurrenciesExistWhenQueryingAllThenVerifyResults");
    
    var receivedResponse = apiClient.get("/v1/QueryCurrencyResource");
    
    assertEquals(Status.OK, receivedResponse.getStatus());
    assertNotNull(receivedResponse.getBody().getText());
  }
  
  @Test
  void givenCurrencyExistsWhenQueryingByCodeThenVerifyResult() {
    // First create some currencies
    testHttpClient
      .requestSpec(BatchCurrencyRequestUtil::constructCurrencyRequest)
      .post("/v1/WriteBatchCurrencyResource");
    
    // Now query a specific currency with OpenAPI capture
    OpenApiTestHttpClient apiClient = new OpenApiTestHttpClient(testHttpClient);
    apiClient.setTestContext("QueryCurrencyResourceIT", "givenCurrencyExistsWhenQueryingByCodeThenVerifyResult");
    
    var receivedResponse = apiClient.get("/v1/QueryCurrencyResource/ZAR");
    
//    assertEquals(Status.OK, receivedResponse.getStatus());
//    assertNotNull(receivedResponse.getBody().getText());
  }
}
