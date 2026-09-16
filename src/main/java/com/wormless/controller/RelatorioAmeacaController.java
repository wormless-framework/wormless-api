package com.wormless.controller;

import com.wormless.entities.RelatorioAmeaca;
import com.wormless.entities.enums.Severidade;
import com.wormless.services.RelatorioAmeacaService;
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
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioAmeacaController {

    private final RelatorioAmeacaService relatorioAmeacaService;

    @PostMapping
    public ResponseEntity<RelatorioAmeaca> salvar(
            @RequestBody RelatorioAmeaca relatorio) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(relatorioAmeacaService.salvar(relatorio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RelatorioAmeaca> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                relatorioAmeacaService.buscarPorId(id)
        );
    }

    @GetMapping("/{id}/resumo")
    public ResponseEntity<String> gerarResumo(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                relatorioAmeacaService.gerarResumo(id)
        );
    }

    @GetMapping("/severidade/{severidade}")
    public ResponseEntity<List<RelatorioAmeaca>> listarPorSeveridade(
            @PathVariable Severidade severidade) {

        return ResponseEntity.ok(
                relatorioAmeacaService.listarPorSeveridade(severidade)
        );
    }
}