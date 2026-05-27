package lab3.exception;
// LAB 3 - Custom exception: same transaction submitted twice
public class DuplicateTransactionException extends Exception {
    public DuplicateTransactionException(String refId) {
        super("Transaction already processed: " + refId);
    }
}
