package com.wormless.analise;

import com.wormless.entities.enums.Severidade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Agregado dos achados de uma análise, com as consultas derivadas usadas pela
 * API e pela geração de relatório.
 *
 * @param achados detecções produzidas pelos testes, na ordem de execução
 */
public record ResultadoAnalise(List<Achado> achados) {

    /**
     * A partir de quantos achados graves a severidade geral sobe um nível.
     * O acúmulo de indicadores é, por si só, um agravante.
     */
    private static final int ACHADOS_GRAVES_PARA_ESCALAR = 3;

    public ResultadoAnalise {
        achados = List.copyOf(achados);
    }

    public static ResultadoAnalise vazio() {
        return new ResultadoAnalise(List.of());
    }

    /**
     * Considera-se ameaça a partir de severidade média. Achados de severidade
     * baixa são observações de contexto e não devem, sozinhos, sinalizar um
     * arquivo como malicioso.
     */
    public boolean ameacaDetectada() {
        return severidadeGeral().ordinal() >= Severidade.MEDIA.ordinal();
    }

    /** Maior severidade encontrada, agravada pelo acúmulo de achados graves. */
    public Severidade severidadeGeral() {

        if (achados.isEmpty()) {
            return Severidade.BAIXA;
        }

        Severidade maior = achados.stream()
                .map(Achado::severidade)
                .max(Comparator.comparingInt(Severidade::ordinal))
                .orElse(Severidade.BAIXA);

        long graves = achados.stream()
                .filter(achado -> achado.severidade().ordinal() >= Severidade.ALTA.ordinal())
                .count();

        if (graves >= ACHADOS_GRAVES_PARA_ESCALAR && maior.ordinal() < Severidade.CRITICA.ordinal()) {
            return Severidade.values()[maior.ordinal() + 1];
        }

        return maior;
    }

    /** Descrições legíveis, no formato exposto pela API. */
    public List<String> descricoes() {
        return achados.stream().map(Achado::paraTexto).toList();
    }

    /** Identificadores MITRE ATT&CK distintos, preservando a ordem de detecção. */
    public List<String> tecnicasMitre() {
        return achados.stream()
                .map(Achado::tecnica)
                .filter(tecnica -> tecnica != null)
                .map(tecnica -> tecnica.getId())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    /**
     * Serializa os achados para {@code AnaliseJob.resultadoBruto}: uma linha
     * por detecção, texto vazio quando nada foi encontrado.
     */
    public String paraResultadoBruto() {
        return achados.stream()
                .map(Achado::paraLinha)
                .collect(Collectors.joining("\n"));
    }

    /** Reconstrói o resultado a partir do conteúdo persistido. */
    public static ResultadoAnalise deResultadoBruto(String bruto) {

        if (bruto == null || bruto.isBlank()) {
            return vazio();
        }

        List<Achado> recuperados = new ArrayList<>();

        for (String linha : Arrays.asList(bruto.split("\n"))) {
            if (!linha.isBlank()) {
                recuperados.add(Achado.deLinha(linha));
            }
        }

        return new ResultadoAnalise(recuperados);
    }
}
