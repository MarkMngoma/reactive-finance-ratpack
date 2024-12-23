package za.co.ratpack.finance.reactive.functions.handlers.currency;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.Status;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.QueryCurrencyDao;
import za.co.ratpack.finance.reactive.functions.handlers.ThrowableHandler;
import za.co.ratpack.finance.reactive.functions.helpers.HttpContentHelper;

/**
 * @author markmngoma
 * @created at 01:53 on 23/12/2024
 */
@Slf4j
@Singleton
public class QueryBatchCurrencyResourceHandler implements Handler {

  private final HttpContentHelper httpContentHelper;
  private final ThrowableHandler throwableHandler;
  private final QueryCurrencyDao queryCurrencyDao;

  @Inject
  public QueryBatchCurrencyResourceHandler(HttpContentHelper httpContentHelper, ThrowableHandler throwableHandler, QueryCurrencyDao queryCurrencyDao) {
    this.httpContentHelper = httpContentHelper;
    this.throwableHandler = throwableHandler;
    this.queryCurrencyDao = queryCurrencyDao;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    log.info("QueryBatchCurrenciesHandler@handle initiated with :: [{}]", ctx.getRequest().getPath());
    Blocking.get(this.queryCurrencyDao::select)
      .next(currencyEntityModels -> log.debug("QueryBatchCurrenciesHandler@handle responding with entry size :: #{}", currencyEntityModels.size()))
      .onError(throwable -> this.throwableHandler.handle(ctx, throwable, Status.NOT_FOUND, "No such entries"))
      .then(currencyEntityModels -> this.httpContentHelper.renderJson(ctx, currencyEntityModels, Status.OK));
  }
}
