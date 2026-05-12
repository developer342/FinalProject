package com.example.FinalServer.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GptResponse {
    private List<Choice> choices;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Choice {
        private Message message;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String role;
        private String content;
    }

    public String getContentOrEmpty() {
        if (choices == null || choices.isEmpty()) return "";
        return Optional.ofNullable(choices.get(0).getMessage())
                .map(Message::getContent)
                .orElse("");
    }
}