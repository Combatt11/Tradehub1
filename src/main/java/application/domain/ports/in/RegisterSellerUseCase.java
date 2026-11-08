package application.domain.ports.in;

import application.domain.models.Seller;

public interface RegisterSellerUseCase {
  Seller execute(
      String fullName,
      String email,
      String password,
      String identityDocument,
      String businessName,
      String taxId,
      String contactPhone);
}
