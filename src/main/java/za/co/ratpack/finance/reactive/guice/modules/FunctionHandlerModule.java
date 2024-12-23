package za.co.ratpack.finance.reactive.guice.modules;

import com.google.inject.AbstractModule;
import za.co.ratpack.finance.reactive.functions.handlers.LoggingMDCHandler;
import za.co.ratpack.finance.reactive.functions.handlers.ServerResponseHandler;
import za.co.ratpack.finance.reactive.functions.handlers.ThrowableHandler;

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
  }
}
