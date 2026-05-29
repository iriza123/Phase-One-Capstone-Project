package lab1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Loan {

    private int           loanId;
    private int           customerId;
    private int           accountId;
    private BigDecimal    amount;
    private String        status;      // PENDING | APPROVED | REJECTED
    private String        reason;
    private String        notes;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    public int           getLoanId()       { return loanId; }
    public int           getCustomerId()   { return customerId; }
    public int           getAccountId()    { return accountId; }
    public BigDecimal    getAmount()       { return amount; }
    public String        getStatus()       { return status; }
    public String        getReason()       { return reason; }
    public String        getNotes()        { return notes; }
    public LocalDateTime getRequestedAt()  { return requestedAt; }
    public LocalDateTime getProcessedAt()  { return processedAt; }

    public void setLoanId(int v)              { loanId = v; }
    public void setCustomerId(int v)          { customerId = v; }
    public void setAccountId(int v)           { accountId = v; }
    public void setAmount(BigDecimal v)       { amount = v; }
    public void setStatus(String v)           { status = v; }
    public void setReason(String v)           { reason = v; }
    public void setNotes(String v)            { notes = v; }
    public void setRequestedAt(LocalDateTime v){ requestedAt = v; }
    public void setProcessedAt(LocalDateTime v){ processedAt = v; }

    @Override
    public String toString() {
        return "[" + loanId + "] " + amount + " RWF | " + status;
    }
}
