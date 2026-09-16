package com.wormless.controller;

import com.wormless.entities.IndicadorAmeaca;
import com.wormless.entities.enums.StatusIndicador;
import com.wormless.services.IndicadorAmeacaService;
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
@RequestMapping("/indicadores")
@RequiredArgsConstructor
public class IndicadorAmeacaController {

    private final IndicadorAmeacaService indicadorAmeacaService;

    @PostMapping
    public ResponseEntity<IndicadorAmeaca> salvar(
            @RequestBody IndicadorAmeaca indicador) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(indicadorAmeacaService.salvar(indicador));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IndicadorAmeaca> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                indicadorAmeacaService.buscarPorId(id)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<IndicadorAmeaca>> buscarPorStatus(
            @PathVariable StatusIndicador status) {

        return ResponseEntity.ok(
                indicadorAmeacaService.buscarPorStatus(status)
        );
    }

    @GetMapping("/ameaca/{ameacaId}")
    public ResponseEntity<List<IndicadorAmeaca>> listarPorAmeaca(
            @PathVariable Long ameacaId) {

        return ResponseEntity.ok(
                indicadorAmeacaService.listarPorAmeaca(ameacaId)
        );
    }
}