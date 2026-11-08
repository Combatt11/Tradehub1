package application.adapters.rest.controllers;

import application.adapters.rest.dto.RegisterSellerRequest;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.ports.in.ApproveSellerUseCase;
import application.domain.ports.in.RegisterMarketplaceWarehouseUseCase;
import application.domain.ports.in.RegisterSellerUseCase;
import application.domain.ports.in.RegisterSellerWarehouseUseCase;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private final RegisterSellerUseCase registerSellerUseCase;
  private final ApproveSellerUseCase approveSellerUseCase;
  private final RegisterMarketplaceWarehouseUseCase registerMarketplaceWarehouseUseCase;
  private final RegisterSellerWarehouseUseCase registerSellerWarehouseUseCase;

  public AdminController(
      RegisterSellerUseCase registerSellerUseCase,
      ApproveSellerUseCase approveSellerUseCase,
      RegisterMarketplaceWarehouseUseCase registerMarketplaceWarehouseUseCase,
      RegisterSellerWarehouseUseCase registerSellerWarehouseUseCase) {
    this.registerSellerUseCase = registerSellerUseCase;
    this.approveSellerUseCase = approveSellerUseCase;
    this.registerMarketplaceWarehouseUseCase = registerMarketplaceWarehouseUseCase;
    this.registerSellerWarehouseUseCase = registerSellerWarehouseUseCase;
  }

  @PostMapping("/sellers")
  public ResponseEntity<?> registerSeller(@Valid @RequestBody RegisterSellerRequest req) {
    Seller seller =
        registerSellerUseCase.execute(
            req.getFullName(),
            req.getEmail(),
            req.getPassword(),
            req.getIdentityDocument(),
            req.getBusinessName(),
            req.getTaxId(),
            req.getContactPhone());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            Map.of(
                "id", seller.getId(),
                "userId", seller.getUserId(),
                "businessName", seller.getBusinessName(),
                "status", seller.getStatus().name()));
  }

  @PostMapping("/sellers/{sellerId}/approve")
  public ResponseEntity<?> approveSeller(@PathVariable String sellerId) {
    Seller seller = approveSellerUseCase.approveSeller(sellerId);
    return ResponseEntity.ok(Map.of("id", seller.getId(), "status", seller.getStatus().name()));
  }

  @PostMapping("/warehouses/marketplace")
  public ResponseEntity<?> createMarketplaceWarehouse(@RequestBody Map<String, String> body) {
    Warehouse wh =
        registerMarketplaceWarehouseUseCase.registerMarketplaceWarehouse(
            body.get("name"), body.get("address"));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("id", wh.getId(), "name", wh.getName(), "type", wh.getType().name()));
  }

  @PostMapping("/warehouses/seller")
  public ResponseEntity<?> createSellerWarehouse(@RequestBody Map<String, String> body) {
    Warehouse wh =
        registerSellerWarehouseUseCase.registerSellerWarehouse(
            body.get("name"), body.get("address"), body.get("sellerId"));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("id", wh.getId(), "name", wh.getName(), "sellerId", wh.getSellerId()));
  }
}
