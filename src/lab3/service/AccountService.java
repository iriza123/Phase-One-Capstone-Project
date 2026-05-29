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

public class AccountService {

    private final AccountDAO dao = new AccountDAOImpl();

    public Account createWallet(int customerId) throws Exception {
        WalletAccount a = new WalletAccount();
        a.setCustomerId(customerId);
        a.setBalance(BigDecimal.ZERO);
        a.setStatus("ACTIVE");
        return dao.save(a);
    }

    public Account createSavings(int customerId) throws Exception {
        boolean alreadyHasSavings = dao.findByCustomerId(customerId).stream()
            .anyMatch(a -> "SAVINGS".equals(a.getAccountType()) && "ACTIVE".equals(a.getStatus()));
        if (alreadyHasSavings)
            throw new Exception("You already have a savings account.");
        SavingsAccount a = new SavingsAccount();
        a.setCustomerId(customerId);
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

    public Account getWalletByPhone(String phone) throws Exception {
        lab1.model.Customer c = new CustomerService().getByPhone(phone);
        return getByCustomer(c.getCustomerId()).stream()
            .filter(a -> "WALLET".equals(a.getAccountType()) && "ACTIVE".equals(a.getStatus()))
            .findFirst()
            .orElseThrow(() -> new InvalidAccountException("No active wallet found for phone: " + phone));
    }

    public void deleteAccount(int id) throws SQLException {
        dao.deleteById(id);
    }

    public int deleteAllInactiveAccounts() throws SQLException {
        return dao.deleteAllInactive();
    }

    public void updateBalance(int id, BigDecimal balance) throws SQLException {
        dao.updateBalance(id, balance);
    }

    public void deactivate(int id) throws Exception {
        Account a = getById(id);
        if (a.getBalance().compareTo(BigDecimal.ZERO) > 0)
            throw new Exception("Cannot deactivate account with balance: " + a.getBalance() + " RWF");
        dao.updateStatus(id, "INACTIVE");
    }

    public void checkActive(Account a) throws AccountLockedException {
        if (!"ACTIVE".equals(a.getStatus()))
            throw new AccountLockedException(a.getAccountNumber());
    }
}
