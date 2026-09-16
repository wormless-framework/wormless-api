package com.wormless.controller;

import com.wormless.entities.UsuarioWeb;
import com.wormless.services.UsuarioWebService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioWebController {

    private final UsuarioWebService usuarioWebService;

    @PostMapping
    public ResponseEntity<UsuarioWeb> salvar(
            @RequestBody UsuarioWeb usuario) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioWebService.salvar(usuario));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioWeb>> listarTodos() {

        return ResponseEntity.ok(
                usuarioWebService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioWeb> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                usuarioWebService.buscarPorId(id)
        );
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioWeb> buscarPorEmail(
            @PathVariable String email) {

        return ResponseEntity.ok(
                usuarioWebService.buscarPorEmail(email)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioWeb> atualizar(
            @PathVariable Long id,
            @RequestBody UsuarioWeb usuario) {

        return ResponseEntity.ok(
                usuarioWebService.atualizar(id, usuario)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        usuarioWebService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
