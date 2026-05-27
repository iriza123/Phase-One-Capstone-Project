/**
 * Run this standalone class ONCE to insert demo customers with properly hashed PINs.
 * Compile and run: javac -cp .:postgresql-*.jar seed_demo.java && java -cp .:postgresql-*.jar seed_demo
 *
 * Or simply register customers through the app UI.
 *
 * Demo credentials:
 *   Phone: 0780000001  PIN: 1234
 *   Phone: 0780000002  PIN: 1234
 *   Phone: 0780000003  PIN: 1234
 */

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

public class seed_demo {

    static String hashPin(String pin) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hash = md.digest(pin.getBytes());
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static void main(String[] args) throws Exception {
        String url  = "jdbc:postgresql://localhost:5432/igirepay_db";
        String user = "postgres";
        String pass = "postgres";

        String[][] customers = {
            {"Alice",  "Uwimana",   "alice@igirepay.rw",  "0780000001", "1199880012345001"},
            {"Bob",    "Nkurunziza","bob@igirepay.rw",    "0780000002", "1199880012345002"},
            {"Claire", "Mukamana",  "claire@igirepay.rw", "0780000003", "1199880012345003"},
            {"David",  "Habimana",  "david@igirepay.rw",  "0780000004", "1199880012345004"},
            {"Eve",    "Ingabire",  "eve@igirepay.rw",    "0780000005", "1199880012345005"},
        };

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            // Clear existing demo data
            conn.createStatement().execute("DELETE FROM transactions WHERE reference_id LIKE '%-DEMO%'");
            conn.createStatement().execute("DELETE FROM processed_requests WHERE reference_id LIKE '%-DEMO%'");
            conn.createStatement().execute("DELETE FROM accounts WHERE account_number LIKE '1000000%' OR account_number LIKE '2000000%'");
            conn.createStatement().execute("DELETE FROM customers WHERE phone LIKE '078000000%'");

            String insertCust = "INSERT INTO customers (first_name,last_name,email,phone,national_id,pin,status) VALUES (?,?,?,?,?,?,'ACTIVE') RETURNING customer_id";
            String insertAcc  = "INSERT INTO accounts (account_number,customer_id,balance,account_type,status) VALUES (?,?,?,?,'ACTIVE')";

            int[] ids = new int[customers.length];
            for (int i = 0; i < customers.length; i++) {
                String[] c = customers[i];
                PreparedStatement ps = conn.prepareStatement(insertCust);
                ps.setString(1, c[0]); ps.setString(2, c[1]); ps.setString(3, c[2]);
                ps.setString(4, c[3]); ps.setString(5, c[4]); ps.setString(6, hashPin("1234"));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) ids[i] = rs.getInt(1);
                System.out.println("Inserted customer: " + c[0] + " " + c[1] + " (ID=" + ids[i] + ")");
            }

            // Create accounts
            String[][] accounts = {
                {"1000000001", "0", "250000.00", "WALLET"},
                {"2000000001", "0", "500000.00", "SAVINGS"},
                {"1000000002", "1", "180000.00", "WALLET"},
                {"2000000002", "1", "320000.00", "SAVINGS"},
                {"1000000003", "2",  "95000.00", "WALLET"},
                {"1000000004", "3", "430000.00", "WALLET"},
                {"2000000004", "3", "750000.00", "SAVINGS"},
                {"1000000005", "4",  "60000.00", "WALLET"},
            };

            int[] accIds = new int[accounts.length];
            for (int i = 0; i < accounts.length; i++) {
                String[] a = accounts[i];
                PreparedStatement ps = conn.prepareStatement(insertAcc + " RETURNING account_id");
                ps.setString(1, a[0]);
                ps.setInt(2, ids[Integer.parseInt(a[1])]);
                ps.setBigDecimal(3, new java.math.BigDecimal(a[2]));
                ps.setString(4, a[3]);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) accIds[i] = rs.getInt(1);
                System.out.println("Created account: " + a[0] + " [" + a[3] + "] balance=" + a[2]);
            }

            System.out.println("\n✅ Demo data seeded successfully!");
            System.out.println("Login with: Phone=0780000001, PIN=1234");
        }
    }
}
