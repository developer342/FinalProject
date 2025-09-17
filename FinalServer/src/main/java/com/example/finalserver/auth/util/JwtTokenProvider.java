package com.example.finalserver.auth.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

  private final SecretKey secretKey;
  private final long validityInMilliseconds;

  public JwtTokenProvider(
          @Value("${jwt.secret}") String secret,
          @Value("${jwt.access-token-validity-seconds}") long validitySeconds
  ) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes()); // 서명키
    this.validityInMilliseconds = validitySeconds * 1000; // 유효시간
  }

  // 액세스 토큰 생성
  public String createToken(Long userId) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
               .setSubject(String.valueOf(userId)) // 사용자ID
               .setIssuedAt(now) // 발급시각
               .setExpiration(expiry) // 만료시각
               .signWith(secretKey, SignatureAlgorithm.HS256) // 서명
               .compact();
  }

  // 토큰 검증
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(secretKey)
          .build()
          .parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // 토큰에서 사용자ID 추출
  public Long getUserId(String token) {
    Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
    return Long.valueOf(claims.getSubject());
  }
}
