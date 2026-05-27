package lab3.exception;

public class AccountLockedException extends Exception {
    public AccountLockedException(String accountNumber) {
        super("Account is locked or inactive: " + accountNumber);
    }
}
