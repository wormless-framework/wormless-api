package com.wormless.services;

import com.wormless.dto.request.ArquivoUploadDTO;
import com.wormless.dto.response.AnaliseJobResponseDTO;
import com.wormless.entities.AnaliseJob;
import com.wormless.entities.Arquivo;
import com.wormless.entities.enums.StatusJob;
import com.wormless.exception.BusinessException;
import com.wormless.exception.ResourceNotFoundException;
import com.wormless.integrations.sandbox.SandboxClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SandboxService {

    private final ArquivoService arquivoService;
    private final AnaliseJobService analiseJobService;
    private final SandboxClient sandboxClient;

    @Transactional
    public AnaliseJobResponseDTO iniciarAnalise(
            ArquivoUploadDTO uploadDTO) {

        if (uploadDTO == null || uploadDTO.getArquivo() == null) {
            throw new BusinessException(
                    "O arquivo para análise deve ser informado."
            );
        }

        if (uploadDTO.getArquivo().isEmpty()) {
            throw new BusinessException(
                    "O arquivo enviado está vazio."
            );
        }

        try {

            var arquivoUpload = uploadDTO.getArquivo();

            Path caminhoTemporario = Files.createTempFile(
                    "wormless-",
                    "-" + arquivoUpload.getOriginalFilename()
            );

            arquivoUpload.transferTo(caminhoTemporario);

            Arquivo arquivo = new Arquivo();

            arquivo.setNomeOriginal(
                    arquivoUpload.getOriginalFilename()
            );

            arquivo.setTamanho(
                    arquivoUpload.getSize()
            );

            arquivo.setTipoMime(
                    arquivoUpload.getContentType()
            );

            arquivo.setCaminhoTemporario(
                    caminhoTemporario.toString()
            );

            arquivo.setDataUpload(
                    LocalDateTime.now()
            );

            Arquivo arquivoSalvo =
                    arquivoService.salvar(arquivo);

            AnaliseJob job = new AnaliseJob();

            job.setArquivo(arquivoSalvo);
            job.setStatus(StatusJob.PENDENTE);

            AnaliseJob jobSalvo =
                    analiseJobService.salvar(job);

            // Coloca a análise em processamento
            analiseJobService.iniciar(jobSalvo.getId());

            // Envia o arquivo para o Sandbox externo
            String resultadoBruto =
                    sandboxClient.executarAnalise(
                            arquivoUpload
                    );

            return consultarResultado(jobSalvo.getId());

        } catch (IOException e) {

            throw new BusinessException(
                    "Não foi possível preparar o arquivo para análise."
            );
        }
    }

    @Transactional(readOnly = true)
    public AnaliseJobResponseDTO consultarResultado(
            Long analiseId) {

        if (analiseId == null) {
            throw new BusinessException(
                    "O ID da análise deve ser informado."
            );
        }

        AnaliseJob job =
                analiseJobService.buscarPorId(analiseId);

        if (job == null) {
            throw new ResourceNotFoundException(
                    "Análise não encontrada com o ID: "
                            + analiseId
            );
        }

        return criarResponse(job);
    }

    private AnaliseJobResponseDTO criarResponse(
            AnaliseJob job) {

        String resumo;

        boolean ameacaDetectada = false;

        switch (job.getStatus()) {

            case PENDENTE:
                resumo = "Análise aguardando processamento.";
                break;

            case EM_PROCESSAMENTO:
                resumo = "Análise em processamento.";
                break;

            case CONCLUIDO:
                resumo = "Análise concluída.";
                break;

            case ERRO:
                resumo = "Ocorreu um erro durante a análise.";
                break;

            case REJEITADO:
                resumo = "A análise foi rejeitada.";
                break;

            default:
                resumo = "Status da análise não identificado.";
        }

        return new AnaliseJobResponseDTO(
                job.getId(),
                job.getStatus().name(),
                ameacaDetectada,
                null,
                resumo
        );
    }
}