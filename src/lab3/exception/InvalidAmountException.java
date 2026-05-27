package lab3.exception;
// LAB 3 - Custom exception: zero or negative amount entered
public class InvalidAmountException extends Exception {
    public InvalidAmountException(String msg) { super(msg); }
}
