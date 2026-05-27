package lab3.exception;

public class DatabaseConnectionException extends RuntimeException {
    public DatabaseConnectionException(String msg, Throwable cause) { super(msg, cause); }
}
