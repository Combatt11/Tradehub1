package application.adapters.rest.controllers;

import application.adapters.rest.dto.AddInventoryRequest;
import application.adapters.rest.dto.CreateProductRequest;
import application.domain.entities.InventoryItem;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.exceptions.SellerNotFoundException;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.usecases.ports.AddInventoryUseCase;
import application.domain.usecases.ports.CreateProductUseCase;
import application.domain.usecases.ports.GetSellerProductsUseCase;
import application.domain.usecases.ports.PublishProductUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

  private final CreateProductUseCase createProductUseCase;
  private final PublishProductUseCase publishProductUseCase;
  private final AddInventoryUseCase addInventoryUseCase;
  private final GetSellerProductsUseCase getSellerProductsUseCase;
  private final SellerRepositoryPort sellerRepository;

  public SellerController(
      CreateProductUseCase createProductUseCase,
      PublishProductUseCase publishProductUseCase,
      AddInventoryUseCase addInventoryUseCase,
      GetSellerProductsUseCase getSellerProductsUseCase,
      SellerRepositoryPort sellerRepository) {
    this.createProductUseCase = createProductUseCase;
    this.publishProductUseCase = publishProductUseCase;
    this.addInventoryUseCase = addInventoryUseCase;
    this.getSellerProductsUseCase = getSellerProductsUseCase;
    this.sellerRepository = sellerRepository;
  }

  private String resolveSellerId(Authentication auth) {
    String userId = (String) auth.getPrincipal();
    return sellerRepository
        .findByUserId(userId)
        .map(Seller::getId)
        .orElseThrow(() -> new SellerNotFoundException(userId));
  }

  @PostMapping("/products")
  public ResponseEntity<?> createProduct(
      @Valid @RequestBody CreateProductRequest req, Authentication auth) {
    String sellerId = resolveSellerId(auth);
    Product product =
        createProductUseCase.execute(
            sellerId,
            req.getName(),
            req.getDescription(),
            Product.parseType(req.getType()),
            req.getPrice(),
            req.getSku());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            Map.of(
                "id",
                product.getId(),
                "name",
                product.getName(),
                "status",
                product.getStatus().name()));
  }

  @PostMapping("/products/{productId}/publish")
  public ResponseEntity<?> publish(@PathVariable String productId, Authentication auth) {
    String sellerId = resolveSellerId(auth);
    Product product = publishProductUseCase.execute(productId, sellerId);
    return ResponseEntity.ok(Map.of("id", product.getId(), "status", product.getStatus().name()));
  }

  @PostMapping("/inventory")
  public ResponseEntity<?> addInventory(
      @Valid @RequestBody AddInventoryRequest req, Authentication auth) {
    String sellerId = resolveSellerId(auth);
    InventoryItem item =
        addInventoryUseCase.execute(
            req.getProductId(), req.getWarehouseId(), req.getQuantity(), sellerId);
    return ResponseEntity.ok(
        Map.of(
            "id", item.getId(),
            "productId", item.getProductId(),
            "warehouseId", item.getWarehouseId(),
            "quantityAvailable", item.getQuantityAvailable()));
  }

  @GetMapping("/products")
  public ResponseEntity<?> myProducts(Authentication auth) {
    String sellerId = resolveSellerId(auth);
    List<Map<String, Object>> list =
        getSellerProductsUseCase.getProducts(sellerId).stream()
            .map(
                p ->
                    Map.<String, Object>of(
                        "id",
                        p.getId(),
                        "name",
                        p.getName(),
                        "price",
                        p.getPrice(),
                        "status",
                        p.getStatus().name(),
                        "type",
                        p.getType().name()))
            .collect(Collectors.toList());
    return ResponseEntity.ok(list);
  }
}
