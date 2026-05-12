package com.example.FinalServer.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

  /** 요청 본문 파싱 불가(JSON 문법 오류 등) → 400 */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
    return ResponseEntity.badRequest()
            .body(new ErrorResponse("COMMON_BAD_REQUEST", "Invalid request body"));
  }

  /** OCR 파싱 실패 등 ledger 관련 커스텀 예외 → 400 */
  @ExceptionHandler(ParseFailedException.class)
  public ResponseEntity<ErrorResponse> handleParseFailed(ParseFailedException e) {
    return ResponseEntity.badRequest()
            .body(new ErrorResponse("LEDGER_PARSE_FAILED", e.getMessage()));
  }

  /** 일반 잘못된 요청 → 400 */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
    return ResponseEntity.badRequest()
            .body(new ErrorResponse("COMMON_BAD_REQUEST", e.getMessage()));
  }

  /** 알 수 없는 예외 → 500 */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception e, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("COMMON_INTERNAL_ERROR", "Internal server error"));
  }
}
