CREATE SCHEMA IF NOT EXISTS app;

SET search_path TO app, public;

CREATE TABLE IF NOT EXISTS users (

    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    secret_phrase VARCHAR(255) NOT NULL CHECK (LENGTH(secret_phrase) >= 6)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    id SERIAL PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(role, action)
    );

INSERT INTO role_permissions(role, action, active) VALUES ('ADMIN', 'CREATE_USER', true);
INSERT INTO role_permissions(role, action, active) VALUES ('ADMIN', 'READ_USER', true);
INSERT INTO users(name, email, password, role, secret_phrase) VALUES ('Admin', 'admin@email.com', '$2a$12$OE54CUDpJLnvX7HSX63b6.yyW6V9rmEBhYJTUS70bt5TmIN5RLbom', 'ADMIN', '$2a$12$SUFMVPTI/Gyfa/.VKJeH..6SgtmHHnzNWa36JlD3qT7Twj8IqyTQu');
