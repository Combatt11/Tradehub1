package application.adapters.rest.controllers;

import application.domain.models.Order;
import application.domain.ports.in.MarkOrderDeliveredUseCase;
import application.domain.ports.in.MarkOrderDispatchedUseCase;
import application.domain.ports.in.MarkOrderPreparingUseCase;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

  private final MarkOrderPreparingUseCase markOrderPreparingUseCase;
  private final MarkOrderDispatchedUseCase markOrderDispatchedUseCase;
  private final MarkOrderDeliveredUseCase markOrderDeliveredUseCase;

  public LogisticsController(
      MarkOrderPreparingUseCase markOrderPreparingUseCase,
      MarkOrderDispatchedUseCase markOrderDispatchedUseCase,
      MarkOrderDeliveredUseCase markOrderDeliveredUseCase) {
    this.markOrderPreparingUseCase = markOrderPreparingUseCase;
    this.markOrderDispatchedUseCase = markOrderDispatchedUseCase;
    this.markOrderDeliveredUseCase = markOrderDeliveredUseCase;
  }

  @PostMapping("/orders/{orderId}/prepare")
  public ResponseEntity<?> markPreparing(@PathVariable String orderId) {
    Order order = markOrderPreparingUseCase.markPreparing(orderId);
    return ResponseEntity.ok(Map.of("id", order.getId(), "status", order.getStatus().name()));
  }

  @PostMapping("/orders/{orderId}/dispatch")
  public ResponseEntity<?> markDispatched(@PathVariable String orderId) {
    Order order = markOrderDispatchedUseCase.markDispatched(orderId);
    return ResponseEntity.ok(Map.of("id", order.getId(), "status", order.getStatus().name()));
  }

  @PostMapping("/orders/{orderId}/deliver")
  public ResponseEntity<?> markDelivered(@PathVariable String orderId) {
    Order order = markOrderDeliveredUseCase.markDelivered(orderId);
    return ResponseEntity.ok(Map.of("id", order.getId(), "status", order.getStatus().name()));
  }
}
