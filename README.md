# TradeHub

![CI](https://github.com/juanestevan-tdea/tradehub/actions/workflows/ci.yml/badge.svg)

## 📊 Project Status & Metrics
- **Architecture:** Hexagonal (Ports & Adapters) with strict SOLID compliance.
- **Test Suite:** 182 unit and integration tests passing.
- **Test Coverage:** 90.95% line coverage (enforced gate at 60%).
- **CI/CD:** GitHub Actions pipeline running on every push.
- **DevOps:** Fully dockerized (App + MySQL 8) with multi-stage builds.

Marketplace platform built with **Spring Boot 3**, **Hexagonal Architecture** (Ports & Adapters), JWT security and Docker.

This project follows the same structure and conventions as the professor's *bank* project, adapted to a marketplace functional specification.

## Architecture

```
application/
├── domain/
│   ├── models/          # Domain entities (User, Buyer, Seller, Product, Order, ...)
│   ├── ports/
│   │   ├── in/          # Input ports: use-case interfaces (driving)
│   │   └── out/         # Output ports: repository interfaces (driven)
│   ├── services/        # Use-case implementations
│   └── exceptions/
├── adapters/
│   ├── persistence/jpa/ # JPA adapters + entities + mappers + Spring Data repos
│   ├── rest/            # REST controllers + DTOs
│   └── security/        # JWT + Spring Security
└── infrastructure/
    └── seed/            # Database seeder
```

## Software Design Document (SDD)

The complete design documentation lives in the [`SDD/`](SDD/) folder:

| Document | Content |
|----------|---------|
| [00 - Introduction](SDD/00-Introduccion.md) | Purpose, scope and glossary |
| [01 - Requirements](SDD/01-Requisitos.md) | Functional and non-functional requirements |
| [02 - Architecture](SDD/02-Arquitectura.md) | Hexagonal architecture and Mermaid component diagram |
| [03 - Domain](SDD/03-Dominio.md) | Entities, invariants, state machines and exceptions |
| [04 - API](SDD/04-API.md) | Endpoints, roles, examples and error contract |
| [05 - SOLID](SDD/05-SOLID.md) | SRP, OCP, LSP, ISP and DIP with real code |

## Domains covered

| Domain | Status |
|--------|--------|
| User administration | ✅ |
| Buyer management | ✅ |
| Seller management (admin-only registration) | ✅ |
| Warehouse management | ✅ |
| Product catalog (Physical / Digital) | ✅ |
| Distributed inventory (no negative stock) | ✅ |
| Shopping cart | ✅ |
| Order lifecycle (CART → PENDING_PAYMENT → PAID → PREPARING → DISPATCHED → DELIVERED → FINALIZED, plus CANCELLED) | ✅ |
| Logistics operations | ✅ |

## Quick start

### Local (H2 in-memory, `dev` profile)

```bash
mvn spring-boot:run
```

App runs at `http://localhost:8080`

### Docker Compose (app + MySQL 8, `prod` profile)

```bash
cp .env.example .env   # set DB_PASSWORD, MYSQL_ROOT_PASSWORD and JWT_SECRET
docker compose up --build
```

App runs at `http://localhost:8080`, backed by MySQL 8 with a persistent volume.
See [SETUP.md](SETUP.md) for profiles, environment variables and troubleshooting.

## Development Setup

### Prerequisites

- **Java 17** (the build enforces this via `maven-enforcer-plugin`)
- **Maven 3.8+**, or use the bundled Maven Wrapper (`./mvnw`) — no local Maven needed

### After cloning the repository

> **Windows users:** set the line-ending policy to `false` (or `input`) before building.
> With `core.autocrlf=true` git rewrites the LF endings stored in the index to CRLF in the
> working tree, and Spotless (`mvn verify`) then fails reporting CRLF lines on files that
> `git status` considers clean.

```bash
# Disable automatic CRLF conversion (local repo config, not committed)
git config core.autocrlf false
```

Line endings are enforced by `.gitattributes` (`* text=auto eol=lf`, `*.cmd text eol=crlf`).

### Build and verify

```bash
./mvnw clean verify
```

This runs, in order:

1. `maven-enforcer-plugin` — fails fast unless Java ≥ 17 and Maven ≥ 3.8
2. `maven-surefire-plugin` — unit and MVC slice tests
3. `jacoco-maven-plugin` — coverage report in `target/site/jacoco/index.html` and a **60 % line-coverage gate**
4. `spotless-maven-plugin` — formatting check (Google Java Format)

To auto-fix formatting before committing:

```bash
./mvnw spotless:apply
```

### Local secrets

`jwt.secret` is read from the `JWT_SECRET` environment variable (with a development
fallback). Copy the example file and set your own value; `.env` is git-ignored.

```bash
cp .env.example .env
```

## Seeded users

| Role | Email | Password |
|------|-------|----------|
| Administrator | admin@tradehub.com | admin123 |
| Buyer | buyer@example.com | buyer123 |
| Seller | seller@example.com | seller123 |
| Logistics Operator | logistics@example.com | logistics123 |

## Main API endpoints

### Public
- `POST /api/public/register/buyer` – Register a new buyer
- `POST /api/public/login` – Login (returns JWT)
- `GET  /api/catalog/products` – Browse published products

### Admin (`ROLE_ADMINISTRATOR`)
- `POST /api/admin/sellers` – Register seller
- `POST /api/admin/sellers/{id}/approve` – Approve seller
- `POST /api/admin/warehouses/marketplace` – Create marketplace warehouse
- `POST /api/admin/warehouses/seller` – Create seller warehouse

### Seller (`ROLE_SELLER`)
- `POST /api/seller/products` – Create product
- `POST /api/seller/products/{id}/publish` – Publish product
- `POST /api/seller/inventory` – Add stock
- `GET  /api/seller/products` – List my products

### Buyer (`ROLE_BUYER`)
- `GET  /api/buyer/cart` – Get/create cart
- `POST /api/buyer/cart/items` – Add item to cart
- `DELETE /api/buyer/cart` – Clear cart
- `POST /api/buyer/cart/submit` – Submit order
- `POST /api/buyer/orders/{id}/pay` — Confirm payment
- `POST /api/buyer/orders/{id}/cancel` — Cancel an order
- `GET  /api/buyer/orders` — My orders

### Logistics (`ROLE_LOGISTICS_OPERATOR`)
- `POST /api/logistics/orders/{id}/prepare`
- `POST /api/logistics/orders/{id}/dispatch`
- `POST /api/logistics/orders/{id}/deliver`

## Business rules enforced

- Every operation requires an authenticated user (JWT)
- Each user has exactly one role
- Participants can only manage information belonging to their role
- No negative inventory under any circumstance
- Finalized orders cannot be modified
- Sellers cannot self-register (only Administrator)

## Tech stack

- Java 17
- Spring Boot 3.3
- Spring Data JPA + H2 (dev profile) / MySQL 8 (prod profile)
- Spring Security + JWT (jjwt 0.12)
- Lombok
- Maven
- Docker + Docker Compose (multi-stage build, app + MySQL 8)

## Project ready for GitHub

```bash
git init
git add .
git commit -m "Initial TradeHub project - hexagonal architecture marketplace"
git remote add origin <your-repo-url>
git push -u origin main
```
