package lab2.dao.impl;

import lab1.model.Loan;
import lab2.dao.LoanDAO;
import lab2.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDAOImpl implements LoanDAO {

    @Override
    public Loan save(Loan loan) throws SQLException {
        String sql = "INSERT INTO loans(customer_id,account_id,amount,status,reason,requested_at) " +
                     "VALUES(?,?,?,'PENDING',?,NOW()) RETURNING loan_id";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, loan.getCustomerId());
            ps.setInt(2, loan.getAccountId());
            ps.setBigDecimal(3, loan.getAmount());
            ps.setString(4, loan.getReason());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) loan.setLoanId(rs.getInt(1));
        }
        return loan;
    }

    @Override
    public List<Loan> findByCustomerId(int customerId) throws SQLException {
        List<Loan> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT * FROM loans WHERE customer_id=? ORDER BY requested_at DESC")) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public List<Loan> findAll() throws SQLException {
        List<Loan> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT * FROM loans ORDER BY requested_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public List<Loan> findByStatus(String status) throws SQLException {
        List<Loan> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT * FROM loans WHERE status=? ORDER BY requested_at DESC")) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public boolean updateStatus(int loanId, String status, String notes) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("UPDATE loans SET status=?,notes=?,processed_at=NOW() WHERE loan_id=?")) {
            ps.setString(1, status);
            ps.setString(2, notes);
            ps.setInt(3, loanId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int countPending() throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT COUNT(*) FROM loans WHERE status='PENDING'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Loan map(ResultSet rs) throws SQLException {
        Loan l = new Loan();
        l.setLoanId(rs.getInt("loan_id"));
        l.setCustomerId(rs.getInt("customer_id"));
        l.setAccountId(rs.getInt("account_id"));
        l.setAmount(rs.getBigDecimal("amount"));
        l.setStatus(rs.getString("status"));
        l.setReason(rs.getString("reason"));
        l.setNotes(rs.getString("notes"));
        Timestamp req = rs.getTimestamp("requested_at");
        Timestamp pro = rs.getTimestamp("processed_at");
        if (req != null) l.setRequestedAt(req.toLocalDateTime());
        if (pro != null) l.setProcessedAt(pro.toLocalDateTime());
        return l;
    }
}
