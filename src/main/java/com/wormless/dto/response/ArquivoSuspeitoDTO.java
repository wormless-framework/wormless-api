package com.wormless.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArquivoSuspeitoDTO {
    private String nome;
    private String caminho;
    private String motivoSuspeita;
}