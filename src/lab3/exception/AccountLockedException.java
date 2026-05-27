package lab3.exception;
// LAB 3 - Custom exception: account is inactive or locked
public class AccountLockedException extends Exception {
    public AccountLockedException(String accountNumber) {
        super("Account is locked or inactive: " + accountNumber);
    }
}
