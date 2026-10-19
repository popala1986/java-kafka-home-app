-- 1. Tabela rodzin
CREATE TABLE households (
    id         UUID PRIMARY KEY,
    name       VARCHAR(100)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- 2. Kolumna z rodziną w users (na razie może być pusta)
ALTER TABLE users ADD COLUMN household_id UUID;

-- 3. Użytkownicy zarejestrowani przed tą migracją trafiają do jednej "Rodziny startowej"
INSERT INTO households (id, name)
SELECT gen_random_uuid(), 'Rodzina startowa'
WHERE EXISTS (SELECT 1 FROM users);

UPDATE users
SET household_id = (SELECT id FROM households LIMIT 1)
WHERE household_id IS NULL;

-- 4. Od teraz każdy użytkownik MUSI należeć do rodziny
ALTER TABLE users ALTER COLUMN household_id SET NOT NULL;
ALTER TABLE users
    ADD CONSTRAINT fk_users_household FOREIGN KEY (household_id) REFERENCES households (id);
CREATE INDEX idx_users_household_id ON users (household_id);

-- 5. Domownik bez konta (np. małe dziecko) nie ma emaila, hasła ani telefonu
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;
ALTER TABLE users ALTER COLUMN phone_number DROP NOT NULL;
