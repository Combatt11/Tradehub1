package application.domain.ports.in;
import application.domain.models.User;
public interface PublicAccessPort {
    User registerBuyer(String fullName, String email, String password, String identityDocument, String primaryAddress);
    String login(String email, String password);
}
