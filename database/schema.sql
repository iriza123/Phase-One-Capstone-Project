-- =====================================================================
-- IgirePay PostgreSQL Database Schema
-- Run this once to initialize the database from scratch
-- =====================================================================

-- Create the database (run as superuser, separate from this script)
-- CREATE DATABASE igirepay_db;
-- \c igirepay_db

-- Drop tables in dependency order if re-initializing
DROP TABLE IF EXISTS processed_requests CASCADE;
DROP TABLE IF EXISTS transactions        CASCADE;
DROP TABLE IF EXISTS accounts            CASCADE;
DROP TABLE IF EXISTS customers           CASCADE;

-- ── customers ─────────────────────────────────────────────────────────
CREATE TABLE customers (
    customer_id     SERIAL        PRIMARY KEY,
    first_name      VARCHAR(100)  NOT NULL,
    last_name       VARCHAR(100)  NOT NULL,
    email           VARCHAR(150)  UNIQUE NOT NULL,
    phone           VARCHAR(20)   UNIQUE NOT NULL,
    pin             VARCHAR(255)  NOT NULL,
    role            VARCHAR(20)   NOT NULL DEFAULT 'USER',
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    failed_attempts INT           NOT NULL DEFAULT 0,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ── accounts ──────────────────────────────────────────────────────────
CREATE TABLE accounts (
    account_id      SERIAL         PRIMARY KEY,
    account_number  VARCHAR(20)    UNIQUE NOT NULL,
    customer_id     INT            NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    balance         NUMERIC(15,2)  NOT NULL DEFAULT 0.00,
    account_type    VARCHAR(20)    NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ── transactions ──────────────────────────────────────────────────────
CREATE TABLE transactions (
    transaction_id   SERIAL         PRIMARY KEY,
    reference_id     VARCHAR(50)    UNIQUE NOT NULL,
    from_account_id  INT            REFERENCES accounts(account_id),
    to_account_id    INT            REFERENCES accounts(account_id),
    transaction_type VARCHAR(20)    NOT NULL,
    amount           NUMERIC(15,2)  NOT NULL,
    fee              NUMERIC(15,2)  NOT NULL DEFAULT 0.00,
    status           VARCHAR(20)    NOT NULL DEFAULT 'SUCCESS',
    description      TEXT,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ── processed_requests (idempotency guard) ────────────────────────────
CREATE TABLE processed_requests (
    id            SERIAL      PRIMARY KEY,
    reference_id  VARCHAR(50) UNIQUE NOT NULL,
    status        VARCHAR(20) NOT NULL,
    processed_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ── Indexes ───────────────────────────────────────────────────────────
CREATE INDEX idx_acc_cust   ON accounts(customer_id);
CREATE INDEX idx_acc_num    ON accounts(account_number);
CREATE INDEX idx_tx_from    ON transactions(from_account_id);
CREATE INDEX idx_tx_to      ON transactions(to_account_id);
CREATE INDEX idx_tx_date    ON transactions(created_at);
CREATE INDEX idx_tx_ref     ON transactions(reference_id);
CREATE INDEX idx_pr_ref     ON processed_requests(reference_id);
CREATE INDEX idx_cust_phone ON customers(phone);
CREATE INDEX idx_cust_email ON customers(email);

-- =====================================================================
-- To create an admin account:
--   1. Register through the app normally (creates a USER)
--   2. Run: UPDATE customers SET role = 'ADMIN' WHERE phone = 'your_phone';
-- =====================================================================
