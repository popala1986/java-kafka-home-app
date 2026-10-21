-- Kopia kontaktów do domowników, budowana z eventów user.registered i user.updated.
-- Dziecko bez konta nie ma emaila ani telefonu, więc obie kolumny mogą być puste.
CREATE TABLE contacts (
    user_id      UUID PRIMARY KEY,
    household_id UUID                     NOT NULL,
    first_name   VARCHAR(100)             NOT NULL,
    email        VARCHAR(255),
    phone_number VARCHAR(20),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Historia wysyłek: jeden wiersz = jedna wiadomość jednym kanałem (EMAIL albo SMS).
CREATE TABLE notifications (
    id           UUID PRIMARY KEY,
    user_id      UUID                     NOT NULL,
    household_id UUID                     NOT NULL,
    digest_date  DATE                     NOT NULL,
    channel      VARCHAR(10)              NOT NULL,
    recipient    VARCHAR(255)             NOT NULL,
    status       VARCHAR(10)              NOT NULL,
    message      TEXT                     NOT NULL,
    error        TEXT,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    -- Ta sama osoba, ten sam dzień, ten sam kanał = tylko raz. Chroni przed podwójnym SMS-em.
    CONSTRAINT uq_notifications_once UNIQUE (user_id, digest_date, channel)
);

CREATE INDEX idx_notifications_household ON notifications (household_id, created_at);
