# TradeHub

Marketplace platform built with **Spring Boot 3**, **Hexagonal Architecture** (Ports & Adapters), JWT security and Docker.

This project follows the same structure and conventions as the professor's *bank* project, adapted to a marketplace functional specification.

## Architecture

```
application/
├── domain/
│   ├── models/          # Domain entities (User, Buyer, Seller, Product, Order, ...)
│   ├── ports/
│   │   ├── in/          # Use-case interfaces (driven ports)
│   │   └── out/         # Repository interfaces (driving ports)
│   ├── services/        # Use-case implementations
│   └── exceptions/
├── adapters/
│   ├── persistence/jpa/ # JPA adapters + entities + mappers + Spring Data repos
│   ├── rest/            # REST controllers + DTOs
│   └── security/        # JWT + Spring Security
└── infrastructure/
    └── seed/            # Database seeder
```

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
| Order lifecycle (CART → PENDING_PAYMENT → PAID → DISPATCHED → DELIVERED → FINALIZED) | ✅ |
| Logistics operations | ✅ |

## Quick start

### Local (H2 in-memory)

```bash
mvn spring-boot:run
```

App runs at `http://localhost:8080`

### Docker

```bash
docker build -t tradehub .
docker run -p 8080:8080 tradehub
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
- `POST /api/buyer/cart/submit` – Submit order
- `POST /api/buyer/orders/{id}/pay` – Confirm payment
- `GET  /api/buyer/orders` – My orders

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
- Spring Data JPA + H2 (dev) / MySQL ready
- Spring Security + JWT (jjwt 0.12)
- Lombok
- Maven
- Docker

## Project ready for GitHub

```bash
git init
git add .
git commit -m "Initial TradeHub project - hexagonal architecture marketplace"
git remote add origin <your-repo-url>
git push -u origin main
```
