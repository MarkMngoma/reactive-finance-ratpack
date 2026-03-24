package za.co.ratpack.finance.reactive.guice.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.CommandCurrencyDao;
import za.co.ratpack.finance.reactive.domain.mybatis.dao.QueryCurrencyDao;

import javax.sql.DataSource;

import static za.co.ratpack.finance.reactive.HttpServer.SERVER_ENVIRONMENT;

/**
 * Explicit Guice {@link Provider} for a MyBatis {@link SqlSessionFactory}.
 *
 * <p>Using an explicit {@code Provider} class instead of a {@code @Provides}-annotated
 * method avoids Guice's CGLIB-based {@code FastClass} proxy, which fails to initialise
 * in GraalVM native image due to runtime bytecode generation.
 *
 * <p>Mapper interfaces are registered explicitly (no VFS classpath scanning) to
 * ensure correct behaviour in GraalVM native image.
 *
 * @author markmngoma
 */
@Singleton
public class SqlSessionFactoryProvider implements Provider<SqlSessionFactory> {

  private final DataSource dataSource;

  @Inject
  public SqlSessionFactoryProvider(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public SqlSessionFactory get() {
    String environmentId = System.getenv(SERVER_ENVIRONMENT);
    Environment environment = new Environment(environmentId, new JdbcTransactionFactory(), dataSource);

    Configuration configuration = new Configuration(environment);
    configuration.setMapUnderscoreToCamelCase(true);
    configuration.setCacheEnabled(true);
    configuration.addMapper(CommandCurrencyDao.class);
    configuration.addMapper(QueryCurrencyDao.class);

    return new SqlSessionFactoryBuilder().build(configuration);
  }
}
