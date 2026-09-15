package com.wormless.controller;

import org.springframework.stereotype.Controller;

import com.wormless.entities.Arquivo;
import com.wormless.service.SandBoxService;

@RestController
@RequestMapping("/file")
public class SandboxController {

    @PostMapping("/upload")
    public ResponseEntity<String> analisarArquivo(@RequestParam("file") MultipartFile file) {
        // Aqui futuramente chamaremos o Service que integra com o VirusTotal e a LLM
        
        return ResponseEntity.ok("Arquivo recebido com sucesso para análise: " + file.getOriginalFilename());
    }

    // Exemplo de endpoint para listar os arquivos analisados (para o painel React)
    @GetMapping
    public ResponseEntity<?> listarArquivosAnalisados() {
        // Aqui retornaremos a lista de arquivos salvos no banco de dados
        return ResponseEntity.ok().build();
    }
}