package za.co.ratpack.finance.reactive.rest.v1.action;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import ratpack.func.Action;
import ratpack.handling.Chain;
import za.co.ratpack.finance.reactive.functions.handlers.currency.QueryBatchCurrencyResourceHandler;
import za.co.ratpack.finance.reactive.functions.handlers.currency.QueryCurrencyResourceHandler;
import za.co.ratpack.finance.reactive.functions.handlers.currency.QueryExchangeRateResourceHandler;
import za.co.ratpack.finance.reactive.functions.handlers.currency.WriteBatchCurrencyResourceHandler;
import za.co.ratpack.finance.reactive.functions.handlers.currency.WriteCurrencyResourceHandler;
import za.co.ratpack.finance.reactive.functions.handlers.currency.WriteModificationCurrencyResourceHandler;

/**
 * @author markmngoma
 * @created at 19:00 on 22/12/2024
 */
@Slf4j
@Singleton
public class FinanceActionChain implements Action<Chain> {

  @Override
  public void execute(Chain chain) {
    chain
      .get("QueryCurrencyResource", QueryBatchCurrencyResourceHandler.class)
      .get("QueryCurrencyResource/:currencyCode", QueryCurrencyResourceHandler.class)
      .get("QueryCurrencyResource/Exchanges/:currencyCode", QueryExchangeRateResourceHandler.class)
      .post("WriteCurrencyResource", WriteCurrencyResourceHandler.class)
      .post("WriteBatchCurrencyResource", WriteBatchCurrencyResourceHandler.class)
      .put("WriteModificationCurrencyResource", WriteModificationCurrencyResourceHandler.class);
  }
}
