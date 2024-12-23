package za.co.ratpack.finance.reactive.functions.rest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ratpack.server.RatpackServer;
import ratpack.test.embed.EmbeddedApp;
import ratpack.test.http.TestHttpClient;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import za.co.ratpack.finance.reactive.ServerCommand;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Mark Mngoma
 * @created at 21:58 on 23/08/2023
 */
@ExtendWith(SystemStubsExtension.class)
public class RatpackServerBaseIT {

  protected static final Logger LOG = LoggerFactory.getLogger(RatpackServerBaseIT.class);

  protected static Map<String, String> imposedProperties;
  protected static RatpackServer ratpackServer;
  protected static TestHttpClient testHttpClient;
  protected static EmbeddedApp embeddedApp;
  protected static ObjectMapper objectMapper;

  @SystemStub
  protected static EnvironmentVariables env;

  @BeforeAll
  static void setup() throws Exception {
    env.set("RATPACK_ENVIRONMENT", "localhost");
    initialisePropertyImpositions();
    initialiseApplicationUnderTest();
  }

  @AfterAll
  static void afterAll() {
    embeddedApp.close();
  }

  private static void initialisePropertyImpositions() {
    imposedProperties = new HashMap<>(34);
  }

  protected static void initialiseApplicationUnderTest() throws Exception {
    MDC.put("requestId", UUID.randomUUID().toString());
    StopWatch stopwatch = new StopWatch();

    if (LOG.isInfoEnabled()) {
      stopwatch.start();
    }

    embeddedApp = EmbeddedApp.fromServer(RatpackServer.start(server -> server
      .serverConfig(ServerCommand::initServerConfigurations)
      .registry(ServerCommand.initDependencies())
      .handlers(ServerCommand::initActionChain))
    );

    ratpackServer = embeddedApp.getServer();
    objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    testHttpClient = embeddedApp.getHttpClient();
  }
}
