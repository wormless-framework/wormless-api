package com.wormless.dto.response;

/**
 * Detecção individual exposta pela API.
 *
 * @param teste      identificador do teste que produziu a detecção
 * @param categoria  família do teste
 * @param severidade severidade atribuída
 * @param tecnica    identificador MITRE ATT&CK, ou {@code null}
 * @param descricao  explicação em linguagem acessível
 * @param evidencia  trecho ou medida que embasou a detecção
 */
public record AchadoDTO(
        String teste,
        String categoria,
        String severidade,
        String tecnica,
        String descricao,
        String evidencia) {
}
