-- Obowiązki konkretnej rodziny. Każda rodzina ma swoją listę i admin może ją dowolnie zmieniać.
CREATE TABLE chores (
    id               UUID PRIMARY KEY,
    household_id     UUID                     NOT NULL,
    code             VARCHAR(10),
    name             VARCHAR(200)             NOT NULL,
    type             VARCHAR(20)              NOT NULL,
    frequency        VARCHAR(30)              NOT NULL,
    min_age          INT                      NOT NULL,
    duration_minutes INT                      NOT NULL,
    points           INT                      NOT NULL,
    active           BOOLEAN                  NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_chores_household_id ON chores (household_id);

-- Rodziny, które już mają domowników w plannerze, dostają kopię wzoru od razu.
-- Nowe rodziny dostaną ją z kodu, gdy przyjdzie event user.registered.
INSERT INTO chores (id, household_id, code, name, type, frequency, min_age, duration_minutes, points, active, created_at)
SELECT gen_random_uuid(), h.household_id, t.code, t.name, t.type, t.frequency,
       t.min_age, t.duration_minutes, t.points, TRUE, now()
FROM (SELECT DISTINCT household_id FROM household_members) h
CROSS JOIN chore_templates t;
