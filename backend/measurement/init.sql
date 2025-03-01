CREATE SCHEMA IF NOT EXISTS app;

SET search_path TO app, public;

CREATE TABLE IF NOT EXISTS itens (
    id       SERIAL PRIMARY KEY,
    name VARCHAR(50),
    unit VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    id SERIAL PRIMARY KEY,
    role VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL,
    UNIQUE(role, action)
);

CREATE TABLE IF NOT EXISTS kafka_status (
    id SERIAL PRIMARY KEY,
    permissions_loaded BOOLEAN NOT NULL DEFAULT FALSE,
    sync_in_progress BOOLEAN NOT NULL DEFAULT FALSE
);
