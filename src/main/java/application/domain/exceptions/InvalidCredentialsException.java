package application.domain.exceptions;
public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() { super("Invalid email or password"); }
}
