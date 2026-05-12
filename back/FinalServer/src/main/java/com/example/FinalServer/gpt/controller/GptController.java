package com.example.FinalServer.gpt.controller;


import com.example.FinalServer.gpt.dto.GptResponse;
import com.example.FinalServer.gpt.service.GptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gpt")
public class GptController {

    private final GptService gptService;

    /** OCR 텍스트를 GPT로 정제 요청 */
    @PostMapping("/refine")
    public ResponseEntity<GptResponse> refine(@RequestBody String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String result = gptService.refineReceiptText(rawText);

        // 앱 GptResponse 구조에 맞게 감싸서 반환
        GptResponse.Message message = GptResponse.Message.builder()
                .role("assistant")
                .content(result)
                .build();

        GptResponse.Choice choice = GptResponse.Choice.builder()
                .message(message)
                .build();

        GptResponse response = GptResponse.builder()
                .choices(List.of(choice))
                .build();

        return ResponseEntity.ok(response);
    }
}
