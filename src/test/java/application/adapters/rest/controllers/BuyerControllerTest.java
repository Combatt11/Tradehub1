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
import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.order.CartService;
import application.domain.services.order.OrderLifecycleService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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

@WebMvcTest(BuyerController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("BuyerController")
class BuyerControllerTest {

  private static final String USER_ID = "user-1";
  private static final String BUYER_ID = "buyer-1";

  @Autowired private MockMvc mockMvc;

  @MockBean private CartService cartService;
  @MockBean private OrderLifecycleService orderLifecycleService;
  @MockBean private BuyerRepositoryPort buyerRepository;
  @MockBean private OrderRepositoryPort orderRepository;
  @MockBean private JwtProvider jwtProvider;

  private Order cart;

  /**
   * Mirrors what JwtAuthFilter does in production: the authentication principal is the raw
   * user id String (not a UserDetails object).
   */
  private static MockHttpServletRequestBuilder asBuyer(MockHttpServletRequestBuilder builder) {
    return builder.with(
        authentication(
            new UsernamePasswordAuthenticationToken(
                USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_BUYER")))));
  }

  private static MockHttpServletRequestBuilder asSeller(MockHttpServletRequestBuilder builder) {
    return builder.with(
        authentication(
            new UsernamePasswordAuthenticationToken(
                USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_SELLER")))));
  }

  @BeforeEach
  void setUp() {
    cart = Order.createCart(BUYER_ID);
    cart.addItem(new OrderItem("product-1", "Laptop", 2, new BigDecimal("50.00"), "wh-1"));
  }

  private void givenBuyerProfile() {
    when(buyerRepository.findByUserId(USER_ID))
        .thenReturn(Optional.of(Buyer.create(USER_ID, "Calle 1")));
  }

  @Test
  @DisplayName("GET /api/buyer/cart returns 200 with the cart payload")
  void getCartReturns200() throws Exception {
    givenBuyerProfile();
    when(cartService.getOrCreateCart(anyString())).thenReturn(cart);

    mockMvc
        .perform(asBuyer(get("/api/buyer/cart")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CART"))
        .andExpect(jsonPath("$.itemCount").value(1))
        .andExpect(jsonPath("$.totalAmount").value(100.00));
  }

  @Test
  @DisplayName("GET /api/buyer/cart returns 500 when the buyer profile is missing")
  void getCartReturns500WhenProfileMissing() throws Exception {
    when(buyerRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

    mockMvc
        .perform(asBuyer(get("/api/buyer/cart")))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("Buyer profile not found"));

    verify(cartService, never()).getOrCreateCart(anyString());
  }

  @Test
  @DisplayName("POST /api/buyer/cart/items returns 200 when the item is added")
  void addToCartReturns200() throws Exception {
    givenBuyerProfile();
    when(cartService.addToCart(anyString(), eq("product-1"), eq(2), eq("wh-1"))).thenReturn(cart);

    mockMvc
        .perform(
            asBuyer(
                post("/api/buyer/cart/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"productId":"product-1","quantity":2,"warehouseId":"wh-1"}
                        """)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.itemCount").value(1));
  }

  @Test
  @DisplayName("POST /api/buyer/cart/items returns 400 when the payload is invalid")
  void addToCartReturns400OnInvalidPayload() throws Exception {
    mockMvc
        .perform(
            asBuyer(
                post("/api/buyer/cart/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"productId":"","quantity":0}
                        """)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").exists());

    verify(cartService, never()).addToCart(anyString(), anyString(), anyInt(), any());
  }

  @Test
  @DisplayName("POST /api/buyer/cart/items returns 400 when stock is insufficient")
  void addToCartReturns400OnInsufficientStock() throws Exception {
    givenBuyerProfile();
    when(cartService.addToCart(anyString(), anyString(), anyInt(), any()))
        .thenThrow(new InsufficientStockException("product-1", 5, 1));

    mockMvc
        .perform(
            asBuyer(
                post("/api/buyer/cart/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"productId":"product-1","quantity":5,"warehouseId":"wh-1"}
                        """)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Insufficient stock")));
  }

  @Test
  @DisplayName("POST /api/buyer/cart/submit returns 200 with the submitted order")
  void submitOrderReturns200() throws Exception {
    givenBuyerProfile();
    Order submitted = Order.createCart(BUYER_ID);
    submitted.addItem(new OrderItem("product-1", "Laptop", 1, new BigDecimal("50.00"), "wh-1"));
    submitted.submitForPayment("Calle 99");
    when(cartService.submitOrder(anyString(), eq("Calle 99"))).thenReturn(submitted);

    mockMvc
        .perform(
            asBuyer(
                post("/api/buyer/cart/submit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"shippingAddress":"Calle 99"}
                        """)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
        .andExpect(jsonPath("$.shippingAddress").value("Calle 99"));
  }

  @Test
  @DisplayName("POST /api/buyer/cart/submit returns 400 when there is no active cart")
  void submitOrderReturns400WhenNoCart() throws Exception {
    givenBuyerProfile();
    when(cartService.submitOrder(anyString(), any()))
        .thenThrow(new UnauthorizedOperationException("No active cart found"));

    mockMvc
        .perform(
            asBuyer(
                post("/api/buyer/cart/submit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"shippingAddress":"Calle 99"}
                        """)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("No active cart found"));
  }

  @Test
  @DisplayName("POST /api/buyer/orders/{id}/pay returns 200 when payment is confirmed")
  void confirmPaymentReturns200() throws Exception {
    givenBuyerProfile();
    Order paid = Order.createCart(BUYER_ID);
    paid.addItem(new OrderItem("product-1", "Laptop", 1, new BigDecimal("50.00"), "wh-1"));
    paid.confirmPayment("PAY-1");
    when(orderLifecycleService.confirmPayment(eq("order-1"), anyString(), eq("PAY-1")))
        .thenReturn(paid);

    mockMvc
        .perform(
            asBuyer(
                post("/api/buyer/orders/order-1/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"paymentReference":"PAY-1"}
                        """)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PAID"));
  }

  @Test
  @DisplayName("POST /api/buyer/orders/{id}/pay returns 400 when the order is not found")
  void confirmPaymentReturns400WhenOrderNotFound() throws Exception {
    givenBuyerProfile();
    when(orderLifecycleService.confirmPayment(anyString(), anyString(), any()))
        .thenThrow(new UnauthorizedOperationException("Order not found"));

    mockMvc
        .perform(
            asBuyer(
                post("/api/buyer/orders/order-1/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"paymentReference":"PAY-1"}
                        """)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Order not found"));
  }

  @Test
  @DisplayName("GET /api/buyer/orders returns 200 with the buyer orders")
  void myOrdersReturns200() throws Exception {
    givenBuyerProfile();
    when(orderRepository.findByBuyerId(anyString())).thenReturn(List.of(cart));

    mockMvc
        .perform(asBuyer(get("/api/buyer/orders")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].status").value("CART"));
  }

  @Test
  @DisplayName("GET /api/buyer/cart returns 401 when unauthenticated")
  void getCartReturns401WhenUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/buyer/cart")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /api/buyer/cart returns 403 when the role is not BUYER")
  void getCartReturns403ForWrongRole() throws Exception {
    mockMvc.perform(asSeller(get("/api/buyer/cart"))).andExpect(status().isForbidden());
  }
}
