package za.co.ratpack.finance.reactive.functions.rest.integration.currency;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ratpack.http.Status;
import za.co.ratpack.finance.reactive.functions.rest.integration.BatchCurrencyRequestUtil;
import za.co.ratpack.finance.reactive.functions.rest.integration.RatpackServerBaseIT;
import za.co.ratpack.finance.reactive.functions.rest.integration.RatpackTestServerExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author markmngoma
 * @created at 03:23 on 23/12/2024
 */
@ExtendWith(RatpackTestServerExtension.class)
public class WriteBatchCurrencyResourceIT extends RatpackServerBaseIT {

  @BeforeAll
  static void beforeAll() throws Exception {
    ratpackServer.start();
  }

  @AfterAll
  static void afterAll() throws Exception {
    ratpackServer.stop();
  }

  @Test
  void givenRequestContainsSupportedCurrenciesWhenCreatingThenVerifyResults() {
    var receivedResponse = testHttpClient
      .requestSpec(BatchCurrencyRequestUtil::constructCurrencyRequest)
      .post("/v1/WriteBatchCurrencyResource");

    var receivedGetResponse = testHttpClient.get("/v1/QueryCurrencyResource");

    assertEquals(Status.OK, receivedGetResponse.getStatus());
    assertEquals(receivedResponse.getBody().getText(), receivedGetResponse.getBody().getText());
    assertEquals(Status.OK, receivedResponse.getStatus());
    assertNotNull(receivedResponse.getBody().getText());
    assertEquals(receivedResponse.getBody().getText(), receivedResponse.getBody().getText());
  }
}
