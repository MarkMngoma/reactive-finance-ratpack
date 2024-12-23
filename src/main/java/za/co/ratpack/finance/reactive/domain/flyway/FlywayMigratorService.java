package za.co.ratpack.finance.reactive.domain.flyway;

import com.google.inject.Inject;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.mybatis.guice.datasource.builtin.UnpooledDataSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ratpack.service.Service;
import ratpack.service.StartEvent;
import ratpack.service.StopEvent;
import za.co.ratpack.finance.reactive.ConfigurationResolver;
import za.co.ratpack.finance.reactive.config.JdbcConfig;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author markmngoma
 * @created at 21:31 on 22/12/2024
 */
public class FlywayMigratorService implements Service {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlywayMigratorService.class);

  private final DataSource dataSource;

  @Inject
  public FlywayMigratorService(UnpooledDataSourceProvider unpooledDataSourceProvider) {
    this.dataSource = unpooledDataSourceProvider.get();
  }

  @Override
  public void onStart(StartEvent event) throws Exception {
    initMdcLogging();
    try {
      var jdbcConfig = ConfigurationResolver.loadConfiguration("/jdbc", JdbcConfig.class);
      var fluentConfiguration = new FluentConfiguration();
      fluentConfiguration.dataSource(dataSource);
      fluentConfiguration.schemas(jdbcConfig.getFlywaySchema());
      fluentConfiguration.table(jdbcConfig.getMigrationsTable());
      fluentConfiguration.locations(new Location(jdbcConfig.getMigrationLocation()));
      fluentConfiguration.baselineOnMigrate(jdbcConfig.isBaselineOnMigrate());
      fluentConfiguration.createSchemas(jdbcConfig.isCreateSchemas());
      fluentConfiguration.cleanDisabled(jdbcConfig.isCleanDisabled());

      Flyway flyway = fluentConfiguration.load();

      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Migrating Database with Flyway Version #({})", flyway.info().getInfoResult().flywayVersion);
      }

      flyway.repair();
      flyway.migrate();
    } catch (Exception e) {
      LOGGER.error("migration error {} {}", e, e.getMessage());
    }
  }

  @Override
  public void onStop(StopEvent event) throws Exception {
    initMdcLogging();
    LOGGER.info("Terminating migration service");
    this.closeConnection();
  }

  private void closeConnection() throws Exception {
    try (var connection = dataSource.getConnection()) {
      LOGGER.info("Shutting down datasource connection");
      connection.close();
      LOGGER.info("Connection pool closed :: [{}]", connection.isClosed());
    } catch (SQLException aE) {
      throw new Exception(aE);
    }
  }

  private void initMdcLogging() {
    MDC.put("requestId", UUID.randomUUID().toString());
  }
}
