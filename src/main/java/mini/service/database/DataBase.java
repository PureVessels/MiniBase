package mini.service.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import mini.service.database.api.DatabaseService;
import mini.service.database.api.thrown.DatabaseConnection;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DataBase implements DatabaseService {

    private static final Logger logger = Logger.getLogger(DataBase.class.getName());

    private final HikariDataSource dataSource;
    private final HikariPoolMXBean pool;
    private final DatabaseThread databaseThread;

    public DataBase(@NotNull DatabaseData data) {

        if (data.port() <= 0 || data.port() > 65535) {
            throw new IllegalArgumentException("Invalid MySQL port: " + data.port());
        }

        HikariConfig config = createConfig(data);

        this.dataSource = new HikariDataSource(config);
        this.pool = dataSource.getHikariPoolMXBean();

        this.databaseThread = new DatabaseThread(10, 30L, "Database-Service", this);

        logger.info("Connected to database: " + data.ip() + ":" + data.port() + "/" + data.database());
    }

    private HikariConfig createConfig(@NotNull DatabaseData data) {
        try {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(
                    "jdbc:mysql://" +
                            data.ip() + ":" +
                            data.port() + "/" +
                            data.database() +
                            "?useSSL=false" +
                            "&characterEncoding=utf8" +
                            "&serverTimezone=UTC"
            );

            config.setUsername(data.username());
            config.setPassword(data.password());

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);

            config.setConnectionTimeout(10_000);
            config.setIdleTimeout(300_000);
            config.setMaxLifetime(1_800_000);

            data.properties().forEach(config::addDataSourceProperty);

            return config;
        } catch (Exception e) {
            throw new DatabaseConnection(e);
        }
    }

    @Override
    @NotNull
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get database connection.", e);
        }
    }

    @Override
    public boolean createTable(@NotNull String tableName, @NotNull String sql) {
        if (tableName.isBlank() || sql.isBlank()) {
            throw new IllegalArgumentException("Table name or sql cannot be blank.");
        }

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
             statement.execute(sql);

            logger.info(tableName + " table created");
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Create Error: " + tableName, e);
            return false;
        }
    }

    @Override
    @NotNull
    public DatabaseThread getExecutor() { return databaseThread; }

    public Logger getLogger() { return logger; }

    public HikariPoolMXBean getPool() { return pool; }
    public void shutdown() {
        try {
            databaseThread.shutdown();
        } finally {
            if (!dataSource.isClosed()) {
                dataSource.close();
                logger.info("Data Source Closed");
            }
        }
    }

}
