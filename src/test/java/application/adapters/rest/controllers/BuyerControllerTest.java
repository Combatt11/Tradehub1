package application.adapters.rest.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.adapters.rest.GlobalExceptionHandler;
import application.adapters.security.JwtProvider;
import application.adapters.security.SecurityConfig;
import application.domain.exceptions.CartNotFoundException;
import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.OrderNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.ports.in.AddItemToCartUseCase;
import application.domain.ports.in.CancelOrderUseCase;
import application.domain.ports.in.ClearCartUseCase;
import application.domain.ports.in.ConfirmPaymentUseCase;
import application.domain.ports.in.GetBuyerOrdersUseCase;
import application.domain.ports.in.GetCartUseCase;
import application.domain.ports.in.SubmitOrderFromCartUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
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

  @MockBean private GetCartUseCase getCartUseCase;
  @MockBean private AddItemToCartUseCase addItemToCartUseCase;
  @MockBean private ClearCartUseCase clearCartUseCase;
  @MockBean private SubmitOrderFromCartUseCase submitOrderFromCartUseCase;
  @MockBean private ConfirmPaymentUseCase confirmPaymentUseCase;
  @MockBean private CancelOrderUseCase cancelOrderUseCase;
  @MockBean private GetBuyerOrdersUseCase getBuyerOrdersUseCase;
  @MockBean private BuyerRepositoryPort buyerRepository;
  @MockBean private JwtProvider jwtProvider;

  private Order cart;

  /**
   * Mirrors what JwtAuthFilter does in production: the authentication principal is the raw user id
   * String (not a UserDetails object).
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
    when(getCartUseCase.getOrCreateCart(anyString())).thenReturn(cart);

    mockMvc
        .perform(asBuyer(get("/api/buyer/cart")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CART"))
        .andExpect(jsonPath("$.itemCount").value(1))
        .andExpect(jsonPath("$.totalAmount").value(100.00));
  }

  @Test
  @DisplayName("GET /api/buyer/cart returns 404 when the buyer profile is missing")
  void getCartReturns404WhenProfileMissing() throws Exception {
    when(buyerRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

    mockMvc
        .perform(asBuyer(get("/api/buyer/cart")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Buyer not found: user-1"));

    verify(getCartUseCase, never()).getOrCreateCart(anyString());
  }

  @Test
  @DisplayName("POST /api/buyer/cart/items returns 200 when the item is added")
  void addToCartReturns200() throws Exception {
    givenBuyerProfile();
    when(addItemToCartUseCase.addItem(anyString(), eq("product-1"), eq(2), eq("wh-1")))
        .thenReturn(cart);

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

    verify(addItemToCartUseCase, never()).addItem(anyString(), anyString(), anyInt(), any());
  }

  @Test
  @DisplayName("POST /api/buyer/cart/items returns 400 when stock is insufficient")
  void addToCartReturns400OnInsufficientStock() throws Exception {
    givenBuyerProfile();
    when(addItemToCartUseCase.addItem(anyString(), anyString(), anyInt(), any()))
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
  @DisplayName("DELETE /api/buyer/cart returns 200 with the emptied cart")
  void clearCartReturns200() throws Exception {
    givenBuyerProfile();
    when(clearCartUseCase.clearCart(anyString())).thenReturn(Order.createCart(BUYER_ID));

    mockMvc
        .perform(asBuyer(delete("/api/buyer/cart")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CART"))
        .andExpect(jsonPath("$.itemCount").value(0))
        .andExpect(jsonPath("$.totalAmount").value(0.00));
  }

  @Test
  @DisplayName("POST /api/buyer/cart/submit returns 200 with the submitted order")
  void submitOrderReturns200() throws Exception {
    givenBuyerProfile();
    Order submitted = Order.createCart(BUYER_ID);
    submitted.addItem(new OrderItem("product-1", "Laptop", 1, new BigDecimal("50.00"), "wh-1"));
    submitted.submitForPayment("Calle 99");
    when(submitOrderFromCartUseCase.submitOrder(anyString(), eq("Calle 99"))).thenReturn(submitted);

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
  @DisplayName("POST /api/buyer/cart/submit returns 404 when there is no active cart")
  void submitOrderReturns404WhenNoCart() throws Exception {
    givenBuyerProfile();
    when(submitOrderFromCartUseCase.submitOrder(anyString(), any()))
        .thenThrow(new CartNotFoundException(BUYER_ID));

    mockMvc
        .perform(
            asBuyer(
                post("/api/buyer/cart/submit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"shippingAddress":"Calle 99"}
                        """)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("No active cart found for buyer: " + BUYER_ID));
  }

  @Test
  @DisplayName("POST /api/buyer/orders/{id}/pay returns 200 when payment is confirmed")
  void confirmPaymentReturns200() throws Exception {
    givenBuyerProfile();
    Order paid = Order.createCart(BUYER_ID);
    paid.addItem(new OrderItem("product-1", "Laptop", 1, new BigDecimal("50.00"), "wh-1"));
    paid.confirmPayment("PAY-1");
    when(confirmPaymentUseCase.confirmPayment(eq("order-1"), anyString(), eq("PAY-1")))
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
  @DisplayName("POST /api/buyer/orders/{id}/pay returns 404 when the order is not found")
  void confirmPaymentReturns404WhenOrderNotFound() throws Exception {
    givenBuyerProfile();
    when(confirmPaymentUseCase.confirmPayment(anyString(), anyString(), any()))
        .thenThrow(new OrderNotFoundException("order-1"));

    mockMvc
        .perform(
            asBuyer(
                post("/api/buyer/orders/order-1/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"paymentReference":"PAY-1"}
                        """)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Order not found: order-1"));
  }

  @Test
  @DisplayName("POST /api/buyer/orders/{id}/cancel returns 200 when the order is cancelled")
  void cancelOrderReturns200() throws Exception {
    givenBuyerProfile();
    Order cancelled = Order.createCart(BUYER_ID);
    cancelled.cancel();
    when(cancelOrderUseCase.cancelOrder(eq("order-1"), anyString())).thenReturn(cancelled);

    mockMvc
        .perform(asBuyer(post("/api/buyer/orders/order-1/cancel")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELLED"));
  }

  @Test
  @DisplayName("GET /api/buyer/orders returns 200 with the buyer orders")
  void myOrdersReturns200() throws Exception {
    givenBuyerProfile();
    when(getBuyerOrdersUseCase.getOrders(anyString())).thenReturn(List.of(cart));

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
