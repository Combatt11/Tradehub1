package application.domain.usecases.ports;

import application.domain.entities.Seller;

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
