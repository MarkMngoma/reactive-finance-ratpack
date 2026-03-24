package za.co.ratpack.finance.reactive.guice.providers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Explicit Guice {@link Provider} for a configured {@link ObjectMapper}.
 *
 * <p>Replaces the {@code @Provides}-annotated method in {@code ServerModule} to avoid
 * Guice's CGLIB-based {@code FastClass} proxy, which fails in GraalVM native image.
 *
 * @author markmngoma
 */
@Singleton
public class ObjectMapperProvider implements Provider<ObjectMapper> {

  private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  @Inject
  public ObjectMapperProvider() {
  }

  @Override
  public ObjectMapper get() {
    return new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .registerModule(new SimpleModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))))
      .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
      .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
      .setDateFormat(new SimpleDateFormat(DATE_TIME_FORMAT))
      .setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }
}
