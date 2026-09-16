package com.wormless.controller;

import com.wormless.entities.Remediacao;
import com.wormless.services.RemediacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/remediacoes")
@RequiredArgsConstructor
public class RemediacaoController {

    private final RemediacaoService remediacaoService;

    @PostMapping
    public ResponseEntity<Remediacao> salvar(
            @RequestBody Remediacao remediacao) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(remediacaoService.salvar(remediacao));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Remediacao> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                remediacaoService.buscarPorId(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        remediacaoService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}