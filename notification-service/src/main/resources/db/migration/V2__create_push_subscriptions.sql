-- Urządzenia, które zgodziły się na powiadomienia push. Jedna osoba może mieć kilka (telefon, laptop).
-- Bez klucza obcego do contacts: push działa także dla osoby, której kontakt nie dotarł tu z Kafki.
-- Sprzątanie po usuniętej osobie robi ContactListener (event user.deleted).
CREATE TABLE push_subscriptions (
    id         UUID PRIMARY KEY,
    user_id    UUID                     NOT NULL,
    -- Adres urządzenia u dostawcy push (Google, Apple, Mozilla). Każda przeglądarka ma swój.
    endpoint   VARCHAR(1000)            NOT NULL UNIQUE,
    -- Klucze, którymi szyfrujemy treść. Odczyta ją tylko ta jedna przeglądarka.
    p256dh     VARCHAR(255)             NOT NULL,
    auth       VARCHAR(255)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_push_subscriptions_user ON push_subscriptions (user_id);
