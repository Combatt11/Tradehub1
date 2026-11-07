package application.domain.models;

import java.util.Objects;
import java.util.UUID;

public class User {
    public enum Role { BUYER, SELLER, LOGISTICS_OPERATOR, ADMINISTRATOR, SUPERVISOR }
    public enum Status { ACTIVE, BLOCKED, INACTIVE }

    private final String id;
    private String fullName;
    private String email;
    private String passwordHash;
    private Role role;
    private Status status;
    private String identityDocument;

    public User(String id, String fullName, String email, String passwordHash,
                Role role, Status status, String identityDocument) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.fullName = Objects.requireNonNull(fullName, "fullName is required");
        this.email = Objects.requireNonNull(email, "email is required");
        this.passwordHash = passwordHash;
        this.role = Objects.requireNonNull(role, "role is required");
        this.status = status != null ? status : Status.ACTIVE;
        this.identityDocument = identityDocument;
    }

    public static User create(String fullName, String email, String passwordHash,
                              Role role, String identityDocument) {
        return new User(null, fullName, email, passwordHash, role, Status.ACTIVE, identityDocument);
    }

    public void block() { this.status = Status.BLOCKED; }
    public void activate() { this.status = Status.ACTIVE; }
    public void changePassword(String newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash);
    }
    public boolean isActive() { return Status.ACTIVE.equals(this.status); }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Status getStatus() { return status; }
    public String getIdentityDocument() { return identityDocument; }
    public void setFullName(String fullName) { this.fullName = Objects.requireNonNull(fullName); }
    public void setEmail(String email) { this.email = Objects.requireNonNull(email); }
}
