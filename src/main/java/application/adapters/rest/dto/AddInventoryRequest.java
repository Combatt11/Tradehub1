package application.adapters.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddInventoryRequest {
    @NotBlank
    private String productId;
    @NotBlank
    private String warehouseId;
    @Positive
    private int quantity;
}
