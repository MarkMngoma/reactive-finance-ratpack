package za.co.ratpack.finance.reactive.functions.handlers.currency;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.Status;
import ratpack.http.client.HttpClient;
import za.co.ratpack.finance.reactive.functions.handlers.ThrowableHandler;
import za.co.ratpack.finance.reactive.functions.helpers.HttpContentHelper;

import java.net.URI;
import java.util.Map;

/**
 * @author markmngoma
 * @created at 02:04 on 23/12/2024
 */
@Slf4j
@Singleton
public class QueryExchangeRateResourceHandler implements Handler {

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final HttpContentHelper httpContentHelper;
  private final ThrowableHandler throwableHandler;

  @Inject
  public QueryExchangeRateResourceHandler(HttpClient httpClient, ObjectMapper objectMapper, HttpContentHelper httpContentHelper, ThrowableHandler throwableHandler) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.httpContentHelper = httpContentHelper;
    this.throwableHandler = throwableHandler;
  }

  @Override
  public void handle(Context ctx) {
    log.info("QueryExchangeRateResourceHandler@handle initiated with :: [{}]", ctx.getRequest().getPath());
    Promise.value(ctx)
      .map(context -> this.httpContentHelper.parsePathVariable(ctx, "currencyCode"))
      .next(currencyCode -> log.info("QueryCurrencyResourceHandler@handle received currency code :: {}", currencyCode))
      .map(currencyCode -> URI.create(String.format("https://latest.currency-api.pages.dev/v1/currencies/%s.json", currencyCode.toLowerCase())))
      .next(endpoint -> log.info("QueryCurrencyResourceHandler@handle [{}]", endpoint))
      .flatMap(httpClient::get)
      .map(receivedResponse -> receivedResponse.getBody().getText())
      .next(responseBody -> log.info("QueryCurrencyResourceHandler@handle response payload {}", responseBody))
      .map(receivedResponse -> objectMapper.readValue(receivedResponse, new TypeReference<Map<String, Object>>() {}))
      .onError(throwable -> this.throwableHandler.handle(ctx, throwable, Status.NOT_FOUND, throwable.getMessage()))
      .then(currencyEntityModel -> this.httpContentHelper.renderJson(ctx, currencyEntityModel, Status.OK));
  }

}
