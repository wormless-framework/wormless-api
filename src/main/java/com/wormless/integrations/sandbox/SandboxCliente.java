package com.wormless.integrations.sandbox;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@FeignClient (
    name = "sandbox",
    url = "${sandbox.url}"
)
public interface SandboxCliente {

    @PostMapping ("/analise")
    String executarAnalise(
            @RequestParam ("arquivo") MultipartFile arquivo
    );
}
