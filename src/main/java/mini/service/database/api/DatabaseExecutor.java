package mini.service.database.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface DatabaseThread {

    void execute(Runnable task);
    <T> CompletableFuture<T> supplyAsync(Supplier<T> task);
}
