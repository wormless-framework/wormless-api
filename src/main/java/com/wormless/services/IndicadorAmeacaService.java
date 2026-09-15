package com.wormless.services;

import com.wormless.entities.IndicadorAmeaca;
import com.wormless.entities.enums.StatusIndicador;
import com.wormless.repository.IndicadorAmeacaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class IndicadorAmeacaService {

    private final IndicadorAmeacaRepository indicadorRepository;

    public IndicadorAmeacaService(
            IndicadorAmeacaRepository indicadorRepository) {
        this.indicadorRepository = indicadorRepository;
    }

    public IndicadorAmeaca salvar(IndicadorAmeaca indicador) {
        return indicadorRepository.save(indicador);
    }

    @Transactional(readOnly = true)
    public IndicadorAmeaca buscarPorId(Long id) {
        return indicadorRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Indicador não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<IndicadorAmeaca> buscarPorStatus(
            StatusIndicador status) {
        return indicadorRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<IndicadorAmeaca> listarPorAmeaca(Long ameacaId) {
        return indicadorRepository.findByAmeacaId(ameacaId);
    }
}