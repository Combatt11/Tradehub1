package application.adapters.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private String type; // PHYSICAL or DIGITAL
    @NotNull @Positive
    private BigDecimal price;
    private String sku;
}
