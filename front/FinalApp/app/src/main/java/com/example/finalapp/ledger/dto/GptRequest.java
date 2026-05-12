package com.example.finalapp.ledger.dto;

import java.util.List;

public class GptRequest {

    private String model;
    private List<Message> messages;

    /** 메시지 객체 */
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
        public void setRole(String role) { this.role = role; }
        public void setContent(String content) { this.content = content; }
    }

    public GptRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() { return model; }
    public List<Message> getMessages() { return messages; }
    public void setModel(String model) { this.model = model; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
}
