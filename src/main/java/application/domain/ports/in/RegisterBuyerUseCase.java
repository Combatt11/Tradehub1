package application.domain.ports.in;

import application.domain.models.User;

public interface RegisterBuyerUseCase {
  User execute(
      String fullName,
      String email,
      String password,
      String identityDocument,
      String primaryAddress);
}
