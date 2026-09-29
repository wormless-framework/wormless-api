package com.wormless.integrations.gemini;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final GeminiCliente cliente;
    private final String apiKey;
    private final List<String> modelos;

    public GeminiService(GeminiCliente cliente,
                         @Value("${gemini.api.key}") String apiKey,
                         @Value("${gemini.models}") List<String> modelos) {
        this.cliente = cliente;
        this.apiKey = apiKey;
        this.modelos = modelos;
    }

    public GeminiResponse gerar(GeminiRequest request) {
        FeignException ultimoErro = null;

        for (String modelo : modelos) {
            for (int tentativa = 1; tentativa <= 2; tentativa++) {
                try {
                    return cliente.gerarConteudo(modelo.trim(), apiKey, request);
                } catch (FeignException e) {
                    ultimoErro = e;
                    log.warn("Modelo {} falhou (status {}), tentativa {}", modelo, e.status(), tentativa);

                    if (e.status() == 404) break;                    // modelo inexistente: próximo
                    if (!deveTentarNovamente(e.status())) throw e;   // 400/401/403: não adianta insistir

                    dormir(500L * tentativa);
                }
            }
        }
        throw new IllegalStateException("Todos os modelos Gemini falharam", ultimoErro);
    }

    private boolean deveTentarNovamente(int status) {
        return status == -1 || status == 429 || status >= 500;
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}