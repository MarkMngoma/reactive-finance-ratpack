package za.co.ratpack.finance.reactive.guice.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

/**
 * Explicit Guice {@link Provider} for a {@link ModelMapper}.
 *
 * <p>Replaces the {@code @Provides}-annotated method in {@code ServerModule} to avoid
 * Guice's CGLIB-based {@code FastClass} proxy, which fails in GraalVM native image.
 *
 * @author markmngoma
 */
@Singleton
public class ModelMapperProvider implements Provider<ModelMapper> {

  @Inject
  public ModelMapperProvider() {
  }

  @Override
  public ModelMapper get() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    return modelMapper;
  }
}
