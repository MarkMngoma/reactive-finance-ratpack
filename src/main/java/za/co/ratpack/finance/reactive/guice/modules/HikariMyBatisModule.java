package za.co.ratpack.finance.reactive.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.CommandCurrencyDao;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.DefaultCommandCurrencyDao;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.DefaultQueryCurrencyDao;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.QueryCurrencyDao;
import za.co.ratpack.finance.reactive.guice.providers.DataSourceProvider;
import za.co.ratpack.finance.reactive.guice.providers.SqlSessionFactoryProvider;

import javax.sql.DataSource;

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
 * <p>Bindings use explicit {@link com.google.inject.Provider} classes rather than
 * {@code @Provides}-annotated methods to avoid Guice's CGLIB-based
 * {@code FastClass} proxy, which fails to initialise in GraalVM native image due
 * to runtime bytecode generation ({@code com.google.inject.internal.cglib.core.$MethodWrapper}).
 *
 * @author markmngoma
 * @created at 21:12 on 22/12/2024
 */
public class HikariMyBatisModule extends AbstractModule {

  public static final String JDBC_DRIVER_CLASS = "org.mariadb.jdbc.Driver";

  private static final Logger LOGGER = LoggerFactory.getLogger(HikariMyBatisModule.class);

  @Override
  protected void configure() {
    bind(DataSource.class).toProvider(DataSourceProvider.class).in(Scopes.SINGLETON);
    bind(SqlSessionFactory.class).toProvider(SqlSessionFactoryProvider.class).in(Scopes.SINGLETON);
    bind(CommandCurrencyDao.class).to(DefaultCommandCurrencyDao.class);
    bind(QueryCurrencyDao.class).to(DefaultQueryCurrencyDao.class);
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
