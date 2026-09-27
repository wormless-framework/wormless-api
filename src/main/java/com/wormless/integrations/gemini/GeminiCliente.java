package com.wormless.integrations.gemini;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gemini", url = "${gemini.api.url}")
public interface GeminiCliente {

    @PostMapping(value = "/v1beta/models/gemini-3.5-flash:generateContent", consumes = "application/json")
    GeminiResponse gerarConteudo(
            @RequestParam("key") String apiKey,
            @RequestBody GeminiRequest request
    );
}