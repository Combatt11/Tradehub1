package application.adapters.rest.controllers;

import application.adapters.rest.dto.AddToCartRequest;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.ports.in.AddItemToCartUseCase;
import application.domain.ports.in.ClearCartUseCase;
import application.domain.ports.in.ConfirmPaymentUseCase;
import application.domain.ports.in.GetBuyerOrdersUseCase;
import application.domain.ports.in.GetCartUseCase;
import application.domain.ports.in.SubmitOrderFromCartUseCase;
import application.domain.ports.out.BuyerRepositoryPort;
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

  private final GetCartUseCase getCartUseCase;
  private final AddItemToCartUseCase addItemToCartUseCase;
  private final ClearCartUseCase clearCartUseCase;
  private final SubmitOrderFromCartUseCase submitOrderFromCartUseCase;
  private final ConfirmPaymentUseCase confirmPaymentUseCase;
  private final GetBuyerOrdersUseCase getBuyerOrdersUseCase;
  private final BuyerRepositoryPort buyerRepository;

  public BuyerController(
      GetCartUseCase getCartUseCase,
      AddItemToCartUseCase addItemToCartUseCase,
      ClearCartUseCase clearCartUseCase,
      SubmitOrderFromCartUseCase submitOrderFromCartUseCase,
      ConfirmPaymentUseCase confirmPaymentUseCase,
      GetBuyerOrdersUseCase getBuyerOrdersUseCase,
      BuyerRepositoryPort buyerRepository) {
    this.getCartUseCase = getCartUseCase;
    this.addItemToCartUseCase = addItemToCartUseCase;
    this.clearCartUseCase = clearCartUseCase;
    this.submitOrderFromCartUseCase = submitOrderFromCartUseCase;
    this.confirmPaymentUseCase = confirmPaymentUseCase;
    this.getBuyerOrdersUseCase = getBuyerOrdersUseCase;
    this.buyerRepository = buyerRepository;
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
    Order cart = getCartUseCase.getOrCreateCart(buyerId);
    return ResponseEntity.ok(toOrderMap(cart));
  }

  @PostMapping("/cart/items")
  public ResponseEntity<?> addToCart(
      @Valid @RequestBody AddToCartRequest req, Authentication auth) {
    String buyerId = resolveBuyerId(auth);
    Order cart =
        addItemToCartUseCase.addItem(
            buyerId, req.getProductId(), req.getQuantity(), req.getWarehouseId());
    return ResponseEntity.ok(toOrderMap(cart));
  }

  @DeleteMapping("/cart")
  public ResponseEntity<?> clearCart(Authentication auth) {
    String buyerId = resolveBuyerId(auth);
    Order cart = clearCartUseCase.clearCart(buyerId);
    return ResponseEntity.ok(toOrderMap(cart));
  }

  @PostMapping("/cart/submit")
  public ResponseEntity<?> submitOrder(@RequestBody Map<String, String> body, Authentication auth) {
    String buyerId = resolveBuyerId(auth);
    Order order = submitOrderFromCartUseCase.submitOrder(buyerId, body.get("shippingAddress"));
    return ResponseEntity.ok(toOrderMap(order));
  }

  @PostMapping("/orders/{orderId}/pay")
  public ResponseEntity<?> confirmPayment(
      @PathVariable String orderId, @RequestBody Map<String, String> body, Authentication auth) {
    String buyerId = resolveBuyerId(auth);
    Order order =
        confirmPaymentUseCase.confirmPayment(orderId, buyerId, body.get("paymentReference"));
    return ResponseEntity.ok(toOrderMap(order));
  }

  @GetMapping("/orders")
  public ResponseEntity<?> myOrders(Authentication auth) {
    String buyerId = resolveBuyerId(auth);
    List<Map<String, Object>> orders =
        getBuyerOrdersUseCase.getOrders(buyerId).stream()
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
