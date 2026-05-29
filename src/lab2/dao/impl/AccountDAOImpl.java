package lab2.dao.impl;

import lab1.model.Account;
import lab1.model.SavingsAccount;
import lab1.model.WalletAccount;
import lab2.dao.AccountDAO;
import lab2.db.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class AccountDAOImpl implements AccountDAO {

    @Override
    public Account save(Account a) throws SQLException {
        String tempNum = "TMP-" + System.nanoTime();
        String sql = "INSERT INTO accounts(account_number,customer_id,balance,account_type,status,created_at) " +
                     "VALUES(?,?,?,?,?,?) RETURNING account_id";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, tempNum);
            ps.setInt(2, a.getCustomerId());
            ps.setBigDecimal(3, a.getBalance());
            ps.setString(4, a.getAccountType());
            ps.setString(5, "ACTIVE");
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                a.setAccountId(id);
                a.setAccountNumber(String.valueOf(id));
                a.setCreatedAt(LocalDateTime.now());
                try (PreparedStatement upd = DatabaseConnection.getConnection()
                        .prepareStatement("UPDATE accounts SET account_number=? WHERE account_id=?")) {
                    upd.setString(1, String.valueOf(id));
                    upd.setInt(2, id);
                    upd.executeUpdate();
                }
            }
        }
        return a;
    }

    @Override
    public Optional<Account> findById(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT * FROM accounts WHERE account_id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(String number) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT * FROM accounts WHERE account_number=?")) {
            ps.setString(1, number);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        }
    }

    @Override
    public List<Account> findByCustomerId(int customerId) throws SQLException {
        List<Account> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT * FROM accounts WHERE customer_id=? ORDER BY created_at DESC")) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public List<Account> findAll() throws SQLException {
        List<Account> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT * FROM accounts ORDER BY created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public boolean updateBalance(int id, BigDecimal balance) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("UPDATE accounts SET balance=? WHERE account_id=?")) {
            ps.setBigDecimal(1, balance);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int id, String status) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("UPDATE accounts SET status=? WHERE account_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int count() throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT COUNT(*) FROM accounts WHERE status='ACTIVE'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    @Override
    public BigDecimal totalBalance() throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT COALESCE(SUM(balance),0) FROM accounts WHERE status='ACTIVE'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    @Override
    public void deleteById(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM accounts WHERE account_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public int deleteAllInactive() throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM accounts WHERE status='INACTIVE'")) {
            return ps.executeUpdate();
        }
    }

    // Map a ResultSet row to the correct Account subclass
    private Account map(ResultSet rs) throws SQLException {
        String type = rs.getString("account_type");
        Account a = "SAVINGS".equalsIgnoreCase(type) ? new SavingsAccount() : new WalletAccount();
        a.setAccountId(rs.getInt("account_id"));
        a.setAccountNumber(rs.getString("account_number"));
        a.setCustomerId(rs.getInt("customer_id"));
        a.setBalance(rs.getBigDecimal("balance"));
        a.setAccountType(type);
        a.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        return a;
    }
}
