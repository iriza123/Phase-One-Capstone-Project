package lab1.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Customer {

    private int customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String pin;       // SHA-256 hashed 5-digit PIN
    private String role;      // USER | ADMIN
    private String status;    // ACTIVE | INACTIVE
    private LocalDateTime createdAt;
    private List<Account> accounts = new ArrayList<>();

    public Customer() {}

    // Helper methods
    public String getFullName() { return firstName + " " + lastName; }
    public boolean isAdmin()    { return "ADMIN".equalsIgnoreCase(role); }

    // Getters and Setters
    public int getCustomerId()                { return customerId; }
    public void setCustomerId(int v)          { this.customerId = v; }
    public String getFirstName()              { return firstName; }
    public void setFirstName(String v)        { this.firstName = v; }
    public String getLastName()               { return lastName; }
    public void setLastName(String v)         { this.lastName = v; }
    public String getEmail()                  { return email; }
    public void setEmail(String v)            { this.email = v; }
    public String getPhone()                  { return phone; }
    public void setPhone(String v)            { this.phone = v; }
    public String getPin()                    { return pin; }
    public void setPin(String v)              { this.pin = v; }
    public String getRole()                   { return role; }
    public void setRole(String v)             { this.role = v; }
    public String getStatus()                 { return status; }
    public void setStatus(String v)           { this.status = v; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public List<Account> getAccounts()        { return accounts; }
    public void setAccounts(List<Account> v)  { this.accounts = v; }
    public void addAccount(Account a)         { this.accounts.add(a); }

    @Override
    public String toString() {
        return "[" + customerId + "] " + getFullName() + " | " + phone + " | " + status;
    }
}
