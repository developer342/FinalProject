package com.example.finalserver.domain.token.entity;

import com.example.finalserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // PK
  private Long id;

  @Column(nullable = false) // 토큰 해시
  private String tokenHash;

  @ManyToOne(fetch = FetchType.LAZY, optional = false) // 사용자 FK
  private User user;

  @Column(nullable = false) // 만료시간
  private Instant expiresAt;

  @Column(nullable = false) // 폐기 여부
  private boolean revoked;

  // 정적 팩토리(선택)
  public static RefreshToken of(String tokenHash, User user, Instant expiresAt) { // 생성 헬퍼
    return new RefreshToken(null, tokenHash, user, expiresAt, false);
  }
}
