package application.adapters.rest.controllers;

import application.domain.models.Order;
import application.domain.services.order.MarkOrderDeliveredService;
import application.domain.services.order.MarkOrderDispatchedService;
import application.domain.services.order.MarkOrderPreparingService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

  private final MarkOrderPreparingService markOrderPreparingService;
  private final MarkOrderDispatchedService markOrderDispatchedService;
  private final MarkOrderDeliveredService markOrderDeliveredService;

  public LogisticsController(
      MarkOrderPreparingService markOrderPreparingService,
      MarkOrderDispatchedService markOrderDispatchedService,
      MarkOrderDeliveredService markOrderDeliveredService) {
    this.markOrderPreparingService = markOrderPreparingService;
    this.markOrderDispatchedService = markOrderDispatchedService;
    this.markOrderDeliveredService = markOrderDeliveredService;
  }

  @PostMapping("/orders/{orderId}/prepare")
  public ResponseEntity<?> markPreparing(@PathVariable String orderId) {
    Order order = markOrderPreparingService.markPreparing(orderId);
    return ResponseEntity.ok(Map.of("id", order.getId(), "status", order.getStatus().name()));
  }

  @PostMapping("/orders/{orderId}/dispatch")
  public ResponseEntity<?> markDispatched(@PathVariable String orderId) {
    Order order = markOrderDispatchedService.markDispatched(orderId);
    return ResponseEntity.ok(Map.of("id", order.getId(), "status", order.getStatus().name()));
  }

  @PostMapping("/orders/{orderId}/deliver")
  public ResponseEntity<?> markDelivered(@PathVariable String orderId) {
    Order order = markOrderDeliveredService.markDelivered(orderId);
    return ResponseEntity.ok(Map.of("id", order.getId(), "status", order.getStatus().name()));
  }
}
