package com.wormless.controller;

import com.wormless.entities.PoliticaBloqueio;
import com.wormless.entities.RelatorioAmeaca;
import com.wormless.services.PoliticaBloqueioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/politicas-bloqueio")
@RequiredArgsConstructor
public class PoliticaBloqueioController {

    private final PoliticaBloqueioService politicaBloqueioService;

    @PostMapping
    public ResponseEntity<PoliticaBloqueio> salvar(
            @RequestBody PoliticaBloqueio politicaBloqueio) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        politicaBloqueioService.salvar(politicaBloqueio)
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoliticaBloqueio> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                politicaBloqueioService.buscarPorId(id)
        );
    }

    @PostMapping("/{politicaId}/avaliar")
    public ResponseEntity<Boolean> avaliar(
            @PathVariable Long politicaId,
            @RequestBody RelatorioAmeaca relatorio) {

        return ResponseEntity.ok(
                politicaBloqueioService.avaliar(
                        politicaId,
                        relatorio
                )
        );
    }
}