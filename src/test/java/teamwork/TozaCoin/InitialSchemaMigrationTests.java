package teamwork.TozaCoin;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.junit.jupiter.api.Test;
import teamwork.entity.*;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class InitialSchemaMigrationTests {
    @Test
    void freshSchemaSupportsSeedMigrationAndMatchesEveryJpaEntity() throws Exception {
        Properties properties = new Properties();
        try (var stream = getClass().getResourceAsStream("/application-dev.properties")) {
            properties.load(stream);
        }
        try (Connection connection = DriverManager.getConnection(
                properties.getProperty("spring.datasource.url"),
                properties.getProperty("spring.datasource.username"),
                properties.getProperty("spring.datasource.password"))) {
            connection.setAutoCommit(false);
            try {
                String schema = "migration_test_" + UUID.randomUUID().toString().replace("-", "");
                try (Statement statement = connection.createStatement()) {
                    statement.execute("CREATE SCHEMA " + schema);
                    statement.execute("SET LOCAL search_path TO " + schema);
                    executeMigration(statement, "V1__create_initial_schema.sql");
                    executeMigration(statement, "V1.2__restore_initial_admin_accounts.sql");
                    executeMigration(statement, "R__report_severity_under_review.sql");
                    assertEquals(9, number(statement, "SELECT count(*) FROM information_schema.tables WHERE table_schema = '" + schema + "'"));
                    assertEquals(7, number(statement, "SELECT count(*) FROM profile"));
                    assertEquals(2, number(statement, "SELECT count(*) FROM profile_role WHERE roles = 'ROLE_ADMIN'"));
                    assertEquals(5, number(statement, "SELECT count(*) FROM profile_role WHERE roles = 'ROLE_USER'"));
                    assertEquals(7, number(statement, "SELECT count(*) FROM profile WHERE coin_balance = 0 AND blocked = false"));
                    // Running the seed twice must not duplicate accounts or roles.
                    executeMigration(statement, "V1.2__restore_initial_admin_accounts.sql");
                    assertEquals(7, number(statement, "SELECT count(*) FROM profile_role"));
                }
                validateHibernateMappings(connection, schema);
                assertFalse(connection.getAutoCommit());
                try (Statement statement = connection.createStatement(); ResultSet current = statement.executeQuery("SELECT current_schema()")) {
                    current.next();
                    assertEquals(schema, current.getString(1));
                }
                assertConstraint(connection, "INSERT INTO reward (id, coin_price) VALUES ('bad-price', -1)");
                assertConstraint(connection, "INSERT INTO profile_reward (id, profile_id, reward_id) VALUES ('bad-link', 'missing', 'missing')");
                try (Statement statement = connection.createStatement()) {
                    statement.execute("INSERT INTO reward (id, title, provider_name, coin_price, quantity) VALUES ('valid', 'Bus ticket', 'Transit', 30, 1)");
                }
                assertConstraint(connection, "INSERT INTO reward (id, title, provider_name) VALUES ('duplicate', ' BUS TICKET ', 'transit')");
            } finally {
                // PostgreSQL rolls back the test schema, tables and seed data together.
                connection.rollback();
            }
        }
    }

    private void executeMigration(Statement statement, String filename) throws Exception {
        try (var stream = getClass().getResourceAsStream("/db/migration/" + filename)) {
            assertNotNull(stream);
            statement.execute(new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private long number(Statement statement, String sql) throws SQLException {
        try (ResultSet result = statement.executeQuery(sql)) { result.next(); return result.getLong(1); }
    }

    private void assertConstraint(Connection connection, String sql) throws SQLException {
        Savepoint savepoint = connection.setSavepoint();
        try (Statement statement = connection.createStatement()) {
            SQLException exception = assertThrows(SQLException.class, () -> statement.execute(sql));
            assertTrue(exception.getSQLState().startsWith("23"));
        } finally { connection.rollback(savepoint); }
    }

    private void validateHibernateMappings(Connection connection, String schema) {
        Connection guarded = (Connection) java.lang.reflect.Proxy.newProxyInstance(
                Connection.class.getClassLoader(), new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                    // Hibernate insists on auto-commit even for metadata-only validation.
                    // Report it as enabled on this read-only facade; the real transaction stays open.
                    if (method.getName().equals("getAutoCommit")) return true;
                    if (method.getName().equals("commit") || method.getName().equals("rollback")
                            || (method.getName().equals("setAutoCommit") && Boolean.TRUE.equals(args[0]))) {
                        throw new SQLException("Schema validation must not change the test transaction");
                    }
                    if (method.getName().equals("close")) return null;
                    try { return method.invoke(connection, args); }
                    catch (java.lang.reflect.InvocationTargetException exception) { throw exception.getCause(); }
                });
        ConnectionProvider provider = new ConnectionProvider() {
            @Override public Connection getConnection() { return guarded; }
            @Override public void closeConnection(Connection unused) { }
            @Override public boolean supportsAggressiveRelease() { return false; }
            @Override public boolean isUnwrappableAs(Class<?> type) { return type.isInstance(this); }
            @Override public <T> T unwrap(Class<T> type) { return type.cast(this); }
        };
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.provider_class", provider)
                .applySetting("hibernate.default_schema", schema)
                .applySetting("jakarta.persistence.schema-generation-connection", guarded)
                .applySetting("hibernate.hbm2ddl.auto", "validate")
                .applySetting("hibernate.connection.autocommit", false)
                .build();
        try {
            MetadataSources metadata = new MetadataSources(registry);
            for (Class<?> entity : new Class<?>[]{AttachEntity.class, LocationEntity.class, ProfileEntity.class,
                    ProfileRoleEntity.class, RewardEntity.class, ProfileRewardEntity.class,
                    CoinTransactionEntity.class, CampaignEntity.class, WasteReportEntity.class}) {
                metadata.addAnnotatedClass(entity);
            }
            try (var factory = metadata.buildMetadata().buildSessionFactory()) { assertTrue(factory.isOpen()); }
        } finally { StandardServiceRegistryBuilder.destroy(registry); }
    }
}
