package za.co.ratpack.finance.reactive.functions.rest.integration;

import org.apache.commons.dbutils.DbUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author markmngoma
 * @created_at 11:17 on 09/08/2024
 */
public class RatpackTestServerExtension implements AfterAllCallback, BeforeAllCallback {

  private static final String JDBC_URL = "jdbc:mariadb://localhost:63306/dboFinance";
  private static final String JDBC_USER = "dboFinance";
  private static final String JDBC_PASSWORD = "mdn9VBYldGcmLo01lt5Y3lpQqeE=";

  private Connection connection;

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    DbUtils.closeQuietly(connection);
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    DbUtils.loadDriver("org.mariadb.jdbc.Driver");
    connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    connection.setAutoCommit(true);
    connection.createStatement().execute("DROP SCHEMA IF EXISTS dboFinance");
    connection.createStatement().execute("CREATE SCHEMA IF NOT EXISTS dboFinance");
    connection.setAutoCommit(false);
    connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
  }
}
