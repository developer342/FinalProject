package com.example.finalapp.core.network;

import com.example.finalapp.auth.dto.LoginRequest;
import com.example.finalapp.auth.dto.RefreshTokenRequest;
import com.example.finalapp.auth.dto.SignupRequest;
import com.example.finalapp.auth.dto.TokenResponse;
import com.example.finalapp.user.dto.UserProfileResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

  @POST("auth/signup") // 회원가입
  Call<Void> signup(@Body SignupRequest request);

  @POST("auth/login") // 로그인
  Call<TokenResponse> login(@Body LoginRequest request);

  @POST("auth/token") // 리프레시 → 새 토큰 발급
  Call<TokenResponse> refresh(@Body RefreshTokenRequest request);

  @POST("auth/logout") // 로그아웃
  Call<Void> logout(@Body RefreshTokenRequest request);

  @GET("users/me") // 내 정보 조회
  Call<UserProfileResponse> me();
}
