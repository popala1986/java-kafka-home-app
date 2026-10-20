-- Wykluczenia: "ta osoba nie robi tego obowiązku" (np. alergia na kota, brak prawa jazdy).
CREATE TABLE chore_exclusions (
    id       UUID PRIMARY KEY,
    chore_id UUID NOT NULL REFERENCES chores (id) ON DELETE CASCADE,
    user_id  UUID NOT NULL REFERENCES household_members (user_id) ON DELETE CASCADE,
    CONSTRAINT uq_chore_exclusions UNIQUE (chore_id, user_id)
);

-- Plan tygodniowy: jeden wiersz = jeden obowiązek dla jednej osoby w jeden dzień.
CREATE TABLE chore_assignments (
    id            UUID PRIMARY KEY,
    household_id  UUID                     NOT NULL,
    week_start    DATE                     NOT NULL,
    assigned_date DATE                     NOT NULL,
    chore_id      UUID                     NOT NULL REFERENCES chores (id) ON DELETE CASCADE,
    user_id       UUID                     NOT NULL REFERENCES household_members (user_id) ON DELETE CASCADE,
    manual        BOOLEAN                  NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_chore_assignments_household_week ON chore_assignments (household_id, week_start);
