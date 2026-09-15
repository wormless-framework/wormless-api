package com.wormless.services;

import com.wormless.entities.Ameaca;
import com.wormless.entities.enums.Severidade;
import com.wormless.repository.AmeacaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AmeacaService {

    private final AmeacaRepository ameacaRepository;

    public AmeacaService(AmeacaRepository ameacaRepository) {
        this.ameacaRepository = ameacaRepository;
    }

    public Ameaca salvar(Ameaca ameaca) {
        return ameacaRepository.save(ameaca);
    }

    @Transactional(readOnly = true)
    public Ameaca buscarPorId(Long id) {
        return ameacaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ameaça não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<Ameaca> listarPorSeveridade(
            Severidade severidade) {
        return ameacaRepository.findBySeveridade(severidade);
    }

    @Transactional(readOnly = true)
    public List<Ameaca> listarTodas() {
        return ameacaRepository.findAll();
    }
}