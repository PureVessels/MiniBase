package mini.service.database.api;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public interface DatabaseExecutor {

    CompletableFuture<Void> execute(String sql, SQLConsumer<Connection> action);
    <T> CompletableFuture<T> query(String sql, SQLFunction<Connection, T> action);

    @FunctionalInterface
    interface SQLConsumer<T> { void accept(T t) throws SQLException; }
    @FunctionalInterface
    interface SQLFunction<T, R> { R apply(T t) throws SQLException; }
}
