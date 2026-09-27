package com.wormless.integrations.gemini;

import java.util.List;

public record GeminiResponse(List<Candidate> candidates) {
    public record Candidate(Content content) {}
    public record Content(List<Part> parts) {}
    public record Part(String text) {}
    
    public String extrairTexto() {
        try {
            return candidates.get(0).content().parts().get(0).text();
        } catch (Exception e) {
            return "";
        }
    }
}