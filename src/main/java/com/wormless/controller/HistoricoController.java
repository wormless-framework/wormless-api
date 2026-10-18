package com.wormless.controller;

import com.wormless.dto.response.HistoricoRelatorioDTO;
import com.wormless.security.UserRepository;
import com.wormless.services.AnaliseJobService;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/relatorios")
public class HistoricoController {

    private final AnaliseJobService analiseJobService;
    private final UserRepository userRepository;

    public HistoricoController(AnaliseJobService analiseJobService, UserRepository userRepository) {
        this.analiseJobService = analiseJobService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<HistoricoRelatorioDTO>> listar(
            Principal principal,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new IllegalArgumentException("Paginação inválida.");
        }
        Long userId = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."))
                .getId();
        return ResponseEntity.ok(analiseJobService.listarHistorico(
                userId, PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataInicio"))));
    }
}
