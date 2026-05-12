package com.example.finalapp.core.network;

import androidx.annotation.NonNull;

import com.example.finalapp.core.storage.TokenStore;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

// 보호 API에만 Bearer 헤더 자동 주입
public class AuthInterceptor implements Interceptor {

  @NonNull
  @Override
  public Response intercept(@NonNull Chain chain) throws IOException {
    Request original = chain.request();
    String path = original.url().encodedPath();

    // /api/auth/** 는 제외 (회원가입/로그인/재발급/로그아웃)
    if (path != null && path.startsWith("/api/auth/")) {
      return chain.proceed(original);
    }

    String access = TokenStore.getAccessToken();
    if (access == null || access.isEmpty()) {
      return chain.proceed(original); // 토큰 없으면 그대로 진행
    }

    Request withAuth = original.newBuilder()
            .header("Authorization", "Bearer " + access) // Bearer 헤더 주입
            .build();

    return chain.proceed(withAuth);
  }
}
