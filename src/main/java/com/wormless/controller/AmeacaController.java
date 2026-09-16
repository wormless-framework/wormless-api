package com.wormless.controller;

import com.wormless.entities.Ameaca;
import com.wormless.entities.enums.Severidade;
import com.wormless.services.AmeacaService;
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
@RequestMapping("/ameacas")
@RequiredArgsConstructor
public class AmeacaController {

    private final AmeacaService ameacaService;

    @PostMapping
    public ResponseEntity<Ameaca> salvar(
            @RequestBody Ameaca ameaca) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ameacaService.salvar(ameaca));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ameaca> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ameacaService.buscarPorId(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<Ameaca>> listarTodas() {

        return ResponseEntity.ok(
                ameacaService.listarTodas()
        );
    }

    @GetMapping("/severidade/{severidade}")
    public ResponseEntity<List<Ameaca>> listarPorSeveridade(
            @PathVariable Severidade severidade) {

        return ResponseEntity.ok(
                ameacaService.listarPorSeveridade(severidade)
        );
    }
}