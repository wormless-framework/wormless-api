package com.wormless.controller;

import com.wormless.entities.DependenciaScan;
import com.wormless.services.DependenciaScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dependencias")
@RequiredArgsConstructor
public class DependenciaScanController {

    private final DependenciaScanService dependenciaScanService;

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