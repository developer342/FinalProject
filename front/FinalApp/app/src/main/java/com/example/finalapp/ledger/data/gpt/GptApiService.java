package com.example.finalapp.ledger.data.gpt;

import com.example.finalapp.ledger.dto.GptRequest;
import com.example.finalapp.ledger.dto.GptResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GptApiService {

    // 서버로 GPT 정제 요청 보내기
    @POST("gpt/refine")
    Call<GptResponse> refineText(@Body GptRequest request);
}
