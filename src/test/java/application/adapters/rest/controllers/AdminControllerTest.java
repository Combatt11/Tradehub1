package application.adapters.rest.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.adapters.rest.GlobalExceptionHandler;
import application.adapters.security.JwtProvider;
import application.domain.entities.Seller;
import application.domain.entities.Warehouse;
import application.domain.exceptions.SellerNotFoundException;
import application.domain.usecases.ports.ApproveSellerUseCase;
import application.domain.usecases.ports.RegisterMarketplaceWarehouseUseCase;
import application.domain.usecases.ports.RegisterSellerUseCase;
import application.domain.usecases.ports.RegisterSellerWarehouseUseCase;
import application.frameworks.config.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("AdminController")
class AdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private RegisterSellerUseCase registerSellerUseCase;
  @MockBean private ApproveSellerUseCase approveSellerUseCase;
  @MockBean private RegisterMarketplaceWarehouseUseCase registerMarketplaceWarehouseUseCase;
  @MockBean private RegisterSellerWarehouseUseCase registerSellerWarehouseUseCase;
  @MockBean private JwtProvider jwtProvider;

  private static MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder builder) {
    return builder.with(
        authentication(
            new UsernamePasswordAuthenticationToken(
                "admin-1", null, List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR")))));
  }

  private static MockHttpServletRequestBuilder asBuyer(MockHttpServletRequestBuilder builder) {
    return builder.with(
        authentication(
            new UsernamePasswordAuthenticationToken(
                "buyer-1", null, List.of(new SimpleGrantedAuthority("ROLE_BUYER")))));
  }

  private static final String VALID_SELLER_REQUEST =
      """
      {"fullName":"Alice","email":"alice@test.com","password":"secret",
       "identityDocument":"DOC-1","businessName":"Acme","taxId":"TAX-1","contactPhone":"+555"}
      """;

  @Test
  @DisplayName("POST /api/admin/sellers returns 201 with the pending seller")
  void registerSellerReturns201() throws Exception {
    when(registerSellerUseCase.execute(
            anyString(), anyString(), anyString(), any(), anyString(), any(), any()))
        .thenReturn(Seller.create("user-1", "Acme", "TAX-1", "+555"));

    mockMvc
        .perform(
            asAdmin(
                post("/api/admin/sellers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(VALID_SELLER_REQUEST)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.userId").value("user-1"))
        .andExpect(jsonPath("$.businessName").value("Acme"))
        .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
  }

  @Test
  @DisplayName("POST /api/admin/sellers returns 400 when the payload is invalid")
  void registerSellerReturns400OnInvalidPayload() throws Exception {
    mockMvc
        .perform(
            asAdmin(
                post("/api/admin/sellers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"fullName":"","email":"not-an-email","password":"","businessName":""}
                        """)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").exists());

    verify(registerSellerUseCase, never())
        .execute(anyString(), anyString(), anyString(), any(), anyString(), any(), any());
  }

  @Test
  @DisplayName("POST /api/admin/sellers returns 401 when unauthenticated")
  void registerSellerReturns401WhenUnauthenticated() throws Exception {
    mockMvc
        .perform(
            post("/api/admin/sellers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_SELLER_REQUEST))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /api/admin/sellers returns 403 for a non-admin role")
  void registerSellerReturns403ForWrongRole() throws Exception {
    mockMvc
        .perform(
            asBuyer(
                post("/api/admin/sellers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(VALID_SELLER_REQUEST)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /api/admin/sellers/{id}/approve returns 200 with the active seller")
  void approveSellerReturns200() throws Exception {
    Seller approved = Seller.create("user-1", "Acme", "TAX-1", "+555");
    approved.approve();
    when(approveSellerUseCase.approveSeller("seller-1")).thenReturn(approved);

    mockMvc
        .perform(asAdmin(post("/api/admin/sellers/seller-1/approve")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(approved.getId()))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  @DisplayName("POST /api/admin/sellers/{id}/approve returns 404 when the seller is missing")
  void approveSellerReturns404WhenMissing() throws Exception {
    when(approveSellerUseCase.approveSeller(eq("seller-404")))
        .thenThrow(new SellerNotFoundException("seller-404"));

    mockMvc
        .perform(asAdmin(post("/api/admin/sellers/seller-404/approve")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Seller not found: seller-404"));
  }

  @Test
  @DisplayName("POST /api/admin/warehouses/marketplace returns 201")
  void createMarketplaceWarehouseReturns201() throws Exception {
    when(registerMarketplaceWarehouseUseCase.registerMarketplaceWarehouse("Central", "Main Ave"))
        .thenReturn(Warehouse.createMarketplace("Central", "Main Ave"));

    mockMvc
        .perform(
            asAdmin(
                post("/api/admin/warehouses/marketplace")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"Central","address":"Main Ave"}
                        """)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Central"))
        .andExpect(jsonPath("$.type").value("MARKETPLACE"));
  }

  @Test
  @DisplayName("POST /api/admin/warehouses/seller returns 201")
  void createSellerWarehouseReturns201() throws Exception {
    when(registerSellerWarehouseUseCase.registerSellerWarehouse("Seller WH", "Calle 1", "seller-1"))
        .thenReturn(Warehouse.createForSeller("Seller WH", "Calle 1", "seller-1"));

    mockMvc
        .perform(
            asAdmin(
                post("/api/admin/warehouses/seller")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"Seller WH","address":"Calle 1","sellerId":"seller-1"}
                        """)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Seller WH"))
        .andExpect(jsonPath("$.sellerId").value("seller-1"));
  }

  @Test
  @DisplayName("POST /api/admin/warehouses/seller returns 403 for a non-admin role")
  void createWarehouseReturns403ForWrongRole() throws Exception {
    mockMvc
        .perform(
            asBuyer(
                post("/api/admin/warehouses/seller")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"Seller WH","address":"Calle 1","sellerId":"seller-1"}
                        """)))
        .andExpect(status().isForbidden());
  }
}
