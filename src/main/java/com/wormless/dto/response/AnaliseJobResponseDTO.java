package com.wormless.dto.response;

import com.wormless.entities.enums.Severidade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    private List<String> testes;
}