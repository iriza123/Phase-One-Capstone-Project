package lab2.dao;

import lab1.model.Loan;
import java.sql.SQLException;
import java.util.List;

public interface LoanDAO {
    Loan       save(Loan loan)                                          throws SQLException;
    List<Loan> findByCustomerId(int customerId)                        throws SQLException;
    List<Loan> findAll()                                               throws SQLException;
    List<Loan> findByStatus(String status)                             throws SQLException;
    boolean    updateStatus(int loanId, String status, String notes)   throws SQLException;
    int        countPending()                                          throws SQLException;
}
