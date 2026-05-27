package lab3.service;

import lab1.model.Account;
import lab1.model.SavingsAccount;
import lab1.model.WalletAccount;
import lab2.dao.AccountDAO;
import lab2.dao.impl.AccountDAOImpl;
import lab3.exception.AccountLockedException;
import lab3.exception.InvalidAccountException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

// LAB 3 - Service: business logic for account operations
public class AccountService {

    private final AccountDAO dao = new AccountDAOImpl();
    private final Random rng = new Random();

    // Create a new Wallet account for a customer
    public Account createWallet(int customerId) throws Exception {
        WalletAccount a = new WalletAccount();
        a.setCustomerId(customerId);
        a.setAccountNumber("10" + String.format("%08d", (long)(rng.nextDouble() * 100_000_000L)));
        a.setBalance(BigDecimal.ZERO);
        a.setStatus("ACTIVE");
        return dao.save(a);
    }

    // Create a new Savings account for a customer
    public Account createSavings(int customerId) throws Exception {
        SavingsAccount a = new SavingsAccount();
        a.setCustomerId(customerId);
        a.setAccountNumber("20" + String.format("%08d", (long)(rng.nextDouble() * 100_000_000L)));
        a.setBalance(BigDecimal.ZERO);
        a.setStatus("ACTIVE");
        return dao.save(a);
    }

    public Account getById(int id) throws Exception {
        return dao.findById(id)
            .orElseThrow(() -> new InvalidAccountException("Account not found: " + id));
    }

    public Account getByNumber(String number) throws Exception {
        return dao.findByAccountNumber(number)
            .orElseThrow(() -> new InvalidAccountException("Account not found: " + number));
    }

    public List<Account> getByCustomer(int customerId) throws SQLException {
        return dao.findByCustomerId(customerId);
    }

    public List<Account> getAll()    throws SQLException { return dao.findAll(); }
    public int count()               throws SQLException { return dao.count(); }
    public BigDecimal totalBalance() throws SQLException { return dao.totalBalance(); }

    public void updateBalance(int id, BigDecimal balance) throws SQLException {
        dao.updateBalance(id, balance);
    }

    // Deactivate account only if balance is zero
    public void deactivate(int id) throws Exception {
        Account a = getById(id);
        if (a.getBalance().compareTo(BigDecimal.ZERO) > 0)
            throw new Exception("Cannot deactivate account with balance: " + a.getBalance() + " RWF");
        dao.updateStatus(id, "INACTIVE");
    }

    // Throw exception if account is not active
    public void checkActive(Account a) throws AccountLockedException {
        if (!"ACTIVE".equals(a.getStatus()))
            throw new AccountLockedException(a.getAccountNumber());
    }
}
