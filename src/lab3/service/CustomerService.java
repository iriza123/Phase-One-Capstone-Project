package lab3.service;

import lab1.model.Customer;
import lab2.dao.CustomerDAO;
import lab2.dao.impl.CustomerDAOImpl;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

public class CustomerService {

    private final CustomerDAO dao = new CustomerDAOImpl();

    public Customer register(String firstName, String lastName,
                             String email, String phone, String pin) throws Exception {

        // Validate inputs
        if (firstName == null || firstName.isBlank()) throw new Exception("First name is required.");
        if (lastName  == null || lastName.isBlank())  throw new Exception("Last name is required.");
        if (email     == null || email.isBlank())     throw new Exception("Email is required.");
        if (phone     == null || phone.isBlank())     throw new Exception("Phone is required.");

        String cleanPhone = phone.trim().replaceAll("\\s+", "");
        if (!cleanPhone.matches("\\d{10}"))
            throw new Exception("Phone must be exactly 10 digits (e.g. 0780000001).");
        if (!pin.matches("\\d{5}"))
            throw new Exception("PIN must be exactly 5 digits.");

        // Check for duplicates before inserting
        if (dao.findByPhone(cleanPhone).isPresent())
            throw new Exception("This phone number is already registered. Please login instead.");
        if (dao.findByEmail(email.trim().toLowerCase()).isPresent())
            throw new Exception("This email is already registered. Please login instead.");

        // Build customer object
        Customer c = new Customer();
        c.setFirstName(firstName.trim());
        c.setLastName(lastName.trim());
        c.setEmail(email.trim().toLowerCase());
        c.setPhone(cleanPhone);
        c.setPin(hashPin(pin));
        c.setRole("USER");
        c.setStatus("ACTIVE");

        try {
            Customer saved = dao.save(c);

            // Automatically create ONE Wallet account for the new customer
            // No need to manually create accounts after registration
            new AccountService().createWallet(saved.getCustomerId());

            return saved;
        } catch (Exception ex) {
            // Convert raw DB errors into friendly messages
            String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            if (msg.contains("phone") || msg.contains("customers_phone_key"))
                throw new Exception("This phone number is already registered. Please login instead.");
            if (msg.contains("email") || msg.contains("customers_email_key"))
                throw new Exception("This email is already registered. Please login instead.");
            if (msg.contains("duplicate") || msg.contains("unique"))
                throw new Exception("This phone or email is already registered. Please login instead.");
            throw new Exception("Registration failed: " + ex.getMessage());
        }
    }

    // Maximum failed PIN attempts before account is locked
    private static final int MAX_FAILED_ATTEMPTS = 3;

    public Customer login(String phone, String pin) throws Exception {
        Customer c = dao.findByPhone(phone.trim())
            .orElseThrow(() -> new Exception("No account found for: " + phone));

        if ("LOCKED".equals(c.getStatus()))
            throw new Exception("Account is locked after too many failed attempts. Contact support.");
        if ("INACTIVE".equals(c.getStatus()))
            throw new Exception("Account is inactive. Contact support.");

        if (!verifyPin(pin, c.getPin())) {
            dao.incrementFailedAttempts(c.getCustomerId());
            int attempts  = dao.getFailedAttempts(c.getCustomerId());
            int remaining = MAX_FAILED_ATTEMPTS - attempts;
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                lockAccount(c.getCustomerId());
                throw new Exception("Account locked after " + MAX_FAILED_ATTEMPTS +
                    " failed attempts. Contact support to unlock.");
            }
            throw new Exception("Incorrect PIN. " + remaining + " attempt(s) remaining before lockout.");
        }

        // Correct PIN — reset failed attempts counter
        dao.resetFailedAttempts(c.getCustomerId());
        return c;
    }

    private void lockAccount(int customerId) throws SQLException {
        try (java.sql.PreparedStatement ps = lab2.db.DatabaseConnection.getConnection()
                .prepareStatement("UPDATE customers SET status='LOCKED' WHERE customer_id=?")) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        }
    }

    public void unlockAccount(int customerId) throws Exception {
        try (java.sql.PreparedStatement ps = lab2.db.DatabaseConnection.getConnection()
                .prepareStatement("UPDATE customers SET status='ACTIVE', failed_attempts=0 WHERE customer_id=?")) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        }
    }

    public Customer getById(int id) throws Exception {
        return dao.findById(id).orElseThrow(() -> new Exception("Customer not found."));
    }

    public List<Customer> getAll()          throws SQLException { return dao.findAll(); }
    public List<Customer> search(String kw) throws SQLException { return dao.search(kw); }
    public void update(Customer c)          throws Exception    { dao.update(c); }
    public void deactivate(int id)          throws Exception    { dao.delete(id); }
    public int count()                      throws SQLException { return dao.count(); }

    public void changePin(int customerId, String oldPin, String newPin) throws Exception {
        Customer c = getById(customerId);
        if (!verifyPin(oldPin, c.getPin())) throw new Exception("Current PIN is incorrect.");
        if (!newPin.matches("\\d{5}"))      throw new Exception("New PIN must be exactly 5 digits.");
        dao.updatePin(customerId, hashPin(newPin));
    }

    public static String hashPin(String pin) throws Exception {
        SecureRandom rng = new SecureRandom();
        byte[] salt = new byte[16];
        rng.nextBytes(salt);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hash = md.digest(pin.getBytes());
        return Base64.getEncoder().encodeToString(salt) + ":" +
               Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPin(String plain, String stored) {
        try {
            String[] parts = stored.split(":");
            if (parts.length != 2) return false;
            byte[] salt     = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] actual = md.digest(plain.getBytes());
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) { return false; }
    }
}
