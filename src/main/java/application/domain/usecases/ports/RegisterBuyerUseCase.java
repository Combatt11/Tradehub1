package application.domain.usecases.ports;

import application.domain.entities.User;

public interface RegisterBuyerUseCase {
  User execute(
      String fullName,
      String email,
      String password,
      String identityDocument,
      String primaryAddress);
}
