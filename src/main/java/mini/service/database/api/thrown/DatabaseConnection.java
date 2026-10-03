package mini.service.database.api.thrown;

public class DatabaseConnection extends RuntimeException {

    public DatabaseConnection(Exception e) {
        this("Database connection failed: " + e.getMessage(), e);
    }

    public DatabaseConnection(String message, Throwable cause) {
        super(message, cause);
    }
}
