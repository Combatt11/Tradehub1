package application.adapters.rest.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.adapters.rest.GlobalExceptionHandler;
import application.adapters.security.JwtProvider;
import application.adapters.security.SecurityConfig;
import application.domain.models.Product;
import application.domain.ports.in.BrowseCatalogUseCase;
import application.domain.ports.in.GetProductUseCase;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CatalogController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("CatalogController")
class CatalogControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private BrowseCatalogUseCase browseCatalogUseCase;
  @MockBean private GetProductUseCase getProductUseCase;
  @MockBean private JwtProvider jwtProvider;

  private static Product publishedProduct() {
    Product product =
        new Product(
            "product-1",
            "seller-1",
            "Laptop",
            "A laptop",
            Product.ProductType.PHYSICAL,
            new BigDecimal("100.00"),
            Product.ProductStatus.PUBLISHED,
            null,
            "SKU-1");
    return product;
  }

  @Test
  @DisplayName("GET /api/catalog/products returns 200 without authentication")
  void listPublishedReturns200() throws Exception {
    when(browseCatalogUseCase.browsePublishedProducts()).thenReturn(List.of(publishedProduct()));

    mockMvc
        .perform(get("/api/catalog/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("product-1"))
        .andExpect(jsonPath("$[0].name").value("Laptop"))
        .andExpect(jsonPath("$[0].type").value("PHYSICAL"))
        .andExpect(jsonPath("$[0].price").value(100.00))
        .andExpect(jsonPath("$[0].sku").value("SKU-1"));
  }

  @Test
  @DisplayName("GET /api/catalog/products returns an empty list when nothing is published")
  void listPublishedReturnsEmptyList() throws Exception {
    when(browseCatalogUseCase.browsePublishedProducts()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/catalog/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @DisplayName("GET /api/catalog/products/{id} returns 200 for a published product")
  void getByIdReturns200() throws Exception {
    when(getProductUseCase.getPublishedProduct("product-1"))
        .thenReturn(Optional.of(publishedProduct()));

    mockMvc
        .perform(get("/api/catalog/products/product-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("product-1"))
        .andExpect(jsonPath("$.sellerId").value("seller-1"));
  }

  @Test
  @DisplayName("GET /api/catalog/products/{id} returns 404 for an unknown product")
  void getByIdReturns404WhenMissing() throws Exception {
    when(getProductUseCase.getPublishedProduct("missing")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/catalog/products/missing")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/catalog/products/{id} tolerates a product without optional fields")
  void getByIdHandlesOptionalFields() throws Exception {
    Product product =
        new Product(
            "product-2",
            "seller-1",
            "eBook",
            null,
            Product.ProductType.DIGITAL,
            new BigDecimal("9.99"),
            Product.ProductStatus.PUBLISHED,
            null,
            null);
    when(getProductUseCase.getPublishedProduct("product-2")).thenReturn(Optional.of(product));

    mockMvc
        .perform(get("/api/catalog/products/product-2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value(""))
        .andExpect(jsonPath("$.sku").value(""));
  }
}
