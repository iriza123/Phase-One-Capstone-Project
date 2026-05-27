package lab3.exception;
// LAB 3 - Custom exception: account not found or invalid
public class InvalidAccountException extends Exception {
    public InvalidAccountException(String msg) { super(msg); }
}
