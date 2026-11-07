package application.adapters.rest.controllers;

import application.domain.models.Product;
import application.domain.ports.out.ProductRepositoryPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final ProductRepositoryPort productRepository;

    public CatalogController(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Map<String, Object>>> listPublished() {
        List<Map<String, Object>> products = productRepository.findPublished().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return productRepository.findById(id)
                .filter(Product::isAvailableForSale)
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
                "sellerId", p.getSellerId()
        );
    }
}
