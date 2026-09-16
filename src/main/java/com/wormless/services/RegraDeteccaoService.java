package com.wormless.services;

import com.wormless.entities.RegraDeteccao;
import com.wormless.repository.RegraDeteccaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RegraDeteccaoService {

    private final RegraDeteccaoRepository regraRepository;

    public RegraDeteccaoService(
            RegraDeteccaoRepository regraRepository) {
        this.regraRepository = regraRepository;
    }

    public RegraDeteccao salvar(RegraDeteccao regra) {
        return regraRepository.save(regra);
    }

    @Transactional(readOnly = true)
    public RegraDeteccao buscarPorId(Long id) {
        return regraRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Regra não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<RegraDeteccao> listarAtivas() {
        return regraRepository.findByAtivaTrue();
    }

    public RegraDeteccao ignorarEmMetricasFuturas(Long id) {
        RegraDeteccao regra = buscarPorId(id);

        regra.ignorarEmMetricasFuturas();

        return regraRepository.save(regra);
    }
}