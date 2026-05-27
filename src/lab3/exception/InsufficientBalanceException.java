package lab3.exception;
// LAB 3 - Custom exception: not enough funds
public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String msg) { super(msg); }
}
