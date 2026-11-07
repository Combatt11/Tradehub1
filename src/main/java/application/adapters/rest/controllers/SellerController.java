package application.adapters.rest.controllers;

import application.adapters.rest.dto.AddInventoryRequest;
import application.adapters.rest.dto.CreateProductRequest;
import application.domain.models.InventoryItem;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.inventory.AddInventoryService;
import application.domain.services.product.CreateProductService;
import application.domain.services.product.PublishProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    private final CreateProductService createProductService;
    private final PublishProductService publishProductService;
    private final AddInventoryService addInventoryService;
    private final SellerRepositoryPort sellerRepository;
    private final application.domain.ports.out.ProductRepositoryPort productRepository;

    public SellerController(CreateProductService createProductService,
                            PublishProductService publishProductService,
                            AddInventoryService addInventoryService,
                            SellerRepositoryPort sellerRepository,
                            application.domain.ports.out.ProductRepositoryPort productRepository) {
        this.createProductService = createProductService;
        this.publishProductService = publishProductService;
        this.addInventoryService = addInventoryService;
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
    }

    private String resolveSellerId(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        return sellerRepository.findByUserId(userId)
                .map(Seller::getId)
                .orElseThrow(() -> new RuntimeException("Seller profile not found"));
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductRequest req, Authentication auth) {
        String sellerId = resolveSellerId(auth);
        Product product = createProductService.execute(
                sellerId, req.getName(), req.getDescription(),
                Product.ProductType.valueOf(req.getType().toUpperCase()),
                req.getPrice(), req.getSku());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", product.getId(), "name", product.getName(), "status", product.getStatus().name()));
    }

    @PostMapping("/products/{productId}/publish")
    public ResponseEntity<?> publish(@PathVariable String productId, Authentication auth) {
        String sellerId = resolveSellerId(auth);
        Product product = publishProductService.execute(productId, sellerId);
        return ResponseEntity.ok(Map.of("id", product.getId(), "status", product.getStatus().name()));
    }

    @PostMapping("/inventory")
    public ResponseEntity<?> addInventory(@Valid @RequestBody AddInventoryRequest req, Authentication auth) {
        String sellerId = resolveSellerId(auth);
        InventoryItem item = addInventoryService.execute(
                req.getProductId(), req.getWarehouseId(), req.getQuantity(), sellerId);
        return ResponseEntity.ok(Map.of(
                "id", item.getId(),
                "productId", item.getProductId(),
                "warehouseId", item.getWarehouseId(),
                "quantityAvailable", item.getQuantityAvailable()));
    }

    @GetMapping("/products")
    public ResponseEntity<?> myProducts(Authentication auth) {
        String sellerId = resolveSellerId(auth);
        List<Map<String, Object>> list = productRepository.findBySellerId(sellerId).stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(), "name", p.getName(),
                        "price", p.getPrice(), "status", p.getStatus().name(),
                        "type", p.getType().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
