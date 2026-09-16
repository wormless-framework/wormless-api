package com.wormless.services;

import com.wormless.entities.PipelineCICD;
import com.wormless.repository.PipelineCICDRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PipelineCICDService {

    private final PipelineCICDRepository pipelineRepository;

    public PipelineCICDService(
            PipelineCICDRepository pipelineRepository) {
        this.pipelineRepository = pipelineRepository;
    }

    public PipelineCICD salvar(PipelineCICD pipeline) {
        if (pipelineRepository
                .existsByRepositorio(pipeline.getRepositorio())) {
            throw new IllegalArgumentException(
                    "Repositório já cadastrado: "
                            + pipeline.getRepositorio());
        }

        return pipelineRepository.save(pipeline);
    }

    @Transactional(readOnly = true)
    public PipelineCICD buscarPorId(Long id) {
        return pipelineRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pipeline não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<PipelineCICD> listarTodos() {
        return pipelineRepository.findAll();
    }
}