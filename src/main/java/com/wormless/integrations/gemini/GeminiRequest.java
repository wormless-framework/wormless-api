package com.wormless.integrations.gemini;

import java.util.List;

public record GeminiRequest(List<Content> contents) {
    public GeminiRequest {
        contents = List.copyOf(contents);
    }

    public record Content(List<Part> parts) {
        public Content {
            parts = List.copyOf(parts);
        }
    }

    public record Part(String text) { }
}
