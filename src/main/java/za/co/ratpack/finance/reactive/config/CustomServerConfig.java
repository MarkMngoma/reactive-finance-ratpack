package za.co.ratpack.finance.reactive.config;

import lombok.Getter;

/**
 * @author markmngoma
 * @created at 19:19 on 22/12/2024
 */
@Getter
public class CustomServerConfig {
  private int defaultPort;
  private String allowedOrigins;
  private String allowedMethods;
  private String allowedHeaders;
  private String allowedCookies;
  private String contentType;
}
