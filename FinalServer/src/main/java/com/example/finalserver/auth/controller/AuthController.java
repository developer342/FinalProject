package com.example.FinalServer.auth.controller;

import com.example.FinalServer.auth.dto.LoginRequest;
import com.example.FinalServer.auth.dto.RefreshTokenRequest;
import com.example.FinalServer.auth.dto.SignupRequest;
import com.example.FinalServer.auth.dto.TokenResponse;
import com.example.FinalServer.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup") // 회원가입
  public void signup(@Valid @RequestBody SignupRequest req) {
    authService.signup(req);
  }

  @PostMapping("/login") // 로그인 → 토큰발급
  public TokenResponse login(@Valid @RequestBody LoginRequest req) {
    return authService.login(req);
  }

  @PostMapping("/token") // 리프레시로 재발급
  public TokenResponse reissue(@Valid @RequestBody RefreshTokenRequest req) {
    return authService.reissue(req);
  }

  @PostMapping("/logout") // 리프레시 폐기
  public void logout(@Valid @RequestBody RefreshTokenRequest req) {
    authService.logout(req);
  }
}
