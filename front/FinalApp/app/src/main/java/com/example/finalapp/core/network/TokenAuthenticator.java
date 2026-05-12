package com.example.finalapp.core.network;

import androidx.annotation.Nullable;

import com.example.finalapp.auth.dto.TokenResponse;
import com.example.finalapp.core.storage.TokenStore;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

// 401 시 리프레시 재발급 + 원요청 1회 재시도
public final class TokenAuthenticator implements Authenticator {

  private static final String HDR_AUTH = "Authorization";
  private static final String HDR_RETRY_FLAG = "X-Refresh-Attempt";
  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private final Gson gson = new Gson();

  @Override
  public @Nullable Request authenticate(@Nullable Route route, Response response) throws IOException {
    // 이미 한 번 재시도했으면 중단
    if (response.request().header(HDR_RETRY_FLAG) != null) return null;

    String path = response.request().url().encodedPath();
    // /api/auth/** 는 재시도 대상 아님
    if (path != null && path.startsWith("/api/auth/")) return null;

    String refresh = TokenStore.getRefreshToken();
    if (refresh == null || refresh.isEmpty()) return null;

    // 리프레시 요청용 클라이언트(순환 방지: 인터셉터/어센티케이터 미적용)
    OkHttpClient plain = new OkHttpClient();

    // 리프레시 URL 구성 (호스트 재사용)
    String refreshUrl = response.request().url().scheme() + "://" +
            response.request().url().host() +
            (response.request().url().port() != 80 && response.request().url().port() != 443
                    ? ":" + response.request().url().port() : "") +
            "/api/auth/token";

    // 바디 생성 {"refreshToken":"..."}
    String bodyJson = "{\"refreshToken\":\"" + refresh + "\"}";
    Request refreshReq = new Request.Builder()
            .url(refreshUrl)
            .post(RequestBody.create(bodyJson, JSON))
            .build();

    Response refreshRes = null;
    try {
      refreshRes = plain.newCall(refreshReq).execute();
      if (!refreshRes.isSuccessful() || refreshRes.body() == null) return null;

      String resBody = refreshRes.body().string();
      TokenResponse tr = gson.fromJson(resBody, TokenResponse.class);
      if (tr == null || tr.getAccessToken() == null || tr.getRefreshToken() == null) return null;

      // 새 토큰 저장
      TokenStore.saveTokens(tr.getAccessToken(), tr.getRefreshToken());

      // 원요청 재작성 (Authorization 교체 + 재시도 플래그)
      return response.request().newBuilder()
              .header(HDR_AUTH, "Bearer " + tr.getAccessToken())
              .header(HDR_RETRY_FLAG, "1")
              .build();

    } catch (JsonSyntaxException e) {
      return null;
    } finally {
      if (refreshRes != null) refreshRes.close();
    }
  }
}
