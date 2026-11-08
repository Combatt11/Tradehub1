package application.domain.exceptions;

public class SellerNotApprovedException extends UnauthorizedOperationException {
  public SellerNotApprovedException(String sellerId, String status) {
    super("Seller " + sellerId + " is not approved to sell. Status: " + status);
  }
}
