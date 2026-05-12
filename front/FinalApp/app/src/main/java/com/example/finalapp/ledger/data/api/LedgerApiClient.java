package com.example.finalapp.ledger.data.api;

import com.example.finalapp.core.network.AuthInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class LedgerApiClient {

    private static volatile LedgerApiClient instance;
    private final Retrofit retrofit;
    private final EntryApiService entryService;

    public static LedgerApiClient getInstance() {
        if (instance == null) {
            synchronized (LedgerApiClient.class) {
                if (instance == null) instance = new LedgerApiClient();
            }
        }
        return instance;
    }

    private LedgerApiClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor())      // Bearer 자동 주입
                .addInterceptor(new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        entryService = retrofit.create(EntryApiService.class);
    }

    // 기본 서버 주소 제공
    private String getBaseUrl() {
        // 에뮬레이터: http://10.0.2.2:8080/api/
        //return "http://192.168.0.60:8080/api/";   // 실기기 (학교)
        return "http://192.168.1.151:8080/api/";     //실기기 (집)
//        return "http://10.0.2.2:8080/api/";  // 에뮬
    }

    public EntryApiService getEntryService() {
        return entryService;
    }

    // GPT 같은 다른 API 인터페이스도 동적으로 제공 가능
    public <T> T createService(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }


}
