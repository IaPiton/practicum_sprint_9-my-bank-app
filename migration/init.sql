CREATE SCHEMA IF NOT EXISTS bank_account;
CREATE SCHEMA IF NOT EXISTS keycloak;

CREATE TABLE IF NOT EXISTS bank_account.bank_user (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    birthday DATE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS bank_account.bank_account (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    balance BIGINT DEFAULT 0 NOT NULL,
    account_number VARCHAR(50) UNIQUE,
    currency VARCHAR(3) DEFAULT 'RUB',
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_bank_account_user FOREIGN KEY (user_id)
        REFERENCES bank_account.bank_user(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_bank_user_keycloak_id ON bank_account.bank_user(keycloak_id);
CREATE INDEX IF NOT EXISTS idx_bank_user_username ON bank_account.bank_user(username);
CREATE INDEX IF NOT EXISTS idx_bank_user_email ON bank_account.bank_user(email);
CREATE INDEX IF NOT EXISTS idx_bank_account_user_id ON bank_account.bank_account(user_id);
CREATE INDEX IF NOT EXISTS idx_bank_account_account_number ON bank_account.bank_account(account_number);
CREATE INDEX IF NOT EXISTS idx_bank_account_is_active ON bank_account.bank_account(is_active);