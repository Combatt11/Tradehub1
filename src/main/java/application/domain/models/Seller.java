package application.domain.models;

import java.util.Objects;
import java.util.UUID;

public class Seller {
    public enum SellerStatus { ACTIVE, SUSPENDED, PENDING_APPROVAL }

    private final String id;
    private final String userId;
    private String businessName;
    private String taxId;
    private SellerStatus status;
    private String contactPhone;

    public Seller(String id, String userId, String businessName, String taxId,
                  SellerStatus status, String contactPhone) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.userId = Objects.requireNonNull(userId);
        this.businessName = Objects.requireNonNull(businessName);
        this.taxId = taxId;
        this.status = status != null ? status : SellerStatus.PENDING_APPROVAL;
        this.contactPhone = contactPhone;
    }

    public static Seller create(String userId, String businessName, String taxId, String contactPhone) {
        return new Seller(null, userId, businessName, taxId, SellerStatus.PENDING_APPROVAL, contactPhone);
    }

    public void approve() { this.status = SellerStatus.ACTIVE; }
    public void suspend() { this.status = SellerStatus.SUSPENDED; }
    public boolean isActive() { return SellerStatus.ACTIVE.equals(this.status); }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getBusinessName() { return businessName; }
    public String getTaxId() { return taxId; }
    public SellerStatus getStatus() { return status; }
    public String getContactPhone() { return contactPhone; }
    public void setBusinessName(String businessName) { this.businessName = Objects.requireNonNull(businessName); }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}
