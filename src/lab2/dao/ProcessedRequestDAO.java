package lab2.dao;

import java.sql.SQLException;
import java.util.Set;

public interface ProcessedRequestDAO {
    boolean isProcessed(String referenceId)              throws SQLException;
    void    markProcessed(String referenceId, String status) throws SQLException;
    Set<String> getAllProcessedIds()                     throws SQLException;
}
