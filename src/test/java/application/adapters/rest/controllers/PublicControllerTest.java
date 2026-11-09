package application.adapters.rest.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import application.adapters.rest.GlobalExceptionHandler;
import application.adapters.security.JwtProvider;
import application.domain.entities.User;
import application.domain.exceptions.DuplicateEmailException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.usecases.ports.LoginUseCase;
import application.domain.usecases.ports.RegisterBuyerUseCase;
import application.frameworks.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublicController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("PublicController")
class PublicControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private RegisterBuyerUseCase registerBuyerUseCase;
  @MockBean private LoginUseCase loginUseCase;
  @MockBean private JwtProvider jwtProvider;

  private static User buyer() {
    return User.create("John Buyer", "buyer@test.com", "hash", User.Role.BUYER, "DOC-1");
  }

  @Test
  @DisplayName("POST /api/public/register/buyer returns 201")
  void registerBuyerReturns201() throws Exception {
    when(registerBuyerUseCase.execute(
            anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(buyer());

    mockMvc
        .perform(
            post("/api/public/register/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"fullName":"John Buyer","email":"buyer@test.com","password":"secret",
                     "identityDocument":"DOC-1","primaryAddress":"Calle 1"}
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("buyer@test.com"))
        .andExpect(jsonPath("$.fullName").value("John Buyer"))
        .andExpect(jsonPath("$.role").value("BUYER"));
  }

  @Test
  @DisplayName("POST /api/public/register/buyer returns 400 when the payload is invalid")
  void registerBuyerReturns400OnInvalidPayload() throws Exception {
    mockMvc
        .perform(
            post("/api/public/register/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"fullName":"","email":"not-an-email","password":"","primaryAddress":""}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").exists());
  }

  @Test
  @DisplayName("POST /api/public/register/buyer returns 409 when the email is taken")
  void registerBuyerReturns409OnDuplicateEmail() throws Exception {
    when(registerBuyerUseCase.execute(anyString(), anyString(), anyString(), any(), anyString()))
        .thenThrow(new DuplicateEmailException("buyer@test.com"));

    mockMvc
        .perform(
            post("/api/public/register/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"fullName":"John Buyer","email":"buyer@test.com","password":"secret",
                     "primaryAddress":"Calle 1"}
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").exists());
  }

  @Test
  @DisplayName("POST /api/public/login returns 200 with the JWT")
  void loginReturns200() throws Exception {
    when(loginUseCase.authenticate("buyer@test.com", "secret")).thenReturn(buyer());
    when(jwtProvider.generateToken(anyString(), anyString(), anyString())).thenReturn("jwt-token");

    mockMvc
        .perform(
            post("/api/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"buyer@test.com","password":"secret"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token"))
        .andExpect(jsonPath("$.email").value("buyer@test.com"))
        .andExpect(jsonPath("$.role").value("BUYER"));
  }

  @Test
  @DisplayName("POST /api/public/login returns 401 on invalid credentials")
  void loginReturns401OnInvalidCredentials() throws Exception {
    when(loginUseCase.authenticate(anyString(), anyString()))
        .thenThrow(new InvalidCredentialsException());

    mockMvc
        .perform(
            post("/api/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"buyer@test.com","password":"wrong"}
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").exists());
  }

  @Test
  @DisplayName("POST /api/public/login returns 400 when the payload is invalid")
  void loginReturns400OnInvalidPayload() throws Exception {
    mockMvc
        .perform(
            post("/api/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"not-an-email","password":""}
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/public/login returns 400 on a malformed JSON body")
  void loginReturns400OnMalformedJson() throws Exception {
    mockMvc
        .perform(
            post("/api/public/login").contentType(MediaType.APPLICATION_JSON).content("{not-json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Malformed request body"));
  }
}
