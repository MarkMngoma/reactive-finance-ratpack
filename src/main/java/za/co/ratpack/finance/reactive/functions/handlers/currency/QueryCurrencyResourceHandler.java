package za.co.ratpack.finance.reactive.functions.handlers.currency;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.Status;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.QueryCurrencyDao;
import za.co.ratpack.finance.reactive.domain.mybatis.model.typed.query.CurrencyModelTypedQuery;
import za.co.ratpack.finance.reactive.functions.handlers.ThrowableHandler;
import za.co.ratpack.finance.reactive.functions.helpers.HttpContentHelper;

/**
 * @author markmngoma
 * @created at 01:58 on 23/12/2024
 */
@Slf4j
@Singleton
public class QueryCurrencyResourceHandler implements Handler {

  private final HttpContentHelper httpContentHelper;
  private final ThrowableHandler throwableHandler;
  private final QueryCurrencyDao queryCurrencyDao;

  @Inject
  public QueryCurrencyResourceHandler(HttpContentHelper httpContentHelper, ThrowableHandler throwableHandler, QueryCurrencyDao queryCurrencyDao) {
    this.httpContentHelper = httpContentHelper;
    this.throwableHandler = throwableHandler;
    this.queryCurrencyDao = queryCurrencyDao;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    log.info("QueryCurrencyResourceHandler@handle initiated with :: [{}]", ctx.getRequest().getPath());
    Promise.value(ctx)
      .map(context -> this.httpContentHelper.parsePathVariable(ctx, "currencyCode"))
      .next(currencyCode -> log.info("QueryCurrencyResourceHandler@handle received currency code :: {}", currencyCode))
      .map(CurrencyModelTypedQuery::new)
      .blockingMap(this.queryCurrencyDao::selectOne)
      .map(currencyEntityModel -> currencyEntityModel.orElseThrow(() -> new IllegalArgumentException("No record found for currency")))
      .onError(throwable -> this.throwableHandler.handle(ctx, throwable, Status.NOT_FOUND, throwable.getMessage()))
      .then(currencyEntityModel -> this.httpContentHelper.renderJson(ctx, currencyEntityModel, Status.OK));
  }
}
