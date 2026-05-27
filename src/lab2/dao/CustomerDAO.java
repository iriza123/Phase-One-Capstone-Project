package lab2.dao;

import lab1.model.Customer;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CustomerDAO {
    Customer    save(Customer customer)                throws SQLException;
    Optional<Customer> findById(int id)               throws SQLException;
    Optional<Customer> findByPhone(String phone)      throws SQLException;
    Optional<Customer> findByEmail(String email)      throws SQLException;
    List<Customer>     findAll()                      throws SQLException;
    List<Customer>     search(String keyword)         throws SQLException;
    boolean update(Customer customer)                 throws SQLException;
    boolean updatePin(int id, String hashedPin)       throws SQLException;
    boolean delete(int id)                            throws SQLException;
    int     count()                                   throws SQLException;
    boolean incrementFailedAttempts(int id)           throws SQLException;
    boolean resetFailedAttempts(int id)               throws SQLException;
    int     getFailedAttempts(int id)                 throws SQLException;
}
