package mini.service.database;

import mini.service.database.api.DatabaseExecutor;
import mini.service.database.api.thrown.DatabaseException;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

public final class DatabaseThread implements DatabaseExecutor {

    private final ThreadPoolExecutor executor;
    private final DataBase dataBase;

    public DatabaseThread(int maximumPoolSize, long keepAliveTime, String threadName, DataBase dataBase) {
        this.dataBase = dataBase;

        this.executor = new ThreadPoolExecutor(
                maximumPoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                runnable -> {
                    Thread thread = new Thread(runnable, threadName);
                    thread.setDaemon(true);
                    return thread;
                }
        );
        executor.allowCoreThreadTimeOut(true);
    }

    @Override
    public CompletableFuture<Void> execute(String sql, SQLConsumer<Connection> action) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataBase.getConnection()) {
                action.accept(connection);
            } catch (SQLException e) {
                throw new DatabaseException("SQL failed. SQL: " + sql, DatabaseException.Function.EXECUTE, e);
            }
        }, executor);
    }

    @Override
    public <T> CompletableFuture<T> query(String sql, SQLFunction<Connection, T> action) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataBase.getConnection()) {
                return action.apply(connection);
            } catch (SQLException e) {
                throw new DatabaseException("SQL failed. SQL: " + sql, DatabaseException.Function.EXECUTE, e);
            }
        }, executor);
    }


    public void shutdown() {
        executor.shutdown();

        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
