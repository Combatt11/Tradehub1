# TradeHub — Setup & DevOps Guide

TradeHub runs against **H2 in-memory** for local development and **MySQL 8** in production.
Spring Boot profiles select the database, and Docker Compose brings up the full stack
(MySQL + the application) with a single command.

## Prerequisites

| Tool | Version | Needed for |
|------|---------|------------|
| Docker Engine / Docker Desktop | 24+ with Compose v2 | Full stack (`docker compose up`) |
| Java | 17+ | Running the app or tests locally |
| Maven | 3.8+ | Building locally (or use `./mvnw`) |

## Quick start with Docker Compose

1. Create your local environment file and set real credentials:

   ```bash
   cp .env.example .env
   ```

   At minimum change `DB_PASSWORD`, `MYSQL_ROOT_PASSWORD` and `JWT_SECRET`.

2. Build the image and start MySQL + the application:

   ```bash
   docker compose up --build
   ```

   MySQL first passes its healthcheck, then the `app` container starts with
   `SPRING_PROFILES_ACTIVE=prod`. The API is available at
   <http://localhost:8080> and the health endpoint at
   <http://localhost:8080/actuator/health>.

3. Stop the stack:

   ```bash
   docker compose down          # keeps the MySQL data volume
   docker compose down -v       # also deletes the data volume
   ```

### What Compose creates

| Resource | Name | Notes |
|----------|------|-------|
| MySQL container | `tradehub-mysql` | `mysql:8.0`, database `${DB_NAME}` |
| App container | `tradehub-app` | Built from the multi-stage `Dockerfile` |
| Named volume | `tradehub-mysql-data` | Persists data between restarts |
| Network | `tradehub_default` | App reaches MySQL at host `mysql:3306` |

The application waits for MySQL's healthcheck (`mysqladmin ping`) before starting,
so `docker compose up` does not race the database on a cold boot.

### Useful commands

```bash
docker compose logs -f app        # follow application logs
docker compose logs -f mysql      # follow database logs
docker compose ps                 # service status and health
docker compose restart app        # restart only the application
docker compose up -d --build app  # rebuild and recreate the app only
docker compose exec mysql mysql -u root -p   # open a MySQL shell
```

## Spring profiles

Base settings live in `application.properties`, which activates the `dev` profile by
default. Override it with `SPRING_PROFILES_ACTIVE` (or `--spring.profiles.active=`).

| Profile | File | Database | H2 console |
|---------|------|----------|------------|
| `dev` (default) | `application-dev.properties` | H2 in-memory, MySQL compatibility mode | `/h2-console` |
| `prod` | `application-prod.properties` | MySQL 8 via environment variables | disabled |

### Production variables (`prod` profile)

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | JDBC URL, including options | `jdbc:mysql://mysql:3306/tradehub?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USER` | Database user | `tradehub` |
| `DB_PASSWORD` | Database password | *(secret)* |
| `JWT_SECRET` | HS256 signing key, ≥ 32 chars | *(secret)* |
| `DB_POOL_SIZE` | Hikari maximum pool size (optional) | `10` |
| `DB_POOL_MIN_IDLE` | Hikari minimum idle connections (optional) | `2` |
| `JPA_DDL_AUTO` | Hibernate schema mode (optional) | `update` |

No credentials are committed: `application-prod.properties` only contains
`${DB_URL}`, `${DB_USER}` and `${DB_PASSWORD}` placeholders.

## Running locally without Docker

### H2 (fast iteration)

```bash
./mvnw spring-boot:run
```

The `dev` profile is active by default, so no database is required. Seeded users
are recreated on every start (see `README.md`).

### Against a MySQL container

Start only the database:

```bash
docker compose up -d mysql
```

Then run the application with the `prod` profile (PowerShell syntax shown,
use `export` on macOS/Linux):

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:mysql://localhost:3306/tradehub?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER="tradehub"
$env:DB_PASSWORD="tradehub"
./mvnw spring-boot:run
```

## Building the image manually

```bash
docker build -t tradehub .
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/tradehub?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e DB_USER=tradehub \
  -e DB_PASSWORD=tradehub \
  -e JWT_SECRET="your-32-char-secret" \
  tradehub
```

The `Dockerfile` is multi-stage: Maven + JDK 17 build the jar, and only the jar
plus a slim JRE 17 runtime is shipped in the final image (running as a
non-root user).

## Troubleshooting

- **Port 8080 or 3306 already in use** — set `APP_PORT` / `MYSQL_PORT` in `.env`.
- **App container restarts repeatedly** — check `docker compose logs app`; the
  `prod` profile requires `DB_URL`, `DB_USER` and `DB_PASSWORD`.
- **Credentials changed but MySQL rejects them** — the data volume keeps the
  initial credentials. Run `docker compose down -v` and start again.
- **First build is slow** — the Maven base image downloads dependencies once;
  subsequent builds reuse the Docker layer cache unless `pom.xml` changes.
- **`docker compose` command not found** — install Compose v2 (`docker compose
  version`); the legacy `docker-compose` binary is not required.
