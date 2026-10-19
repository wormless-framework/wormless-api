package com.wormless.integrations.gemini;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gemini", url = "${gemini.api.url}")
public interface GeminiCliente {

    @PostMapping(value = "/v1beta/models/{model}:generateContent", consumes = "application/json")
    GeminiResponse gerarConteudo(
            @PathVariable("model") String model,
            @RequestHeader("x-goog-api-key") String apiKey,
            @RequestBody GeminiRequest request
    );
}
