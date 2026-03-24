package za.co.ratpack.finance.reactive.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.ratpack.finance.reactive.ConfigurationResolver;
import za.co.ratpack.finance.reactive.config.JdbcConfig;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.CommandCurrencyDao;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.DefaultCommandCurrencyDao;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.DefaultQueryCurrencyDao;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.QueryCurrencyDao;

import javax.sql.DataSource;

import static za.co.ratpack.finance.reactive.HttpServer.SERVER_ENVIRONMENT;

/**
 * Guice module that wires up HikariCP and MyBatis <em>without</em> mybatis-guice.
 *
 * <p>A plain {@link SqlSessionFactory} is constructed programmatically from a
 * {@link DataSource} backed by HikariCP. The mapper interfaces
 * ({@link CommandCurrencyDao}, {@link QueryCurrencyDao}) are bound to
 * session-per-operation implementations that manage their own
 * {@link org.apache.ibatis.session.SqlSession} lifecycle via
 * {@link za.co.ratpack.finance.reactive.domain.mybatis.SqlSessionTemplate}.
 *
 * @author markmngoma
 * @created at 21:12 on 22/12/2024
 */
public class HikariMyBatisModule extends AbstractModule {

  public static final String JDBC_DRIVER_CLASS = "org.mariadb.jdbc.Driver";

  private static final Logger LOGGER = LoggerFactory.getLogger(HikariMyBatisModule.class);

  @Override
  protected void configure() {
    initialiseJdbcDriver();
    bind(CommandCurrencyDao.class).to(DefaultCommandCurrencyDao.class);
    bind(QueryCurrencyDao.class).to(DefaultQueryCurrencyDao.class);
  }

  /**
   * Provides a singleton HikariCP {@link DataSource}.  All connection-pool
   * properties are read directly from the application YAML configuration so that
   * there is no need for mybatis-guice's {@code Names.bindProperties()} approach.
   */
  @Provides
  @Singleton
  public DataSource dataSource() {
    JdbcConfig config = ConfigurationResolver.loadConfiguration("/jdbc", JdbcConfig.class);
    String env = System.getenv(SERVER_ENVIRONMENT);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("HikariMyBatisModule#dataSource configuring connection pool for [{}]", env.toUpperCase());
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

  /**
   * Provides a singleton {@link SqlSessionFactory} built directly from the
   * {@link DataSource}.  Mapper interfaces are registered explicitly — no VFS
   * classpath scanning — which is required for correct GraalVM native-image
   * behaviour.
   */
  @Provides
  @Singleton
  public SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
    String environmentId = System.getenv(SERVER_ENVIRONMENT);
    Environment environment = new Environment(environmentId, new JdbcTransactionFactory(), dataSource);

    Configuration configuration = new Configuration(environment);
    configuration.setMapUnderscoreToCamelCase(true);
    configuration.setCacheEnabled(true);
    configuration.addMapper(CommandCurrencyDao.class);
    configuration.addMapper(QueryCurrencyDao.class);

    return new SqlSessionFactoryBuilder().build(configuration);
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
