package lab2.dao.impl;

import lab1.model.Transaction;
import lab2.dao.TransactionDAO;
import lab2.db.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

// LAB 2 - Pure SQL implementation of TransactionDAO
// FIX: using java.sql.Date explicitly to avoid ambiguity with java.util.Date
public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public Transaction save(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions(reference_id,from_account_id,to_account_id," +
                     "transaction_type,amount,fee,status,description,created_at) " +
                     "VALUES(?,?,?,?,?,?,?,?,?) RETURNING transaction_id";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, t.getReferenceId());
            // Use NULL for missing account IDs
            if (t.getFromAccountId() > 0) ps.setInt(2, t.getFromAccountId());
            else ps.setNull(2, Types.INTEGER);
            if (t.getToAccountId() > 0) ps.setInt(3, t.getToAccountId());
            else ps.setNull(3, Types.INTEGER);
            ps.setString(4, t.getTransactionType());
            ps.setBigDecimal(5, t.getAmount());
            ps.setBigDecimal(6, t.getFee());
            ps.setString(7, t.getStatus());
            ps.setString(8, t.getDescription());
            ps.setTimestamp(9, Timestamp.valueOf(
                t.getCreatedAt() != null ? t.getCreatedAt() : LocalDateTime.now()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) t.setTransactionId(rs.getInt(1));
        }
        return t;
    }

    @Override
    public Optional<Transaction> findByReferenceId(String refId) throws SQLException {
        String sql = "SELECT t.*, fa.account_number AS from_num, ta.account_number AS to_num " +
                     "FROM transactions t " +
                     "LEFT JOIN accounts fa ON t.from_account_id = fa.account_id " +
                     "LEFT JOIN accounts ta ON t.to_account_id   = ta.account_id " +
                     "WHERE t.reference_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, refId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        }
    }

    @Override
    public List<Transaction> findAll() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, fa.account_number AS from_num, ta.account_number AS to_num, " +
                     "CONCAT(c.first_name,' ',c.last_name) AS cname " +
                     "FROM transactions t " +
                     "LEFT JOIN accounts fa ON t.from_account_id = fa.account_id " +
                     "LEFT JOIN accounts ta ON t.to_account_id   = ta.account_id " +
                     "LEFT JOIN customers c ON fa.customer_id    = c.customer_id " +
                     "ORDER BY t.created_at DESC LIMIT 500";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public List<Transaction> findByAccountId(int accountId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, fa.account_number AS from_num, ta.account_number AS to_num " +
                     "FROM transactions t " +
                     "LEFT JOIN accounts fa ON t.from_account_id = fa.account_id " +
                     "LEFT JOIN accounts ta ON t.to_account_id   = ta.account_id " +
                     "WHERE t.from_account_id = ? OR t.to_account_id = ? " +
                     "ORDER BY t.created_at DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, accountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public List<Transaction> findByCustomerId(int customerId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, fa.account_number AS from_num, ta.account_number AS to_num " +
                     "FROM transactions t " +
                     "LEFT JOIN accounts fa ON t.from_account_id = fa.account_id " +
                     "LEFT JOIN accounts ta ON t.to_account_id   = ta.account_id " +
                     "WHERE fa.customer_id = ? OR ta.customer_id = ? " +
                     "ORDER BY t.created_at DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public List<Transaction> findByDateRange(LocalDate from, LocalDate to) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, fa.account_number AS from_num, ta.account_number AS to_num " +
                     "FROM transactions t " +
                     "LEFT JOIN accounts fa ON t.from_account_id = fa.account_id " +
                     "LEFT JOIN accounts ta ON t.to_account_id   = ta.account_id " +
                     "WHERE DATE(t.created_at) BETWEEN ? AND ? " +
                     "ORDER BY t.created_at DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            // FIX: explicitly use java.sql.Date to avoid ambiguity with java.util.Date
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public int countToday() throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT COUNT(*) FROM transactions WHERE DATE(created_at)=CURRENT_DATE");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    @Override
    public double sumToday() throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT COALESCE(SUM(amount),0) FROM transactions " +
                                  "WHERE DATE(created_at)=CURRENT_DATE AND status='SUCCESS'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    // Map a ResultSet row to a Transaction object
    private Transaction map(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setReferenceId(rs.getString("reference_id"));
        t.setFromAccountId(rs.getInt("from_account_id"));
        t.setToAccountId(rs.getInt("to_account_id"));
        t.setTransactionType(rs.getString("transaction_type"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setFee(rs.getBigDecimal("fee"));
        t.setStatus(rs.getString("status"));
        t.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
        try { t.setFromAccountNumber(rs.getString("from_num")); } catch (SQLException ignored) {}
        try { t.setToAccountNumber(rs.getString("to_num")); }    catch (SQLException ignored) {}
        try { t.setCustomerName(rs.getString("cname")); }        catch (SQLException ignored) {}
        return t;
    }
}
