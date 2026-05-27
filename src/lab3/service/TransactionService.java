package lab3.service;

import lab1.model.Account;
import lab1.model.SavingsAccount;
import lab1.model.Transaction;
import lab2.dao.ProcessedRequestDAO;
import lab2.dao.TransactionDAO;
import lab2.dao.impl.ProcessedRequestDAOImpl;
import lab2.dao.impl.TransactionDAOImpl;
import lab2.db.DatabaseConnection;
import lab3.exception.DuplicateTransactionException;
import lab3.exception.InsufficientBalanceException;
import lab3.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

// LAB 3 - Service: business logic for all transactions (MoMo-style)
public class TransactionService {

    private final TransactionDAO      txDAO  = new TransactionDAOImpl();
    private final ProcessedRequestDAO prDAO  = new ProcessedRequestDAOImpl();
    private final AccountService      accSvc = new AccountService();
    private static final Random       RNG    = new Random();

    // Generate a unique reference ID: e.g. DEP-20250526-12345
    private String generateRef(String prefix) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String rand = String.format("%05d", RNG.nextInt(99999));
        return prefix + "-" + date + "-" + rand;
    }

    // DEPOSIT: add money to an account
    public Transaction deposit(int accountId, BigDecimal amount, String description) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidAmountException("Deposit amount must be positive.");

        Account account = accSvc.getById(accountId);
        accSvc.checkActive(account);

        String ref = generateRef("DEP");
        if (prDAO.isProcessed(ref)) throw new DuplicateTransactionException(ref);

        account.deposit(amount);
        accSvc.updateBalance(accountId, account.getBalance());

        Transaction tx = new Transaction(ref, 0, accountId, "DEPOSIT",
            amount, BigDecimal.ZERO, "SUCCESS",
            description == null || description.isBlank() ? "Deposit" : description);
        txDAO.save(tx);
        prDAO.markProcessed(ref, "SUCCESS");
        return tx;
    }

    // WITHDRAW: take money from an account
    public Transaction withdraw(int accountId, BigDecimal amount, String description) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidAmountException("Withdrawal amount must be positive.");

        Account account = accSvc.getById(accountId);
        accSvc.checkActive(account);

        // Calculate fee for savings accounts
        BigDecimal fee = BigDecimal.ZERO;
        if (account instanceof SavingsAccount sa) fee = sa.calculateFee(amount);

        if (account.getBalance().compareTo(amount.add(fee)) < 0)
            throw new InsufficientBalanceException(
                "Insufficient funds. Available: " + account.getBalance() + " RWF");

        String ref = generateRef("WIT");
        if (prDAO.isProcessed(ref)) throw new DuplicateTransactionException(ref);

        account.withdraw(amount);
        accSvc.updateBalance(accountId, account.getBalance());

        Transaction tx = new Transaction(ref, accountId, 0, "WITHDRAW",
            amount, fee, "SUCCESS",
            description == null || description.isBlank() ? "Withdrawal" : description);
        txDAO.save(tx);
        prDAO.markProcessed(ref, "SUCCESS");
        return tx;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BONUS: JDBC Transaction Rollback
    // sendMoney wraps all DB operations in a single JDBC transaction.
    // If anything fails after debiting the sender, the entire operation
    // is rolled back — no money is lost or created.
    // ─────────────────────────────────────────────────────────────────────────
    public Transaction sendMoney(int fromAccountId, String toAccountNumber,
                                 BigDecimal amount, String description) throws Exception {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidAmountException("Transfer amount must be positive.");

        // Validate both accounts before touching the DB
        Account from = accSvc.getById(fromAccountId);
        Account to   = accSvc.getByNumber(toAccountNumber);

        accSvc.checkActive(from);
        accSvc.checkActive(to);

        if (from.getAccountId() == to.getAccountId())
            throw new Exception("Cannot transfer to the same account.");
        if (from.getBalance().compareTo(amount) < 0)
            throw new InsufficientBalanceException(
                "Insufficient funds. Available: " + from.getBalance() + " RWF");

        String ref = generateRef("TRF");
        if (prDAO.isProcessed(ref)) throw new DuplicateTransactionException(ref);

        // Get the shared connection and start a JDBC transaction
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false); // BEGIN transaction

        try {
            // Step 1: Debit sender
            from.withdraw(amount);
            accSvc.updateBalance(fromAccountId, from.getBalance());

            // Step 2: Credit receiver
            to.deposit(amount);
            accSvc.updateBalance(to.getAccountId(), to.getBalance());

            // Step 3: Record the transaction
            Transaction tx = new Transaction(ref, fromAccountId, to.getAccountId(), "TRANSFER",
                amount, BigDecimal.ZERO, "SUCCESS",
                description == null || description.isBlank() ? "Send Money" : description);
            txDAO.save(tx);

            // Step 4: Mark as processed (duplicate prevention)
            prDAO.markProcessed(ref, "SUCCESS");

            // All steps succeeded — commit everything atomically
            conn.commit();
            conn.setAutoCommit(true);
            return tx;

        } catch (Exception e) {
            // Something failed — roll back ALL changes so no money is lost
            conn.rollback();
            conn.setAutoCommit(true);
            throw new Exception("Transfer failed and was rolled back: " + e.getMessage());
        }
    }

    public List<Transaction> getAll()                                 throws SQLException { return txDAO.findAll(); }
    public List<Transaction> getByAccount(int id)                     throws SQLException { return txDAO.findByAccountId(id); }
    public List<Transaction> getByCustomer(int id)                    throws SQLException { return txDAO.findByCustomerId(id); }
    public List<Transaction> getByDateRange(LocalDate f, LocalDate t) throws SQLException { return txDAO.findByDateRange(f, t); }
    public int countToday()                                           throws SQLException { return txDAO.countToday(); }
    public double sumToday()                                          throws SQLException { return txDAO.sumToday(); }
}
