package com.wormless.controller;

import com.wormless.entities.PipelineCICD;
import com.wormless.services.PipelineCICDService;
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
@RequestMapping("/pipelines")
@RequiredArgsConstructor
public class PipelineCICDController {

    private final PipelineCICDService pipelineCICDService;

    @PostMapping
    public ResponseEntity<PipelineCICD> salvar(
            @RequestBody PipelineCICD pipeline) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pipelineCICDService.salvar(pipeline));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PipelineCICD> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                pipelineCICDService.buscarPorId(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<PipelineCICD>> listarTodos() {

        return ResponseEntity.ok(
                pipelineCICDService.listarTodos()
        );
    }
}