package za.co.ratpack.finance.reactive.guice.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.ratpack.finance.reactive.ConfigurationResolver;
import za.co.ratpack.finance.reactive.config.JdbcConfig;
import za.co.ratpack.finance.reactive.guice.modules.HikariMyBatisModule;

import javax.sql.DataSource;

import static za.co.ratpack.finance.reactive.HttpServer.SERVER_ENVIRONMENT;

/**
 * Explicit Guice {@link Provider} for a HikariCP {@link DataSource}.
 *
 * <p>Using an explicit {@code Provider} class instead of a {@code @Provides}-annotated
 * method avoids Guice's CGLIB-based {@code FastClass} proxy, which fails to initialise
 * in GraalVM native image due to runtime bytecode generation.
 *
 * @author markmngoma
 */
@Singleton
public class DataSourceProvider implements Provider<DataSource> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceProvider.class);

  @Inject
  public DataSourceProvider() {
  }

  @Override
  public DataSource get() {
    HikariMyBatisModule.initialiseJdbcDriver();

    JdbcConfig config = ConfigurationResolver.loadConfiguration("/jdbc", JdbcConfig.class);
    String env = System.getenv(SERVER_ENVIRONMENT);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("DataSourceProvider#get configuring connection pool for [{}]", env.toUpperCase());
    }

    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDriverClassName(config.getDriverClassName());
    hikariConfig.setJdbcUrl(config.getUrl());
    hikariConfig.setUsername(config.getUsername());
    hikariConfig.setPassword(config.getPassword());
    hikariConfig.setAutoCommit(config.isAutoCommit());
    hikariConfig.setConnectionTimeout(config.getConnectionTimeoutMs());
    hikariConfig.setIdleTimeout(config.getIdleTimeoutMs());
    hikariConfig.setMaxLifetime(config.getMaxLifetimeMs());
    hikariConfig.setAllowPoolSuspension(config.isAllowPoolSuspension());
    hikariConfig.setPoolName("HikariPool-" + env);
    return new HikariDataSource(hikariConfig);
  }
}
