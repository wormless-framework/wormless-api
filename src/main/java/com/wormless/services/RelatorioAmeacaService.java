package com.wormless.services;

import com.wormless.entities.RelatorioAmeaca;
import com.wormless.entities.enums.Severidade;
import com.wormless.exception.BusinessException;
import com.wormless.exception.ResourceNotFoundException;
import com.wormless.repository.RelatorioAmeacaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioAmeacaService {

    private final RelatorioAmeacaRepository relatorioRepository;

    @Transactional
    public RelatorioAmeaca salvar(RelatorioAmeaca relatorio) {

        if (relatorio == null) {
            throw new BusinessException(
                    "O relatório não pode ser nulo."
            );
        }

        if (relatorio.getAnaliseJob() == null) {
            throw new BusinessException(
                    "O relatório deve estar associado a uma análise."
            );
        }

        if (relatorio.getDataGeracao() == null) {
            relatorio.setDataGeracao(LocalDateTime.now());
        }

        return relatorioRepository.save(relatorio);
    }

    @Transactional(readOnly = true)
    public RelatorioAmeaca buscarPorId(Long id) {

        if (id == null) {
            throw new BusinessException(
                    "O ID do relatório deve ser informado."
            );
        }

        return relatorioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Relatório não encontrado com o ID: " + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public String gerarResumo(Long id) {

        return buscarPorId(id).gerarResumo();
    }

    @Transactional(readOnly = true)
    public List<RelatorioAmeaca> listarPorSeveridade(
            Severidade severidade) {

        if (severidade == null) {
            throw new BusinessException(
                    "A severidade deve ser informada."
            );
        }

        return relatorioRepository
                .findBySeveridadeGeral(severidade);
    }
}