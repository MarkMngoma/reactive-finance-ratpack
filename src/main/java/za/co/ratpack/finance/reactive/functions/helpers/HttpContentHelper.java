package za.co.ratpack.finance.reactive.functions.helpers;

import com.google.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.http.Status;
import ratpack.jackson.Jackson;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author markmngoma
 * @created at 22:00 on 22/12/2024
 */
@Slf4j
@Singleton
public class HttpContentHelper {

  public <T> Promise<T> parseJson(Class<T> clazz, Context ctx) {
    return ctx.parse(Jackson.fromJson(clazz));
  }

  public <T> void renderJson(Context ctx, T response, Status status) {
    ctx.getResponse().status(status);
    ctx.render(Jackson.json(response));
  }

  public Optional<String> parsePathVariable(Context ctx, final String pathVariable) {
    return Optional.ofNullable(pathVariable)
      .filter(pathToken -> ctx.getPathBinding().getTokens().containsKey(pathVariable))
      .map(pathToken -> ctx.getPathBinding().getTokens().get(pathVariable));
  }

  public Optional<String> parseQueryParameter(Context ctx, final String aQueryParameter) {
    return Optional.ofNullable(aQueryParameter)
      .filter(pathQuery -> !ctx.getRequest().getQueryParams().isEmpty())
      .map(pathQuery -> ctx.getRequest().getQueryParams().get(aQueryParameter));
  }

  public <T> Promise<T> validate(Context ctx, Promise<T> promise) {
    return promise.route(dto -> (ctx.get(Validator.class).validate(dto).size() > BigInteger.ZERO.intValue()),
      dto -> {
        Set<ConstraintViolation<T>> errors = ctx.get(Validator.class).validate(dto);
        Map<String, Object> validationResult = new HashMap<>();
        for (ConstraintViolation<T> e : errors) {
          validationResult.put(e.getPropertyPath().toString(), e.getMessage());
        }
        if (!validationResult.isEmpty()) {
          this.renderJson(ctx, validationResult, Status.BAD_REQUEST);
        }
      }
    );
  }
}
