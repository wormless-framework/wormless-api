package com.wormless.services;

import com.wormless.entities.RelatorioAmeaca;
import com.wormless.entities.enums.Severidade;
import com.wormless.repository.RelatorioAmeacaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RelatorioAmeacaService {

    private final RelatorioAmeacaRepository relatorioRepository;

    public RelatorioAmeacaService(
            RelatorioAmeacaRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    public RelatorioAmeaca salvar(RelatorioAmeaca relatorio) {
        if (relatorio.getDataGeracao() == null) {
            relatorio.setDataGeracao(LocalDateTime.now());
        }

        return relatorioRepository.save(relatorio);
    }

    @Transactional(readOnly = true)
    public RelatorioAmeaca buscarPorId(Long id) {
        return relatorioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Relatório não encontrado: " + id));
    }

    public String gerarResumo(Long id) {
        return buscarPorId(id).gerarResumo();
    }

    @Transactional(readOnly = true)
    public List<RelatorioAmeaca> listarPorSeveridade(
            Severidade severidade) {
        return relatorioRepository
                .findBySeveridadeGeral(severidade);
    }
}