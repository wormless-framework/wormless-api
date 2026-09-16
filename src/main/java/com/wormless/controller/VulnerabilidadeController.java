package com.wormless.controller;

import com.wormless.entities.Vulnerabilidade;
import com.wormless.services.VulnerabilidadeService;
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
@RequestMapping("/vulnerabilidades")
@RequiredArgsConstructor
public class VulnerabilidadeController {

    private final VulnerabilidadeService vulnerabilidadeService;

    @PostMapping
    public ResponseEntity<Vulnerabilidade> salvar(
            @RequestBody Vulnerabilidade vulnerabilidade) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vulnerabilidadeService.salvar(vulnerabilidade));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vulnerabilidade> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                vulnerabilidadeService.buscarPorId(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<Vulnerabilidade>> listarTodas() {

        return ResponseEntity.ok(
                vulnerabilidadeService.listarTodas()
        );
    }

    @GetMapping("/cve/{cve}")
    public ResponseEntity<Vulnerabilidade> buscarPorCve(
            @PathVariable String cve) {

        return ResponseEntity.ok(
                vulnerabilidadeService.buscarPorCve(cve)
        );
    }
}