package com.wormless.dto.response;

import com.wormless.entities.enums.Severidade;
import java.time.LocalDateTime;

public record HistoricoRelatorioDTO(
        Long analiseId,
        String nomeArquivo,
        String status,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        Severidade severidade,
        String resumo) {
}
