package com.wormless.services;

import com.wormless.entities.Remediacao;
import com.wormless.repository.RemediacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RemediacaoService {

    private final RemediacaoRepository remediacaoRepository;

    public RemediacaoService(
            RemediacaoRepository remediacaoRepository) {
        this.remediacaoRepository = remediacaoRepository;
    }

    public Remediacao salvar(Remediacao remediacao) {
        return remediacaoRepository.save(remediacao);
    }

    @Transactional(readOnly = true)
    public Remediacao buscarPorId(Long id) {
        return remediacaoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Remediação não encontrada: " + id));
    }

    public void excluir(Long id) {
        remediacaoRepository.deleteById(id);
    }
}