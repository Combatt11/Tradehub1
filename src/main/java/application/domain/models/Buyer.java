package application.domain.models;

import application.domain.exceptions.BuyerAddressRequiredException;
import application.domain.exceptions.BuyerAlreadyActiveException;
import application.domain.exceptions.BuyerAlreadySuspendedException;
import application.domain.exceptions.BuyerCannotPurchaseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Buyer {
  public enum CommercialStatus {
    ACTIVE,
    SUSPENDED,
    RESTRICTED
  }

  private final String id;
  private final String userId;
  private String primaryAddress;
  private List<String> additionalAddresses;
  private CommercialStatus commercialStatus;

  public Buyer(
      String id,
      String userId,
      String primaryAddress,
      List<String> additionalAddresses,
      CommercialStatus commercialStatus) {
    this.id = id != null ? id : UUID.randomUUID().toString();
    this.userId = Objects.requireNonNull(userId);
    if (primaryAddress == null || primaryAddress.isBlank()) {
      throw new BuyerAddressRequiredException();
    }
    this.primaryAddress = primaryAddress;
    this.additionalAddresses =
        additionalAddresses != null ? new ArrayList<>(additionalAddresses) : new ArrayList<>();
    this.commercialStatus = commercialStatus != null ? commercialStatus : CommercialStatus.ACTIVE;
  }

  public static Buyer create(String userId, String primaryAddress) {
    return new Buyer(null, userId, primaryAddress, new ArrayList<>(), CommercialStatus.ACTIVE);
  }

  public void addAddress(String address) {
    if (address == null || address.isBlank()) throw new BuyerAddressRequiredException();
    this.additionalAddresses.add(address);
  }

  public void suspend() {
    if (commercialStatus == CommercialStatus.SUSPENDED) {
      throw new BuyerAlreadySuspendedException(id);
    }
    this.commercialStatus = CommercialStatus.SUSPENDED;
  }

  public void activate() {
    if (commercialStatus == CommercialStatus.ACTIVE) {
      throw new BuyerAlreadyActiveException(id);
    }
    this.commercialStatus = CommercialStatus.ACTIVE;
  }

  public boolean canPurchase() {
    return CommercialStatus.ACTIVE.equals(this.commercialStatus);
  }

  public void ensureCanPurchase() {
    if (!canPurchase()) {
      throw new BuyerCannotPurchaseException(id, commercialStatus.name());
    }
  }

  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getPrimaryAddress() {
    return primaryAddress;
  }

  public List<String> getAdditionalAddresses() {
    return List.copyOf(additionalAddresses);
  }

  public CommercialStatus getCommercialStatus() {
    return commercialStatus;
  }

  public void setPrimaryAddress(String primaryAddress) {
    if (primaryAddress == null || primaryAddress.isBlank()) {
      throw new BuyerAddressRequiredException();
    }
    this.primaryAddress = primaryAddress;
  }
}
