package application.domain.models;

import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.InvalidQuantityException;
import application.domain.exceptions.ProductOutOfStockException;
import application.domain.exceptions.ReservationExceedsReservedException;
import java.util.Objects;
import java.util.UUID;

public class InventoryItem {
  public enum MovementType {
    INBOUND,
    RESERVATION,
    SALE_OUTBOUND,
    ADJUSTMENT,
    RETURN
  }

  private final String id;
  private final String productId;
  private final String warehouseId;
  private int quantityAvailable;
  private int quantityReserved;
  private int quantityDamaged;

  public InventoryItem(
      String id,
      String productId,
      String warehouseId,
      int quantityAvailable,
      int quantityReserved,
      int quantityDamaged) {
    this.id = id != null ? id : UUID.randomUUID().toString();
    this.productId = Objects.requireNonNull(productId);
    this.warehouseId = Objects.requireNonNull(warehouseId);
    if (quantityAvailable < 0 || quantityReserved < 0 || quantityDamaged < 0) {
      throw new InvalidQuantityException("Inventory quantities cannot be negative");
    }
    this.quantityAvailable = quantityAvailable;
    this.quantityReserved = quantityReserved;
    this.quantityDamaged = quantityDamaged;
  }

  public static InventoryItem create(String productId, String warehouseId, int initialQuantity) {
    return new InventoryItem(null, productId, warehouseId, initialQuantity, 0, 0);
  }

  public void addStock(int amount) {
    if (amount <= 0) throw new InvalidQuantityException("Stock amount must be positive");
    this.quantityAvailable += amount;
  }

  public void reserve(int amount) {
    if (amount <= 0) throw new InvalidQuantityException("Reservation amount must be positive");
    if (quantityAvailable == 0) {
      throw new ProductOutOfStockException(productId);
    }
    if (quantityAvailable < amount) {
      throw new InsufficientStockException(productId, amount, quantityAvailable);
    }
    this.quantityAvailable -= amount;
    this.quantityReserved += amount;
  }

  public void releaseReservation(int amount) {
    if (amount <= 0) throw new InvalidQuantityException("Release amount must be positive");
    if (amount > quantityReserved) {
      throw new ReservationExceedsReservedException(productId, amount, quantityReserved);
    }
    this.quantityReserved -= amount;
    this.quantityAvailable += amount;
  }

  public void confirmSale(int amount) {
    if (amount <= 0) throw new InvalidQuantityException("Sale amount must be positive");
    if (amount > quantityReserved) {
      throw new ReservationExceedsReservedException(productId, amount, quantityReserved);
    }
    this.quantityReserved -= amount;
  }

  public void markDamaged(int amount) {
    if (amount <= 0) throw new InvalidQuantityException("Damaged amount must be positive");
    if (quantityAvailable < amount) {
      throw new InsufficientStockException(productId, amount, quantityAvailable);
    }
    this.quantityAvailable -= amount;
    this.quantityDamaged += amount;
  }

  public int getTotalOnHand() {
    return quantityAvailable + quantityReserved + quantityDamaged;
  }

  public boolean hasAvailable(int amount) {
    return quantityAvailable >= amount;
  }

  public String getId() {
    return id;
  }

  public String getProductId() {
    return productId;
  }

  public String getWarehouseId() {
    return warehouseId;
  }

  public int getQuantityAvailable() {
    return quantityAvailable;
  }

  public int getQuantityReserved() {
    return quantityReserved;
  }

  public int getQuantityDamaged() {
    return quantityDamaged;
  }
}
