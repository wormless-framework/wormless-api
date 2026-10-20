package com.wormless.dto.request;

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

    /** Validado no SandboxService (existência, tipo e tamanho). */
    private MultipartFile arquivo;
}