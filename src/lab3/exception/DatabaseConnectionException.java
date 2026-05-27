package lab3.exception;
// LAB 3 - Custom exception: database connection failed
public class DatabaseConnectionException extends RuntimeException {
    public DatabaseConnectionException(String msg, Throwable cause) { super(msg, cause); }
}
