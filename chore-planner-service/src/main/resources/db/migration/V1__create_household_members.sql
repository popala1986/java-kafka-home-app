-- Kopia domowników na potrzeby plannera, budowana z eventów user.registered.
-- Planner trzyma tylko to, czego potrzebuje do układania obowiązków (bez emaila, hasła, telefonu).
CREATE TABLE household_members (
    user_id       UUID PRIMARY KEY,
    household_id  UUID                     NOT NULL,
    first_name    VARCHAR(100)             NOT NULL,
    birth_date    DATE                     NOT NULL,
    role          VARCHAR(20)              NOT NULL,
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_household_members_household_id ON household_members (household_id);
