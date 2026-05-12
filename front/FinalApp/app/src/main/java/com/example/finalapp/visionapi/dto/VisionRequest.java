package com.example.finalapp.visionapi.dto;

import java.util.List;

public class VisionRequest {

    /** Vision API에 보낼 요청 목록 */
    private List<AnnotateImageRequest> requests;

    public List<AnnotateImageRequest> getRequests() { return requests; }
    public void setRequests(List<AnnotateImageRequest> requests) { this.requests = requests; }

    /** 편의 생성자: base64 + 기능 + 언어힌트 */
    public static VisionRequest fromBase64(
            String base64Content,
            String featureType,               // null 이면 DOCUMENT_TEXT_DETECTION
            List<String> languageHintsOrNull  // 예: Arrays.asList("ko","en")
    ) {
        VisionRequest vr = new VisionRequest();
        AnnotateImageRequest r = new AnnotateImageRequest();

        Image img = new Image();
        img.setContent(base64Content);
        r.setImage(img);

        Feature f = new Feature();
        f.setType(featureType == null ? "DOCUMENT_TEXT_DETECTION" : featureType); // ← 기본값 변경
        r.setFeatures(java.util.Collections.singletonList(f));

        if (languageHintsOrNull != null && !languageHintsOrNull.isEmpty()) {
            ImageContext ctx = new ImageContext();
            ctx.setLanguageHints(languageHintsOrNull); // ← ko/en 힌트 전달
            r.setImageContext(ctx);
        }

        vr.setRequests(java.util.Collections.singletonList(r));
        return vr;
    }

    // --- inner DTOs ---
    public static class AnnotateImageRequest {
        private Image image;
        private List<Feature> features;
        private ImageContext imageContext;
        public Image getImage() { return image; }
        public void setImage(Image image) { this.image = image; }
        public List<Feature> getFeatures() { return features; }
        public void setFeatures(List<Feature> features) { this.features = features; }
        public ImageContext getImageContext() { return imageContext; }
        public void setImageContext(ImageContext imageContext) { this.imageContext = imageContext; }
    }
    public static class Image {
        private String content; // base64
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    public static class Feature {
        private String type; // DOCUMENT_TEXT_DETECTION
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
    public static class ImageContext {
        private List<String> languageHints; // 예: ["ko","en"]
        public List<String> getLanguageHints() { return languageHints; }
        public void setLanguageHints(List<String> languageHints) { this.languageHints = languageHints; }
    }
}
