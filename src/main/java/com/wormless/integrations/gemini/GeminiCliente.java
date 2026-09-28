package com.wormless.integrations.gemini;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gemini", url = "${gemini.api.url}")
public interface GeminiCliente {

    @PostMapping(value = "/v1beta/models/{model}:generateContent", consumes = "application/json")
    GeminiResponse gerarConteudo(
            @PathVariable("model") String model,
            @RequestHeader("x-goog-api-key") String apiKey,
            @RequestBody GeminiRequest request
    );
}