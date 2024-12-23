package za.co.ratpack.finance.reactive;

import org.flywaydb.core.internal.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ratpack.exec.Promise;
import ratpack.func.Function;
import ratpack.guice.Guice;
import ratpack.handling.Chain;
import ratpack.handling.RequestLogger;
import ratpack.health.HealthCheck;
import ratpack.registry.Registry;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfigBuilder;
import za.co.ratpack.finance.reactive.config.CustomServerConfig;
import za.co.ratpack.finance.reactive.config.JdbcConfig;
import za.co.ratpack.finance.reactive.functions.handlers.LoggingMDCHandler;
import za.co.ratpack.finance.reactive.functions.handlers.ServerResponseHandler;
import za.co.ratpack.finance.reactive.guice.modules.FunctionHandlerModule;
import za.co.ratpack.finance.reactive.guice.modules.HikariMyBatisModule;
import za.co.ratpack.finance.reactive.guice.modules.ServerModule;
import za.co.ratpack.finance.reactive.rest.v1.action.FinanceActionChain;

import java.util.UUID;

import static za.co.ratpack.finance.reactive.HttpServer.SERVER_ENVIRONMENT;

/**
 * @author markmngoma
 * @created at 19:01 on 22/12/2024
 */
public class ServerCommand {

  private static final Logger LOG = LoggerFactory.getLogger(ServerCommand.class);

  protected void run() throws Exception {
    MDC.put("requestId", UUID.randomUUID().toString());
    StopWatch stopwatch = new StopWatch();

    if (LOG.isInfoEnabled()) {
      stopwatch.start();
    }

    RatpackServer.start(server -> server
      .serverConfig(ServerCommand::initServerConfigurations)
      .registry(initDependencies())
      .handlers(ServerCommand::initActionChain)
    );

    if (LOG.isInfoEnabled()) {
      stopwatch.stop();
      LOG.info("Server start-up time :: [{}] ms", stopwatch.getTotalTimeMillis());
    }
  }

  public static Function<Registry, Registry> initDependencies() {
    return Guice.registry(bindingsSpec -> bindingsSpec
      .module(ServerModule.class)
      .module(HikariMyBatisModule.class)
      .module(FunctionHandlerModule.class)
      .bindInstance(HealthCheck.of("started", registry -> Promise.value(HealthCheck.Result.healthy()))));
  }

  public static void initActionChain(Chain chain) throws Exception {
    chain
      .all(RequestLogger.ncsa())
      .all(LoggingMDCHandler.class)
      .all(ServerResponseHandler.class)
      .prefix("v1", FinanceActionChain.class);
  }

  public static void initServerConfigurations(ServerConfigBuilder builder) {
    CustomServerConfig config = ConfigurationResolver.loadConfiguration("/server", CustomServerConfig.class);

    if (LOG.isInfoEnabled()) {
      LOG.info("Service running on environment: [{}], using port :: [{}]", System.getenv(SERVER_ENVIRONMENT), config.getDefaultPort());
    }

    builder
      .baseDir(BaseDir.find())
      .env()
      .sysProps()
      .yaml(ConfigurationResolver.loadDefaultConfigurationPath())
      .require("/server", CustomServerConfig.class)
      .require("/jdbc", JdbcConfig.class)
      .port(config.getDefaultPort());
  }
}
