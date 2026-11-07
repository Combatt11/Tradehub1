package application.domain.exceptions;
public class DuplicateEmailException extends DomainException {
    public DuplicateEmailException(String email) { super("Email already registered: " + email); }
}
