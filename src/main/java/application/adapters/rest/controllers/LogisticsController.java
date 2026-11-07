package application.adapters.rest.controllers;

import application.domain.models.Order;
import application.domain.services.order.OrderLifecycleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

    private final OrderLifecycleService orderLifecycleService;

    public LogisticsController(OrderLifecycleService orderLifecycleService) {
        this.orderLifecycleService = orderLifecycleService;
    }

    @PostMapping("/orders/{orderId}/prepare")
    public ResponseEntity<?> markPreparing(@PathVariable String orderId) {
        Order order = orderLifecycleService.markPreparing(orderId);
        return ResponseEntity.ok(Map.of("id", order.getId(), "status", order.getStatus().name()));
    }

    @PostMapping("/orders/{orderId}/dispatch")
    public ResponseEntity<?> markDispatched(@PathVariable String orderId) {
        Order order = orderLifecycleService.markDispatched(orderId);
        return ResponseEntity.ok(Map.of("id", order.getId(), "status", order.getStatus().name()));
    }

    @PostMapping("/orders/{orderId}/deliver")
    public ResponseEntity<?> markDelivered(@PathVariable String orderId) {
        Order order = orderLifecycleService.markDelivered(orderId);
        return ResponseEntity.ok(Map.of("id", order.getId(), "status", order.getStatus().name()));
    }
}
