# 01 - Requisitos

## 1. Actores

| Actor | Descripción |
|-------|-------------|
| **Visitante** | Persona sin autenticar. Puede registrarse como comprador, iniciar sesión y consultar el catálogo público. |
| **Comprador (Buyer)** | Usuario autenticado con rol `BUYER`. Gestiona su carrito y sus órdenes. |
| **Vendedor (Seller)** | Usuario autenticado con rol `SELLER`. Crea productos, los publica y gestiona inventario. Debe estar aprobado. |
| **Operador logístico** | Usuario autenticado con rol `LOGISTICS_OPERATOR`. Avanza el estado de las órdenes pagadas. |
| **Administrador** | Usuario autenticado con rol `ADMINISTRATOR`. Registra vendedores, los aprueba y crea almacenes. |
| **Supervisor** | Rol previsto en el modelo (`SUPERVISOR`); no tiene endpoints propios en esta versión. |

## 2. Requisitos funcionales

### 2.1 Usuarios y autenticación

| ID | Requisito | Caso de uso | Endpoint |
|----|-----------|-------------|----------|
| RF-01 | Un visitante puede registrarse como comprador con nombre, email, contraseña, documento y dirección principal. | `RegisterBuyerUseCase` | `POST /api/public/register/buyer` |
| RF-02 | Un usuario registrado puede iniciar sesión con email y contraseña y recibir un JWT. | `LoginUseCase` | `POST /api/public/login` |
| RF-03 | El sistema rechaza el registro si el email ya existe. | `RegisterBuyerUseCase`, `RegisterSellerUseCase` | — |
| RF-04 | El sistema rechaza credenciales inválidas o usuarios inactivos. | `LoginUseCase` | — |
| RF-05 | Un administrador puede cambiar el estado de un usuario (activo, bloqueado, inactivo). | `ChangeUserStatusUseCase` | Sin endpoint en esta versión |
| RF-06 | Las contraseñas se almacenan con hash BCrypt, nunca en texto plano. | — | — |

### 2.2 Vendedores y almacenes

| ID | Requisito | Caso de uso | Endpoint |
|----|-----------|-------------|----------|
| RF-07 | Solo un administrador puede registrar vendedores. | `RegisterSellerUseCase` | `POST /api/admin/sellers` |
| RF-08 | El vendedor queda en estado `PENDING_APPROVAL` hasta que un administrador lo apruebe. | `RegisterSellerUseCase` | — |
| RF-09 | Un administrador puede aprobar un vendedor y pasarlo a `ACTIVE`. | `ApproveSellerUseCase` | `POST /api/admin/sellers/{sellerId}/approve` |
| RF-10 | Un administrador puede crear almacenes del marketplace. | `RegisterMarketplaceWarehouseUseCase` | `POST /api/admin/warehouses/marketplace` |
| RF-11 | Un administrador puede crear almacenes asociados a un vendedor. | `RegisterSellerWarehouseUseCase` | `POST /api/admin/warehouses/seller` |

### 2.3 Catálogo y productos

| ID | Requisito | Caso de uso | Endpoint |
|----|-----------|-------------|----------|
| RF-12 | Cualquier visitante puede listar los productos publicados. | `BrowseCatalogUseCase` | `GET /api/catalog/products` |
| RF-13 | Cualquier visitante puede consultar un producto publicado por su identificador. | `GetProductUseCase` | `GET /api/catalog/products/{id}` |
| RF-14 | Un vendedor activo puede crear productos físicos o digitales. | `CreateProductUseCase` | `POST /api/seller/products` |
| RF-15 | Un vendedor puede publicar un producto propio. | `PublishProductUseCase` | `POST /api/seller/products/{productId}/publish` |
| RF-16 | Un vendedor puede listar únicamente sus propios productos. | `GetSellerProductsUseCase` | `GET /api/seller/products` |

### 2.4 Inventario

| ID | Requisito | Caso de uso | Endpoint |
|----|-----------|-------------|----------|
| RF-17 | Un vendedor puede agregar stock de un producto físico en un almacén activo. | `AddInventoryUseCase` | `POST /api/seller/inventory` |
| RF-18 | Solo el vendedor propietario del producto puede modificar su inventario. | `AddInventoryUseCase` | — |
| RF-19 | Los productos digitales no requieren inventario. | `AddInventoryUseCase` | — |
| RF-20 | El stock disponible nunca puede ser negativo. | `InventoryItem` | — |

### 2.5 Carrito y órdenes

| ID | Requisito | Caso de uso | Endpoint |
|----|-----------|-------------|----------|
| RF-21 | Un comprador puede obtener su carrito activo; si no existe, se crea. | `GetCartUseCase` | `GET /api/buyer/cart` |
| RF-22 | Un comprador puede agregar un producto al carrito indicando cantidad y almacén. | `AddItemToCartUseCase` | `POST /api/buyer/cart/items` |
| RF-23 | Un comprador puede vaciar su carrito. | `ClearCartUseCase` | `DELETE /api/buyer/cart` |
| RF-24 | Un comprador puede enviar el carrito a pago con una dirección de envío. | `SubmitOrderFromCartUseCase` | `POST /api/buyer/cart/submit` |
| RF-25 | Un comprador puede confirmar el pago de una orden propia. | `ConfirmPaymentUseCase` | `POST /api/buyer/orders/{orderId}/pay` |
| RF-26 | Un comprador puede listar sus órdenes. | `GetBuyerOrdersUseCase` | `GET /api/buyer/orders` |
| RF-27 | Un comprador puede cancelar una orden propia si su estado lo permite. | `CancelOrderUseCase` | `POST /api/buyer/orders/{orderId}/cancel` |
| RF-28 | Solo se puede agregar al carrito un producto publicado y un comprador activo. | `AddItemToCartUseCase` | — |
| RF-29 | Al agregar un producto físico se reserva stock; si no alcanza, la operación falla. | `AddItemToCartUseCase` | — |

### 2.6 Logística

| ID | Requisito | Caso de uso | Endpoint |
|----|-----------|-------------|----------|
| RF-30 | Un operador logístico puede marcar una orden pagada como en preparación. | `MarkOrderPreparingUseCase` | `POST /api/logistics/orders/{orderId}/prepare` |
| RF-31 | Un operador logístico puede marcar una orden en preparación o pagada como despachada. | `MarkOrderDispatchedUseCase` | `POST /api/logistics/orders/{orderId}/dispatch` |
| RF-32 | Un operador logístico puede marcar una orden despachada como entregada. | `MarkOrderDeliveredUseCase` | `POST /api/logistics/orders/{orderId}/deliver` |
| RF-33 | Un administrador o proceso interno puede finalizar una orden entregada. | `FinalizeOrderUseCase` | Sin endpoint en esta versión |

### 2.7 Errores y validación

| ID | Requisito |
|----|-----------|
| RF-34 | Las reglas de negocio incumplidas devuelven HTTP 400 con un mensaje claro. |
| RF-35 | Los recursos inexistentes devuelven HTTP 404. |
| RF-36 | Las operaciones no autorizadas sobre recursos ajenos devuelven HTTP 403. |
| RF-37 | Las credenciales inválidas devuelven HTTP 401. |
| RF-38 | Los emails duplicados devuelven HTTP 409. |
| RF-39 | Los cuerpos JSON malformados o inválidos devuelven HTTP 400. |
| RF-40 | Los errores inesperados devuelven HTTP 500 sin filtrar detalles internos. |

## 3. Requisitos no funcionales

| ID | Categoría | Requisito | Evidencia |
|----|-----------|-----------|-----------|
| RNF-01 | Seguridad | API stateless autenticada con JWT en cada petición. | `JwtAuthFilter`, `JwtProvider`, `frameworks/config/SecurityConfig` |
| RNF-02 | Seguridad | Autorización por rol a nivel de ruta. | `frameworks/config/SecurityConfig` |
| RNF-03 | Seguridad | Contraseñas con BCrypt. | `PasswordEncoder` |
| RNF-04 | Seguridad | Las reglas de propiedad se validan en el dominio, no solo en el controlador. | `Product.ensureOwnedBy`, `Order.ensureOwnedBy` |
| RNF-05 | Arquitectura | El dominio no depende de frameworks de persistencia ni de HTTP. | `domain/entities`, `domain/usecases`, `domain/ports/out` |
| RNF-06 | Arquitectura | Las dependencias apuntan hacia el dominio. | Puertos + inyección por constructor |
| RNF-07 | Mantenibilidad | Un caso de uso = una interfaz = una implementación. | 24 puertos de entrada atómicos |
| RNF-08 | Mantenibilidad | Cada excepción representa una única causa de fallo. | `domain/exceptions` |
| RNF-09 | Testabilidad | Los servicios se prueban con mocks de los puertos de salida. | `src/test/java/application/domain/usecases` |
| RNF-10 | Calidad | Cobertura de líneas ≥ 60 %. | `jacoco-maven-plugin` |
| RNF-11 | Calidad | Formato uniforme Google Java Format. | `spotless-maven-plugin` |
| RNF-12 | Calidad | Java ≥ 17 y Maven ≥ 3.8. | `maven-enforcer-plugin` |
| RNF-13 | Persistencia | H2 en desarrollo y MySQL 8 en producción. | Perfiles `dev` y `prod` |
| RNF-14 | Despliegue | Imagen Docker y orquestación con Docker Compose. | `Dockerfile`, `docker-compose.yml` |
| RNF-15 | Observabilidad | Endpoints de salud y métricas. | Spring Boot Actuator |
| RNF-16 | Documentación | SDD completo y enlazado desde el README. | Carpeta `SDD/` |
| RNF-17 | Portabilidad | Codificación UTF-8 y finales de línea LF. | `pom.xml`, `.gitattributes` |

## 4. Restricciones y supuestos

- El pago es simulado: se registra una referencia, no se integra una pasarela real.
- No existe endpoint para finalizar órdenes en esta versión, aunque `Order.finalizeOrder()`
  implementa la regla y la excepción `OrderNotFinalizableException`.
- `ChangeUserStatusUseCase` y `FinalizeOrderUseCase` existen como capacidades internas, pero no
  están expuestas por HTTP.
- El identificador del usuario autenticado se obtiene del `principal` del token; el controlador lo
  traduce al identificador del perfil (`Buyer` o `Seller`).
- La aplicación asume un único rol por usuario.

## 5. Trazabilidad resumida

```text
Requisito → Caso de uso → Servicio → Puerto de salida → Adaptador JPA
```

Ejemplo:

```text
RF-22 → AddItemToCartUseCase → AddItemToCartService
      → BuyerRepositoryPort / ProductRepositoryPort
      → InventoryRepositoryPort / OrderRepositoryPort
      → BuyerJpaAdapter / ProductJpaAdapter / InventoryJpaAdapter / OrderJpaAdapter
```
