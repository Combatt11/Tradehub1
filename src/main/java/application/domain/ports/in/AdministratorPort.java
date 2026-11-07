package application.domain.ports.in;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
public interface AdministratorPort {
    Seller registerSeller(String fullName, String email, String password, String identityDocument,
                          String businessName, String taxId, String contactPhone);
    Warehouse registerMarketplaceWarehouse(String name, String address);
    Warehouse registerSellerWarehouse(String name, String address, String sellerId);
    void approveSeller(String sellerId);
    void changeUserStatus(String userId, User.Status status);
}
