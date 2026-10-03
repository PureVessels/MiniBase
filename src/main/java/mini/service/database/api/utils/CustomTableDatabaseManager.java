package mini.service.database.api.utils;

import mini.service.database.api.DatabaseService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class CustomTableDatabaseManager extends CustomDatabaseManager {

    protected CustomTableDatabaseManager(DatabaseService databaseService, String tableName, List<String> tableColumns) {
        super(databaseService, tableName, tableColumns);
    }

    protected abstract <T> CompletableFuture<T> getValue(String key, SQLValueType<T> type, Object... arg);
    protected abstract <T> CompletableFuture<Void> setValue(String key, T value, SQLValueType<T> type, Object... arg);
}
