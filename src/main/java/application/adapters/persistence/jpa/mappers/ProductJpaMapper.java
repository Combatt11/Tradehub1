package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.domain.models.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductJpaMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Product toDomain(ProductJpaEntity e) {
        if (e == null) return null;
        List<String> variants = new ArrayList<>();
        try {
            if (e.getVariantsJson() != null) {
                variants = objectMapper.readValue(e.getVariantsJson(), new TypeReference<>() {});
            }
        } catch (Exception ignored) {}
        return new Product(e.getId(), e.getSellerId(), e.getName(), e.getDescription(),
                Product.ProductType.valueOf(e.getType()), e.getPrice(),
                Product.ProductStatus.valueOf(e.getStatus()), variants, e.getSku());
    }

    public ProductJpaEntity toEntity(Product d) {
        if (d == null) return null;
        ProductJpaEntity e = new ProductJpaEntity();
        e.setId(d.getId());
        e.setSellerId(d.getSellerId());
        e.setName(d.getName());
        e.setDescription(d.getDescription());
        e.setType(d.getType().name());
        e.setPrice(d.getPrice());
        e.setStatus(d.getStatus().name());
        e.setSku(d.getSku());
        try {
            e.setVariantsJson(objectMapper.writeValueAsString(d.getVariants()));
        } catch (Exception ignored) {
            e.setVariantsJson("[]");
        }
        return e;
    }
}
