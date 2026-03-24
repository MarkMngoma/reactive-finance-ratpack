package za.co.ratpack.finance.reactive.guice.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jakarta.validation.Validator;
import org.modelmapper.ModelMapper;
import ratpack.error.ClientErrorHandler;
import ratpack.error.ServerErrorHandler;
import ratpack.logging.MDCInterceptor;
import za.co.ratpack.finance.reactive.domain.flyway.FlywayMigratorService;
import za.co.ratpack.finance.reactive.functions.handlers.GlobalErrorHandler;
import za.co.ratpack.finance.reactive.guice.providers.MDCInterceptorProvider;
import za.co.ratpack.finance.reactive.guice.providers.ModelMapperProvider;
import za.co.ratpack.finance.reactive.guice.providers.ObjectMapperProvider;
import za.co.ratpack.finance.reactive.guice.providers.ValidatorProvider;
import za.co.ratpack.finance.reactive.rest.v1.action.FinanceActionChain;

import java.time.ZoneOffset;
import java.util.TimeZone;

/**
 * Guice module for application-level infrastructure bindings.
 *
 * <p>All bindings use explicit {@link com.google.inject.Provider} classes rather than
 * {@code @Provides}-annotated methods to avoid Guice's CGLIB-based {@code FastClass}
 * proxy, which fails to initialise in GraalVM native image due to runtime bytecode
 * generation ({@code $MethodWrapper}).
 *
 * @author markmngoma
 * @created at 19:16 on 22/12/2024
 */
public class ServerModule extends AbstractModule {

  @Override
  protected void configure() {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));

    bind(ClientErrorHandler.class).to(GlobalErrorHandler.class);
    bind(ServerErrorHandler.class).to(GlobalErrorHandler.class);

    bind(FlywayMigratorService.class);
    bind(FinanceActionChain.class);

    bind(MDCInterceptor.class).toProvider(MDCInterceptorProvider.class).in(Scopes.SINGLETON);
    bind(Validator.class).toProvider(ValidatorProvider.class).in(Scopes.SINGLETON);
    bind(ModelMapper.class).toProvider(ModelMapperProvider.class).in(Scopes.SINGLETON);
    bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).in(Scopes.SINGLETON);
  }
}
