package application.domain.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Buyer {
    public enum CommercialStatus { ACTIVE, SUSPENDED, RESTRICTED }

    private final String id;
    private final String userId;
    private String primaryAddress;
    private List<String> additionalAddresses;
    private CommercialStatus commercialStatus;

    public Buyer(String id, String userId, String primaryAddress,
                 List<String> additionalAddresses, CommercialStatus commercialStatus) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.userId = Objects.requireNonNull(userId);
        this.primaryAddress = Objects.requireNonNull(primaryAddress, "primaryAddress is required");
        this.additionalAddresses = additionalAddresses != null ? new ArrayList<>(additionalAddresses) : new ArrayList<>();
        this.commercialStatus = commercialStatus != null ? commercialStatus : CommercialStatus.ACTIVE;
    }

    public static Buyer create(String userId, String primaryAddress) {
        return new Buyer(null, userId, primaryAddress, new ArrayList<>(), CommercialStatus.ACTIVE);
    }

    public void addAddress(String address) {
        if (address != null && !address.isBlank()) this.additionalAddresses.add(address);
    }
    public void suspend() { this.commercialStatus = CommercialStatus.SUSPENDED; }
    public void activate() { this.commercialStatus = CommercialStatus.ACTIVE; }
    public boolean canPurchase() { return CommercialStatus.ACTIVE.equals(this.commercialStatus); }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getPrimaryAddress() { return primaryAddress; }
    public List<String> getAdditionalAddresses() { return List.copyOf(additionalAddresses); }
    public CommercialStatus getCommercialStatus() { return commercialStatus; }
    public void setPrimaryAddress(String primaryAddress) { this.primaryAddress = Objects.requireNonNull(primaryAddress); }
}
