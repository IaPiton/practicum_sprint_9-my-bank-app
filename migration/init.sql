CREATE SCHEMA IF NOT EXISTS bank_account;
CREATE SCHEMA IF NOT EXISTS keycloack;

CREATE TABLE IF NOT EXISTS bank_account.bank_user (
    id UUID PRIMARY KEY,
    username varchar(255) NOT NULL UNIQUE,
    password text NOT NULL,
    birthday date,
    full_name varchar(255),
    email varchar(255),
    roles varchar[]
);