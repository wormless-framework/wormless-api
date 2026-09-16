package com.wormless.dto.response;

import com.wormless.entities.enums.Severidade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseJobResponseDTO {

    private Long analiseId;

    private String status;

    private boolean ameacaDetectada;

    private Severidade severidade;

    private String resumo;
}