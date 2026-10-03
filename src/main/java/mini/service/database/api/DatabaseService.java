package mini.service.database.api;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public interface DatabaseService {
    @NotNull
    Connection getConnection();

    boolean createTable(@NotNull String tableName, @NotNull String sql);

    @NotNull
    DatabaseExecutor getExecutor();
}