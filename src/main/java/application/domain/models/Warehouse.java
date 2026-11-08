package application.domain.models;

import application.domain.exceptions.InvalidWarehouseAddressException;
import application.domain.exceptions.InvalidWarehouseNameException;
import application.domain.exceptions.SellerRequiredForWarehouseException;
import application.domain.exceptions.WarehouseAlreadyActiveException;
import application.domain.exceptions.WarehouseAlreadyInactiveException;
import application.domain.exceptions.WarehouseNotActiveException;
import java.util.Objects;
import java.util.UUID;

public class Warehouse {
  public enum WarehouseType {
    MARKETPLACE,
    SELLER
  }

  public enum WarehouseStatus {
    ACTIVE,
    INACTIVE,
    MAINTENANCE
  }

  private final String id;
  private String name;
  private String address;
  private WarehouseType type;
  private WarehouseStatus status;
  private String sellerId;
  private String operatorUserId;

  public Warehouse(
      String id,
      String name,
      String address,
      WarehouseType type,
      WarehouseStatus status,
      String sellerId,
      String operatorUserId) {
    this.id = id != null ? id : UUID.randomUUID().toString();
    if (name == null || name.isBlank()) throw new InvalidWarehouseNameException();
    if (address == null || address.isBlank()) throw new InvalidWarehouseAddressException();
    this.name = name;
    this.address = address;
    this.type = Objects.requireNonNull(type);
    this.status = status != null ? status : WarehouseStatus.ACTIVE;
    this.sellerId = sellerId;
    this.operatorUserId = operatorUserId;
  }

  public static Warehouse createMarketplace(String name, String address) {
    return new Warehouse(
        null, name, address, WarehouseType.MARKETPLACE, WarehouseStatus.ACTIVE, null, null);
  }

  public static Warehouse createForSeller(String name, String address, String sellerId) {
    if (sellerId == null || sellerId.isBlank()) {
      throw new SellerRequiredForWarehouseException();
    }
    return new Warehouse(
        null, name, address, WarehouseType.SELLER, WarehouseStatus.ACTIVE, sellerId, null);
  }

  public void assignOperator(String operatorUserId) {
    this.operatorUserId = operatorUserId;
  }

  public void activate() {
    if (status == WarehouseStatus.ACTIVE) throw new WarehouseAlreadyActiveException(id);
    this.status = WarehouseStatus.ACTIVE;
  }

  public void deactivate() {
    if (status == WarehouseStatus.INACTIVE) throw new WarehouseAlreadyInactiveException(id);
    this.status = WarehouseStatus.INACTIVE;
  }

  public boolean isActive() {
    return WarehouseStatus.ACTIVE.equals(this.status);
  }

  public void ensureActive() {
    if (!isActive()) {
      throw new WarehouseNotActiveException(id, status.name());
    }
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getAddress() {
    return address;
  }

  public WarehouseType getType() {
    return type;
  }

  public WarehouseStatus getStatus() {
    return status;
  }

  public String getSellerId() {
    return sellerId;
  }

  public String getOperatorUserId() {
    return operatorUserId;
  }

  public void setName(String name) {
    if (name == null || name.isBlank()) throw new InvalidWarehouseNameException();
    this.name = name;
  }

  public void setAddress(String address) {
    if (address == null || address.isBlank()) throw new InvalidWarehouseAddressException();
    this.address = address;
  }
}
