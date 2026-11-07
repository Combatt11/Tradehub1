package application.domain.exceptions;
public class UserNotFoundException extends DomainException {
    public UserNotFoundException(String id) { super("User not found: " + id); }
}
