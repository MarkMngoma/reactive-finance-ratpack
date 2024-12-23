package za.co.ratpack.finance.reactive.functions.handlers.currency;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.Status;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.CommandCurrencyDao;
import za.co.ratpack.finance.reactive.domain.mybatis.model.CurrencyEntityModel;
import za.co.ratpack.finance.reactive.domain.mybatis.model.mapper.BatchObjectMapper;
import za.co.ratpack.finance.reactive.functions.handlers.ThrowableHandler;
import za.co.ratpack.finance.reactive.functions.helpers.HttpContentHelper;
import za.co.ratpack.finance.reactive.rest.v1.dto.CurrencyRequest;

/**
 * @author markmngoma
 * @created at 01:45 on 23/12/2024
 */
@Slf4j
@Singleton
public class WriteCurrencyResourceHandler implements Handler {

  private final HttpContentHelper httpContentHelper;
  private final ThrowableHandler throwableHandler;
  private final CommandCurrencyDao commandCurrencyDao;
  private final BatchObjectMapper<CurrencyEntityModel, CurrencyRequest> batchObjectMapper;

  @Inject
  public WriteCurrencyResourceHandler(HttpContentHelper httpContentHelper, ThrowableHandler throwableHandler, CommandCurrencyDao commandCurrencyDao, BatchObjectMapper<CurrencyEntityModel, CurrencyRequest> batchObjectMapper) {
    this.httpContentHelper = httpContentHelper;
    this.throwableHandler = throwableHandler;
    this.commandCurrencyDao = commandCurrencyDao;
    this.batchObjectMapper = batchObjectMapper;
  }

  @Override
  public void handle(Context ctx) {
    log.info("WriteCurrencyResourceHandler@handle initiated with :: [{}]", ctx.getRequest().getPath());
    this.httpContentHelper.parseJson(ctx, CurrencyRequest.class)
      .apply(currencyRequestPromise -> this.httpContentHelper.validate(ctx, currencyRequestPromise))
      .next(currencyRequest -> log.info("WriteCurrencyResourceHandler@handle received payload :: {}", currencyRequest))
      .map(currencyRequest -> this.batchObjectMapper.map(currencyRequest, CurrencyEntityModel.class))
      .next(currencyEntityModel -> log.info("WriteCurrencyResourceHandler@handle preparing currency entity model :: {}", currencyEntityModel))
      .blockingMap(this.commandCurrencyDao::insert)
      .next(entityResultId -> log.info("WriteCurrencyResourceHandler@handle preparing entity result id :: #{}", entityResultId))
      .onError(throwable -> this.throwableHandler.handle(ctx, throwable, Status.UNPROCESSABLE_ENTITY, "Currency creation failed"))
      .next(insertResultId -> ctx.getResponse().status(Status.CREATED))
      .then(insertResultId -> ctx.getResponse().send("{\"success\": true}"));
  }
}
