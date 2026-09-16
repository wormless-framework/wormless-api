package com.wormless.controller;

import com.wormless.entities.FalsoPositivo;
import com.wormless.services.FalsoPositivoService;
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
@RequestMapping("/falsos-positivos")
@RequiredArgsConstructor
public class FalsoPositivoController {

    private final FalsoPositivoService falsoPositivoService;

    @PostMapping
    public ResponseEntity<FalsoPositivo> salvar(
            @RequestBody FalsoPositivo falsoPositivo) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(falsoPositivoService.salvar(falsoPositivo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FalsoPositivo> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                falsoPositivoService.buscarPorId(id)
        );
    }

    @GetMapping("/indicador/{indicadorId}")
    public ResponseEntity<List<FalsoPositivo>> listarPorIndicador(
            @PathVariable Long indicadorId) {

        return ResponseEntity.ok(
                falsoPositivoService.listarPorIndicador(indicadorId)
        );
    }

    @GetMapping("/analista/{analistaId}")
    public ResponseEntity<List<FalsoPositivo>> listarPorAnalista(
            @PathVariable Long analistaId) {

        return ResponseEntity.ok(
                falsoPositivoService.listarPorAnalista(analistaId)
        );
    }
}