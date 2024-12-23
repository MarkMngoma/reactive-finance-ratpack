package za.co.ratpack.finance.reactive;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import ratpack.config.ConfigData;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static za.co.ratpack.finance.reactive.HttpServer.SERVER_ENVIRONMENT;

/**
 * @author markmngoma
 * @created at 19:31 on 22/12/2024
 */
public class ConfigurationResolver {

  @SneakyThrows
  public static <T> T loadConfiguration(String pointer, Class<T> clazz) {
    return ConfigData
      .of(configDataBuilder -> configDataBuilder
        .yaml(loadDefaultConfigurationPath())
        .build()
      )
      .get(pointer, clazz);
  }

  public static Path loadDefaultConfigurationPath() {
    var defaultConfig = StringUtils.join("application", "-", System.getenv(SERVER_ENVIRONMENT).toLowerCase(), ".yml");
    return Optional.ofNullable(System.getenv("CONFIGURATION_PATH"))
      .map(configPath -> Paths.get(System.getProperty("user.dir").concat(configPath)).resolve(defaultConfig))
      .orElse(Paths.get(System.getProperty("user.dir").concat("/src/main/resources/")).resolve(defaultConfig));
  }
}
