package com.wormless.integrations.gemini;

import java.util.List;

public record GeminiResponse(List<Candidate> candidates) {
    public GeminiResponse {
        candidates = List.copyOf(candidates);
    }

    public record Candidate(Content content) { }
    public record Content(List<Part> parts) {
        public Content {
            parts = List.copyOf(parts);
        }
    }

    public record Part(String text) { }
    
    public String extrairTexto() {
        try {
            return candidates.get(0).content().parts().get(0).text();
        } catch (Exception e) {
            return "";
        }
    }
}
