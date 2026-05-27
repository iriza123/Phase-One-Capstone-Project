package lab1.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class SavingsAccount extends Account {

    public static final BigDecimal FEE_RATE       = new BigDecimal("0.015"); // 1.5%
    public static final BigDecimal MAX_WITHDRAWAL = new BigDecimal("500000");
    public static final int        MAX_MONTHLY    = 5;

    private int monthlyWithdrawals = 0;

    public SavingsAccount() {
        this.accountType = "SAVINGS";
    }

    public SavingsAccount(int id, String number, int customerId,
                          BigDecimal balance, String status, LocalDateTime createdAt) {
        this.accountId     = id;
        this.accountNumber = number;
        this.customerId    = customerId;
        this.balance       = balance;
        this.accountType   = "SAVINGS";
        this.status        = status;
        this.createdAt     = createdAt;
    }

    // Calculate 1.5% fee on withdrawal amount
    public BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
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
        if (amount.compareTo(MAX_WITHDRAWAL) > 0)
            throw new Exception("Max withdrawal per transaction: " + MAX_WITHDRAWAL + " RWF");
        if (monthlyWithdrawals >= MAX_MONTHLY)
            throw new Exception("Monthly withdrawal limit reached (" + MAX_MONTHLY + " per month).");

        BigDecimal fee   = calculateFee(amount);
        BigDecimal total = amount.add(fee);

        if (total.compareTo(this.balance) > 0)
            throw new Exception("Insufficient funds. Need " + total + " RWF (fee: " + fee + " RWF)");

        this.balance = this.balance.subtract(total);
        this.monthlyWithdrawals++;
        return this.balance;
    }

    @Override
    public String processTransaction(String type, BigDecimal amount) throws Exception {
        return switch (type.toUpperCase()) {
            case "DEPOSIT"  -> { deposit(amount); yield "Saved " + amount + " RWF. Balance: " + balance; }
            case "WITHDRAW" -> {
                BigDecimal fee = calculateFee(amount);
                withdraw(amount);
                yield "Withdrew " + amount + " RWF (fee: " + fee + " RWF). Balance: " + balance;
            }
            default -> throw new Exception("Savings does not support: " + type);
        };
    }

    public int getMonthlyWithdrawals()       { return monthlyWithdrawals; }
    public void setMonthlyWithdrawals(int v) { this.monthlyWithdrawals = v; }
}
