package application.adapters.rest.controllers;

import application.adapters.rest.dto.RegisterSellerRequest;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.seller.RegisterSellerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RegisterSellerService registerSellerService;
    private final SellerRepositoryPort sellerRepository;
    private final WarehouseRepositoryPort warehouseRepository;

    public AdminController(RegisterSellerService registerSellerService,
                           SellerRepositoryPort sellerRepository,
                           WarehouseRepositoryPort warehouseRepository) {
        this.registerSellerService = registerSellerService;
        this.sellerRepository = sellerRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @PostMapping("/sellers")
    public ResponseEntity<?> registerSeller(@Valid @RequestBody RegisterSellerRequest req) {
        Seller seller = registerSellerService.execute(
                req.getFullName(), req.getEmail(), req.getPassword(), req.getIdentityDocument(),
                req.getBusinessName(), req.getTaxId(), req.getContactPhone());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", seller.getId(),
                "userId", seller.getUserId(),
                "businessName", seller.getBusinessName(),
                "status", seller.getStatus().name()
        ));
    }

    @PostMapping("/sellers/{sellerId}/approve")
    public ResponseEntity<?> approveSeller(@PathVariable String sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        seller.approve();
        sellerRepository.save(seller);
        return ResponseEntity.ok(Map.of("id", seller.getId(), "status", seller.getStatus().name()));
    }

    @PostMapping("/warehouses/marketplace")
    public ResponseEntity<?> createMarketplaceWarehouse(@RequestBody Map<String, String> body) {
        Warehouse wh = Warehouse.createMarketplace(body.get("name"), body.get("address"));
        wh = warehouseRepository.save(wh);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", wh.getId(), "name", wh.getName(), "type", wh.getType().name()));
    }

    @PostMapping("/warehouses/seller")
    public ResponseEntity<?> createSellerWarehouse(@RequestBody Map<String, String> body) {
        Warehouse wh = Warehouse.createForSeller(body.get("name"), body.get("address"), body.get("sellerId"));
        wh = warehouseRepository.save(wh);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", wh.getId(), "name", wh.getName(), "sellerId", wh.getSellerId()));
    }
}
