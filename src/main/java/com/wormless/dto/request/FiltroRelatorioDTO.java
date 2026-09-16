package com.wormless.dto.request;

import com.wormless.entities.enums.Severidade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FiltroRelatorioDTO {

    private Severidade severidade;

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;
}