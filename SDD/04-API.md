# 04 - API

## 1. Convenciones

| Elemento | Valor |
|----------|-------|
| URL base local | `http://localhost:8080` |
| Formato | JSON (`application/json`) |
| Autenticación | `Authorization: Bearer <JWT>` |
| Codificación | UTF-8 |
| Fechas | ISO-8601 (`Instant.toString()`) |
| Dinero | `BigDecimal` serializado como número decimal |

### Formato de error

Todas las respuestas de error tienen la misma forma:

```json
{
  "error": "Mensaje legible para el cliente"
}
```

### Códigos de estado usados

| Código | Significado |
|--------|-------------|
| 200 | Operación correcta. |
| 201 | Recurso creado. |
| 400 | Regla de negocio incumplida, validación de entrada o cuerpo malformado. |
| 401 | Token ausente, inválido o credenciales incorrectas. |
| 403 | Usuario autenticado sin permiso o recurso ajeno. |
| 404 | Recurso inexistente. |
| 409 | Conflicto (email duplicado). |
| 500 | Error inesperado no controlado. |

## 2. Resumen de endpoints

| Método | Ruta | Rol | Caso de uso | Éxito |
|--------|------|-----|-------------|-------|
| POST | `/api/public/register/buyer` | Público | `RegisterBuyerUseCase` | 201 |
| POST | `/api/public/login` | Público | `LoginUseCase` | 200 |
| GET | `/api/catalog/products` | Público | `BrowseCatalogUseCase` | 200 |
| GET | `/api/catalog/products/{id}` | Público | `GetProductUseCase` | 200 / 404 |
| POST | `/api/admin/sellers` | ADMINISTRATOR | `RegisterSellerUseCase` | 201 |
| POST | `/api/admin/sellers/{sellerId}/approve` | ADMINISTRATOR | `ApproveSellerUseCase` | 200 |
| POST | `/api/admin/warehouses/marketplace` | ADMINISTRATOR | `RegisterMarketplaceWarehouseUseCase` | 201 |
| POST | `/api/admin/warehouses/seller` | ADMINISTRATOR | `RegisterSellerWarehouseUseCase` | 201 |
| POST | `/api/seller/products` | SELLER | `CreateProductUseCase` | 201 |
| POST | `/api/seller/products/{productId}/publish` | SELLER | `PublishProductUseCase` | 200 |
| POST | `/api/seller/inventory` | SELLER | `AddInventoryUseCase` | 200 |
| GET | `/api/seller/products` | SELLER | `GetSellerProductsUseCase` | 200 |
| GET | `/api/buyer/cart` | BUYER | `GetCartUseCase` | 200 |
| POST | `/api/buyer/cart/items` | BUYER | `AddItemToCartUseCase` | 200 |
| DELETE | `/api/buyer/cart` | BUYER | `ClearCartUseCase` | 200 |
| POST | `/api/buyer/cart/submit` | BUYER | `SubmitOrderFromCartUseCase` | 200 |
| POST | `/api/buyer/orders/{orderId}/pay` | BUYER | `ConfirmPaymentUseCase` | 200 |
| POST | `/api/buyer/orders/{orderId}/cancel` | BUYER | `CancelOrderUseCase` | 200 |
| GET | `/api/buyer/orders` | BUYER | `GetBuyerOrdersUseCase` | 200 |
| POST | `/api/logistics/orders/{orderId}/prepare` | LOGISTICS_OPERATOR | `MarkOrderPreparingUseCase` | 200 |
| POST | `/api/logistics/orders/{orderId}/dispatch` | LOGISTICS_OPERATOR | `MarkOrderDispatchedUseCase` | 200 |
| POST | `/api/logistics/orders/{orderId}/deliver` | LOGISTICS_OPERATOR | `MarkOrderDeliveredUseCase` | 200 |

## 3. API pública

### 3.1 Registro de comprador

`POST /api/public/register/buyer`

Request:

```json
{
  "fullName": "Ana Torres",
  "email": "ana@example.com",
  "password": "secreto123",
  "identityDocument": "12345678",
  "primaryAddress": "Calle 10 # 20-30"
}
```

Validaciones:

- `fullName`, `email`, `password` y `primaryAddress` son obligatorios.
- `email` debe tener formato válido.
- El email no debe existir (`409`).

Response `201`:

```json
{
  "id": "d6f1...",
  "email": "ana@example.com",
  "fullName": "Ana Torres",
  "role": "BUYER"
}
```

### 3.2 Inicio de sesión

`POST /api/public/login`

Request:

```json
{
  "email": "ana@example.com",
  "password": "secreto123"
}
```

Response `200`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "d6f1...",
  "email": "ana@example.com",
  "role": "BUYER",
  "fullName": "Ana Torres"
}
```

Errores:

- `401` si las credenciales son inválidas o el usuario no está activo.

### 3.3 Catálogo

`GET /api/catalog/products`

Response `200`:

```json
[
  {
    "id": "p-1",
    "name": "Laptop",
    "description": "14 pulgadas",
    "type": "PHYSICAL",
    "price": 1200.00,
    "sku": "LAP-001",
    "sellerId": "s-1"
  }
]
```

`GET /api/catalog/products/{id}`

- `200` con el producto si está publicado.
- `404` si no existe o no está publicado.

## 4. API de administración

### 4.1 Registrar vendedor

`POST /api/admin/sellers` — rol `ADMINISTRATOR`

Request:

```json
{
  "fullName": "Carlos Ruiz",
  "email": "carlos@example.com",
  "password": "secreto123",
  "identityDocument": "87654321",
  "businessName": "Carlos Store",
  "taxId": "900123456",
  "contactPhone": "3001234567"
}
```

Response `201`:

```json
{
  "id": "s-1",
  "userId": "u-1",
  "businessName": "Carlos Store",
  "status": "PENDING_APPROVAL"
}
```

### 4.2 Aprobar vendedor

`POST /api/admin/sellers/{sellerId}/approve`

Response `200`:

```json
{
  "id": "s-1",
  "status": "ACTIVE"
}
```

Errores:

- `404` si el vendedor no existe.

### 4.3 Crear almacén del marketplace

`POST /api/admin/warehouses/marketplace`

```json
{
  "name": "Centro de distribución",
  "address": "Zona Industrial 1"
}
```

Response `201`:

```json
{
  "id": "w-1",
  "name": "Centro de distribución",
  "type": "MARKETPLACE"
}
```

### 4.4 Crear almacén de vendedor

`POST /api/admin/warehouses/seller`

```json
{
  "name": "Bodega Carlos",
  "address": "Calle 5",
  "sellerId": "s-1"
}
```

Response `201`:

```json
{
  "id": "w-2",
  "name": "Bodega Carlos",
  "sellerId": "s-1"
}
```

## 5. API de vendedor

### 5.1 Crear producto

`POST /api/seller/products` — rol `SELLER`

```json
{
  "name": "Laptop",
  "description": "14 pulgadas",
  "type": "PHYSICAL",
  "price": 1200.00,
  "sku": "LAP-001"
}
```

Response `201`:

```json
{
  "id": "p-1",
  "name": "Laptop",
  "status": "DRAFT"
}
```

Errores:

- `404` si el perfil de vendedor no existe.
- `403` si el vendedor no está aprobado (`SellerNotApprovedException`).
- `400` si el nombre, precio o tipo son inválidos.

### 5.2 Publicar producto

`POST /api/seller/products/{productId}/publish`

Response `200`:

```json
{
  "id": "p-1",
  "status": "PUBLISHED"
}
```

Errores:

- `404` si el producto no existe.
- `403` si el producto pertenece a otro vendedor.

### 5.3 Agregar inventario

`POST /api/seller/inventory` — rol `SELLER`

```json
{
  "productId": "p-1",
  "warehouseId": "w-1",
  "quantity": 50
}
```

Response `200`:

```json
{
  "id": "i-1",
  "productId": "p-1",
  "warehouseId": "w-1",
  "quantityAvailable": 50
}
```

Errores:

- `404` si producto o almacén no existen.
- `403` si el producto es de otro vendedor.
- `400` si el producto es digital o el almacén no está activo.

### 5.4 Listar mis productos

`GET /api/seller/products`

Response `200`:

```json
[
  {
    "id": "p-1",
    "name": "Laptop",
    "price": 1200.00,
    "status": "PUBLISHED",
    "type": "PHYSICAL"
  }
]
```

## 6. API de comprador

### 6.1 Obtener carrito

`GET /api/buyer/cart` — rol `BUYER`

Response `200`:

```json
{
  "id": "o-1",
  "status": "CART",
  "totalAmount": 0,
  "itemCount": 0,
  "shippingAddress": "",
  "createdAt": "2026-11-08T10:00:00Z"
}
```

Si no existe carrito activo, se crea uno.

### 6.2 Agregar item al carrito

`POST /api/buyer/cart/items`

```json
{
  "productId": "p-1",
  "quantity": 2,
  "warehouseId": "w-1"
}
```

`warehouseId` es obligatorio para productos físicos.

Response `200`: el carrito actualizado con `itemCount`, `totalAmount` y estado `CART`.

Errores:

- `404` si el comprador o el producto no existen.
- `403` si el comprador no puede comprar.
- `400` si el producto no está publicado.
- `400` si no hay stock suficiente (`InsufficientStockException`).

### 6.3 Vaciar carrito

`DELETE /api/buyer/cart`

Response `200`: carrito con `itemCount = 0` y `totalAmount = 0`.

Errores:

- `404` si no hay carrito activo.

### 6.4 Enviar carrito a pago

`POST /api/buyer/cart/submit`

```json
{
  "shippingAddress": "Calle 99 # 10-20"
}
```

Response `200`:

```json
{
  "id": "o-1",
  "status": "PENDING_PAYMENT",
  "totalAmount": 2400.00,
  "itemCount": 2,
  "shippingAddress": "Calle 99 # 10-20",
  "createdAt": "2026-11-08T10:00:00Z"
}
```

Errores:

- `404` si no hay carrito activo.
- `400` si el carrito está vacío o falta la dirección.

### 6.5 Confirmar pago

`POST /api/buyer/orders/{orderId}/pay`

```json
{
  "paymentReference": "PAY-12345"
}
```

Response `200`:

```json
{
  "id": "o-1",
  "status": "PAID",
  "totalAmount": 2400.00,
  "itemCount": 2,
  "shippingAddress": "Calle 99 # 10-20",
  "createdAt": "2026-11-08T10:00:00Z"
}
```

Errores:

- `404` si la orden no existe.
- `403` si la orden pertenece a otro comprador.
- `400` si la orden ya fue pagada o no admite pago.

### 6.6 Cancelar orden

`POST /api/buyer/orders/{orderId}/cancel`

Response `200`: la orden con estado `CANCELLED`.

```json
{
  "id": "o-1",
  "status": "CANCELLED",
  "totalAmount": 2400.00,
  "itemCount": 2,
  "shippingAddress": "Calle 99 # 10-20",
  "createdAt": "2026-11-08T10:00:00Z"
}
```

Errores:

- `404` si la orden no existe.
- `403` si la orden pertenece a otro comprador.
- `400` si la orden está despachada, entregada o finalizada.

### 6.7 Listar mis órdenes

`GET /api/buyer/orders`

Response `200`: arreglo de órdenes del comprador autenticado, con el mismo formato anterior.

## 7. API de logística

Todas requieren rol `LOGISTICS_OPERATOR`.

| Endpoint | Transición | Errores |
|----------|------------|---------|
| `POST /api/logistics/orders/{orderId}/prepare` | `PAID → PREPARING` | `404`, `400` |
| `POST /api/logistics/orders/{orderId}/dispatch` | `PAID/PREPARING → DISPATCHED` | `404`, `400` |
| `POST /api/logistics/orders/{orderId}/deliver` | `DISPATCHED → DELIVERED` | `404`, `400` |

Response típica:

```json
{
  "id": "o-1",
  "status": "PREPARING"
}
```

## 8. Mapeo global de excepciones

`GlobalExceptionHandler` centraliza la traducción:

| Excepción | HTTP | Cuándo |
|-----------|------|--------|
| `ResourceNotFoundException` y subclases | 404 | `Buyer`, `Seller`, `Product`, `Warehouse`, `Order`, `Cart`, `User` inexistente. |
| `DuplicateEmailException` | 409 | Email ya registrado. |
| `InvalidCredentialsException` | 401 | Login inválido o usuario inactivo. |
| `UnauthorizedOperationException` y subclases | 403 | Comprador suspendido, vendedor no aprobado, recurso ajeno. |
| `DomainException` (resto) | 400 | Reglas de negocio: stock, estados, cantidades, precios, tipos. |
| `MethodArgumentNotValidException` | 400 | Bean Validation sobre DTOs. |
| `HttpMessageNotReadableException` | 400 | JSON malformado o tipo incorrecto. |
| `Exception` | 500 | Error inesperado; respuesta genérica. |
| Sin token / token inválido | 401 | `AuthenticationEntryPoint` de Spring Security. |
| Rol incorrecto | 403 | `SecurityConfig`. |

### Familias de excepciones

| Familia | Subclases principales |
|---------|-----------------------|
| `ResourceNotFoundException` | `UserNotFoundException`, `BuyerNotFoundException`, `SellerNotFoundException`, `ProductNotFoundException`, `WarehouseNotFoundException`, `OrderNotFoundException`, `CartNotFoundException` |
| `UnauthorizedOperationException` | `BuyerCannotPurchaseException`, `SellerNotApprovedException`, `ProductOwnershipException`, `OrderOwnershipException` |
| `DomainException` | `InvalidQuantityException`, `InsufficientStockException`, `EmptyCartException`, `ShippingAddressRequiredException`, `InvalidProductNameException`, `InvalidProductPriceException`, `InvalidProductTypeException`, `ProductNotAvailableException`, `ProductNotPhysicalException`, `OrderAlreadyPaidException`, `OrderNotPayableException`, `OrderNotPreparableException`, `OrderNotDispatchableException`, `OrderNotDeliverableException`, `OrderNotFinalizableException`, `OrderNotCancellableException`, `OrderNotSubmittableException`, `OrderNotModifiableException` |

## 9. Ejemplo de error

```http
POST /api/buyer/cart/items
Authorization: Bearer <token>
Content-Type: application/json

{"productId":"p-1","quantity":100,"warehouseId":"w-1"}
```

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{"error":"Insufficient stock for product p-1. Requested: 100, Available: 50"}
```

```http
GET /api/buyer/cart
Authorization: Bearer <token-de-otro-rol>
```

```http
HTTP/1.1 403 Forbidden
```

## 10. Consideraciones de seguridad

- El JWT incluye `userId`, `email` y `role`; el filtro construye la autenticación con el rol
  prefijado `ROLE_`.
- El controlador nunca confía en un `buyerId` o `sellerId` enviado por el cliente: lo resuelve a
  partir del usuario autenticado.
- La pertenencia del recurso se valida en el dominio (`ensureOwnedBy`), no solo por rol.
- Los errores 500 no exponen stack traces ni detalles internos.
