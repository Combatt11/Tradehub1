package application.adapters.rest;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.DuplicateEmailException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex));
  }

  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<?> handleConflict(DuplicateEmailException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex));
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(ex));
  }

  @ExceptionHandler(UnauthorizedOperationException.class)
  public ResponseEntity<?> handleUnauthorizedOperation(UnauthorizedOperationException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex));
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<?> handleBusinessRule(DomainException ex) {
    return ResponseEntity.badRequest().body(error(ex));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
    String msg =
        ex.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .collect(Collectors.joining(", "));
    return ResponseEntity.badRequest().body(Map.of("error", msg));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<?> handleUnreadableBody(HttpMessageNotReadableException ex) {
    return ResponseEntity.badRequest().body(Map.of("error", "Malformed request body"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", "Internal error"));
  }

  private Map<String, String> error(DomainException ex) {
    return Map.of("error", ex.getMessage());
  }
}
