package za.co.ratpack.finance.reactive.functions.handlers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import ratpack.error.internal.ErrorHandler;
import ratpack.handling.Context;
import ratpack.http.MediaType;
import ratpack.http.Status;

/**
 * @author markmngoma
 * @created at 03:40 on 23/12/2024
 */
@Slf4j
@Singleton
public class GlobalErrorHandler implements ErrorHandler {

  private final ObjectMapper mapper;

  public GlobalErrorHandler() {
    this.mapper = new ObjectMapper();
    this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @Override
  public void error(Context context, int statusCode) throws Exception {
    context.getResponse().status(statusCode).send();
  }

  @Override
  public void error(Context context, Throwable throwable) throws Exception {
   log.error("GlobalErrorHandler@error with :: [{}]", throwable.getMessage());
    var throwableResponse = new ImmutableMap.Builder<String, Object>()
      .put("error", "Service failure on request");
    if (throwable instanceof IllegalArgumentException illegalArgumentException) {
      throwableResponse.put("message", illegalArgumentException.getLocalizedMessage());
      context.getResponse().status(Status.BAD_REQUEST);
    } else {
      throwableResponse.put("message", "Request could not be satisfied");
      context.getResponse().status(Status.NOT_ACCEPTABLE);
    }
    context.getResponse().contentType(MediaType.APPLICATION_JSON);
    context.getResponse().send(mapper.writeValueAsString(throwableResponse));
  }
}
