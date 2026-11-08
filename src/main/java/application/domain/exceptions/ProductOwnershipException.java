package application.domain.exceptions;

public class ProductOwnershipException extends UnauthorizedOperationException {
  public ProductOwnershipException(String productId, String sellerId) {
    super("Product " + productId + " does not belong to seller: " + sellerId);
  }
}
