# Wordle+

A full, working Wordle game — plus three game modes the original doesn't have. Built with a Spring Boot backend with JWT authentication, bcrypt password hashing, and a relational database (H2 for development, Supabase PostgreSQL for production).

## What it does

- **Normal** – classic daily word
- **60 Seconds** – solve before the timer runs out
- **Sudden Death** – one wrong guess ends the round
- **Custom word packs** – admin-created themed word sets

The game is fully server-authoritative: word selection, guess judging, attempt limits, and stats are all handled by the backend, so the answer and remaining attempts can't be read or manipulated from the browser.

## Features

- **Accounts & security**
  - Register/login with bcrypt-hashed passwords and stateless JWT tokens
  - No hardcoded credentials or secrets in the repository — everything is environment-driven
  - Admins flagged via `ADMIN_USERNAME` at startup (no unsafe role flag)
- **Persistence**
  - Per-user statistics (played, wins, win rate, streaks, guess distribution)
  - Game history logging
  - Global leaderboard (win rate / best streak / average guesses)
- **Hints**
  - Daily hints per mode
  - "Reveal word" endpoint protected by authentication **and** a per-user daily rate limit
- **Admin panel** (`/admin`)
  - Overview, user list, and create/edit/delete of custom word packs

## Tech stack

| Layer | Tools |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring Security, REST API |
| Database | H2 (dev) / Supabase PostgreSQL (prod, via JDBC) |
| Auth | JWT (jjwt), bcrypt (Spring Security) |
| Frontend | HTML, CSS, vanilla JavaScript |
| Deployment | Docker, Render |

## Running locally (development)

Requires Java 17+ and Maven.

```bash
cd wordle
./mvnw spring-boot:run
```

The default `dev` profile uses an in-memory H2 database (no setup needed) and serves the app at `http://localhost:8081`.

The first registered user is a normal user. To get an admin account, relaunch the app with an `ADMIN_USERNAME` before registering that user:

```bash
ADMIN_USERNAME=admin ./mvnw spring-boot:run
```

## Running in production (Supabase)

1. Create a Supabase project and grab the **database connection string** (use the "Session pooler" `/ Session mode" JDBC URL), plus the database password.
2. Run the app with the `prod` profile and required environment variables:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://db.<ref>.supabase.co:5432/postgres?sslmode=require
export DB_USERNAME=postgres
export DB_PASSWORD=<database password>
export JWT_SECRET=<long random string, at least 32 chars>
export ADMIN_USERNAME=admin
export ALLOWED_ORIGINS=http://localhost:8081
export REVEAL_DAILY_LIMIT=5
export PORT=8080
./mvnw spring-boot:run
```

Tables are created automatically on first boot (Hibernate `ddl-auto=update`), and the word list is loaded from `words.txt` on startup.

## Configuration reference

| Env var | Default | Purpose |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` uses H2; `prod` uses Supabase |
| `DB_URL` | — (prod) | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | — (prod) | Database credentials |
| `JWT_SECRET` | random (dev) | HMAC secret for signing JWTs; **must** be set in prod |
| `ADMIN_USERNAME` | `admin` | Username of the admin account created at startup |
| `ALLOWED_ORIGINS` | `http://localhost:8080,http://localhost:8081` | Comma-separated CORS origins |
| `REVEAL_DAILY_LIMIT` | `5` | Daily "reveal word" limit per user |
| `PORT` | `8080` | Server port |

## API overview

| Endpoint | Auth | Description |
|---|---|---|
| `POST /api/auth/register`, `POST /api/auth/login` | — | Register / login, returns a JWT |
| `GET /api/auth/me` | ✓ | Current user |
| `GET /api/games/status` | ✓ | Today's played modes + active game |
| `POST /api/games/start` | ✓ | Start/resume a game (`mode` = `normal`/`time`/`sudden`/`pack`, plus `packId`) |
| `POST /api/games/{id}/guess` | ✓ | Submit a guess (server judges, records stats/history) |
| `POST /api/games/{id}/forfeit` | ✓ | End a game (e.g. time-up) |
| `GET /api/games/recent` | ✓ | Game history |
| `GET /api/hint?mode=` | ✓ | Hint for a mode |
| `GET /api/validate-word?word=` | ✓ | Dictionary check |
| `GET /api/reveal-word?mode=` | ✓ | Word + definition/trivia/example (rate-limited) |
| `GET /api/stats/me` | ✓ | Your statistics |
| `GET /api/stats/leaderboard?sortBy=` | ✓ | Global leaderboard |
| `GET /api/packs` | ✓ | Available custom packs |
| `/api/admin/overview`, `/api/admin/users`, `/api/admin/packs` | ADMIN | Admin panel endpoints |

## Author

Aditya Agrawal — B.Tech IT, Medi-Caps University