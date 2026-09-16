package com.wormless.services;

import com.wormless.entities.AnaliseJob;
import com.wormless.entities.enums.StatusJob;
import com.wormless.repository.AnaliseJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AnaliseJobService {

    private final AnaliseJobRepository analiseJobRepository;

    public AnaliseJobService(
            AnaliseJobRepository analiseJobRepository) {
        this.analiseJobRepository = analiseJobRepository;
    }

    public AnaliseJob salvar(AnaliseJob job) {
        return analiseJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public AnaliseJob buscarPorId(Long id) {
        return analiseJobRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Job não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public StatusJob consultarStatus(Long id) {
        return buscarPorId(id).consultarStatus();
    }

    public AnaliseJob iniciar(Long id) {
        AnaliseJob job = buscarPorId(id);

        job.setStatus(StatusJob.EM_PROCESSAMENTO);
        job.setDataInicio(LocalDateTime.now());

        return analiseJobRepository.save(job);
    }

    public AnaliseJob concluir(Long id) {
        AnaliseJob job = buscarPorId(id);

        job.setStatus(StatusJob.CONCLUIDO);
        job.setDataFim(LocalDateTime.now());

        return analiseJobRepository.save(job);
    }

    public AnaliseJob marcarErro(Long id) {
        AnaliseJob job = buscarPorId(id);

        job.setStatus(StatusJob.ERRO);
        job.setDataFim(LocalDateTime.now());

        return analiseJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public List<AnaliseJob> buscarPorStatus(StatusJob status) {
        return analiseJobRepository.findByStatus(status);
    }
}