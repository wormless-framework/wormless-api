package com.wormless.controller;

import com.wormless.entities.RegraDeteccao;
import com.wormless.services.RegraDeteccaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/regras-deteccao")
@RequiredArgsConstructor
public class RegraDeteccaoController {

    private final RegraDeteccaoService regraDeteccaoService;

    @PostMapping
    public ResponseEntity<RegraDeteccao> salvar(
            @RequestBody RegraDeteccao regra) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(regraDeteccaoService.salvar(regra));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegraDeteccao> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                regraDeteccaoService.buscarPorId(id)
        );
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<RegraDeteccao>> listarAtivas() {

        return ResponseEntity.ok(
                regraDeteccaoService.listarAtivas()
        );
    }

    @PutMapping("/{id}/ignorar")
    public ResponseEntity<RegraDeteccao> ignorarEmMetricasFuturas(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                regraDeteccaoService.ignorarEmMetricasFuturas(id)
        );
    }
}