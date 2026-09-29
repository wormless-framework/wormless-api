package com.wormless.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wormless.entities.*;
import com.wormless.entities.enums.Severidade;
import com.wormless.entities.enums.StatusIndicador;
import com.wormless.integrations.gemini.GeminiRequest;
import com.wormless.integrations.gemini.GeminiResponse;
import com.wormless.integrations.gemini.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnaliseIAServiceImpl implements AnaliseIAService {

    private final AmeacaService ameacaService;
    private final RemediacaoService remediacaoService;
    private final VulnerabilidadeService vulnerabilidadeService;
    private final GeminiService geminiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public RelatorioAmeaca analisar(String resultadoBruto, AnaliseJob analiseJob) {
        RelatorioAmeaca relatorio = new RelatorioAmeaca();
        relatorio.setAnaliseJob(analiseJob);
        relatorio.setDataGeracao(LocalDateTime.now());

        if (resultadoBruto == null || resultadoBruto.isBlank()) {
            relatorio.setSeveridadeGeral(Severidade.BAIXA);
            relatorio.setResumo("A análise estática não detetou anomalias. Nenhuma verificação de IA foi necessária.");
            return relatorio;
        }

        String prompt = """
            És um analista sênior de SOC e especialista forense. Analisa o seguinte log detetado numa Sandbox estática de um ficheiro PDF:

            %s

            Com base nisto, age como uma API e devolve APENAS um JSON válido. Não utilizes formatação Markdown (como ```json).
            O JSON deve ter exatamente estas chaves:
            "severidadeGeral": (deve ser BAIXA, MEDIA, ALTA ou CRITICA),
            "resumo": "Um resumo executivo da ameaça e do seu impacto potencial",
            "nomeAmeaca": "Nome técnico da ameaça detetada",
            "tipoAmeaca": "Família ou tipo (ex: Malware, Phishing, Dropper)",
            "comoAge": "Explicação técnica detalhada de como o atacante exploraria isto",
            "cve": "Código CVE provável ou 'N/A' se for falha arquitetural",
            "descricaoVuln": "Explicação da vulnerabilidade",
            "remediacao": "Passos técnicos detalhados e enumerados para resolver a ameaça"
            """.formatted(resultadoBruto);

        JsonNode iaNode;

        try {
            GeminiRequest request = new GeminiRequest(List.of(
                    new GeminiRequest.Content(List.of(new GeminiRequest.Part(prompt)))
            ));

            GeminiResponse resp = geminiService.gerar(request);
            String jsonLimpo = resp.extrairTexto()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            iaNode = objectMapper.readTree(jsonLimpo);

        } catch (Exception e) {
            log.error("Erro na comunicação/leitura da IA: {}", e.getMessage(), e);
            relatorio.setSeveridadeGeral(Severidade.ALTA);
            relatorio.setResumo("Ameaça detetada na Sandbox, mas falha ao processar análise detalhada via IA. Reveja o ficheiro manualmente.");
            return relatorio;
        }

        // Gravar na base de dados fora do catch
        relatorio.setSeveridadeGeral(parseSeveridade(iaNode.path("severidadeGeral").asText()));
        relatorio.setResumo(iaNode.path("resumo").asText());

        Ameaca ameaca = new Ameaca();
        ameaca.setNome(iaNode.path("nomeAmeaca").asText());
        ameaca.setTipo(iaNode.path("tipoAmeaca").asText());
        ameaca.setComoAge(iaNode.path("comoAge").asText());
        ameaca.setSeveridade(relatorio.getSeveridadeGeral());
        Ameaca ameacaSalva = ameacaService.salvar(ameaca);

        Vulnerabilidade vuln = new Vulnerabilidade();
        vuln.setCve(iaNode.path("cve").asText());
        vuln.setDescricao(iaNode.path("descricaoVuln").asText());
        vuln.setAmeaca(ameacaSalva);
        vulnerabilidadeService.salvar(vuln);

        Remediacao remediacao = new Remediacao();
        remediacao.setOrientacao(iaNode.path("remediacao").asText());
        remediacao.setAmeaca(ameacaSalva);
        remediacaoService.salvar(remediacao);

        String[] linhas = resultadoBruto.split("\n");
        for (int i = 0; i < linhas.length; i++) {
            IndicadorAmeaca indicador = new IndicadorAmeaca();
            indicador.setTrechoArquivo(linhas[i]);
            indicador.setLinhaOcorrencia(i + 1);
            indicador.setDescricaoComportamento("Identificado pela IA como vetor ofensivo associado a: " + ameaca.getNome());
            indicador.setStatus(StatusIndicador.ATIVO);
            indicador.setAmeaca(ameacaSalva);
            indicador.setRelatorioAmeaca(relatorio);
            relatorio.getIndicadores().add(indicador);
        }

        return relatorio;
    }

    private Severidade parseSeveridade(String valor) {
        try {
            return Severidade.valueOf(valor.trim().toUpperCase());
        } catch (Exception e) {
            log.warn("Severidade inválida devolvida pela IA: '{}'. Usando ALTA por segurança.", valor);
            return Severidade.ALTA;
        }
    }
}