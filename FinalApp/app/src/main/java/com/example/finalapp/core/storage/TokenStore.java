package com.example.finalapp.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStore {

  private static final String PREFS_NAME = "token_prefs";
  private static final String KEY_ACCESS = "access_token";
  private static final String KEY_REFRESH = "refresh_token";

  private static SharedPreferences prefs;

  private TokenStore() {}

  // 초기화(앱 시작 시 1회)
  public static synchronized void init(Context context) {
    if (prefs == null) {
      prefs = context.getApplicationContext()
              .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
  }

  // 저장
  public static void saveTokens(String accessToken, String refreshToken) {
    ensureInit();
    prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .apply();
  }

  // 조회(Access)
  public static String getAccessToken() {
    ensureInit();
    return prefs.getString(KEY_ACCESS, null);
  }

  // 조회(Refresh)
  public static String getRefreshToken() {
    ensureInit();
    return prefs.getString(KEY_REFRESH, null);
  }

  // 삭제(로그아웃)
  public static void clear() {
    ensureInit();
    prefs.edit().remove(KEY_ACCESS).remove(KEY_REFRESH).apply();
  }

  private static void ensureInit() {
    if (prefs == null) {
      throw new IllegalStateException("TokenStore not initialized. Call TokenStore.init(context) first.");
    }
  }
}
