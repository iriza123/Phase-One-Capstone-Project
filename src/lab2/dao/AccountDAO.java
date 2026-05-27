package lab2.dao;

import lab1.model.Account;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// LAB 2 - Interface: defines all Account database operations
public interface AccountDAO {
    Account            save(Account account)                    throws SQLException;
    Optional<Account>  findById(int id)                        throws SQLException;
    Optional<Account>  findByAccountNumber(String number)      throws SQLException;
    List<Account>      findByCustomerId(int customerId)        throws SQLException;
    List<Account>      findAll()                               throws SQLException;
    boolean            updateBalance(int id, BigDecimal bal)   throws SQLException;
    boolean            updateStatus(int id, String status)     throws SQLException;
    int                count()                                 throws SQLException;
    BigDecimal         totalBalance()                          throws SQLException;
}
