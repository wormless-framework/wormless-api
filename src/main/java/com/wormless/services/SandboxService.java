package com.wormless.services;

import com.wormless.dto.request.ArquivoUploadDTO;
import com.wormless.dto.response.AnaliseJobResponseDTO;
import com.wormless.entities.AnaliseJob;
import com.wormless.entities.Arquivo;
import com.wormless.entities.enums.StatusJob;
import com.wormless.exception.BusinessException;
import com.wormless.exception.ResourceNotFoundException;
import com.wormless.entities.enums.Severidade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SandboxService {

    // Marcador procurado no PDF -> título e descrição do teste
    private static final Map<String, String> TESTES_PDF = Map.of(
            "/JavaScript",
            "JavaScript embutido: o PDF contém código que pode ser executado ao abrir.",
            "/OpenAction",
            "Ação automática: o PDF executa uma ação assim que é aberto.",
            "/Launch",
            "Execução de programa: o PDF tenta abrir um programa externo.",
            "/EmbeddedFile",
            "Arquivo embutido: o PDF carrega outro arquivo dentro dele."
    );

    private final ArquivoService arquivoService;
    private final AnaliseJobService analiseJobService;

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

        if (!"application/pdf".equals(
                uploadDTO.getArquivo().getContentType())) {
            throw new BusinessException(
                    "Apenas arquivos PDF são aceitos."
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

            // Executa os testes de segurança no conteúdo do PDF
            String conteudo = new String(
                    Files.readAllBytes(caminhoTemporario),
                    StandardCharsets.ISO_8859_1
            );

            List<String> testesDetectados = new ArrayList<>();

            TESTES_PDF.forEach((marcador, descricao) -> {
                if (conteudo.contains(marcador)) {
                    testesDetectados.add(descricao);
                }
            });

            // Guarda um teste detectado por linha
            jobSalvo.setResultadoBruto(
                    String.join("\n", testesDetectados)
            );

            analiseJobService.salvar(jobSalvo);

            analiseJobService.concluir(jobSalvo.getId());

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

        List<String> testes =
                job.getResultadoBruto() == null || job.getResultadoBruto().isBlank()
                        ? List.of()
                        : List.of(job.getResultadoBruto().split("\n"));

        boolean ameacaDetectada = !testes.isEmpty();

        // Risco pela quantidade de testes detectados: 0 = BAIXA ... 3 ou mais = CRITICA
        int nivel = Math.min(testes.size(), Severidade.values().length - 1);
        Severidade severidade = Severidade.values()[nivel];

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
                severidade,
                resumo,
                testes
        );
    }
}