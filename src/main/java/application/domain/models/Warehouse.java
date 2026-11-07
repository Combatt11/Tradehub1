package application.domain.models;

import java.util.Objects;
import java.util.UUID;

public class Warehouse {
    public enum WarehouseType { MARKETPLACE, SELLER }
    public enum WarehouseStatus { ACTIVE, INACTIVE, MAINTENANCE }

    private final String id;
    private String name;
    private String address;
    private WarehouseType type;
    private WarehouseStatus status;
    private String sellerId;
    private String operatorUserId;

    public Warehouse(String id, String name, String address, WarehouseType type,
                     WarehouseStatus status, String sellerId, String operatorUserId) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = Objects.requireNonNull(name);
        this.address = Objects.requireNonNull(address);
        this.type = Objects.requireNonNull(type);
        this.status = status != null ? status : WarehouseStatus.ACTIVE;
        this.sellerId = sellerId;
        this.operatorUserId = operatorUserId;
    }

    public static Warehouse createMarketplace(String name, String address) {
        return new Warehouse(null, name, address, WarehouseType.MARKETPLACE, WarehouseStatus.ACTIVE, null, null);
    }

    public static Warehouse createForSeller(String name, String address, String sellerId) {
        return new Warehouse(null, name, address, WarehouseType.SELLER, WarehouseStatus.ACTIVE, sellerId, null);
    }

    public void assignOperator(String operatorUserId) { this.operatorUserId = operatorUserId; }
    public void activate() { this.status = WarehouseStatus.ACTIVE; }
    public void deactivate() { this.status = WarehouseStatus.INACTIVE; }
    public boolean isActive() { return WarehouseStatus.ACTIVE.equals(this.status); }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public WarehouseType getType() { return type; }
    public WarehouseStatus getStatus() { return status; }
    public String getSellerId() { return sellerId; }
    public String getOperatorUserId() { return operatorUserId; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }
    public void setAddress(String address) { this.address = Objects.requireNonNull(address); }
}
