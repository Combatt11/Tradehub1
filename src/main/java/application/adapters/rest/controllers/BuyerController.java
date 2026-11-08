package application.adapters.rest.controllers;

import application.adapters.rest.dto.AddToCartRequest;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.order.AddItemToCartService;
import application.domain.services.order.ClearCartService;
import application.domain.services.order.ConfirmPaymentService;
import application.domain.services.order.GetCartService;
import application.domain.services.order.SubmitOrderFromCartService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buyer")
public class BuyerController {

  private final GetCartService getCartService;
  private final AddItemToCartService addItemToCartService;
  private final ClearCartService clearCartService;
  private final SubmitOrderFromCartService submitOrderFromCartService;
  private final ConfirmPaymentService confirmPaymentService;
  private final BuyerRepositoryPort buyerRepository;
  private final OrderRepositoryPort orderRepository;

  public BuyerController(
      GetCartService getCartService,
      AddItemToCartService addItemToCartService,
      ClearCartService clearCartService,
      SubmitOrderFromCartService submitOrderFromCartService,
      ConfirmPaymentService confirmPaymentService,
      BuyerRepositoryPort buyerRepository,
      OrderRepositoryPort orderRepository) {
    this.getCartService = getCartService;
    this.addItemToCartService = addItemToCartService;
    this.clearCartService = clearCartService;
    this.submitOrderFromCartService = submitOrderFromCartService;
    this.confirmPaymentService = confirmPaymentService;
    this.buyerRepository = buyerRepository;
    this.orderRepository = orderRepository;
  }

  private String resolveBuyerId(Authentication auth) {
    String userId = (String) auth.getPrincipal();
    return buyerRepository
        .findByUserId(userId)
        .map(Buyer::getId)
        .orElseThrow(() -> new RuntimeException("Buyer profile not found"));
  }

  @GetMapping("/cart")
  public ResponseEntity<?> getCart(Authentication auth) {
    String buyerId = resolveBuyerId(auth);
    Order cart = getCartService.getOrCreateCart(buyerId);
    return ResponseEntity.ok(toOrderMap(cart));
  }

  @PostMapping("/cart/items")
  public ResponseEntity<?> addToCart(
      @Valid @RequestBody AddToCartRequest req, Authentication auth) {
    String buyerId = resolveBuyerId(auth);
    Order cart =
        addItemToCartService.addItem(
            buyerId, req.getProductId(), req.getQuantity(), req.getWarehouseId());
    return ResponseEntity.ok(toOrderMap(cart));
  }

  @DeleteMapping("/cart")
  public ResponseEntity<?> clearCart(Authentication auth) {
    String buyerId = resolveBuyerId(auth);
    Order cart = clearCartService.clearCart(buyerId);
    return ResponseEntity.ok(toOrderMap(cart));
  }

  @PostMapping("/cart/submit")
  public ResponseEntity<?> submitOrder(@RequestBody Map<String, String> body, Authentication auth) {
    String buyerId = resolveBuyerId(auth);
    Order order = submitOrderFromCartService.submitOrder(buyerId, body.get("shippingAddress"));
    return ResponseEntity.ok(toOrderMap(order));
  }

  @PostMapping("/orders/{orderId}/pay")
  public ResponseEntity<?> confirmPayment(
      @PathVariable String orderId, @RequestBody Map<String, String> body, Authentication auth) {
    String buyerId = resolveBuyerId(auth);
    Order order =
        confirmPaymentService.confirmPayment(orderId, buyerId, body.get("paymentReference"));
    return ResponseEntity.ok(toOrderMap(order));
  }

  @GetMapping("/orders")
  public ResponseEntity<?> myOrders(Authentication auth) {
    String buyerId = resolveBuyerId(auth);
    List<Map<String, Object>> orders =
        orderRepository.findByBuyerId(buyerId).stream()
            .map(this::toOrderMap)
            .collect(Collectors.toList());
    return ResponseEntity.ok(orders);
  }

  private Map<String, Object> toOrderMap(Order o) {
    return Map.of(
        "id", o.getId(),
        "status", o.getStatus().name(),
        "totalAmount", o.getTotalAmount(),
        "itemCount", o.getItems().size(),
        "shippingAddress", o.getShippingAddress() != null ? o.getShippingAddress() : "",
        "createdAt", o.getCreatedAt().toString());
  }
}
