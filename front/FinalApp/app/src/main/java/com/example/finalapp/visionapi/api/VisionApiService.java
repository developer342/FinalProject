package com.example.finalapp.visionapi.api;

import com.example.finalapp.visionapi.dto.VisionRequest;
import com.example.finalapp.visionapi.dto.VisionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface VisionApiService {

    // POST https://vision.googleapis.com/v1/images:annotate?key=YOUR_API_KEY
    @POST("v1/images:annotate")
    Call<VisionResponse> annotate(@Body VisionRequest body);
}
