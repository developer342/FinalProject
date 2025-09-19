package com.example.FinalServer.auth.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class RefreshTokenUtil {

  private static final SecureRandom RANDOM = new SecureRandom();

  private RefreshTokenUtil() {}

  // 원문 생성
  public static String generateToken() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  // 해시 생성 (SHA-256)
  public static String hash(String token) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digested = md.digest(token.getBytes());
      return Base64.getUrlEncoder().withoutPadding().encodeToString(digested);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
