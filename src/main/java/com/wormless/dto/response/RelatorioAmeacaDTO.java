package com.wormless.dto.response;

import com.wormless.entities.enums.Severidade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioAmeacaDTO {

    private Long id;

    private Long analiseId;

    private LocalDateTime dataGeracao;

    private Severidade severidadeGeral;

    private String resumo;

    private List<String> indicadores;

    private List<String> ameacas;

    private List<String> vulnerabilidades;

    private List<String> remediacoes;
}