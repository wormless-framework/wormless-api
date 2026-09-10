package com.wormless.api.controller;

import com.wormless.dto.request.ArquivoUploadDTO;
import com.wormless.dto.response.AnaliseJobResponseDTO;
import com.wormless.entities.Arquivo;
import com.wormless.service.SandboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class ArquivoController {

    private final SandboxService sandboxService;

    @PostMapping("/upload")
    public ResponseEntity<AnaliseJobResponseDTO> uploadArquivo(@RequestBody ArquivoUploadDTO dto) {
        // Conversão simples de DTO para Entidade
        Arquivo arquivo = new Arquivo();
        arquivo.setNomeOriginal(dto.nomeOriginal());
        // Lógica de salvar o arquivo no disco/S3 iria aqui
        
        Arquivo arquivoSalvo = sandboxService.registrarUpload(arquivo);
        
        // Dispara o processamento em background (Assíncrono)
        sandboxService.iniciarProcessamentoAsync(arquivoSalvo.getId());
        
        return ResponseEntity.ok(new AnaliseJobResponseDTO(
            arquivoSalvo.getId(), 
            com.wormless.api.model.enums.StatusJob.PENDENTE, 
            "Upload realizado com sucesso. Processamento iniciado."
        ));
    }
}