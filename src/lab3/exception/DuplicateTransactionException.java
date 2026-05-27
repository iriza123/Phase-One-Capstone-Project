package lab3.exception;

public class DuplicateTransactionException extends Exception {
    public DuplicateTransactionException(String refId) {
        super("Transaction already processed: " + refId);
    }
}
