package com.wormless.services;

import com.wormless.entities.FalsoPositivo;
import com.wormless.repository.FalsoPositivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FalsoPositivoService {

    private final FalsoPositivoRepository falsoPositivoRepository;

    public FalsoPositivoService(
            FalsoPositivoRepository falsoPositivoRepository) {
        this.falsoPositivoRepository = falsoPositivoRepository;
    }

    public FalsoPositivo salvar(FalsoPositivo falsoPositivo) {
        if (falsoPositivo.getDataMarcacao() == null) {
            falsoPositivo.setDataMarcacao(LocalDateTime.now());
        }

        falsoPositivo.aplicar();

        return falsoPositivoRepository.save(falsoPositivo);
    }

    @Transactional(readOnly = true)
    public FalsoPositivo buscarPorId(Long id) {
        return falsoPositivoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Falso positivo não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<FalsoPositivo> listarPorIndicador(
            Long indicadorId) {
        return falsoPositivoRepository
                .findByIndicadorAmeacaId(indicadorId);
    }

    @Transactional(readOnly = true)
    public List<FalsoPositivo> listarPorAnalista(
            Long analistaId) {
        return falsoPositivoRepository
                .findByAnalistaSOCId(analistaId);
    }
}