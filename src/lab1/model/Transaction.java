package lab1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// LAB 1 - OOP: Encapsulation. Represents one financial transaction.
public class Transaction {

    private int transactionId;
    private String referenceId;
    private int fromAccountId;
    private int toAccountId;
    private String transactionType;  // DEPOSIT | WITHDRAW | TRANSFER
    private BigDecimal amount;
    private BigDecimal fee;
    private String status;           // SUCCESS | FAILED
    private String description;
    private LocalDateTime createdAt;

    // Extra fields for display (not stored in DB columns directly)
    private String fromAccountNumber;
    private String toAccountNumber;
    private String customerName;

    public Transaction() {}

    public Transaction(String referenceId, int fromAccountId, int toAccountId,
                       String type, BigDecimal amount, BigDecimal fee,
                       String status, String description) {
        this.referenceId     = referenceId;
        this.fromAccountId   = fromAccountId;
        this.toAccountId     = toAccountId;
        this.transactionType = type;
        this.amount          = amount;
        this.fee             = fee;
        this.status          = status;
        this.description     = description;
        this.createdAt       = LocalDateTime.now();
    }

    public BigDecimal getTotalAmount() {
        return fee != null ? amount.add(fee) : amount;
    }

    // Getters and Setters
    public int getTransactionId()              { return transactionId; }
    public void setTransactionId(int v)        { this.transactionId = v; }
    public String getReferenceId()             { return referenceId; }
    public void setReferenceId(String v)       { this.referenceId = v; }
    public int getFromAccountId()              { return fromAccountId; }
    public void setFromAccountId(int v)        { this.fromAccountId = v; }
    public int getToAccountId()                { return toAccountId; }
    public void setToAccountId(int v)          { this.toAccountId = v; }
    public String getTransactionType()         { return transactionType; }
    public void setTransactionType(String v)   { this.transactionType = v; }
    public BigDecimal getAmount()              { return amount; }
    public void setAmount(BigDecimal v)        { this.amount = v; }
    public BigDecimal getFee()                 { return fee; }
    public void setFee(BigDecimal v)           { this.fee = v; }
    public String getStatus()                  { return status; }
    public void setStatus(String v)            { this.status = v; }
    public String getDescription()             { return description; }
    public void setDescription(String v)       { this.description = v; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime v)  { this.createdAt = v; }
    public String getFromAccountNumber()       { return fromAccountNumber; }
    public void setFromAccountNumber(String v) { this.fromAccountNumber = v; }
    public String getToAccountNumber()         { return toAccountNumber; }
    public void setToAccountNumber(String v)   { this.toAccountNumber = v; }
    public String getCustomerName()            { return customerName; }
    public void setCustomerName(String v)      { this.customerName = v; }
}
