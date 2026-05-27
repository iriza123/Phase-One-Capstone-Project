package lab2.dao.impl;

import lab2.dao.ProcessedRequestDAO;
import lab2.db.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// LAB 2 - Pure SQL implementation of ProcessedRequestDAO
// Uses a HashSet (Collection) to cache processed IDs in memory for fast duplicate detection
public class ProcessedRequestDAOImpl implements ProcessedRequestDAO {

    // In-memory cache using Set (Collections requirement)
    private static final Set<String> cache = new HashSet<>();

    @Override
    public boolean isProcessed(String refId) throws SQLException {
        // Check memory cache first (fast)
        if (cache.contains(refId)) return true;

        // Then check database
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT COUNT(*) FROM processed_requests WHERE reference_id=?")) {
            ps.setString(1, refId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                cache.add(refId); // add to cache
                return true;
            }
        }
        return false;
    }

    @Override
    public void markProcessed(String refId, String status) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("INSERT INTO processed_requests(reference_id,status,processed_at) VALUES(?,?,?)")) {
            ps.setString(1, refId);
            ps.setString(2, status);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            cache.add(refId);
        }
    }

    @Override
    public Set<String> getAllProcessedIds() throws SQLException {
        Set<String> ids = new HashSet<>();
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement("SELECT reference_id FROM processed_requests");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getString("reference_id"));
        }
        cache.addAll(ids);
        return ids;
    }
}
