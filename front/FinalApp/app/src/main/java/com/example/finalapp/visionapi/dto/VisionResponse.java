package com.example.finalapp.visionapi.dto;

import java.util.List;

public class VisionResponse {

    /** 응답 리스트(일반적으로 1건) */
    private List<AnnotateImageResponse> responses;

    public List<AnnotateImageResponse> getResponses() { return responses; }
    public void setResponses(List<AnnotateImageResponse> responses) { this.responses = responses; }

    /** 전체 텍스트 편의 접근자 (fullText → 없으면 textAnnotations[0]) */
    public String getFullTextOrEmpty() {
        if (responses == null || responses.isEmpty()) return "";
        AnnotateImageResponse r = responses.get(0);
        if (r == null) return "";
        if (r.getFullTextAnnotation() != null && r.getFullTextAnnotation().getText() != null) {
            return r.getFullTextAnnotation().getText();
        }
        if (r.getTextAnnotations() != null && !r.getTextAnnotations().isEmpty()) {
            EntityAnnotation ea = r.getTextAnnotations().get(0);
            if (ea != null && ea.getDescription() != null) return ea.getDescription();
        }
        return "";
    }

    /* -------- inner DTOs (필요 부분만) -------- */

    /** 단건 응답 */
    public static class AnnotateImageResponse {
        private TextAnnotation fullTextAnnotation;  // 전체 텍스트 블록
        private List<EntityAnnotation> textAnnotations; // 개별 엔티티(첫 요소가 전체일 수 있음)

        public TextAnnotation getFullTextAnnotation() { return fullTextAnnotation; }
        public void setFullTextAnnotation(TextAnnotation fullTextAnnotation) { this.fullTextAnnotation = fullTextAnnotation; }

        public List<EntityAnnotation> getTextAnnotations() { return textAnnotations; }
        public void setTextAnnotations(List<EntityAnnotation> textAnnotations) { this.textAnnotations = textAnnotations; }
    }

    /** 전체 텍스트 컨테이너 */
    public static class TextAnnotation {
        private String text; // OCR 결과 전체 텍스트

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    /** 개별 텍스트 엔티티(첫 요소가 전체 설명일 수 있음) */
    public static class EntityAnnotation {
        private String description; // 텍스트 조각(또는 전체)

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
