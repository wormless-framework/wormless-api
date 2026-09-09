package com.wormless.service;

import com.wormless.api.model.AnaliseJob;
import com.wormless.api.model.Arquivo;
import com.wormless.api.model.enums.StatusJob;
import com.wormless.api.repository.ArquivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SandBoxService {

    private final ArquivoRepository arquivoRepository;
    private final AnaliseJobRepository analiseJobRepository;

    public Arquivo registrarUpload(Arquivo arquivo) {
        if (!arquivo.validarFormato() || !arquivo.validarTamanho()) {
            throw new BusinessException("Formato ou tamanho de arquivo inválido.");
        }
        arquivo.setDataUpload(LocalDateTime.now());
        return arquivoRepository.save(arquivo);
    }

    @Async
    public void iniciarProcessamentoAsync(Long arquivoId) {
        Arquivo arquivo = arquivoRepository.findById(arquivoId).orElseThrow();
        
        AnaliseJob job = new AnaliseJob();
        job.setArquivo(arquivo);
        job.setStatus(StatusJob.EM_PROCESSAMENTO);
        job.setDataInicio(LocalDateTime.now());
        analiseJobRepository.save(job);

        // TODO: Lógica real de análise (integração com motores de scan)
        
        job.setStatus(StatusJob.CONCLUIDO);
        job.setDataFim(LocalDateTime.now());
        analiseJobRepository.save(job);
    }
}