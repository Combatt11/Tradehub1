package application.domain.models;

import application.domain.exceptions.InvalidProductNameException;
import application.domain.exceptions.InvalidProductPriceException;
import application.domain.exceptions.InvalidProductTypeException;
import application.domain.exceptions.ProductAlreadyDiscontinuedException;
import application.domain.exceptions.ProductAlreadyPublishedException;
import application.domain.exceptions.ProductAlreadySuspendedException;
import application.domain.exceptions.ProductNotAvailableException;
import application.domain.exceptions.ProductNotPhysicalException;
import application.domain.exceptions.ProductNotPublishableException;
import application.domain.exceptions.ProductOwnershipException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class Product {
  public enum ProductType {
    PHYSICAL,
    DIGITAL
  }

  public enum ProductStatus {
    DRAFT,
    PUBLISHED,
    SUSPENDED,
    DISCONTINUED
  }

  private final String id;
  private final String sellerId;
  private String name;
  private String description;
  private ProductType type;
  private BigDecimal price;
  private ProductStatus status;
  private List<String> variants;
  private String sku;

  public Product(
      String id,
      String sellerId,
      String name,
      String description,
      ProductType type,
      BigDecimal price,
      ProductStatus status,
      List<String> variants,
      String sku) {
    this.id = id != null ? id : UUID.randomUUID().toString();
    this.sellerId = Objects.requireNonNull(sellerId);
    if (name == null || name.isBlank()) throw new InvalidProductNameException();
    if (price == null || price.signum() <= 0) {
      throw new InvalidProductPriceException(String.valueOf(price));
    }
    if (type == null) throw new InvalidProductTypeException(null);
    this.name = name;
    this.description = description;
    this.type = type;
    this.price = price;
    this.status = status != null ? status : ProductStatus.DRAFT;
    this.variants = variants != null ? new ArrayList<>(variants) : new ArrayList<>();
    this.sku = sku;
  }

  public static ProductType parseType(String rawType) {
    if (rawType == null || rawType.isBlank()) {
      throw new InvalidProductTypeException(rawType);
    }
    try {
      return ProductType.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new InvalidProductTypeException(rawType);
    }
  }

  public static Product create(
      String sellerId,
      String name,
      String description,
      ProductType type,
      BigDecimal price,
      String sku) {
    return new Product(
        null,
        sellerId,
        name,
        description,
        type,
        price,
        ProductStatus.DRAFT,
        new ArrayList<>(),
        sku);
  }

  public void publish() {
    if (status == ProductStatus.PUBLISHED) throw new ProductAlreadyPublishedException(id);
    if (status == ProductStatus.DISCONTINUED) {
      throw new ProductNotPublishableException(id, status.name());
    }
    this.status = ProductStatus.PUBLISHED;
  }

  public void suspend() {
    if (status == ProductStatus.SUSPENDED) throw new ProductAlreadySuspendedException(id);
    this.status = ProductStatus.SUSPENDED;
  }

  public void discontinue() {
    if (status == ProductStatus.DISCONTINUED) {
      throw new ProductAlreadyDiscontinuedException(id);
    }
    this.status = ProductStatus.DISCONTINUED;
  }

  public boolean isAvailableForSale() {
    return ProductStatus.PUBLISHED.equals(this.status);
  }

  public boolean isPhysical() {
    return ProductType.PHYSICAL.equals(this.type);
  }

  public void ensureAvailableForSale() {
    if (!isAvailableForSale()) {
      throw new ProductNotAvailableException(id, status.name());
    }
  }

  public void ensurePhysical() {
    if (!isPhysical()) {
      throw new ProductNotPhysicalException(id);
    }
  }

  public void ensureOwnedBy(String sellerId) {
    if (!this.sellerId.equals(sellerId)) {
      throw new ProductOwnershipException(id, sellerId);
    }
  }

  public String getId() {
    return id;
  }

  public String getSellerId() {
    return sellerId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public ProductType getType() {
    return type;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public ProductStatus getStatus() {
    return status;
  }

  public List<String> getVariants() {
    return List.copyOf(variants);
  }

  public String getSku() {
    return sku;
  }

  public void setName(String name) {
    if (name == null || name.isBlank()) throw new InvalidProductNameException();
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setPrice(BigDecimal price) {
    if (price == null || price.signum() <= 0) {
      throw new InvalidProductPriceException(String.valueOf(price));
    }
    this.price = price;
  }

  public void addVariant(String variant) {
    if (variant != null && !variant.isBlank()) this.variants.add(variant);
  }
}
