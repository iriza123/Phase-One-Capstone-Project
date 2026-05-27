package lab1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// LAB 1 - OOP: Abstract base class (Abstraction + Encapsulation)
public abstract class Account {

    protected int accountId;
    protected String accountNumber;
    protected int customerId;
    protected BigDecimal balance;
    protected String accountType;
    protected String status;
    protected LocalDateTime createdAt;

    public Account() {}

    // LAB 1 - Polymorphism: subclasses must override these methods
    public abstract BigDecimal deposit(BigDecimal amount);
    public abstract BigDecimal withdraw(BigDecimal amount) throws Exception;
    public abstract String processTransaction(String type, BigDecimal amount) throws Exception;

    // Getters and Setters
    public int getAccountId()                 { return accountId; }
    public void setAccountId(int v)           { this.accountId = v; }
    public String getAccountNumber()          { return accountNumber; }
    public void setAccountNumber(String v)    { this.accountNumber = v; }
    public int getCustomerId()                { return customerId; }
    public void setCustomerId(int v)          { this.customerId = v; }
    public BigDecimal getBalance()            { return balance; }
    public void setBalance(BigDecimal v)      { this.balance = v; }
    public String getAccountType()            { return accountType; }
    public void setAccountType(String v)      { this.accountType = v; }
    public String getStatus()                 { return status; }
    public void setStatus(String v)           { this.status = v; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    @Override
    public String toString() {
        return accountType + " [" + accountNumber + "] Balance: " + balance + " RWF";
    }
}
