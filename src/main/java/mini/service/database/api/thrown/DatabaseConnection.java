package mini.service.database.api.thrown;

public class DatabaseLoaded extends RuntimeException {
  public DatabaseLoaded(String message) {
    super(message);
  }
}
