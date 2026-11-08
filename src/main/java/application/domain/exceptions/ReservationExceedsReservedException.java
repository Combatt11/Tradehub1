package application.domain.exceptions;

/** Raised when more reserved units are released or sold than are actually reserved. */
public class ReservationExceedsReservedException extends DomainException {
  public ReservationExceedsReservedException(String productId, int requested, int reserved) {
    super(
        String.format(
            "Cannot move %d reserved units for product %s. Reserved: %d",
            requested, productId, reserved));
  }
}
