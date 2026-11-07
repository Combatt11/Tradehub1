package application.domain.models;

import java.util.Objects;
import java.util.UUID;

public class InventoryItem {
    public enum MovementType { INBOUND, RESERVATION, SALE_OUTBOUND, ADJUSTMENT, RETURN }

    private final String id;
    private final String productId;
    private final String warehouseId;
    private int quantityAvailable;
    private int quantityReserved;
    private int quantityDamaged;

    public InventoryItem(String id, String productId, String warehouseId,
                         int quantityAvailable, int quantityReserved, int quantityDamaged) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.productId = Objects.requireNonNull(productId);
        this.warehouseId = Objects.requireNonNull(warehouseId);
        this.quantityAvailable = Math.max(0, quantityAvailable);
        this.quantityReserved = Math.max(0, quantityReserved);
        this.quantityDamaged = Math.max(0, quantityDamaged);
    }

    public static InventoryItem create(String productId, String warehouseId, int initialQuantity) {
        return new InventoryItem(null, productId, warehouseId, initialQuantity, 0, 0);
    }

    public void addStock(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Cannot add negative stock");
        this.quantityAvailable += amount;
    }

    public void reserve(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (quantityAvailable < amount) {
            throw new IllegalStateException("Insufficient available stock. Available: " + quantityAvailable);
        }
        this.quantityAvailable -= amount;
        this.quantityReserved += amount;
    }

    public void releaseReservation(int amount) {
        if (amount <= 0) return;
        int toRelease = Math.min(amount, quantityReserved);
        this.quantityReserved -= toRelease;
        this.quantityAvailable += toRelease;
    }

    public void confirmSale(int amount) {
        if (amount <= 0) return;
        int toConfirm = Math.min(amount, quantityReserved);
        this.quantityReserved -= toConfirm;
    }

    public void markDamaged(int amount) {
        if (amount <= 0) return;
        if (quantityAvailable < amount) {
            throw new IllegalStateException("Not enough available stock to mark as damaged");
        }
        this.quantityAvailable -= amount;
        this.quantityDamaged += amount;
    }

    public int getTotalOnHand() { return quantityAvailable + quantityReserved + quantityDamaged; }
    public boolean hasAvailable(int amount) { return quantityAvailable >= amount; }

    public String getId() { return id; }
    public String getProductId() { return productId; }
    public String getWarehouseId() { return warehouseId; }
    public int getQuantityAvailable() { return quantityAvailable; }
    public int getQuantityReserved() { return quantityReserved; }
    public int getQuantityDamaged() { return quantityDamaged; }
}
