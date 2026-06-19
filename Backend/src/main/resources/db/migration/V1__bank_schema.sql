-- Flyway migration for production-ready banking schema
-- Users table holds application users and login credentials
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    account_id VARCHAR(64),
    display_name VARCHAR(150),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE accounts (
    id VARCHAR(64) PRIMARY KEY,
    account_number VARCHAR(64) NOT NULL UNIQUE,
    account_type VARCHAR(50) NOT NULL,
    holder_name VARCHAR(50) NOT NULL,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    user_id BIGINT NOT NULL,
    version INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_accounts_user ON accounts(user_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);

CREATE TABLE transaction_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_number VARCHAR(100) NOT NULL UNIQUE,
    from_account_id VARCHAR(64) NOT NULL,
    to_account_id VARCHAR(64) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    failure_reason VARCHAR(512),
    idempotency_key VARCHAR(128) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_from_account FOREIGN KEY (from_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_transaction_to_account FOREIGN KEY (to_account_id) REFERENCES accounts(id)
);
CREATE INDEX idx_transaction_from_account ON transaction_logs(from_account_id);
CREATE INDEX idx_transaction_to_account ON transaction_logs(to_account_id);
CREATE INDEX idx_transaction_status ON transaction_logs(status);
CREATE INDEX idx_transaction_created_at ON transaction_logs(created_at);

CREATE TABLE beneficiaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_account_id VARCHAR(64) NOT NULL,
    beneficiary_name VARCHAR(150) NOT NULL,
    beneficiary_account_number VARCHAR(64) NOT NULL,
    bank_name VARCHAR(150),
    ifsc VARCHAR(20),
    nickname VARCHAR(100),
    favorite BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_beneficiary_owner_account FOREIGN KEY (owner_account_id) REFERENCES accounts(id),
    UNIQUE(owner_account_id, beneficiary_account_number)
);
CREATE INDEX idx_beneficiaries_owner ON beneficiaries(owner_account_id);

CREATE TABLE reward_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    current_points INT NOT NULL DEFAULT 0,
    lifetime_points INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reward_account_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE reward_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reward_account_id BIGINT NOT NULL,
    banking_transaction_id BIGINT NOT NULL UNIQUE,
    points_earned INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reward_transaction_account FOREIGN KEY (reward_account_id) REFERENCES reward_accounts(id),
    CONSTRAINT fk_reward_transaction_banking FOREIGN KEY (banking_transaction_id) REFERENCES transaction_logs(id)
);
CREATE INDEX idx_reward_transactions_account ON reward_transactions(reward_account_id);

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_notifications_user_is_read ON notifications(user_id, is_read);

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(150) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_log_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
