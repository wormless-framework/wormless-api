package com.wormless.services;

import com.wormless.analise.ContextoAnalise;
import com.wormless.analise.MotorAnaliseSeguranca;
import com.wormless.analise.ResultadoAnalise;
import com.wormless.analise.TipoArquivo;
import com.wormless.dto.request.ArquivoUploadDTO;
import com.wormless.dto.response.AchadoDTO;
import com.wormless.dto.response.AnaliseJobResponseDTO;
import com.wormless.dto.response.RelatorioIADTO;
import com.wormless.entities.Ameaca;
import com.wormless.entities.AnaliseJob;
import com.wormless.entities.Arquivo;
import com.wormless.entities.enums.StatusJob;
import com.wormless.exception.BusinessException;
import com.wormless.exception.ResourceNotFoundException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SandboxService {

    private final ArquivoService arquivoService;
    private final AnaliseJobService analiseJobService;
    private final MotorAnaliseSeguranca motorAnaliseSeguranca;
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

        if (TipoArquivo.porNome(
                uploadDTO.getArquivo().getOriginalFilename()).isEmpty()) {
            throw new BusinessException(
                    "Tipo de arquivo não suportado. Envie um arquivo "
                            + TipoArquivo.extensoesAceitas() + "."
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

            // Executa os testes de segurança no conteúdo do arquivo
            ContextoAnalise contexto = ContextoAnalise.de(
                    arquivoUpload.getOriginalFilename(),
                    arquivoUpload.getContentType(),
                    Files.readAllBytes(caminhoTemporario)
            );

            ResultadoAnalise resultado =
                    motorAnaliseSeguranca.analisar(contexto);

            // Guarda um achado por linha
            jobSalvo.setResultadoBruto(
                    resultado.paraResultadoBruto()
            );

            analiseJobService.salvar(jobSalvo);

            // A IA gera o relatório forense a partir dos achados, em texto legível
            RelatorioAmeaca relatorio = analiseIAService.analisar(
                    textoParaIA(resultado),
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

        ResultadoAnalise resultado =
                ResultadoAnalise.deResultadoBruto(job.getResultadoBruto());

        List<AchadoDTO> achados = resultado.achados().stream()
                .map(achado -> new AchadoDTO(
                        achado.idTeste(),
                        achado.categoria().name(),
                        achado.severidade().name(),
                        achado.tecnica() == null ? null : achado.tecnica().getId(),
                        achado.descricao(),
                        achado.evidencia()
                ))
                .toList();

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
                resultado.ameacaDetectada(),
                resultado.severidadeGeral(),
                resumo,
                resultado.descricoes(),
                achados,
                criarRelatorioIA(job.getId())
        );
    }

    /**
     * Um achado por linha, com categoria e severidade, para o prompt da IA
     * (o TSV do resultadoBruto é pensado para máquina, não para leitura).
     */
    private String textoParaIA(ResultadoAnalise resultado) {
        return resultado.achados().stream()
                .map(achado -> "[" + achado.categoria() + "] "
                        + achado.severidade() + " - " + achado.paraTexto())
                .collect(Collectors.joining("\n"));
    }

    /** Relatório da IA gravado para a análise, ou {@code null} se não houver. */
    private RelatorioIADTO criarRelatorioIA(Long analiseId) {

        List<RelatorioAmeaca> relatorios =
                relatorioAmeacaService.listarPorAnaliseId(analiseId);

        if (relatorios.isEmpty()) {
            return null;
        }

        RelatorioAmeaca relatorio = relatorios.get(0);

        Ameaca ameaca = relatorio.getIndicadores().isEmpty()
                ? null
                : relatorio.getIndicadores().get(0).getAmeaca();

        if (ameaca == null) {
            return new RelatorioIADTO(
                    nome(relatorio.getSeveridadeGeral()), relatorio.getResumo(),
                    null, null, null, null, null, null);
        }

        var vulnerabilidade = ameaca.getVulnerabilidades().isEmpty()
                ? null
                : ameaca.getVulnerabilidades().get(0);

        return new RelatorioIADTO(
                nome(relatorio.getSeveridadeGeral()),
                relatorio.getResumo(),
                ameaca.getNome(),
                ameaca.getTipo(),
                ameaca.getComoAge(),
                vulnerabilidade == null ? null : vulnerabilidade.getCve(),
                vulnerabilidade == null ? null : vulnerabilidade.getDescricao(),
                ameaca.getRemediacao() == null ? null : ameaca.getRemediacao().getOrientacao()
        );
    }

    private static String nome(Enum<?> valor) {
        return valor == null ? null : valor.name();
    }
}
