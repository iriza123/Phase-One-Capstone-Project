package lab1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// LAB 1 - OOP: Inheritance. Wallet = instant transfers, no fees, no limits.
public class WalletAccount extends Account {

    public WalletAccount() {
        this.accountType = "WALLET";
    }

    public WalletAccount(int id, String number, int customerId,
                         BigDecimal balance, String status, LocalDateTime createdAt) {
        this.accountId     = id;
        this.accountNumber = number;
        this.customerId    = customerId;
        this.balance       = balance;
        this.accountType   = "WALLET";
        this.status        = status;
        this.createdAt     = createdAt;
    }

    @Override
    public BigDecimal deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Deposit amount must be positive.");
        this.balance = this.balance.add(amount);
        return this.balance;
    }

    @Override
    public BigDecimal withdraw(BigDecimal amount) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (amount.compareTo(this.balance) > 0)
            throw new Exception("Insufficient funds. Available: " + this.balance + " RWF");
        this.balance = this.balance.subtract(amount);
        return this.balance;
    }

    @Override
    public String processTransaction(String type, BigDecimal amount) throws Exception {
        return switch (type.toUpperCase()) {
            case "DEPOSIT"  -> { deposit(amount);  yield "Deposited " + amount + " RWF. Balance: " + balance; }
            case "WITHDRAW" -> { withdraw(amount); yield "Withdrew "  + amount + " RWF. Balance: " + balance; }
            case "TRANSFER" -> { withdraw(amount); yield "Sent "      + amount + " RWF. Balance: " + balance; }
            default -> throw new Exception("Wallet does not support: " + type);
        };
    }
}
