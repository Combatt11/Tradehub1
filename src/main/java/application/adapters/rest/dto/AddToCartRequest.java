package application.adapters.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddToCartRequest {
    @NotBlank
    private String productId;
    @Positive
    private int quantity;
    private String warehouseId; // required for physical products
}
