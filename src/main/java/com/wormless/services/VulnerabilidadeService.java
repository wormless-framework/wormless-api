package com.wormless.services;

import com.wormless.entities.Vulnerabilidade;
import com.wormless.repository.VulnerabilidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VulnerabilidadeService {

    private final VulnerabilidadeRepository vulnerabilidadeRepository;

    public VulnerabilidadeService(
            VulnerabilidadeRepository vulnerabilidadeRepository) {
        this.vulnerabilidadeRepository = vulnerabilidadeRepository;
    }

    public Vulnerabilidade salvar(
            Vulnerabilidade vulnerabilidade) {
        return vulnerabilidadeRepository.save(vulnerabilidade);
    }

    @Transactional(readOnly = true)
    public Vulnerabilidade buscarPorId(Long id) {
        return vulnerabilidadeRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vulnerabilidade não encontrada: "
                                        + id));
    }

    @Transactional(readOnly = true)
    public Vulnerabilidade buscarPorCve(String cve) {
        return vulnerabilidadeRepository.findByCve(cve)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "CVE não encontrada: " + cve));
    }

    @Transactional(readOnly = true)
    public List<Vulnerabilidade> listarTodas() {
        return vulnerabilidadeRepository.findAll();
    }
}