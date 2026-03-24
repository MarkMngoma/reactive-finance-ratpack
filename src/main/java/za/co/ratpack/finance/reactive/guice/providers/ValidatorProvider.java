package za.co.ratpack.finance.reactive.guice.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

/**
 * Explicit Guice {@link Provider} for a bean {@link Validator}.
 *
 * <p>Replaces the {@code @Provides}-annotated method in {@code ServerModule} to avoid
 * Guice's CGLIB-based {@code FastClass} proxy, which fails in GraalVM native image.
 *
 * @author markmngoma
 */
@Singleton
public class ValidatorProvider implements Provider<Validator> {

  @Inject
  public ValidatorProvider() {
  }

  @Override
  public Validator get() {
    return Validation.byDefaultProvider()
      .configure()
      .messageInterpolator(new ParameterMessageInterpolator())
      .buildValidatorFactory()
      .getValidator();
  }
}
