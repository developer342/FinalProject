package com.example.FinalServer.auth.service;

import com.example.FinalServer.auth.dto.LoginRequest;
import com.example.FinalServer.auth.dto.RefreshTokenRequest;
import com.example.FinalServer.auth.dto.SignupRequest;
import com.example.FinalServer.auth.dto.TokenResponse;
import com.example.FinalServer.auth.util.JwtTokenProvider;
import com.example.FinalServer.auth.util.RefreshTokenUtil;
import com.example.FinalServer.common.exception.DuplicateEmailException;
import com.example.FinalServer.common.exception.InvalidCredentialsException;
import com.example.FinalServer.common.exception.InvalidRefreshTokenException;
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
                  .ifPresent(u -> { throw new DuplicateEmailException("이미 사용 중인 이메일입니다."); });

    String encoded = passwordEncoder.encode(req.getPassword());
    User user = User.of(req.getEmail(), encoded, req.getNickname());
    userRepository.save(user);
  }

  // 로그인: Access/Refresh 발급
  @Transactional
  public TokenResponse login(LoginRequest req) {
    User user = userRepository.findByEmail(req.getEmail())
                              .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

    if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
      throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
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
                                             .orElseThrow(() -> new InvalidRefreshTokenException("리프레시 토큰이 유효하지 않습니다."));

    if (old.getExpiresAt().isBefore(Instant.now())) {
      throw new InvalidRefreshTokenException("리프레시 토큰이 만료되었습니다.");
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
