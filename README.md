# IgirePay — Secure African Fintech Platform

A JavaFX desktop mobile-money application inspired by MTN MoMo, built for the Igire Rwanda Organization capstone project. Demonstrates OOP design, JDBC persistence, custom exceptions, and a full three-layer architecture.

---

## Features

| Category | Details |
|---|---|
| Auth | PIN login (5 digits), SHA-256 + salt hashing, lockout after 3 failures |
| Accounts | Wallet (instant, no limits) · Savings (1.5% fee, 500K RWF max, 5 withdrawals/month) |
| Transactions | Deposit · Withdraw · Send Money (wallet-to-wallet) |
| Safety | Idempotency via `processed_requests` table + in-memory `HashSet` |
| Admin | Customer management · All accounts · Transaction history · CSV reports · Reset database |
| Reports | Date-range filters · CSV export · Daily summary export |

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | JavaFX 17+ |
| Database | PostgreSQL 14+ |
| Connectivity | PostgreSQL JDBC Driver 42.x |
| Architecture | DAO pattern + Service layer + OOP (3 packages) |
| Security | SHA-256 PIN hashing with random 16-byte salt |
| Export | CSV via Java I/O |

---

## Project Structure

```
src/
├── Main.java
│
├── lab1/model/
│   ├── Account.java            # Abstract base — deposit(), withdraw(), processTransaction()
│   ├── WalletAccount.java      # No fee, no withdrawal limits
│   ├── SavingsAccount.java     # 1.5% fee · max 500K RWF · max 5 withdrawals/month
│   ├── Customer.java           # Customer entity with role + status
│   └── Transaction.java        # Transaction entity with reference ID
│
├── lab2/
│   ├── db/DatabaseConnection.java        # Singleton JDBC + schema init
│   └── dao/
│       ├── AccountDAO.java               # Interface
│       ├── CustomerDAO.java
│       ├── TransactionDAO.java
│       ├── ProcessedRequestDAO.java
│       └── impl/
│           ├── AccountDAOImpl.java       # PreparedStatement SQL
│           ├── CustomerDAOImpl.java
│           ├── TransactionDAOImpl.java
│           └── ProcessedRequestDAOImpl.java
│
└── lab3/
    ├── exception/
    │   ├── InsufficientBalanceException.java
    │   ├── InvalidAmountException.java
    │   ├── InvalidAccountException.java
    │   ├── AccountLockedException.java
    │   ├── DuplicateTransactionException.java
    │   └── DatabaseConnectionException.java  # extends RuntimeException
    ├── service/
    │   ├── AccountService.java
    │   ├── CustomerService.java
    │   └── TransactionService.java       # deposit/withdraw/sendMoney with JDBC rollback
    ├── util/
    │   └── CSVExporter.java
    └── ui/
        ├── BaseScreen.java               # Abstract — shared top bar, form card, alerts
        ├── SessionManager.java
        ├── SplashScreen.java
        ├── LoginScreen.java
        ├── RegisterScreen.java
        ├── DashboardScreen.java
        ├── DepositScreen.java
        ├── WithdrawScreen.java
        ├── SendMoneyScreen.java
        ├── SavingsScreen.java
        ├── HistoryScreen.java
        ├── ReportsScreen.java
        ├── ProfileScreen.java
        ├── AdminDashboardScreen.java
        ├── AdminScreen.java
        ├── AdminAccountsScreen.java
        ├── AdminHistoryScreen.java
        └── AdminReportsScreen.java

database/
├── schema.sql      # PostgreSQL DDL — run once to create tables and indexes
└── seed_demo.java  # Standalone seeder for demo data (optional)

image/
└── igirelogo.jpg   # App logo (IRO branding)
```

---

## Setup

### Prerequisites
- Java 17+
- JavaFX SDK 17+
- PostgreSQL 14+
- IntelliJ IDEA (recommended)
- PostgreSQL JDBC Driver (`postgresql-42.x.x.jar`)

### 1. Create the database

```sql
psql -U postgres
CREATE DATABASE igirepay_db;
\q
```

### 2. Run the schema

```bash
psql -U postgres -d igirepay_db -f database/schema.sql
```

### 3. Configure IntelliJ IDEA

1. **File → Project Structure → Libraries → `+`**
   - Add `postgresql-42.x.x.jar`
   - Add all JARs from your JavaFX SDK `lib/` folder

2. **Run Configuration → VM Options:**
   ```
   --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.graphics
   ```

3. **Database credentials** are in `src/lab2/db/DatabaseConnection.java`:
   ```java
   private static final String URL  = "jdbc:postgresql://localhost:5432/igirepay_db";
   private static final String USER = "postgres";
   private static final String PASS = "12345";
   ```
   Change `PASS` if your PostgreSQL password differs.

### 4. Run

Execute `Main.java`. The app will auto-initialize the schema on first launch.

### 5. Create an admin account

Register normally through the app, then run:
```sql
UPDATE customers SET role = 'ADMIN' WHERE phone = 'your_phone_number';
```
Log out and log back in — you will be routed to the Admin Dashboard.

---

## OOP Design

```
Account  (abstract)
├── deposit(BigDecimal)           — shared implementation
├── withdraw(BigDecimal)          — abstract: each subtype enforces its own rules
└── processTransaction(...)       — abstract: polymorphic dispatch

WalletAccount  extends Account   — no fee, no limits
SavingsAccount extends Account   — 1.5% fee, 500K max/tx, 5 withdrawals/month
```

**Collections used:**
- `List<Account>` — customer accounts
- `List<Transaction>` — transaction history
- `Set<String>` — in-memory idempotency cache (processed reference IDs)
- `Map<String, Long>` — transaction type summary for reports

---

## Security

- All SQL via `PreparedStatement` — no string concatenation (SQL injection safe)
- PINs hashed with SHA-256 + random 16-byte salt (never stored in plain text)
- Duplicate transactions blocked via `processed_requests` table + `HashSet` cache
- Account locked after 3 failed PIN attempts; unlock requires admin
- Account status checked before every transaction

---

## Git Workflow

This project uses feature branches and pull requests:

| Branch | Purpose |
|---|---|
| `feature/lab1-domain-models` | OOP model classes (Lab 1) |
| `feature/lab2-database-layer` | DAO interfaces, implementations, DB schema (Lab 2) |
| `feature/lab3-service-layer` | Custom exceptions + service layer (Lab 3) |
| `feature/lab3-ui-layer` | All JavaFX UI screens (Lab 3) |
| `feature/project-docs` | README and database scripts |

**To resolve a merge conflict:**
1. `git fetch origin`
2. `git merge origin/master` — conflicting sections appear as `<<<<<<< HEAD ... >>>>>>> origin/master`
3. Edit the file to keep the correct version
4. `git add <file> && git commit`

---

## License

MIT License — © 2025 Igire Rwanda Organization
