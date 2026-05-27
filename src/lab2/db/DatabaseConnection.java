package lab2.db;

import java.sql.*;

// LAB 2 - JDBC: Singleton connection to PostgreSQL igirepay_db
public class DatabaseConnection {

    private static final String URL  = "jdbc:postgresql://localhost:5432/igirepay_db";
    private static final String USER = "postgres";
    private static final String PASS = "12345";

    private static Connection conn;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed())
            conn = DriverManager.getConnection(URL, USER, PASS);
        return conn;
    }

    public static void close() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            System.err.println("DB close error: " + e.getMessage());
        } finally {
            conn = null;
        }
    }

    // Creates all 4 tables if they don't exist
    public static void initSchema() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {

            s.execute("CREATE TABLE IF NOT EXISTS customers(" +
                "customer_id SERIAL PRIMARY KEY," +
                "first_name  VARCHAR(100) NOT NULL," +
                "last_name   VARCHAR(100) NOT NULL," +
                "email       VARCHAR(150) UNIQUE NOT NULL," +
                "phone       VARCHAR(20)  UNIQUE NOT NULL," +
                "pin         VARCHAR(255) NOT NULL," +
                "role        VARCHAR(20)  NOT NULL DEFAULT 'USER'," +
                "status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'," +
                "failed_attempts INT       NOT NULL DEFAULT 0," +
                "created_at  TIMESTAMP    NOT NULL DEFAULT NOW())");

            s.execute("CREATE TABLE IF NOT EXISTS accounts(" +
                "account_id     SERIAL        PRIMARY KEY," +
                "account_number VARCHAR(20)   UNIQUE NOT NULL," +
                "customer_id    INT           NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE," +
                "balance        NUMERIC(15,2) NOT NULL DEFAULT 0.00," +
                "account_type   VARCHAR(20)   NOT NULL," +
                "status         VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'," +
                "created_at     TIMESTAMP     NOT NULL DEFAULT NOW())");

            s.execute("CREATE TABLE IF NOT EXISTS transactions(" +
                "transaction_id  SERIAL        PRIMARY KEY," +
                "reference_id    VARCHAR(50)   UNIQUE NOT NULL," +
                "from_account_id INT           REFERENCES accounts(account_id)," +
                "to_account_id   INT           REFERENCES accounts(account_id)," +
                "transaction_type VARCHAR(20)  NOT NULL," +
                "amount          NUMERIC(15,2) NOT NULL," +
                "fee             NUMERIC(15,2) NOT NULL DEFAULT 0.00," +
                "status          VARCHAR(20)   NOT NULL DEFAULT 'SUCCESS'," +
                "description     TEXT," +
                "created_at      TIMESTAMP     NOT NULL DEFAULT NOW())");

            s.execute("CREATE TABLE IF NOT EXISTS processed_requests(" +
                "id           SERIAL      PRIMARY KEY," +
                "reference_id VARCHAR(50) UNIQUE NOT NULL," +
                "status       VARCHAR(20) NOT NULL," +
                "processed_at TIMESTAMP   NOT NULL DEFAULT NOW())");

            // Indexes for faster queries
            s.execute("CREATE INDEX IF NOT EXISTS idx_acc_cust ON accounts(customer_id)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_tx_ref   ON transactions(reference_id)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_tx_date  ON transactions(created_at)");

            // BONUS: Add failed_attempts column if it doesn't exist yet (safe migration)
            s.execute("ALTER TABLE customers ADD COLUMN IF NOT EXISTS failed_attempts INT NOT NULL DEFAULT 0");

            // Migration: add role column if it doesn't exist (for existing databases)
            s.execute("ALTER TABLE customers ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER'");

            // Migration: remove national_id constraint if column exists (we no longer use it)
            try { s.execute("ALTER TABLE customers ALTER COLUMN national_id DROP NOT NULL"); } catch (Exception ignored) {}
            try { s.execute("ALTER TABLE customers ALTER COLUMN national_id SET DEFAULT ''"); } catch (Exception ignored) {}
            // Drop the unique constraint on national_id so empty values don't conflict
            try { s.execute("ALTER TABLE customers DROP CONSTRAINT IF EXISTS customers_national_id_key"); } catch (Exception ignored) {}
            // Also drop any index on national_id
            try { s.execute("DROP INDEX IF EXISTS customers_national_id_key"); } catch (Exception ignored) {}
            try { s.execute("DROP INDEX IF EXISTS idx_customers_national_id"); } catch (Exception ignored) {}

            // Cleanup: remove old demo/seed data that was auto-inserted
            // Only removes accounts/customers that were created by the seeder (phone pattern 078000000X)
            try {
                s.execute("DELETE FROM processed_requests WHERE reference_id LIKE '%-DEMO-%'");
                s.execute("DELETE FROM transactions WHERE reference_id LIKE '%-DEMO-%'");
                s.execute(
                    "DELETE FROM accounts WHERE customer_id IN (" +
                    "  SELECT customer_id FROM customers WHERE phone LIKE '078000000%'" +
                    ")");
                s.execute("DELETE FROM customers WHERE phone LIKE '078000000%'");
            } catch (Exception ignored) {}

            System.out.println("[DB] Schema ready.");

        } catch (Exception e) {
            System.err.println("[DB] Schema error: " + e.getMessage());
        }
    }
}
