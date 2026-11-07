package application.domain.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Product {
    public enum ProductType { PHYSICAL, DIGITAL }
    public enum ProductStatus { DRAFT, PUBLISHED, SUSPENDED, DISCONTINUED }

    private final String id;
    private final String sellerId;
    private String name;
    private String description;
    private ProductType type;
    private BigDecimal price;
    private ProductStatus status;
    private List<String> variants;
    private String sku;

    public Product(String id, String sellerId, String name, String description,
                   ProductType type, BigDecimal price, ProductStatus status,
                   List<String> variants, String sku) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.sellerId = Objects.requireNonNull(sellerId);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.type = Objects.requireNonNull(type);
        this.price = Objects.requireNonNull(price);
        this.status = status != null ? status : ProductStatus.DRAFT;
        this.variants = variants != null ? new ArrayList<>(variants) : new ArrayList<>();
        this.sku = sku;
    }

    public static Product create(String sellerId, String name, String description,
                                 ProductType type, BigDecimal price, String sku) {
        return new Product(null, sellerId, name, description, type, price, ProductStatus.DRAFT, new ArrayList<>(), sku);
    }

    public void publish() { this.status = ProductStatus.PUBLISHED; }
    public void suspend() { this.status = ProductStatus.SUSPENDED; }
    public void discontinue() { this.status = ProductStatus.DISCONTINUED; }
    public boolean isAvailableForSale() { return ProductStatus.PUBLISHED.equals(this.status); }
    public boolean isPhysical() { return ProductType.PHYSICAL.equals(this.type); }

    public String getId() { return id; }
    public String getSellerId() { return sellerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ProductType getType() { return type; }
    public BigDecimal getPrice() { return price; }
    public ProductStatus getStatus() { return status; }
    public List<String> getVariants() { return List.copyOf(variants); }
    public String getSku() { return sku; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = Objects.requireNonNull(price); }
    public void addVariant(String variant) {
        if (variant != null && !variant.isBlank()) this.variants.add(variant);
    }
}
