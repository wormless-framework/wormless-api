// --- PACOTE: com.wormless.api.config ---

package com.wormless.api.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Habilita a execução de métodos em background (ex: @Async no SandboxService)
}