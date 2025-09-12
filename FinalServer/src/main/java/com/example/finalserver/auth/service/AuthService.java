package com.example.FinalServer.auth.service;

import com.example.FinalServer.auth.dto.LoginRequest;
import com.example.FinalServer.auth.dto.RefreshTokenRequest;
import com.example.FinalServer.auth.dto.SignupRequest;
import com.example.FinalServer.auth.dto.TokenResponse;
import com.example.FinalServer.auth.util.JwtTokenProvider;
import com.example.FinalServer.auth.util.RefreshTokenUtil;
import com.example.FinalServer.domain.token.entity.RefreshToken;
import com.example.FinalServer.domain.token.repository.RefreshTokenRepository;
import com.example.FinalServer.domain.user.entity.User;
import com.example.FinalServer.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${jwt.refresh-token-validity-days:14}")
  private int refreshValidityDays;

  // 회원가입
  @Transactional
  public void signup(SignupRequest req) {
    userRepository.findByEmail(req.getEmail())
                  .ifPresent(u -> { throw new IllegalArgumentException("email already exists"); });

    String encoded = passwordEncoder.encode(req.getPassword());
    User user = User.of(req.getEmail(), encoded);
    userRepository.save(user);
  }

  // 로그인: Access/Refresh 발급
  @Transactional
  public TokenResponse login(LoginRequest req) {
    User user = userRepository.findByEmail(req.getEmail())
                              .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));

    if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
      throw new IllegalArgumentException("invalid credentials");
    }

    String access = jwtTokenProvider.createToken(user.getId());

    String refreshPlain = RefreshTokenUtil.generateToken();     // 원문
    String refreshHash  = RefreshTokenUtil.hash(refreshPlain);  // 해시
    Instant expiresAt   = Instant.now().plus(Duration.ofDays(refreshValidityDays));

    RefreshToken rt = RefreshToken.of(refreshHash, user, expiresAt);
    refreshTokenRepository.save(rt);

    return new TokenResponse(access, refreshPlain);
  }

  // 재발급: 기존 Refresh 삭제 후 새 Access/Refresh 발급
  @Transactional
  public TokenResponse reissue(RefreshTokenRequest req) {
    String hash = RefreshTokenUtil.hash(req.getRefreshToken());
    RefreshToken old = refreshTokenRepository.findByTokenHash(hash)
                                             .orElseThrow(() -> new IllegalArgumentException("invalid refresh"));

    if (old.getExpiresAt().isBefore(Instant.now())) {
      throw new IllegalArgumentException("invalid refresh");
    }

    refreshTokenRepository.delete(old);

    String access = jwtTokenProvider.createToken(old.getUser().getId());
    String newPlain = RefreshTokenUtil.generateToken();
    String newHash  = RefreshTokenUtil.hash(newPlain);
    Instant newExp  = Instant.now().plus(Duration.ofDays(refreshValidityDays));

    RefreshToken fresh = RefreshToken.of(newHash, old.getUser(), newExp);
    refreshTokenRepository.save(fresh);

    return new TokenResponse(access, newPlain);
  }

  // 로그아웃: Refresh 삭제
  @Transactional
  public void logout(RefreshTokenRequest req) {
    String hash = RefreshTokenUtil.hash(req.getRefreshToken());
    refreshTokenRepository.findByTokenHash(hash)
                          .ifPresent(refreshTokenRepository::delete);
  }
}
