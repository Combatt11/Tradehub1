package application.adapters.rest.controllers;

import application.adapters.rest.dto.LoginRequest;
import application.adapters.rest.dto.LoginResponse;
import application.adapters.rest.dto.RegisterBuyerRequest;
import application.adapters.security.JwtProvider;
import application.domain.models.User;
import application.domain.ports.in.LoginUseCase;
import application.domain.ports.in.RegisterBuyerUseCase;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class PublicController {

  private final RegisterBuyerUseCase registerBuyerUseCase;
  private final LoginUseCase loginUseCase;
  private final JwtProvider jwtProvider;

  public PublicController(
      RegisterBuyerUseCase registerBuyerUseCase,
      LoginUseCase loginUseCase,
      JwtProvider jwtProvider) {
    this.registerBuyerUseCase = registerBuyerUseCase;
    this.loginUseCase = loginUseCase;
    this.jwtProvider = jwtProvider;
  }

  @PostMapping("/register/buyer")
  public ResponseEntity<?> registerBuyer(@Valid @RequestBody RegisterBuyerRequest req) {
    User user =
        registerBuyerUseCase.execute(
            req.getFullName(),
            req.getEmail(),
            req.getPassword(),
            req.getIdentityDocument(),
            req.getPrimaryAddress());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "role", user.getRole().name()));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
    User user = loginUseCase.authenticate(req.getEmail(), req.getPassword());
    String token = jwtProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
    return ResponseEntity.ok(
        new LoginResponse(
            token, user.getId(), user.getEmail(), user.getRole().name(), user.getFullName()));
  }
}
