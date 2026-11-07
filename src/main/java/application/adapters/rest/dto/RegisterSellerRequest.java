package application.adapters.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterSellerRequest {
    @NotBlank
    private String fullName;
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
    private String identityDocument;
    @NotBlank
    private String businessName;
    private String taxId;
    private String contactPhone;
}
