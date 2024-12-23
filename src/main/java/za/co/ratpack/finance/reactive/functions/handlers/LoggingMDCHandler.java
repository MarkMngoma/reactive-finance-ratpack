package za.co.ratpack.finance.reactive.functions.handlers;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import java.util.UUID;

/**
 * @author markmngoma
 * @created at 19:22 on 22/12/2024
 */
@Slf4j
public class LoggingMDCHandler implements Handler {
  @Override
  public void handle(Context ctx) {
    MDC.put("requestId", UUID.randomUUID().toString());
    ctx.next();
  }
}
