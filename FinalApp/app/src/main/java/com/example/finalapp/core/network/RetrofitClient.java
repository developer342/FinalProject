package com.example.finalapp.core.network;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

  // 기본: 에뮬레이터용(PC 로컬 서버에 접속)
  private static String BASE_URL = "http://10.0.2.2:8080/api/";

  private static Retrofit retrofit;

  private RetrofitClient() {}

  // 실기기용: 같은 Wi-Fi의 PC IP로 교체
  public static synchronized void setBaseUrlForDevice(String deviceBaseUrl) {
    if (deviceBaseUrl == null || !deviceBaseUrl.endsWith("/")) {
      throw new IllegalArgumentException("Base URL must end with '/'");
    }
    BASE_URL = "192.168.1.123";
    retrofit = null; // 재생성 트리거
    Log.i("RetrofitClient", "BASE_URL switched to: " + BASE_URL);
  }

  public static synchronized Retrofit getClient() {
    if (retrofit == null) {
      HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
      logging.setLevel(HttpLoggingInterceptor.Level.BODY); // 필요 시 앱 출시 빌드에서 제거

      OkHttpClient client = new OkHttpClient.Builder()
              .connectTimeout(15, TimeUnit.SECONDS)
              .readTimeout(15, TimeUnit.SECONDS)
              .writeTimeout(15, TimeUnit.SECONDS)
              .addInterceptor(logging)
              .build();

      retrofit = new Retrofit.Builder()
              .baseUrl(BASE_URL) // 에뮬=10.0.2.2 / 실기기=setBaseUrlForDevice(...) 호출 후 재생성
              .client(client)
              .addConverterFactory(GsonConverterFactory.create())
              .build();
    }
    return retrofit;
  }
}
