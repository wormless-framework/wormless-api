package com.wormless.controller;

import com.wormless.entities.DependenciaScan;
import com.wormless.dto.request.DependencyScanRequestDTO;
import com.wormless.dto.response.DependencyScanResponseDTO;
import com.wormless.security.GitHubActionsOidcService;
import com.wormless.services.RepositorioDependencyScannerService;
import com.wormless.services.DependenciaScanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/dependencias")
@RequiredArgsConstructor
public class DependenciaScanController {

    private final DependenciaScanService dependenciaScanService;
    private final RepositorioDependencyScannerService repositorioDependencyScannerService;
    private final GitHubActionsOidcService gitHubActionsOidcService;

    @PostMapping("/scan")
    public ResponseEntity<DependencyScanResponseDTO> scan(
            @Valid @RequestBody DependencyScanRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token OIDC ausente.");
        }
        String token = authorization.substring("Bearer ".length());
        gitHubActionsOidcService.validate(token, request);
        return ResponseEntity.ok(repositorioDependencyScannerService.scan(request));
    }

    @PostMapping
    public ResponseEntity<DependenciaScan> salvar(
            @RequestBody DependenciaScan dependencia) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        dependenciaScanService.salvar(dependencia)
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DependenciaScan> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                dependenciaScanService.buscarPorId(id)
        );
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<DependenciaScan>> listarPorJob(
            @PathVariable Long jobId) {

        return ResponseEntity.ok(
                dependenciaScanService.listarPorJob(jobId)
        );
    }

    @GetMapping("/pacote/{nomePacote}")
    public ResponseEntity<List<DependenciaScan>> buscarPacote(
            @PathVariable String nomePacote) {

        return ResponseEntity.ok(
                dependenciaScanService.buscarPacote(nomePacote)
        );
    }

    @GetMapping("/pacote/{nomePacote}/versao/{versao}")
    public ResponseEntity<List<DependenciaScan>> buscarPacoteEVersao(
            @PathVariable String nomePacote,
            @PathVariable String versao) {

        return ResponseEntity.ok(
                dependenciaScanService.buscarPacoteEVersao(
                        nomePacote,
                        versao
                )
        );
    }
}
