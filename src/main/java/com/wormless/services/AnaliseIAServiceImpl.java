package com.wormless.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wormless.entities.*;
import com.wormless.entities.enums.Severidade;
import com.wormless.entities.enums.StatusIndicador;
import com.wormless.integrations.gemini.GeminiCliente;
import com.wormless.integrations.gemini.GeminiRequest;
import com.wormless.integrations.gemini.GeminiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service 
@RequiredArgsConstructor 
public class AnaliseIAServiceImpl implements AnaliseIAService {

    private final AmeacaService ameacaService;
    private final RemediacaoService remediacaoService;
    private final VulnerabilidadeService vulnerabilidadeService;
    
    private final GeminiCliente geminiCliente;
    private final ObjectMapper objectMapper = new ObjectMapper(); 

    @Value("${gemini.api.key}")
    private String apiKey;

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

        JsonNode iaNode = null;

        // 1. ISOLAR A CHAMADA DA API NO TRY-CATCH
        try {
            GeminiRequest request = new GeminiRequest(List.of(
                new GeminiRequest.Content(List.of(new GeminiRequest.Part(prompt)))
            ));
            
            GeminiResponse response = geminiCliente.gerarConteudo(apiKey, request);
            String jsonLimpo = response.extrairTexto().replace("```json", "").replace("```", "").trim();
            iaNode = objectMapper.readTree(jsonLimpo);

        } catch (Exception e) {
            System.err.println("Erro real na comunicação/leitura da IA: " + e.getMessage());
            relatorio.setSeveridadeGeral(Severidade.ALTA);
            relatorio.setResumo("Ameaça detetada na Sandbox, mas falha ao processar análise detalhada via IA. Reveja o ficheiro manualmente.");
            return relatorio; // Retorna imediatamente o fallback
        }

        // 2. GRAVAR NA BASE DE DADOS FORA DO CATCH
        relatorio.setSeveridadeGeral(Severidade.valueOf(iaNode.get("severidadeGeral").asText()));
        relatorio.setResumo(iaNode.get("resumo").asText());

        Ameaca ameaca = new Ameaca();
        ameaca.setNome(iaNode.get("nomeAmeaca").asText());
        ameaca.setTipo(iaNode.get("tipoAmeaca").asText());
        ameaca.setComoAge(iaNode.get("comoAge").asText());
        ameaca.setSeveridade(relatorio.getSeveridadeGeral());
        Ameaca ameacaSalva = ameacaService.salvar(ameaca); // Se o MySQL falhar aqui agora, o Spring vai dar o erro correto!

        Vulnerabilidade vuln = new Vulnerabilidade();
        vuln.setCve(iaNode.get("cve").asText());
        vuln.setDescricao(iaNode.get("descricaoVuln").asText());
        vuln.setAmeaca(ameacaSalva);
        vulnerabilidadeService.salvar(vuln);

        Remediacao remediacao = new Remediacao();
        remediacao.setOrientacao(iaNode.get("remediacao").asText());
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
}