package com.example.finalapp.visionapi.api;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Cloud Vision API 전용 Retrofit 클라이언트 */
public final class VisionApiClient {

    private static final String BASE_URL = "https://vision.googleapis.com/";
    private static volatile VisionApiClient instance;

    private final Retrofit retrofit;
    private volatile String apiKey = "AIzaSyDwEk2tf_NRdn8_piS43GUnKgpGwB0ppZA";

    public static VisionApiClient getInstance() {
        if (instance == null) {
            synchronized (VisionApiClient.class) {
                if (instance == null) instance = new VisionApiClient();
            }
        }
        return instance;
    }

    private VisionApiClient() {
        // API 키를 쿼리파라미터로 자동 부착
        Interceptor apiKeyInterceptor = chain -> {
            Request original = chain.request();
            HttpUrl originalUrl = original.url();

            HttpUrl newUrl = originalUrl.newBuilder()
                    .setQueryParameter("key", apiKey == null ? "" : apiKey)
                    .build();

            Request newReq = original.newBuilder().url(newUrl).build();
            return chain.proceed(newReq);
        };

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(apiKeyInterceptor)
                .addInterceptor(logging)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL) // https://vision.googleapis.com/
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /** 앱 시작 시 한 번 호출해서 API 키 설정 */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public VisionApiService getService() {
        return retrofit.create(VisionApiService.class);
    }
}
