package za.co.ratpack.finance.reactive.guice.modules;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.datasource.builtin.PooledDataSourceProvider;
import org.mybatis.guice.datasource.helper.JdbcHelper;
import org.mybatis.guice.datasource.hikaricp.HikariCPProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.ratpack.finance.reactive.ConfigurationResolver;
import za.co.ratpack.finance.reactive.config.JdbcConfig;

import java.util.Properties;

import static za.co.ratpack.finance.reactive.HttpServer.SERVER_ENVIRONMENT;

/**
 * @author markmngoma
 * @created at 21:12 on 22/12/2024
 */
public class HikariMyBatisModule extends MyBatisModule {

  public static final String JDBC_DRIVER_CLASS = "org.mariadb.jdbc.Driver";

  private static final Logger LOGGER = LoggerFactory.getLogger(HikariMyBatisModule.class);

  @Override
  protected void initialize() {
    initialiseJdbcDriver();
    bindDataSourceProviderType(HikariCPProvider.class);
    bindTransactionFactoryType(JdbcTransactionFactory.class);
    JdbcConfig jdbcConfig = ConfigurationResolver.loadConfiguration("/jdbc", JdbcConfig.class);
    Names.bindProperties(binder(), properties(jdbcConfig));
    addMapperClasses(jdbcConfig.getMapperPackage());
    mapUnderscoreToCamelCase(true);
  }

  @Provides
  @Singleton
  public static Properties properties(JdbcConfig config) {
    Properties myBatisProperties = new Properties();
    String env = System.getenv(SERVER_ENVIRONMENT);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("MyBatisProperties loading database values using [{}]", env.toUpperCase());
    }

    myBatisProperties.setProperty("mybatis.environment.id", env);
    myBatisProperties.setProperty("JDBC.driver", config.getDriverClassName());
    myBatisProperties.setProperty("JDBC.url", config.getUrl());
    myBatisProperties.setProperty("JDBC.username", config.getUsername());
    myBatisProperties.setProperty("JDBC.password", config.getPassword());
    myBatisProperties.setProperty("JDBC.loginTimeout", String.valueOf(config.getLoginTimeout()));
    myBatisProperties.setProperty("hikaricp.autoCommit", String.valueOf(config.isAutoCommit()));
    myBatisProperties.setProperty("hikaricp.allowPoolSuspension", String.valueOf(config.isAllowPoolSuspension()));
    myBatisProperties.setProperty("hikaricp.connectionTimeoutMs", String.valueOf(config.getConnectionTimeoutMs()));
    myBatisProperties.setProperty("hikaricp.idleTimeoutMs", String.valueOf(config.getIdleTimeoutMs()));
    myBatisProperties.setProperty("hikaricp.maxLifetimeMs", String.valueOf(config.getMaxLifetimeMs()));

    return myBatisProperties;
  }

  public static void initialiseJdbcDriver() {
    try {
      Class.forName(JDBC_DRIVER_CLASS);
    } catch (ClassNotFoundException e) {
      LOGGER.error("HikariMyBatisModule#initialiseJdbcDriver jdbc driver not found in classpath.");
      throw new RuntimeException("Service failed to start, jdbc driver not found in classpath.");
    }
  }
}
