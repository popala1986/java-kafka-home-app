-- Tabela użytkowników aplikacji.
-- Flyway wykonuje ten plik raz i zapisuje to w tabeli flyway_schema_history.

CREATE TABLE users (
    id            UUID         PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    birth_date    DATE         NOT NULL,
    phone_number  VARCHAR(20)  NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
