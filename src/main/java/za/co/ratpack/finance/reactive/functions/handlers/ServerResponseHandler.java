package za.co.ratpack.finance.reactive.functions.handlers;

import com.google.inject.Inject;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.MutableHeaders;
import za.co.ratpack.finance.reactive.config.CustomServerConfig;

import java.nio.charset.StandardCharsets;

/**
 * @author markmngoma
 * @created at 19:18 on 22/12/2024
 */
public class ServerResponseHandler implements Handler {

  private final CustomServerConfig customServerConfig;

  @Inject
  public ServerResponseHandler(CustomServerConfig customServerConfig) {
    this.customServerConfig = customServerConfig;
  }

  @Override
  public void handle(Context context) {
    MutableHeaders headers = context.getResponse().getHeaders();
    headers.set("Access-Control-Allow-Origin", this.customServerConfig.getAllowedOrigins());
    headers.add("Access-Control-Allow-Methods", this.customServerConfig.getAllowedMethods());
    headers.set("Access-Control-Allow-Headers", this.customServerConfig.getAllowedHeaders());
    headers.set("Content-Type", this.customServerConfig.getContentType());
    headers.set("Accept-Charset", StandardCharsets.UTF_8);
    if (context.getRequest().getMethod().isOptions()) {
      context.render("");
    } else {
      context.next();
    }
  }
}
