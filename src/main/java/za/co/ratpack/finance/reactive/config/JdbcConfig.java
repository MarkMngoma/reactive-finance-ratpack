package za.co.ratpack.finance.reactive.config;

import lombok.Getter;

/**
 * @author markmngoma
 * @created at 21:10 on 22/12/2024
 */
@Getter
public class JdbcConfig {
  private String url;
  private String driverClassName;
  private String username;
  private String password;
  private final boolean autoCommit = true;
  private int loginTimeout;
  private final boolean allowPoolSuspension = true;
  private int connectionTimeoutMs;
  private int idleTimeoutMs;
  private int maxLifetimeMs;
  private String mapperPackage;
  private final String migrationsTable = "MIGRATION_SCHEMAS_HISTORY";
  private String flywaySchema;
  private boolean validateOnMigrationNaming;
  private boolean validateOnMigrate;
  private boolean createSchemas;
  private boolean cleanDisabled;
  private boolean baselineOnMigrate;
  private String migrationLocation;
}
