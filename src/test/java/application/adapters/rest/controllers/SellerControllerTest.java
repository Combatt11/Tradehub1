package application.adapters.rest.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.adapters.rest.GlobalExceptionHandler;
import application.adapters.security.JwtProvider;
import application.adapters.security.SecurityConfig;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.InventoryItem;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.inventory.AddInventoryService;
import application.domain.services.product.CreateProductService;
import application.domain.services.product.PublishProductService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(SellerController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("SellerController")
class SellerControllerTest {

  private static final String USER_ID = "user-1";
  private static final String SELLER_ID = "seller-1";

  @Autowired private MockMvc mockMvc;

  @MockBean private CreateProductService createProductService;
  @MockBean private PublishProductService publishProductService;
  @MockBean private AddInventoryService addInventoryService;
  @MockBean private SellerRepositoryPort sellerRepository;
  @MockBean private ProductRepositoryPort productRepository;
  @MockBean private JwtProvider jwtProvider;

  /**
   * Mirrors what JwtAuthFilter does in production: the authentication principal is the raw
   * user id String (not a UserDetails object).
   */
  private static MockHttpServletRequestBuilder asSeller(MockHttpServletRequestBuilder builder) {
    return builder.with(
        authentication(
            new UsernamePasswordAuthenticationToken(
                USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_SELLER")))));
  }

  private static MockHttpServletRequestBuilder asBuyer(MockHttpServletRequestBuilder builder) {
    return builder.with(
        authentication(
            new UsernamePasswordAuthenticationToken(
                USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_BUYER")))));
  }

  private void givenSellerProfile() {
    Seller seller = Seller.create(USER_ID, "Acme", "TAX-1", "3001234567");
    seller.approve();
    when(sellerRepository.findByUserId(USER_ID)).thenReturn(Optional.of(seller));
  }

  private static Product draftProduct() {
    return new Product(
        "product-1",
        SELLER_ID,
        "Laptop",
        "A laptop",
        Product.ProductType.PHYSICAL,
        new BigDecimal("100.00"),
        Product.ProductStatus.DRAFT,
        null,
        "SKU-1");
  }

  @Test
  @DisplayName("POST /api/seller/products returns 201 when the product is created")
  void createProductReturns201() throws Exception {
    givenSellerProfile();
    when(createProductService.execute(
            anyString(), anyString(), any(), any(), any(BigDecimal.class), any()))
        .thenReturn(draftProduct());

    mockMvc
        .perform(
            asSeller(
                post("/api/seller/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"Laptop","description":"A laptop","type":"PHYSICAL","price":100.00,"sku":"SKU-1"}
                        """)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("product-1"))
        .andExpect(jsonPath("$.status").value("DRAFT"));
  }

  @Test
  @DisplayName("POST /api/seller/products returns 400 when the payload is invalid")
  void createProductReturns400OnInvalidPayload() throws Exception {
    mockMvc
        .perform(
            asSeller(
                post("/api/seller/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"","type":"","price":-5}
                        """)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").exists());

    verify(createProductService, never()).execute(anyString(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("POST /api/seller/products returns 500 when the seller profile is missing")
  void createProductReturns500WhenProfileMissing() throws Exception {
    when(sellerRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            asSeller(
                post("/api/seller/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"Laptop","type":"PHYSICAL","price":100.00}
                        """)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("Seller profile not found"));
  }

  @Test
  @DisplayName("POST /api/seller/products/{id}/publish returns 200 when published")
  void publishReturns200() throws Exception {
    givenSellerProfile();
    Product published = draftProduct();
    published.publish();
    when(publishProductService.execute(eq("product-1"), anyString())).thenReturn(published);

    mockMvc
        .perform(asSeller(post("/api/seller/products/product-1/publish")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PUBLISHED"));
  }

  @Test
  @DisplayName("POST /api/seller/products/{id}/publish returns 400 when the product is not found")
  void publishReturns400WhenProductNotFound() throws Exception {
    givenSellerProfile();
    when(publishProductService.execute(anyString(), anyString()))
        .thenThrow(new UnauthorizedOperationException("Product not found"));

    mockMvc
        .perform(asSeller(post("/api/seller/products/product-1/publish")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Product not found"));
  }

  @Test
  @DisplayName("POST /api/seller/inventory returns 200 when stock is added")
  void addInventoryReturns200() throws Exception {
    givenSellerProfile();
    when(addInventoryService.execute(anyString(), anyString(), anyInt(), anyString()))
        .thenReturn(InventoryItem.create("product-1", "wh-1", 10));

    mockMvc
        .perform(
            asSeller(
                post("/api/seller/inventory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"productId":"product-1","warehouseId":"wh-1","quantity":10}
                        """)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.productId").value("product-1"))
        .andExpect(jsonPath("$.quantityAvailable").value(10));
  }

  @Test
  @DisplayName("POST /api/seller/inventory returns 400 when the payload is invalid")
  void addInventoryReturns400OnInvalidPayload() throws Exception {
    mockMvc
        .perform(
            asSeller(
                post("/api/seller/inventory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"productId":"","warehouseId":"","quantity":0}
                        """)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").exists());

    verify(addInventoryService, never()).execute(any(), any(), anyInt(), any());
  }

  @Test
  @DisplayName("POST /api/seller/inventory returns 400 when the product is not owned by the seller")
  void addInventoryReturns400WhenNotOwner() throws Exception {
    givenSellerProfile();
    when(addInventoryService.execute(anyString(), anyString(), anyInt(), anyString()))
        .thenThrow(new UnauthorizedOperationException("Product does not belong to this seller"));

    mockMvc
        .perform(
            asSeller(
                post("/api/seller/inventory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"productId":"product-1","warehouseId":"wh-1","quantity":5}
                        """)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Product does not belong to this seller"));
  }

  @Test
  @DisplayName("GET /api/seller/products returns 200 with the seller catalog")
  void myProductsReturns200() throws Exception {
    givenSellerProfile();
    when(productRepository.findBySellerId(anyString())).thenReturn(List.of(draftProduct()));

    mockMvc
        .perform(asSeller(get("/api/seller/products")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].name").value("Laptop"))
        .andExpect(jsonPath("$[0].type").value("PHYSICAL"));
  }

  @Test
  @DisplayName("GET /api/seller/products returns 401 when unauthenticated")
  void myProductsReturns401WhenUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/seller/products")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /api/seller/products returns 403 when the role is not SELLER")
  void myProductsReturns403ForWrongRole() throws Exception {
    mockMvc
        .perform(asBuyer(get("/api/seller/products")))
        .andExpect(status().isForbidden());
  }
}
