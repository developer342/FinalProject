package com.example.finalapp.user.data;

import androidx.annotation.NonNull;

import com.example.finalapp.core.network.ApiService;
import com.example.finalapp.user.dto.UserProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;

// 사용자 API 래퍼(me)
public class UserRepository {

  private final ApiService api;

  public UserRepository(@NonNull ApiService api) {
    this.api = api;
  }

  // 내 정보 조회 (Bearer 헤더는 AuthInterceptor가 자동 주입)
  public void me(Callback<UserProfileResponse> callback) {
    Call<UserProfileResponse> call = api.me();
    call.enqueue(callback);
  }
}
