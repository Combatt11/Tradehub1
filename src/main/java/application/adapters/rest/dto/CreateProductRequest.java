package application.adapters.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateProductRequest {
  @NotBlank private String name;
  private String description;
  @NotBlank private String type; // PHYSICAL or DIGITAL
  @NotNull @Positive private BigDecimal price;
  private String sku;
}
