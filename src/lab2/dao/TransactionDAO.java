package lab2.dao;

import lab1.model.Transaction;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionDAO {
    Transaction            save(Transaction t)                          throws SQLException;
    Optional<Transaction>  findByReferenceId(String referenceId)       throws SQLException;
    List<Transaction>      findAll()                                    throws SQLException;
    List<Transaction>      findByAccountId(int accountId)              throws SQLException;
    List<Transaction>      findByCustomerId(int customerId)            throws SQLException;
    List<Transaction>      findByDateRange(LocalDate from, LocalDate to) throws SQLException;
    int                    countToday()                                 throws SQLException;
    double                 sumToday()                                   throws SQLException;
    int                    countMonthlyWithdrawals(int accountId)      throws SQLException;
}
