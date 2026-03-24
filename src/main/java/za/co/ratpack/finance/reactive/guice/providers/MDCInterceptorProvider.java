package za.co.ratpack.finance.reactive.guice.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import ratpack.handling.RequestId;
import ratpack.logging.MDCInterceptor;
import org.slf4j.MDC;

/**
 * Explicit Guice {@link Provider} for {@link MDCInterceptor}.
 *
 * <p>Replaces the {@code @Provides}-annotated method in {@code ServerModule} to avoid
 * Guice's CGLIB-based {@code FastClass} proxy, which fails in GraalVM native image.
 *
 * @author markmngoma
 */
@Singleton
public class MDCInterceptorProvider implements Provider<MDCInterceptor> {

  @Inject
  public MDCInterceptorProvider() {
  }

  @Override
  public MDCInterceptor get() {
    return MDCInterceptor.withInit(execution -> execution.maybeGet(RequestId.class)
      .ifPresent(requestId -> MDC.put("requestId", requestId.toString())));
  }
}
