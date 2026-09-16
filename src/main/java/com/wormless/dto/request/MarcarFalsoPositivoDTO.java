package com.wormless.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarcarFalsoPositivoDTO {

    @NotNull(message = "O indicador deve ser informado.")
    private Long indicadorId;

    @NotNull(message = "O analista deve ser informado.")
    private Long analistaId;

    @NotNull(message = "A regra de detecção deve ser informada.")
    private Long regraDeteccaoId;

    @NotBlank(message = "A justificativa técnica deve ser informada.")
    private String justificativaTecnica;
}