# 05 - Principios SOLID

## 1. Método de auditoría

La revisión se hizo sobre el código de `src/main/java/application`, no sobre diagramas. Para cada
principio se identificó:

1. La **responsabilidad** o contrato que el principio protege.
2. La **clase concreta** que lo evidencia.
3. La **prueba** o el flujo que demuestra que el diseño funciona.

Resultado global:

| Principio | Evidencia principal | Estado |
|-----------|---------------------|--------|
| SRP | Excepciones específicas, modelos con invariantes, servicios orquestadores, `EmailUniquenessValidator`. | Cumplido |
| OCP | Nuevos casos de uso y adaptadores sin modificar el dominio; jerarquía de excepciones extensible. | Cumplido |
| LSP | Implementaciones de puertos intercambiables; subclases de excepciones con semántica compatible. | Cumplido |
| ISP | 24 puertos de entrada atómicos; controladores que dependen solo de lo que usan. | Cumplido |
| DIP | El dominio depende de `*RepositoryPort`; los adaptadores JPA los implementan. | Cumplido |

## 2. SRP — Single Responsibility Principle

> Una clase debe tener una sola razón para cambiar.

### 2.1 Excepciones: una causa, una clase

Antes de la auditoría había excepciones genéricas como `RuntimeException("Product not found")`,
`IllegalStateException("Only PAID orders can be prepared")` o
`IllegalArgumentException("Quantity must be positive")`. Eso obligaba al handler y a los tests a
interpretar mensajes de texto.

Ahora cada causa tiene su propia clase:

| Excepción | Responsabilidad única | HTTP |
|-----------|-----------------------|------|
| `ProductNotFoundException` | El producto solicitado no existe. | 404 |
| `BuyerNotFoundException` | El comprador solicitado no existe. | 404 |
| `OrderNotFoundException` | La orden solicitada no existe. | 404 |
| `InsufficientStockException` | No hay unidades suficientes para reservar. | 400 |
| `OrderAlreadyPaidException` | Se intentó pagar una orden ya pagada. | 400 |
| `OrderNotPreparableException` | Se intentó preparar una orden que no está `PAID`. | 400 |
| `SellerNotApprovedException` | El vendedor no está activo para vender. | 403 |
| `ProductOwnershipException` | El producto pertenece a otro vendedor. | 403 |
| `InvalidProductTypeException` | El tipo no es `PHYSICAL` ni `DIGITAL`. | 400 |

Cada clase tiene un constructor con el contexto necesario y un mensaje claro:

```java
public class InsufficientStockException extends DomainException {
  public InsufficientStockException(String productId, int requested, int available) {
    super(
        String.format(
            "Insufficient stock for product %s. Requested: %d, Available: %d",
            productId, requested, available));
  }
}
```

```java
public class SellerNotApprovedException extends UnauthorizedOperationException {
  public SellerNotApprovedException(String sellerId, String status) {
    super("Seller " + sellerId + " is not approved to sell. Status: " + status);
  }
}
```

La jerarquía separa tres responsabilidades de traducción HTTP:

```text
DomainException                  → 400 (regla de negocio)
├── ResourceNotFoundException    → 404 (no encontrado)
├── UnauthorizedOperationException → 403 (prohibido)
├── InvalidCredentialsException  → 401 (no autenticado)
└── DuplicateEmailException      → 409 (conflicto)
```

`GlobalExceptionHandler` solo traduce; no decide reglas de negocio:

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
  return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex));
}

@ExceptionHandler(DomainException.class)
public ResponseEntity<?> handleBusinessRule(DomainException ex) {
  return ResponseEntity.badRequest().body(error(ex));
}
```

### 2.2 El modelo protege sus propias invariantes

`Product` no es un contenedor de getters/setters: valida nombre, precio y tipo en el constructor y
en los setters.

```java
if (name == null || name.isBlank()) throw new InvalidProductNameException();
if (price == null || price.signum() <= 0) {
  throw new InvalidProductPriceException(String.valueOf(price));
}
if (type == null) throw new InvalidProductTypeException(null);
```

`Order` concentra las transiciones de estado y sus excepciones:

```java
public void markPreparing() {
  if (status != OrderStatus.PAID) {
    throw new OrderNotPreparableException(id, status.name());
  }
  this.status = OrderStatus.PREPARING;
  touch();
}
```

`InventoryItem` protege el stock:

```java
public void reserve(int amount) {
  if (amount <= 0) throw new InvalidQuantityException("Reservation amount must be positive");
  if (quantityAvailable < amount) {
    throw new InsufficientStockException(productId, amount, quantityAvailable);
  }
  this.quantityAvailable -= amount;
  this.quantityReserved += amount;
}
```

Consecuencia: un servicio no puede dejar el stock negativo ni saltarse una transición, aunque
olvide validarlo, porque el modelo lo impide.

### 2.3 Servicios: orquestar, no revalidar

`CreateProductService` tiene una sola responsabilidad: coordinar la creación.

```java
@Override
@Transactional
public Product execute(
    String sellerId,
    String name,
    String description,
    Product.ProductType type,
    BigDecimal price,
    String sku) {
  Seller seller =
      sellerRepository.findById(sellerId)
          .orElseThrow(() -> new SellerNotFoundException(sellerId));
  seller.ensureActive();
  return productRepository.save(Product.create(sellerId, name, description, type, price, sku));
}
```

No valida el precio (lo hace `Product`), no valida el estado del vendedor con un `if` local (lo hace
`Seller.ensureActive()`), no construye SQL (lo hace el adaptador). Busca, delega y persiste.

### 2.4 Validación de email separada de la persistencia

Antes, `RegisterSellerService` y `RegisterBuyerService` repetían:

```java
if (userRepository.existsByEmail(email)) throw new DuplicateEmailException(email);
```

Ahora ambos delegan en un colaborador con una sola razón de cambio:

```java
@Component
public class EmailUniquenessValidator {
  private final UserRepositoryPort userRepository;

  public void ensureAvailable(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new DuplicateEmailException(email);
    }
  }
}
```

`RegisterSellerService` queda como orquestador:

```java
emailUniquenessValidator.ensureAvailable(email);
String hash = passwordEncoder.encode(rawPassword);
User user = User.create(fullName, email, hash, User.Role.SELLER, identityDocument);
user = userRepository.save(user);
return sellerRepository.save(Seller.create(user.getId(), businessName, taxId, contactPhone));
```

Si mañana cambia la regla de unicidad (por ejemplo, por dominio de email), se modifica una sola
clase.

### 2.5 Controladores delgados

`BuyerController` resuelve la identidad, invoca el caso de uso y mapea la respuesta. No contiene
reglas de stock, estados ni precios.

```java
@PostMapping("/cart/items")
public ResponseEntity<?> addToCart(
    @Valid @RequestBody AddToCartRequest req, Authentication auth) {
  String buyerId = resolveBuyerId(auth);
  Order cart =
      addItemToCartUseCase.addItem(
          buyerId, req.getProductId(), req.getQuantity(), req.getWarehouseId());
  return ResponseEntity.ok(toOrderMap(cart));
}
```

### 2.6 Resumen SRP por clase

| Clase | Única responsabilidad |
|-------|-----------------------|
| `GlobalExceptionHandler` | Traducir excepciones a HTTP. |
| `JwtAuthFilter` | Autenticar la petición a partir del token. |
| `JwtProvider` | Firmar y validar tokens. |
| `SecurityConfig` | Definir reglas de acceso por ruta y rol. |
| `Product` | Proteger las invariantes del producto. |
| `Order` | Proteger la máquina de estados de la orden. |
| `InventoryItem` | Proteger las cantidades de inventario. |
| `EmailUniquenessValidator` | Validar unicidad de email. |
| `CreateProductService` | Orquestar la creación de un producto. |
| `CancelOrderService` | Orquestar la cancelación de una orden propia. |
| `GlobalExceptionHandler` | Centralizar el contrato de error. |

## 3. OCP — Open/Closed Principle

> El software debe estar abierto a extensión y cerrado a modificación.

### 3.1 Nuevos casos de uso sin tocar los existentes

Agregar una operación nueva sigue un patrón aditivo:

```text
1. Nueva interfaz en domain/ports/in (por ejemplo, CancelOrderUseCase).
2. Nueva implementación en domain/services/order (CancelOrderService).
3. Nuevo endpoint en el controlador correspondiente.
```

No se modifica `AddItemToCartService`, `ConfirmPaymentService` ni el modelo `Order` para agregar la
cancelación: la regla ya estaba en `Order.cancel()` y el nuevo servicio solo la invoca. Ese es el
punto de extensión natural.

### 3.2 Nuevos adaptadores sin tocar el dominio

Si se quisiera persistir en MongoDB o publicar eventos, basta con implementar el puerto de salida:

```java
public class MongoOrderAdapter implements OrderRepositoryPort { ... }
```

Los servicios siguen dependiendo de `OrderRepositoryPort`; no se reescribe ninguna regla.

### 3.3 Jerarquía de excepciones extensible

Agregar una nueva regla consiste en crear una subclase de la familia correcta:

```java
public class OrderNotRefundableException extends DomainException { ... }
```

El handler la trata como `DomainException` (400) sin cambios. Si la regla debe ser 409, se hace
subclase de `DuplicateEmailException` o se agrega un handler específico; el resto del sistema no se
modifica.

### 3.4 Configuración por perfil

La persistencia se selecciona por configuración (`dev` → H2, `prod` → MySQL) sin cambiar código.
La aplicación está abierta a nuevos entornos y cerrada a modificaciones del dominio.

## 4. LSP — Liskov Substitution Principle

> Los subtipos deben poder sustituir a sus supertipos sin romper el contrato.

### 4.1 Implementaciones de puertos de salida

`UserJpaAdapter` implementa `UserRepositoryPort`. Cualquier otra implementación (en memoria, otro
motor, un mock de test) puede sustituirlo porque el contrato es el mismo:

```java
public interface UserRepositoryPort {
  User save(User user);
  Optional<User> findById(String id);
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  List<User> findAll();
}
```

Los tests de servicios usan mocks de estos puertos y demuestran que el servicio no depende de la
implementación concreta.

### 4.2 Implementaciones de casos de uso

`AddItemToCartService` implementa `AddItemToCartUseCase`. El controlador depende de la interfaz, por
lo que puede recibir cualquier implementación que respete la firma y el contrato:

```java
public interface AddItemToCartUseCase {
  Order addItem(String buyerId, String productId, int quantity, String warehouseId);
}
```

El contrato implícito es: devuelve una orden persistida, con el ítem agregado y el stock reservado, o
lanza una excepción de dominio. La implementación no devuelve `null` ni deja el stock a medias.

### 4.3 Excepciones

`ProductNotFoundException` es un `ResourceNotFoundException`, que es un `DomainException`. El handler
puede tratarla como cualquiera de los tres sin romper el contrato:

```java
catch (DomainException ex) { ... }        // válido
catch (ResourceNotFoundException ex) { ... } // válido y más específico
catch (ProductNotFoundException ex) { ... }  // válido y más específico
```

Las subclases no cambian la semántica de la familia: siguen representando un error esperado del
dominio.

### 4.4 Contratos respetados en las transiciones

`Order.cancel()` no deja la orden en un estado inconsistente cuando falla: lanza
`OrderNotCancellableException` antes de mutar. Lo mismo ocurre con `markPreparing`,
`markDispatched`, etc. El contrato "si retorna, el estado es el esperado; si falla, el estado no
cambió" se cumple en todas las implementaciones.

## 5. ISP — Interface Segregation Principle

> Ningún cliente debe depender de métodos que no usa.

### 5.1 De puertos monolíticos a 24 casos de uso atómicos

Antes existían interfaces amplias como `BuyerPort`, `SellerPort`, `AdministratorPort`,
`PublicAccessPort` y `LogisticsOperatorPort`. Un controlador que solo necesitaba "obtener carrito"
dependía de una interfaz con muchas operaciones no relacionadas.

Hoy hay **24 puertos de entrada atómicos**, cada uno con una sola operación:

| # | Puerto | Operación |
|---|--------|-----------|
| 1 | `RegisterBuyerUseCase` | `execute(fullName, email, password, identityDocument, primaryAddress)` |
| 2 | `LoginUseCase` | `authenticate(email, password)` |
| 3 | `RegisterSellerUseCase` | `execute(fullName, email, password, identityDocument, businessName, taxId, contactPhone)` |
| 4 | `ApproveSellerUseCase` | `approveSeller(sellerId)` |
| 5 | `ChangeUserStatusUseCase` | `changeUserStatus(userId, status)` |
| 6 | `RegisterMarketplaceWarehouseUseCase` | `registerMarketplaceWarehouse(name, address)` |
| 7 | `RegisterSellerWarehouseUseCase` | `registerSellerWarehouse(name, address, sellerId)` |
| 8 | `CreateProductUseCase` | `execute(sellerId, name, description, type, price, sku)` |
| 9 | `PublishProductUseCase` | `execute(productId, sellerId)` |
| 10 | `GetSellerProductsUseCase` | `getProducts(sellerId)` |
| 11 | `BrowseCatalogUseCase` | `browsePublishedProducts()` |
| 12 | `GetProductUseCase` | `getPublishedProduct(productId)` |
| 13 | `AddInventoryUseCase` | `execute(productId, warehouseId, quantity, sellerId)` |
| 14 | `GetCartUseCase` | `getOrCreateCart(buyerId)` |
| 15 | `AddItemToCartUseCase` | `addItem(buyerId, productId, quantity, warehouseId)` |
| 16 | `ClearCartUseCase` | `clearCart(buyerId)` |
| 17 | `SubmitOrderFromCartUseCase` | `submitOrder(buyerId, shippingAddress)` |
| 18 | `ConfirmPaymentUseCase` | `confirmPayment(orderId, buyerId, paymentReference)` |
| 19 | `GetBuyerOrdersUseCase` | `getOrders(buyerId)` |
| 20 | `CancelOrderUseCase` | `cancelOrder(orderId, buyerId)` |
| 21 | `MarkOrderPreparingUseCase` | `markPreparing(orderId)` |
| 22 | `MarkOrderDispatchedUseCase` | `markDispatched(orderId)` |
| 23 | `MarkOrderDeliveredUseCase` | `markDelivered(orderId)` |
| 24 | `FinalizeOrderUseCase` | `finalizeOrder(orderId)` |

### 5.2 Controladores que dependen solo de lo que usan

`BuyerController` declara exactamente los seis casos de uso que atiende, más el repositorio necesario
para resolver el perfil:

```java
public BuyerController(
    GetCartUseCase getCartUseCase,
    AddItemToCartUseCase addItemToCartUseCase,
    ClearCartUseCase clearCartUseCase,
    SubmitOrderFromCartUseCase submitOrderFromCartUseCase,
    ConfirmPaymentUseCase confirmPaymentUseCase,
    CancelOrderUseCase cancelOrderUseCase,
    GetBuyerOrdersUseCase getBuyerOrdersUseCase,
    BuyerRepositoryPort buyerRepository) { ... }
```

No recibe un `BuyerPort` con 20 métodos: recibe las interfaces mínimas. Si se agrega un caso de uso
de comprador, no hay que recompilar ni modificar los controladores existentes.

`LogisticsController` es aún más claro: solo depende de tres operaciones logísticas.

```java
public LogisticsController(
    MarkOrderPreparingUseCase markOrderPreparingUseCase,
    MarkOrderDispatchedUseCase markOrderDispatchedUseCase,
    MarkOrderDeliveredUseCase markOrderDeliveredUseCase) { ... }
```

### 5.3 Colaboradores estrechos

`EmailUniquenessValidator` expone un único método (`ensureAvailable`) en lugar de obligar a los
servicios a conocer toda la API de usuarios. Es ISP aplicado a un colaborador de dominio.

### 5.4 Tests más simples

Los tests de MVC declaran solo los `@MockBean` que el controlador necesita. Por ejemplo,
`LogisticsControllerTest` mockea tres casos de uso, no una fachada completa. Los tests de servicio
mockean solo los puertos de salida que el caso de uso usa.

## 6. DIP — Dependency Inversion Principle

> Las abstracciones no deben depender de detalles; los detalles deben depender de las abstracciones.

### 6.1 El dominio define contratos, la infraestructura los implementa

```java
public interface OrderRepositoryPort {
  Order save(Order order);
  Optional<Order> findById(String id);
  List<Order> findByBuyerId(String buyerId);
  Optional<Order> findActiveCartByBuyerId(String buyerId);
  List<Order> findAll();
}
```

```java
@Repository
public class OrderJpaAdapter implements OrderRepositoryPort {
  // traduce dominio ⇄ entidad JPA
}
```

La flecha de dependencia va del adaptador al puerto, no al revés. El dominio no conoce
`OrderJpaEntity` ni `SpringDataJpaOrderRepository`.

### 6.2 Inyección por constructor

Los servicios reciben sus dependencias como interfaces:

```java
public CreateProductService(
    ProductRepositoryPort productRepository, SellerRepositoryPort sellerRepository) {
  this.productRepository = productRepository;
  this.sellerRepository = sellerRepository;
}
```

No hay `new ProductJpaAdapter()` dentro del servicio ni búsquedas de beans. Esto permite sustituir
implementaciones en tests y en producción. Incluso la colaboración entre casos de uso se hace por
interfaz: `AddItemToCartService` recibe `GetCartUseCase`, no la clase concreta `GetCartService`.

### 6.3 Modelo limpio de JPA

Las entidades de dominio no llevan `@Entity`, `@Table` ni `@Column`. Las entidades JPA viven en
`adapters/persistence/jpa/entities` y los mappers hacen la conversión. Así, una decisión de
persistencia no obliga a cambiar el modelo ni las reglas.

### 6.4 Compromiso pragmático

Los servicios de aplicación usan `@Service` y `@Transactional` de Spring. Es un acoplamiento
aceptado al framework de infraestructura, no al de persistencia: las reglas y los modelos siguen
siendo puros, y los casos de uso se pueden probar con mocks. Si se quisiera pureza total, las
anotaciones podrían moverse a una clase de configuración; no se hizo para no agregar complejidad sin
beneficio en este alcance.

## 7. Matriz de cumplimiento

| Principio | Evidencia en código | Prueba / verificación |
|-----------|---------------------|-----------------------|
| SRP | `domain/exceptions/*`, `Product`, `Order`, `InventoryItem`, `EmailUniquenessValidator` | Tests unitarios de servicios y modelos; `mvn test` |
| OCP | `domain/ports/in/*`, `domain/ports/out/*`, adaptadores | Nuevos casos de uso sin modificar los existentes |
| LSP | `*JpaAdapter implements *RepositoryPort`, `*Service implements *UseCase`, jerarquía de excepciones | Mocks de puertos; handler polimórfico |
| ISP | 24 puertos atómicos; controladores con dependencias mínimas | Constructores de controladores; tests MVC |
| DIP | Puertos de salida + inyección por constructor; dominio sin JPA | `domain` no importa `adapters` |

## 8. Conclusión

La auditoría no se limitó a renombrar excepciones. Se movieron las reglas al lugar correcto (el
modelo), se separaron las validaciones compartidas (`EmailUniquenessValidator`), se dividieron las
fachadas de entrada en 24 casos de uso atómicos y se dejó a los adaptadores la responsabilidad de
traducir HTTP y persistencia.

El resultado es un diseño donde:

- El dominio se puede entender y probar sin levantar Spring.
- Un cambio en la base de datos no toca las reglas de negocio.
- Un cambio en una regla tiene un único lugar responsable.
- Cada cliente depende solo de las operaciones que realmente usa.
- Los errores esperados se comunican con tipos específicos y códigos HTTP coherentes.
