package mini.service.database.api.utils;

import mini.service.database.api.DatabaseExecutor;
import mini.service.database.api.DatabaseService;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class CustomDatabaseManager {

    private final DatabaseExecutor databaseExecutor;

    private final SQLBuilder builder;

    protected CustomDatabaseManager(@NotNull DatabaseService databaseService, String tableName, List<String> tableColumns){
        this.databaseExecutor = databaseService.getExecutor();

        this.builder = new SQLBuilder(tableName, new LinkedHashSet<>(tableColumns));
    }

    protected final CompletableFuture<Void> executeUpdate(String sql, DatabaseExecutor.SQLConsumer<PreparedStatement> consumer) {
        return databaseExecutor.execute(sql,connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                consumer.accept(ps);
                ps.executeUpdate();
            }
        });
    }

    protected final CompletableFuture<Void> executeBatchUpdate(String sql, DatabaseExecutor.SQLConsumer<PreparedStatement> consumer){
        return databaseExecutor.execute(sql ,connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                consumer.accept(ps);
                ps.executeBatch();
            }
        });
    }

    protected final <T> CompletableFuture<T> query(String sql, DatabaseExecutor.SQLConsumer<PreparedStatement> params, DatabaseExecutor.SQLFunction<ResultSet, T> mapper) {
        return databaseExecutor.query(sql,connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                params.accept(ps);

                try (ResultSet rs = ps.executeQuery()) {
                    return mapper.apply(rs);
                }
            }
        });
    }

    protected final <T> CompletableFuture<T> query(String sql, DatabaseExecutor.SQLFunction<ResultSet, T> mapper) {
        return query(sql, ps -> {}, mapper);
    }

    protected void createData(){ }
    public boolean exist(){ return false; }

    protected final SQLBuilder builder() { return builder; }
}
