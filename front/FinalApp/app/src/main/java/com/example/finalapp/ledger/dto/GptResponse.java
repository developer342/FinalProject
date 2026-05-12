package com.example.finalapp.ledger.dto;

import java.util.List;

public class GptResponse {

    private List<Choice> choices;

    /** 단일 응답 */
    public static class Choice {
        private Message message;

        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }
    }

    /** 메시지 내용 */
    public static class Message {
        private String role;
        private String content;

        public String getRole() { return role; }
        public String getContent() { return content; }
        public void setRole(String role) { this.role = role; }
        public void setContent(String content) { this.content = content; }
    }

    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }

    /** 편의 메서드: 첫 번째 응답 텍스트 반환 */
    public String getContentOrEmpty() {
        if (choices == null || choices.isEmpty()) return "";
        Choice c = choices.get(0);
        if (c == null || c.getMessage() == null) return "";
        return c.getMessage().getContent() == null ? "" : c.getMessage().getContent();
    }
}
