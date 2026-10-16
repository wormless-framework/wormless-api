package com.wormless.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepositorioRequestDTO {
    @NotBlank(message = "A URL do repositório deve ser informada.")
    private String url;
}