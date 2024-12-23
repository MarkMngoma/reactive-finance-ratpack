package za.co.ratpack.finance.reactive.functions.handlers.currency;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.Status;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.CommandCurrencyDao;
import za.co.ratpack.finance.reactive.domain.mybatis.functions.BatchCommandDomainExecutorFunction;
import za.co.ratpack.finance.reactive.domain.mybatis.model.CurrencyEntityModel;
import za.co.ratpack.finance.reactive.domain.mybatis.model.mapper.BatchObjectMapper;
import za.co.ratpack.finance.reactive.functions.handlers.ThrowableHandler;
import za.co.ratpack.finance.reactive.functions.helpers.HttpContentHelper;
import za.co.ratpack.finance.reactive.rest.v1.dto.BatchCurrencyRequest;
import za.co.ratpack.finance.reactive.rest.v1.dto.CurrencyRequest;

/**
 * @author markmngoma
 * @created at 01:00 on 23/12/2024
 */
@Slf4j
@Singleton
public class WriteBatchCurrencyResourceHandler implements Handler {

  private final HttpContentHelper httpContentHelper;
  private final BatchCommandDomainExecutorFunction batchCommandDomainExecutorFunction;
  private final ThrowableHandler throwableHandler;
  private final CommandCurrencyDao commandCurrencyDao;
  private final BatchObjectMapper<CurrencyEntityModel, CurrencyRequest> batchObjectMapper;

  @Inject
  public WriteBatchCurrencyResourceHandler(HttpContentHelper httpContentHelper, BatchCommandDomainExecutorFunction batchCommandDomainExecutorFunction, ThrowableHandler throwableHandler, CommandCurrencyDao commandCurrencyDao, BatchObjectMapper<CurrencyEntityModel, CurrencyRequest> batchObjectMapper) {
    this.httpContentHelper = httpContentHelper;
    this.batchCommandDomainExecutorFunction = batchCommandDomainExecutorFunction;
    this.throwableHandler = throwableHandler;
    this.commandCurrencyDao = commandCurrencyDao;
    this.batchObjectMapper = batchObjectMapper;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    log.info("WriteBatchCurrencyResourceHandler@handle initiated with :: [{}]", ctx.getRequest().getPath());
    this.httpContentHelper.parseJson(ctx, BatchCurrencyRequest.class)
      .apply(batchCurrencyRequestPromise -> this.httpContentHelper.validate(ctx, batchCurrencyRequestPromise))
      .map(batchCurrencyRequest -> batchObjectMapper.apply(batchCurrencyRequest.getBatchCurrencies(), CurrencyEntityModel.class))
      .blockingOp(currencyEntityModels -> this.batchCommandDomainExecutorFunction.batchInsertCommand(commandCurrencyDao::insert, commandCurrencyDao, currencyEntityModels))
      .onError(throwable -> this.throwableHandler.handle(ctx, throwable, Status.UNPROCESSABLE_ENTITY, "Failed to create currencies"))
      .then(batchResult -> ctx.insert(ctx.get(QueryBatchCurrencyResourceHandler.class)));
  }
}
