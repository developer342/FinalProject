package com.example.FinalServer.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 이메일 중복 → 409
  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateEmailException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
                         .body(new ErrorResponse("AUTH_DUPLICATE_EMAIL", e.getMessage()));
  }

  // 자격 증명 실패 → 401
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                         .body(new ErrorResponse("AUTH_INVALID_CREDENTIALS", e.getMessage()));
  }

  // 리프레시 무효/만료 → 401
  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRefresh(InvalidRefreshTokenException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                         .body(new ErrorResponse("AUTH_INVALID_REFRESH", e.getMessage()));
  }

  // 요청 바디 검증 실패 → 400
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getFieldErrors().stream()
                  .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()))
                  .collect(Collectors.joining(", "));
    return ResponseEntity.badRequest()
                         .body(new ErrorResponse("COMMON_INVALID_ARGUMENT", msg));
  }

  // 그 외 일반 예외(임시) → 400 (최소 처리)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
    return ResponseEntity.badRequest()
                         .body(new ErrorResponse("COMMON_BAD_REQUEST", e.getMessage()));
  }
}
