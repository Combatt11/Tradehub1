package application.domain.exceptions;

public class UserNotFoundException extends ResourceNotFoundException {
  public UserNotFoundException(String id) {
    super("User not found: " + id);
  }
}
