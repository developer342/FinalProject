package com.example.finalapp.core.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

  @POST("auth/signup") // 회원가입
  Call<Void> signup(@Body SignupRequest request);

  @POST("auth/login") // 로그인
  Call<TokenResponse> login(@Body LoginRequest request);
}
