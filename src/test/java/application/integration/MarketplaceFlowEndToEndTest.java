package application.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Marketplace end-to-end flow")
class MarketplaceFlowEndToEndTest {

  private static final String PASSWORD = "Secret123!";
  private static final String ADMIN_EMAIL = "admin@tradehub.com";
  private static final String ADMIN_PASSWORD = "admin123";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private record ProductSetup(String sellerToken, String productId, String warehouseId) {}

  private record BuyerSetup(String email, String token) {}

  private record PendingOrder(String orderId, BuyerSetup buyer) {}

  private static String bearer(String token) {
    return "Bearer " + token;
  }

  private JsonNode json(MvcResult result) throws Exception {
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private String login(String email, String password) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/public/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of("email", email, "password", password))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();
    return json(result).get("token").asText();
  }

  private String adminToken() throws Exception {
    return login(ADMIN_EMAIL, ADMIN_PASSWORD);
  }

  private String registerSeller(String adminToken, String email) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/admin/sellers")
                    .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "fullName", "Seller " + email,
                                "email", email,
                                "password", PASSWORD,
                                "identityDocument", "SELLER-" + UUID.randomUUID(),
                                "businessName", "Business " + email,
                                "taxId", "TAX-" + UUID.randomUUID(),
                                "contactPhone", "+5550001"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
            .andReturn();
    return json(result).get("id").asText();
  }

  private void approveSeller(String adminToken, String sellerId) throws Exception {
    mockMvc
        .perform(
            post("/api/admin/sellers/{sellerId}/approve", sellerId)
                .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  private String createProduct(String sellerToken) throws Exception {
    String suffix = UUID.randomUUID().toString();
    MvcResult result =
        mockMvc
            .perform(
                post("/api/seller/products")
                    .header(HttpHeaders.AUTHORIZATION, bearer(sellerToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "name",
                                "Product " + suffix,
                                "description",
                                "Integration product",
                                "type",
                                "PHYSICAL",
                                "price",
                                new BigDecimal("25.50"),
                                "sku",
                                "SKU-" + suffix))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andReturn();
    return json(result).get("id").asText();
  }

  private void publishProduct(String sellerToken, String productId) throws Exception {
    mockMvc
        .perform(
            post("/api/seller/products/{productId}/publish", productId)
                .header(HttpHeaders.AUTHORIZATION, bearer(sellerToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PUBLISHED"));
  }

  private String createWarehouse(String adminToken, String sellerId) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/admin/warehouses/seller")
                    .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "name",
                                "Warehouse " + UUID.randomUUID(),
                                "address",
                                "Integration Ave 123",
                                "sellerId",
                                sellerId))))
            .andExpect(status().isCreated())
            .andReturn();
    return json(result).get("id").asText();
  }

  private void addInventory(String sellerToken, String productId, String warehouseId, int quantity)
      throws Exception {
    mockMvc
        .perform(
            post("/api/seller/inventory")
                .header(HttpHeaders.AUTHORIZATION, bearer(sellerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "productId", productId,
                            "warehouseId", warehouseId,
                            "quantity", quantity))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.quantityAvailable").value(quantity));
  }

  private BuyerSetup registerBuyer() throws Exception {
    String email = "buyer-" + UUID.randomUUID() + "@test.com";
    mockMvc
        .perform(
            post("/api/public/register/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "fullName",
                            "Integration Buyer",
                            "email",
                            email,
                            "password",
                            PASSWORD,
                            "identityDocument",
                            "BUYER-" + UUID.randomUUID(),
                            "primaryAddress",
                            "Buyer Street 1"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.role").value("BUYER"));
    return new BuyerSetup(email, login(email, PASSWORD));
  }

  private JsonNode addToCart(String buyerToken, String productId, int quantity, String warehouseId)
      throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/buyer/cart/items")
                    .header(HttpHeaders.AUTHORIZATION, bearer(buyerToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "productId", productId,
                                "quantity", quantity,
                                "warehouseId", warehouseId))))
            .andExpect(status().isOk())
            .andReturn();
    return json(result);
  }

  private String submitOrder(String buyerToken) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/buyer/cart/submit")
                    .header(HttpHeaders.AUTHORIZATION, bearer(buyerToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(Map.of("shippingAddress", "Calle 99"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
            .andReturn();
    return json(result).get("id").asText();
  }

  private JsonNode payOrder(String buyerToken, String orderId) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/buyer/orders/{orderId}/pay", orderId)
                    .header(HttpHeaders.AUTHORIZATION, bearer(buyerToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of("paymentReference", "PAY-" + UUID.randomUUID()))))
            .andExpect(status().isOk())
            .andReturn();
    return json(result);
  }

  private ProductSetup createPublishedProductWithStock(int stock) throws Exception {
    String adminToken = adminToken();
    String sellerEmail = "seller-" + UUID.randomUUID() + "@test.com";
    String sellerId = registerSeller(adminToken, sellerEmail);
    approveSeller(adminToken, sellerId);

    String sellerToken = login(sellerEmail, PASSWORD);
    String productId = createProduct(sellerToken);
    publishProduct(sellerToken, productId);
    String warehouseId = createWarehouse(adminToken, sellerId);
    addInventory(sellerToken, productId, warehouseId, stock);

    return new ProductSetup(sellerToken, productId, warehouseId);
  }

  private PendingOrder createPendingOrder(int stock) throws Exception {
    ProductSetup product = createPublishedProductWithStock(stock);
    BuyerSetup buyer = registerBuyer();
    addToCart(buyer.token(), product.productId(), 1, product.warehouseId());
    return new PendingOrder(submitOrder(buyer.token()), buyer);
  }

  @Test
  @DisplayName("seller onboarding, catalog publication, cart checkout and payment")
  void fullMarketplaceFlowFromSellerApprovalToPayment() throws Exception {
    ProductSetup product = createPublishedProductWithStock(10);
    BuyerSetup buyer = registerBuyer();

    JsonNode cart = addToCart(buyer.token(), product.productId(), 2, product.warehouseId());
    assertThat(cart.get("status").asText()).isEqualTo("CART");
    assertThat(cart.get("itemCount").asInt()).isEqualTo(1);
    assertThat(cart.get("totalAmount").decimalValue()).isEqualByComparingTo("51.00");

    String orderId = submitOrder(buyer.token());
    JsonNode paid = payOrder(buyer.token(), orderId);
    assertThat(paid.get("status").asText()).isEqualTo("PAID");

    mockMvc
        .perform(get("/api/catalog/products/{id}", product.productId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(product.productId()));

    mockMvc
        .perform(get("/api/buyer/orders").header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("PAID"));
  }

  @Test
  @DisplayName("a buyer can cancel a pending order")
  void buyerCanCancelPendingOrder() throws Exception {
    PendingOrder pending = createPendingOrder(5);

    mockMvc
        .perform(
            post("/api/buyer/orders/{orderId}/cancel", pending.orderId())
                .header(HttpHeaders.AUTHORIZATION, bearer(pending.buyer().token())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELLED"));
  }

  @Test
  @DisplayName("a buyer cannot pay another buyer's order")
  void buyerCannotPayAnotherBuyersOrder() throws Exception {
    PendingOrder pending = createPendingOrder(5);
    BuyerSetup otherBuyer = registerBuyer();

    mockMvc
        .perform(
            post("/api/buyer/orders/{orderId}/pay", pending.orderId())
                .header(HttpHeaders.AUTHORIZATION, bearer(otherBuyer.token()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("paymentReference", "PAY-X"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").exists());
  }

  @Test
  @DisplayName("adding more units than available stock returns 400")
  void buyerCannotAddMoreStockThanAvailable() throws Exception {
    ProductSetup product = createPublishedProductWithStock(1);
    BuyerSetup buyer = registerBuyer();

    mockMvc
        .perform(
            post("/api/buyer/cart/items")
                .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "productId", product.productId(),
                            "quantity", 2,
                            "warehouseId", product.warehouseId()))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value(containsString("Insufficient stock")));
  }

  @Test
  @DisplayName("a draft product cannot be added to a cart")
  void buyerCannotAddDraftProductToCart() throws Exception {
    String adminToken = adminToken();
    String sellerEmail = "seller-" + UUID.randomUUID() + "@test.com";
    String sellerId = registerSeller(adminToken, sellerEmail);
    approveSeller(adminToken, sellerId);
    String sellerToken = login(sellerEmail, PASSWORD);
    String draftProductId = createProduct(sellerToken);
    BuyerSetup buyer = registerBuyer();

    mockMvc
        .perform(
            post("/api/buyer/cart/items")
                .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "productId",
                            draftProductId,
                            "quantity",
                            1,
                            "warehouseId",
                            "any-warehouse"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value(containsString("not available for sale")));
  }

  @Test
  @DisplayName("the catalog only exposes published products")
  void catalogOnlyListsPublishedProducts() throws Exception {
    ProductSetup published = createPublishedProductWithStock(5);

    String adminToken = adminToken();
    String sellerEmail = "seller-" + UUID.randomUUID() + "@test.com";
    String sellerId = registerSeller(adminToken, sellerEmail);
    approveSeller(adminToken, sellerId);
    String sellerToken = login(sellerEmail, PASSWORD);
    String draftProductId = createProduct(sellerToken);

    MvcResult result =
        mockMvc.perform(get("/api/catalog/products")).andExpect(status().isOk()).andReturn();
    JsonNode products = json(result);
    List<String> ids = new ArrayList<>();
    products.forEach(product -> ids.add(product.get("id").asText()));

    assertThat(ids).contains(published.productId()).doesNotContain(draftProductId);

    mockMvc
        .perform(get("/api/catalog/products/{id}", draftProductId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("logistics advances a paid order through preparation, dispatch and delivery")
  void logisticsOperatorAdvancesPaidOrderThroughDelivery() throws Exception {
    PendingOrder pending = createPendingOrder(5);
    payOrder(pending.buyer().token(), pending.orderId());
    String logisticsToken = login("logistics@example.com", "logistics123");

    mockMvc
        .perform(
            post("/api/logistics/orders/{orderId}/prepare", pending.orderId())
                .header(HttpHeaders.AUTHORIZATION, bearer(logisticsToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PREPARING"));

    mockMvc
        .perform(
            post("/api/logistics/orders/{orderId}/dispatch", pending.orderId())
                .header(HttpHeaders.AUTHORIZATION, bearer(logisticsToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DISPATCHED"));

    mockMvc
        .perform(
            post("/api/logistics/orders/{orderId}/deliver", pending.orderId())
                .header(HttpHeaders.AUTHORIZATION, bearer(logisticsToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DELIVERED"));
  }

  @Test
  @DisplayName("a seller cannot create products until approved")
  void unapprovedSellerCannotCreateProducts() throws Exception {
    String adminToken = adminToken();
    String sellerEmail = "seller-" + UUID.randomUUID() + "@test.com";
    registerSeller(adminToken, sellerEmail);
    String sellerToken = login(sellerEmail, PASSWORD);

    mockMvc
        .perform(
            post("/api/seller/products")
                .header(HttpHeaders.AUTHORIZATION, bearer(sellerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "name", "Blocked Product",
                            "type", "PHYSICAL",
                            "price", new BigDecimal("10.00")))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value(containsString("not approved to sell")));
  }

  @Test
  @DisplayName("buyer role cannot reach admin endpoints")
  void buyerCannotAccessAdminEndpoints() throws Exception {
    BuyerSetup buyer = registerBuyer();

    mockMvc
        .perform(
            post("/api/admin/sellers")
                .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("seller role cannot reach buyer endpoints")
  void sellerCannotAccessBuyerEndpoints() throws Exception {
    ProductSetup product = createPublishedProductWithStock(1);

    mockMvc
        .perform(
            get("/api/buyer/cart").header(HttpHeaders.AUTHORIZATION, bearer(product.sellerToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("duplicate buyer registration returns 409")
  void duplicateBuyerEmailReturnsConflict() throws Exception {
    BuyerSetup buyer = registerBuyer();

    mockMvc
        .perform(
            post("/api/public/register/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "fullName",
                            "Duplicate Buyer",
                            "email",
                            buyer.email(),
                            "password",
                            PASSWORD,
                            "identityDocument",
                            "BUYER-" + UUID.randomUUID(),
                            "primaryAddress",
                            "Another Street"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").exists());
  }

  @Test
  @DisplayName("invalid credentials return 401")
  void invalidCredentialsReturnUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/api/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("email", ADMIN_EMAIL, "password", "wrong-password"))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("a malformed bearer token is rejected as unauthenticated")
  void invalidTokenReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            get("/api/buyer/cart").header(HttpHeaders.AUTHORIZATION, bearer("not-a-valid-token")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("an unknown catalog product returns 404")
  void unknownCatalogProductReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/api/catalog/products/{id}", "missing-product"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("submitting an empty cart returns 404")
  void submittingEmptyCartReturnsNotFound() throws Exception {
    BuyerSetup buyer = registerBuyer();

    mockMvc
        .perform(
            post("/api/buyer/cart/submit")
                .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("shippingAddress", "Calle 1"))))
        .andExpect(status().isNotFound());
  }
}
