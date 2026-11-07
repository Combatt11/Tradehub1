package application.adapters.rest.controllers;

import application.adapters.rest.dto.LoginRequest;
import application.adapters.rest.dto.LoginResponse;
import application.adapters.rest.dto.RegisterBuyerRequest;
import application.adapters.security.JwtProvider;
import application.domain.models.User;
import application.domain.services.user.LoginService;
import application.domain.services.user.RegisterBuyerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final RegisterBuyerService registerBuyerService;
    private final LoginService loginService;
    private final JwtProvider jwtProvider;

    public PublicController(RegisterBuyerService registerBuyerService,
                            LoginService loginService,
                            JwtProvider jwtProvider) {
        this.registerBuyerService = registerBuyerService;
        this.loginService = loginService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/register/buyer")
    public ResponseEntity<?> registerBuyer(@Valid @RequestBody RegisterBuyerRequest req) {
        User user = registerBuyerService.execute(
                req.getFullName(), req.getEmail(), req.getPassword(),
                req.getIdentityDocument(), req.getPrimaryAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "role", user.getRole().name()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        User user = loginService.authenticate(req.getEmail(), req.getPassword());
        String token = jwtProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new LoginResponse(
                token, user.getId(), user.getEmail(), user.getRole().name(), user.getFullName()));
    }
}
