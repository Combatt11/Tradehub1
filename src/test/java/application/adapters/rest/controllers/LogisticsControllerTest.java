package application.adapters.rest.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.adapters.rest.GlobalExceptionHandler;
import application.adapters.security.JwtProvider;
import application.domain.entities.Order;
import application.domain.exceptions.OrderNotFoundException;
import application.domain.usecases.ports.MarkOrderDeliveredUseCase;
import application.domain.usecases.ports.MarkOrderDispatchedUseCase;
import application.domain.usecases.ports.MarkOrderPreparingUseCase;
import application.frameworks.config.SecurityConfig;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(LogisticsController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("LogisticsController")
class LogisticsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private MarkOrderPreparingUseCase markOrderPreparingUseCase;
  @MockBean private MarkOrderDispatchedUseCase markOrderDispatchedUseCase;
  @MockBean private MarkOrderDeliveredUseCase markOrderDeliveredUseCase;
  @MockBean private JwtProvider jwtProvider;

  private static MockHttpServletRequestBuilder asLogistics(MockHttpServletRequestBuilder builder) {
    return builder.with(
        authentication(
            new UsernamePasswordAuthenticationToken(
                "logistics-1",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_LOGISTICS_OPERATOR")))));
  }

  private static MockHttpServletRequestBuilder asBuyer(MockHttpServletRequestBuilder builder) {
    return builder.with(
        authentication(
            new UsernamePasswordAuthenticationToken(
                "buyer-1", null, List.of(new SimpleGrantedAuthority("ROLE_BUYER")))));
  }

  private static Order order(Order.OrderStatus status) {
    return new Order(
        "order-1",
        "buyer-1",
        status,
        List.of(),
        new BigDecimal("10.00"),
        "Calle 1",
        Instant.parse("2024-01-01T00:00:00Z"),
        Instant.parse("2024-01-01T00:00:00Z"),
        null);
  }

  @Test
  @DisplayName("POST /api/logistics/orders/{id}/prepare returns 200")
  void markPreparingReturns200() throws Exception {
    when(markOrderPreparingUseCase.markPreparing("order-1"))
        .thenReturn(order(Order.OrderStatus.PREPARING));

    mockMvc
        .perform(asLogistics(post("/api/logistics/orders/order-1/prepare")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("order-1"))
        .andExpect(jsonPath("$.status").value("PREPARING"));
  }

  @Test
  @DisplayName("POST /api/logistics/orders/{id}/dispatch returns 200")
  void markDispatchedReturns200() throws Exception {
    when(markOrderDispatchedUseCase.markDispatched("order-1"))
        .thenReturn(order(Order.OrderStatus.DISPATCHED));

    mockMvc
        .perform(asLogistics(post("/api/logistics/orders/order-1/dispatch")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DISPATCHED"));
  }

  @Test
  @DisplayName("POST /api/logistics/orders/{id}/deliver returns 200")
  void markDeliveredReturns200() throws Exception {
    when(markOrderDeliveredUseCase.markDelivered("order-1"))
        .thenReturn(order(Order.OrderStatus.DELIVERED));

    mockMvc
        .perform(asLogistics(post("/api/logistics/orders/order-1/deliver")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DELIVERED"));
  }

  @Test
  @DisplayName("POST /api/logistics/orders/{id}/prepare returns 404 when the order is missing")
  void markPreparingReturns404WhenMissing() throws Exception {
    when(markOrderPreparingUseCase.markPreparing(anyString()))
        .thenThrow(new OrderNotFoundException("order-404"));

    mockMvc
        .perform(asLogistics(post("/api/logistics/orders/order-404/prepare")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Order not found: order-404"));
  }

  @Test
  @DisplayName("POST /api/logistics/orders/{id}/dispatch returns 404 when the order is missing")
  void markDispatchedReturns404WhenMissing() throws Exception {
    when(markOrderDispatchedUseCase.markDispatched(anyString()))
        .thenThrow(new OrderNotFoundException("order-404"));

    mockMvc
        .perform(asLogistics(post("/api/logistics/orders/order-404/dispatch")))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/logistics/orders/{id}/deliver returns 404 when the order is missing")
  void markDeliveredReturns404WhenMissing() throws Exception {
    when(markOrderDeliveredUseCase.markDelivered(anyString()))
        .thenThrow(new OrderNotFoundException("order-404"));

    mockMvc
        .perform(asLogistics(post("/api/logistics/orders/order-404/deliver")))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/logistics/orders/{id}/prepare returns 401 when unauthenticated")
  void markPreparingReturns401WhenUnauthenticated() throws Exception {
    mockMvc
        .perform(post("/api/logistics/orders/order-1/prepare"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /api/logistics/orders/{id}/prepare returns 403 for a non-logistics role")
  void markPreparingReturns403ForWrongRole() throws Exception {
    mockMvc
        .perform(asBuyer(post("/api/logistics/orders/order-1/prepare")))
        .andExpect(status().isForbidden());
  }
}
