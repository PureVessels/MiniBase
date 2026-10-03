package mini.service.database.api.thrown;

import org.jetbrains.annotations.NotNull;

public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, @NotNull Function function) {
        super("Database " + function + " function failed: " + message);
    }

    public DatabaseException(String message, @NotNull Function function, Throwable cause) {
        super("Database " + function + " function failed: " + message, cause);
    }


    public enum Function {
        BUILDER,
        EXECUTE,
    }
}
