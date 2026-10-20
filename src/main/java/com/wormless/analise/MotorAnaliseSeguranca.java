package com.wormless.analise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Esteira de análise: executa todos os {@link TesteSeguranca} registrados
 * sobre o mesmo contexto e consolida os achados.
 *
 * <p>Os testes chegam por injeção de dependência, então a esteira não conhece
 * nenhuma implementação concreta — acrescentar um analisador não exige tocar
 * nesta classe nem no {@code SandboxService}.</p>
 */
@Component
public class MotorAnaliseSeguranca {

    private static final Logger LOG = LoggerFactory.getLogger(MotorAnaliseSeguranca.class);

    private final List<TesteSeguranca> testes;

    public MotorAnaliseSeguranca(List<TesteSeguranca> testes) {
        this.testes = testes.stream()
                .sorted(Comparator.comparingInt(TesteSeguranca::ordem)
                        .thenComparing(TesteSeguranca::id))
                .toList();
    }

    public ResultadoAnalise analisar(ContextoAnalise contexto) {

        if (contexto == null) {
            return ResultadoAnalise.vazio();
        }

        List<Achado> achados = new ArrayList<>();

        for (TesteSeguranca teste : testes) {
            achados.addAll(teste.executar(contexto));
        }

        LOG.debug(
                "Análise de '{}' concluída: {} teste(s) executado(s), {} achado(s).",
                contexto.nomeArquivo(),
                testes.size(),
                achados.size()
        );

        return new ResultadoAnalise(achados);
    }

    /** Testes ativos na esteira, na ordem de execução. */
    public List<String> testesRegistrados() {
        return testes.stream()
                .map(teste -> teste.id() + " (" + teste.categoria() + "): " + teste.nome())
                .toList();
    }
}
