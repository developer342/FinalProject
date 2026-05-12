package com.example.finalapp.ledger.data.api;

import com.example.finalapp.ledger.dto.EntryRequest;
import com.example.finalapp.ledger.dto.EntryResponse;
import com.example.finalapp.ledger.dto.PageResponse;
import com.example.finalapp.ledger.dto.ParseRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface EntryApiService {

    // 가계부 항목 저장: POST /api/entries
    @POST("entries")
    Call<EntryResponse> create(@Body EntryRequest request);

    // 기간별 목록 조회: GET /api/entries
    @GET("entries")
    Call<PageResponse<EntryResponse>> list(
            @Query("from") String from,
            @Query("to") String to,
            @Query("page") int page,
            @Query("size") int size
    );

    // 기간별 CSV 다운로드: GET /api/entries/export
    @GET("entries/export")
    Call<ResponseBody> export(
            @Query("from") String from,
            @Query("to") String to
    );

    /** 엑셀 파일 다운로드 (공개 요청) */
    @GET("entries/export.xlsx")
    Call<ResponseBody> exportXlsx(
            @Query("from") String from,
            @Query("to") String to
    );

    @POST("/api/entries/parse")
    Call<EntryResponse> parseAndSave(
            @Header("Authorization") String token,
            @Body ParseRequest request
    );
}
