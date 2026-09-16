package com.wormless.controller;

import com.wormless.entities.AnaliseJob;
import com.wormless.entities.enums.StatusJob;
import com.wormless.services.AnaliseJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analises")
@RequiredArgsConstructor
public class AnaliseJobController {

    private final AnaliseJobService analiseJobService;

    @PostMapping
    public ResponseEntity<AnaliseJob> salvar(
            @RequestBody AnaliseJob analiseJob) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(analiseJobService.salvar(analiseJob));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnaliseJob> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                analiseJobService.buscarPorId(id)
        );
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<StatusJob> consultarStatus(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                analiseJobService.consultarStatus(id)
        );
    }

    @PutMapping("/{id}/iniciar")
    public ResponseEntity<AnaliseJob> iniciar(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                analiseJobService.iniciar(id)
        );
    }

    @PutMapping("/{id}/concluir")
    public ResponseEntity<AnaliseJob> concluir(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                analiseJobService.concluir(id)
        );
    }

    @PutMapping("/{id}/erro")
    public ResponseEntity<AnaliseJob> marcarErro(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                analiseJobService.marcarErro(id)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AnaliseJob>> buscarPorStatus(
            @PathVariable StatusJob status) {

        return ResponseEntity.ok(
                analiseJobService.buscarPorStatus(status)
        );
    }
}