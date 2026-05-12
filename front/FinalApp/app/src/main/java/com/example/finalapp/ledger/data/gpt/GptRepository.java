package com.example.finalapp.ledger.data.gpt;

import com.example.finalapp.ledger.data.api.LedgerApiClient;
import com.example.finalapp.ledger.dto.GptRequest;
import com.example.finalapp.ledger.dto.GptResponse;

import java.util.Arrays;

import retrofit2.Call;

public class GptRepository {

    private final GptApiService api;

    public GptRepository() {
        this.api = LedgerApiClient.getInstance().createService(GptApiService.class);
    }

    public Call<GptResponse> refineWithServer(String rawText) {
        GptRequest req = new GptRequest(
                null,
                Arrays.asList(new GptRequest.Message("user", rawText))
        );
        return api.refineText(req);
    }
}
