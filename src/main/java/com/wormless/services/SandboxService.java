package com.wormless.services;

import com.wormless.dto.request.ArquivoUploadDTO;
import com.wormless.dto.response.AnaliseJobResponseDTO;
import com.wormless.entities.AnaliseJob;
import com.wormless.entities.Arquivo;
import com.wormless.entities.enums.StatusJob;
import com.wormless.exception.BusinessException;
import com.wormless.exception.ResourceNotFoundException;
import com.wormless.entities.enums.Severidade;
import com.wormless.entities.RelatorioAmeaca;
import com.wormless.entities.PipelineCICD;
import com.wormless.repository.PipelineCICDRepository;
import com.wormless.security.User;
import com.wormless.repository.AnaliseJobRepository;
import org.eclipse.jgit.api.errors.GitAPIException;

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
    private final AnaliseIAService analiseIAService;
    private final RelatorioAmeacaService relatorioAmeacaService;
    private final PipelineCICDRepository pipelineCICDRepository;
    private final AnaliseJobRepository analiseJobRepository;

    @Transactional
    public AnaliseJobResponseDTO iniciarAnalise(
            ArquivoUploadDTO uploadDTO, User proprietario) {

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
            arquivo.setNomeOriginal(arquivoUpload.getOriginalFilename());
            arquivo.setTamanho(arquivoUpload.getSize());
            arquivo.setTipoMime(arquivoUpload.getContentType());
            arquivo.setCaminhoTemporario(caminhoTemporario.toString());
            arquivo.setDataUpload(LocalDateTime.now());

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
            arquivo.setProprietario(proprietario);

            Arquivo arquivoSalvo =
                    arquivoService.salvar(arquivo);

            AnaliseJob job = new AnaliseJob();
            job.setArquivo(arquivoSalvo);
            job.setStatus(StatusJob.PENDENTE);
            AnaliseJob jobSalvo = analiseJobService.salvar(job);

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

            jobSalvo.setResultadoBruto(String.join("\n", testesDetectados));
            analiseJobService.salvar(jobSalvo);

            RelatorioAmeaca relatorio = analiseIAService.analisar(
                jobSalvo.getResultadoBruto(), 
                jobSalvo
            );
                
            relatorioAmeacaService.salvar(relatorio);
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

    @Transactional(readOnly = true)
    public AnaliseJobResponseDTO consultarResultadoDoUsuario(Long analiseId, Long proprietarioId) {
        if (analiseId == null || proprietarioId == null
                || !analiseJobRepository.existsByIdAndArquivoProprietarioId(analiseId, proprietarioId)) {
            throw new ResourceNotFoundException("Análise não encontrada.");
        }
        return consultarResultado(analiseId);
    }

    @Transactional
    public AnaliseJobResponseDTO iniciarAnaliseDeRepositorio(String repoUrl, String caminhoArquivo) {
        if (repoUrl == null || repoUrl.isBlank() || caminhoArquivo == null || caminhoArquivo.isBlank()) {
            throw new BusinessException("A URL do repositório e o caminho do arquivo são obrigatórios.");
        }
        java.nio.file.Path diretorioTemp = null;
        try {
            diretorioTemp = java.nio.file.Files.createTempDirectory("wormless-deepscan-");
            
            org.eclipse.jgit.api.Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(diretorioTemp.toFile())
                    .setDepth(1)
                    .call();

            java.nio.file.Path raizRepositorio = diretorioTemp.toRealPath();
            java.nio.file.Path arquivoFisico = diretorioTemp.resolve(caminhoArquivo).toRealPath();
            if (!arquivoFisico.startsWith(raizRepositorio)
                    || !java.nio.file.Files.isRegularFile(arquivoFisico)) {
                throw new BusinessException("Ficheiro não encontrado no repositório clonado.");
            }

            Arquivo arquivo = new Arquivo();
            java.nio.file.Path nomeArquivo = arquivoFisico.getFileName();
            if (nomeArquivo == null) {
                throw new BusinessException("O caminho informado não é um arquivo válido.");
            }
            arquivo.setNomeOriginal(nomeArquivo.toString());
            arquivo.setTamanho(java.nio.file.Files.size(arquivoFisico));
            arquivo.setTipoMime("application/octet-stream");
            arquivo.setCaminhoTemporario(arquivoFisico.toString());
            arquivo.setDataUpload(java.time.LocalDateTime.now());
            Arquivo arquivoSalvo = arquivoService.salvar(arquivo);

            // ---> LÓGICA DO PIPELINE AQUI <---
            PipelineCICD pipeline = pipelineCICDRepository.findByRepositorio(repoUrl)
                    .orElseGet(() -> {
                        PipelineCICD novo = new PipelineCICD();
                        novo.setRepositorio(repoUrl);
                        return pipelineCICDRepository.save(novo);
                    });

            AnaliseJob job = new AnaliseJob();
            job.setArquivo(arquivoSalvo);
            job.setPipelineCICD(pipeline); // Vincula o repositório à análise
            job.setStatus(StatusJob.PENDENTE);
            AnaliseJob jobSalvo = analiseJobService.salvar(job);

            analiseJobService.iniciar(jobSalvo.getId());

            // Lê o conteúdo bruto do arquivo com o encoding correto
            String conteudo = new String(
                    java.nio.file.Files.readAllBytes(arquivoFisico),
                    java.nio.charset.StandardCharsets.ISO_8859_1);
            
            if (conteudo.length() > 6000) {
                conteudo = conteudo.substring(0, 6000) + "\n...[TRUNCADO]";
            }
            
            jobSalvo.setResultadoBruto(conteudo);
            analiseJobService.salvar(jobSalvo);

            // Envia o conteúdo bruto do arquivo diretamente para a IA
            RelatorioAmeaca relatorio = analiseIAService.analisar(
                    jobSalvo.getResultadoBruto(), jobSalvo);
            relatorioAmeacaService.salvar(relatorio);

            analiseJobService.concluir(jobSalvo.getId());

            return consultarResultado(jobSalvo.getId());

        } catch (IOException | GitAPIException e) {
            throw new BusinessException(
                    "Falha ao analisar ficheiro do repositório: " + e.getMessage());
        } finally {
            if (diretorioTemp != null) {
                try {
                    org.springframework.util.FileSystemUtils.deleteRecursively(diretorioTemp);
                } catch (IOException ignored) {
                    // Ignora falha de limpeza do diretório temporário.
                }
            }
        }
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
