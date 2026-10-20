package com.wormless.services;

import com.wormless.dto.response.DashboardResponseDTO;
import com.wormless.dto.response.HistoricoRelatorioDTO;
import com.wormless.entities.AnaliseJob;
import com.wormless.entities.RelatorioAmeaca;
import com.wormless.entities.enums.Severidade;
import com.wormless.repository.AmeacaRepository;
import com.wormless.repository.AnaliseJobRepository;
import com.wormless.repository.FalsoPositivoRepository;
import com.wormless.repository.RelatorioAmeacaRepository;
import com.wormless.repository.VulnerabilidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AnaliseJobRepository analiseJobRepository;
    private final AmeacaRepository ameacaRepository;
    private final VulnerabilidadeRepository vulnerabilidadeRepository;
    private final FalsoPositivoRepository falsoPositivoRepository;
    private final RelatorioAmeacaRepository relatorioAmeacaRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDTO obterMetricas() {
        long totalAnalises = analiseJobRepository.count();
        long totalAmeacas = ameacaRepository.count();
        long totalVulnerabilidades = vulnerabilidadeRepository.count();
        long totalFalsosPositivos = falsoPositivoRepository.count();

        Map<Severidade, Long> analisesPorSeveridade = new EnumMap<>(Severidade.class);
        for (Severidade severidade : Severidade.values()) {
            long count = relatorioAmeacaRepository.findBySeveridadeGeral(severidade).size();
            analisesPorSeveridade.put(severidade, count);
        }

        Page<AnaliseJob> paginaRecentes = analiseJobRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataInicio"))
        );

        List<HistoricoRelatorioDTO> ultimasAnalises = paginaRecentes.stream().map(job -> {
            RelatorioAmeaca relatorio = job.getRelatorioAmeaca();
            return new HistoricoRelatorioDTO(
                    job.getId(),
                    job.getArquivo() != null ? job.getArquivo().getNomeOriginal() : "N/A",
                    job.getStatus().name(),
                    job.getDataInicio(),
                    job.getDataFim(),
                    relatorio != null ? relatorio.getSeveridadeGeral() : null,
                    relatorio != null ? relatorio.getResumo() : null
            );
        }).collect(Collectors.toList());

        return new DashboardResponseDTO(
                totalAnalises,
                totalAmeacas,
                totalVulnerabilidades,
                totalFalsosPositivos,
                analisesPorSeveridade,
                ultimasAnalises
        );
    }
}