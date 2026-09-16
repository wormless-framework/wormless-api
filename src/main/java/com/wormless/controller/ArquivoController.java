package com.wormless.controller;

import com.wormless.entities.Arquivo;
import com.wormless.services.ArquivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/arquivos")
@RequiredArgsConstructor
public class ArquivoController {

    private final ArquivoService arquivoService;

    @PostMapping
    public ResponseEntity<Arquivo> salvar(
            @RequestBody Arquivo arquivo) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(arquivoService.salvar(arquivo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Arquivo> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                arquivoService.buscarPorId(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<Arquivo>> listarTodos() {

        return ResponseEntity.ok(
                arquivoService.listarTodos()
        );
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Arquivo>> buscarPorNome(
            @PathVariable String nome) {

        return ResponseEntity.ok(
                arquivoService.buscarPorNome(nome)
        );
    }

    @GetMapping("/usuario/{usuarioWebId}")
    public ResponseEntity<List<Arquivo>> listarPorUsuario(
            @PathVariable Long usuarioWebId) {

        return ResponseEntity.ok(
                arquivoService.listarPorUsuario(usuarioWebId)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        arquivoService.excluir(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}