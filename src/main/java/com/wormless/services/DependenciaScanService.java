package com.wormless.services;

import com.wormless.entities.DependenciaScan;
import com.wormless.repository.DependenciaScanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DependenciaScanService {

    private final DependenciaScanRepository dependenciaRepository;

    public DependenciaScanService(
            DependenciaScanRepository dependenciaRepository) {
        this.dependenciaRepository = dependenciaRepository;
    }

    public DependenciaScan salvar(DependenciaScan dependencia) {
        return dependenciaRepository.save(dependencia);
    }

    @Transactional(readOnly = true)
    public DependenciaScan buscarPorId(Long id) {
        return dependenciaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Dependência não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<DependenciaScan> listarPorJob(Long jobId) {
        return dependenciaRepository.findByAnaliseJobId(jobId);
    }

    @Transactional(readOnly = true)
    public List<DependenciaScan> buscarPacote(
            String nomePacote,
            String versao) {
        return dependenciaRepository
                .findByNomePacoteAndVersao(nomePacote, versao);
    }
}