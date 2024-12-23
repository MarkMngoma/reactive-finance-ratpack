package za.co.ratpack.finance.reactive.guice.modules;

import com.google.inject.AbstractModule;
import za.co.ratpack.finance.reactive.functions.handlers.LoggingMDCHandler;
import za.co.ratpack.finance.reactive.functions.handlers.ServerResponseHandler;
import za.co.ratpack.finance.reactive.functions.handlers.ThrowableHandler;
import za.co.ratpack.finance.reactive.functions.handlers.currency.QueryBatchCurrencyResourceHandler;
import za.co.ratpack.finance.reactive.functions.handlers.currency.QueryCurrencyResourceHandler;
import za.co.ratpack.finance.reactive.functions.handlers.currency.QueryExchangeRateResourceHandler;
import za.co.ratpack.finance.reactive.functions.handlers.currency.WriteBatchCurrencyResourceHandler;
import za.co.ratpack.finance.reactive.functions.handlers.currency.WriteCurrencyResourceHandler;
import za.co.ratpack.finance.reactive.functions.handlers.currency.WriteModificationCurrencyResourceHandler;

/**
 * @author markmngoma
 * @created at 21:45 on 22/12/2024
 */
public class FunctionHandlerModule extends AbstractModule {

  @Override
  protected void configure() {
    // Handlers
    bind(ServerResponseHandler.class);
    bind(LoggingMDCHandler.class);
    bind(ThrowableHandler.class);

    bind(QueryBatchCurrencyResourceHandler.class);
    bind(QueryCurrencyResourceHandler.class);
    bind(QueryExchangeRateResourceHandler.class);
    bind(WriteBatchCurrencyResourceHandler.class);
    bind(WriteCurrencyResourceHandler.class);
    bind(WriteModificationCurrencyResourceHandler.class);
  }
}
