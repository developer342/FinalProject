package com.example.finalapp.ledger.data.repo;

import androidx.annotation.NonNull;

import com.example.finalapp.core.storage.TokenStore;
import com.example.finalapp.ledger.data.api.EntryApiService;
import com.example.finalapp.ledger.data.api.LedgerApiClient;
import com.example.finalapp.ledger.dto.EntryRequest;
import com.example.finalapp.ledger.dto.EntryResponse;
import com.example.finalapp.ledger.dto.PageResponse;
import com.example.finalapp.ledger.dto.ParseRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class EntryRepository {

    private final EntryApiService api;

    public EntryRepository() {
        this.api = LedgerApiClient.getInstance().createService(EntryApiService.class);
    }

    /** OCR 정제 텍스트 서버 분석 요청: POST /api/entries/parse */
    public Call<EntryResponse> parseText(@NonNull String rawText) {
        String token = TokenStore.getAccessToken(); // 저장된 JWT 토큰 불러오기
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("로그인 토큰이 없습니다.");
        }

        ParseRequest req = ParseRequest.builder()
                .rawText(rawText)
                .build();

        return api.parseAndSave("Bearer " + token, req);
    }

    /** 기간별 목록 조회 요청 */
    public Call<PageResponse<EntryResponse>> list(String from, String to, int page, int size) {
        return api.list(from, to, page, size);
    }

    /** 기간 CSV 다운로드: GET /api/entries/export?from&to */
    public Call<ResponseBody> exportCsv(@NonNull String from, @NonNull String to) {
        return api.export(from, to);
    }

    /** 가계부 항목 저장 요청 (/api/entries) */
    public Call<EntryResponse> createEntry(@NonNull EntryRequest req) {
        return api.create(req);
    }

    /** 엑셀 다운로드 요청 (JWT 불필요) */
    public Call<ResponseBody> exportXlsx(String from, String to) {
        return api.exportXlsx(from, to);
    }
}
