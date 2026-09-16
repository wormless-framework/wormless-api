package com.wormless.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArquivoUploadDTO {

    @NotNull(message = "O arquivo deve ser informado.")
    private MultipartFile arquivo;
}