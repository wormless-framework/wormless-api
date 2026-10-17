package com.wormless.controller;

import com.wormless.dto.request.ArquivoUploadDTO;
import com.wormless.dto.request.RepositorioRequestDTO;
import com.wormless.dto.response.AnaliseJobResponseDTO;
import com.wormless.dto.response.ArquivoSuspeitoDTO;
import com.wormless.services.RepositorioScannerService;
import com.wormless.services.SandboxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sandbox")
@RequiredArgsConstructor
public class SandboxController {

    private final SandboxService sandboxService;
    private final RepositorioScannerService repositorioScannerService;

    @PostMapping("/analises")
    public ResponseEntity<AnaliseJobResponseDTO> iniciarAnalise(
            @Valid @ModelAttribute ArquivoUploadDTO uploadDTO) {

        AnaliseJobResponseDTO response =
                sandboxService.iniciarAnalise(uploadDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/analises/{id}")
    public ResponseEntity<AnaliseJobResponseDTO> consultarResultado(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                sandboxService.consultarResultado(id)
        );
    }

    @PostMapping("/repositorio/triagem")
    public ResponseEntity<List<ArquivoSuspeitoDTO>> triagemRepositorio(
            @Valid @RequestBody RepositorioRequestDTO request) {

        List<ArquivoSuspeitoDTO> suspeitos = repositorioScannerService.fazerTriagemRapida(request);
        return ResponseEntity.ok(suspeitos);
    }

    @PostMapping("/repositorio/deep-scan")
    public ResponseEntity<AnaliseJobResponseDTO> deepScanRepositorio(
            @Valid @RequestBody RepositorioRequestDTO request) {
        
        AnaliseJobResponseDTO response = sandboxService.iniciarAnaliseDeRepositorio(
                request.getUrl(), 
                request.getCaminhoArquivo()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}