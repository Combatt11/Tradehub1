package application.adapters.rest.controllers;

import application.domain.entities.Product;
import application.domain.usecases.ports.BrowseCatalogUseCase;
import application.domain.usecases.ports.GetProductUseCase;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

  private final BrowseCatalogUseCase browseCatalogUseCase;
  private final GetProductUseCase getProductUseCase;

  public CatalogController(
      BrowseCatalogUseCase browseCatalogUseCase, GetProductUseCase getProductUseCase) {
    this.browseCatalogUseCase = browseCatalogUseCase;
    this.getProductUseCase = getProductUseCase;
  }

  @GetMapping("/products")
  public ResponseEntity<List<Map<String, Object>>> listPublished() {
    List<Map<String, Object>> products =
        browseCatalogUseCase.browsePublishedProducts().stream()
            .map(this::toMap)
            .collect(Collectors.toList());
    return ResponseEntity.ok(products);
  }

  @GetMapping("/products/{id}")
  public ResponseEntity<?> getById(@PathVariable String id) {
    return getProductUseCase
        .getPublishedProduct(id)
        .map(p -> ResponseEntity.ok(toMap(p)))
        .orElse(ResponseEntity.notFound().build());
  }

  private Map<String, Object> toMap(Product p) {
    return Map.of(
        "id", p.getId(),
        "name", p.getName(),
        "description", p.getDescription() != null ? p.getDescription() : "",
        "type", p.getType().name(),
        "price", p.getPrice(),
        "sku", p.getSku() != null ? p.getSku() : "",
        "sellerId", p.getSellerId());
  }
}
