# java-kafka-home-app

Domowa aplikacja do planowania obowiązków dla rodziny (multi-user).
Projekt edukacyjny: Java 21, Spring Boot, Apache Kafka, Docker, GitHub Actions.

## Architektura

| Komponent | Port | Odpowiedzialność |
|---|---|---|
| user-auth-service | 8081 | rejestracja, logowanie (JWT), profile, role |
| chore-planner-service | 8082 | obowiązki wg wieku, plan tygodniowy, kalendarz, panel admina |
| notification-service | 8083 | poranne powiadomienia |
| frontend | 5173 | aplikacja webowa na telefon (React, PWA): logowanie, kalendarz, panel admina |

Komunikacja między serwisami: asynchronicznie przez Kafkę.

## Gałęzie

- `main` – wersje gotowe do wdrożenia
- `develop` – bieżąca integracja, cel wszystkich PR
- `feature/<nr>-<opis>` – jedna gałąź na jedno zadanie

## Konwencja commitów (Conventional Commits)

`<typ>(<zakres>): <opis>`, np. `feat(auth): add user registration endpoint`

Typy: `feat`, `fix`, `chore`, `docs`, `test`, `refactor`, `ci`, `build`.

## Wymagania

- JDK 21
- Docker Desktop
- Git


## Uruchomienie lokalne

1. Skopiuj `.env.example` jako `.env` i ustaw hasło do bazy.
2. Uruchom infrastrukturę: `docker compose up -d`
3. Kafka UI: http://localhost:8080
4. Kafka dla aplikacji: `localhost:9092`, PostgreSQL: `localhost:5432` (bazy: `auth_db`, `planner_db`, `notification_db`)
5. Zatrzymanie: `docker compose down` (z usunięciem danych: `docker compose down -v`)
