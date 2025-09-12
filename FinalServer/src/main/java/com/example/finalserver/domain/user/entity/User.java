package com.example.FinalServer.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 191, unique = true)
  private String email;

  @Column(nullable = false)
  private String password; // BCrypt 해시 저장

  // 정적 팩토리
  public static User of(String email, String hashedPassword) {
    return new User(null, email, hashedPassword);
  }
}
