package com.example.finalapp.auth.data;

import androidx.annotation.NonNull;

import com.example.finalapp.auth.dto.LoginRequest;
import com.example.finalapp.auth.dto.RefreshTokenRequest;
import com.example.finalapp.auth.dto.SignupRequest;
import com.example.finalapp.auth.dto.TokenResponse;
import com.example.finalapp.core.network.ApiService;
import com.example.finalapp.core.storage.TokenStore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 인증 API 래퍼(토큰 저장/정리 포함)
public class AuthRepository {

  private final ApiService api;

  public AuthRepository(@NonNull ApiService api) {
    this.api = api;
  }

  // 회원가입
  public void signup(SignupRequest request, Callback<Void> callback) {
    api.signup(request).enqueue(callback);
  }

  // 로그인 → 토큰 저장
  public void login(LoginRequest request, Callback<TokenResponse> callback) {
    api.login(request).enqueue(new Callback<TokenResponse>() {
      @Override public void onResponse(Call<TokenResponse> call, Response<TokenResponse> resp) {
        if (resp.isSuccessful() && resp.body() != null) {
          // 토큰 저장
          TokenStore.saveTokens(resp.body().getAccessToken(), resp.body().getRefreshToken());
        }
        if (callback != null) callback.onResponse(call, resp);
      }
      @Override public void onFailure(Call<TokenResponse> call, Throwable t) {
        if (callback != null) callback.onFailure(call, t);
      }
    });
  }

  // 리프레시 → 토큰 갱신 (수동 호출이 필요할 때만)
  public void refresh(RefreshTokenRequest refreshToken, Callback<TokenResponse> callback) {
    api.refresh(refreshToken).enqueue(new Callback<TokenResponse>() {
      @Override public void onResponse(Call<TokenResponse> call, Response<TokenResponse> resp) {
        if (resp.isSuccessful() && resp.body() != null) {
          // 토큰 갱신
          TokenStore.saveTokens(resp.body().getAccessToken(), resp.body().getRefreshToken());
        }
        if (callback != null) callback.onResponse(call, resp);
      }
      @Override public void onFailure(Call<TokenResponse> call, Throwable t) {
        if (callback != null) callback.onFailure(call, t);
      }
    });
  }

  // 로그아웃 → 서버 호출 성공 시 토큰 삭제
  public void logout(RefreshTokenRequest refreshToken, Callback<Void> callback) {
    api.logout(refreshToken).enqueue(new Callback<Void>() {
      @Override public void onResponse(Call<Void> call, Response<Void> resp) {
        if (resp.isSuccessful()) {
          // 토큰 삭제
          TokenStore.clear();
        }
        if (callback != null) callback.onResponse(call, resp);
      }
      @Override public void onFailure(Call<Void> call, Throwable t) {
        if (callback != null) callback.onFailure(call, t);
      }
    });
  }
}
