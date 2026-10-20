package com.wormless.dto.response;

/**
 * Relatório forense gerado pela IA (Gemini) a partir dos achados dos testes.
 *
 * <p>Os campos da ameaça ficam {@code null} quando a IA não foi chamada
 * (nenhum achado) ou quando a chamada falhou; nesses casos só a severidade e
 * o resumo são preenchidos.</p>
 *
 * @param severidade  severidade atribuída pela IA
 * @param resumo      resumo executivo
 * @param ameaca      nome técnico da ameaça
 * @param tipo        família ou tipo (ex.: Malware, Phishing, Dropper)
 * @param comoAge     como o atacante exploraria o arquivo
 * @param cve         CVE provável, ou "N/A"
 * @param vulnerabilidade explicação da vulnerabilidade
 * @param remediacao  passos para resolver a ameaça
 */
public record RelatorioIADTO(
        String severidade,
        String resumo,
        String ameaca,
        String tipo,
        String comoAge,
        String cve,
        String vulnerabilidade,
        String remediacao) {
}
