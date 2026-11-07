package application.domain.models;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderItem {
    private final String productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final String warehouseId;

    public OrderItem(String productId, String productName, int quantity,
                     BigDecimal unitPrice, String warehouseId) {
        this.productId = Objects.requireNonNull(productId);
        this.productName = productName;
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice);
        this.warehouseId = warehouseId;
    }

    public BigDecimal getSubtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getWarehouseId() { return warehouseId; }
}
