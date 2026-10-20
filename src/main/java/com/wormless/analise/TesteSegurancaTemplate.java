package com.wormless.analise;

import com.wormless.analise.mitre.TecnicaMitre;
import com.wormless.entities.enums.Severidade;

import java.util.List;

/**
 * Esqueleto de um teste de segurança, no padrão Template Method.
 *
 * <p>{@link #executar(ContextoAnalise)} é final e fixa a sequência invariante
 * que todo teste deve seguir:</p>
 *
 * <ol>
 *   <li>descartar contexto ausente;</li>
 *   <li>consultar {@link #aplicavel(ContextoAnalise)} — o gancho que permite a
 *       um teste se declarar irrelevante para o artefato;</li>
 *   <li>delegar a detecção à subclasse, em {@link #detectar(ContextoAnalise)};</li>
 *   <li>normalizar o retorno em uma lista imutável;</li>
 *   <li>converter falhas inesperadas em um achado, via
 *       {@link #aoFalhar(ContextoAnalise, RuntimeException)}.</li>
 * </ol>
 *
 * <p>O quinto passo é o que garante o isolamento entre os testes: um analisador
 * com defeito degrada o próprio resultado e não derruba a análise inteira.</p>
 */
public abstract class TesteSegurancaTemplate implements TesteSeguranca {

    @Override
    public final List<Achado> executar(ContextoAnalise contexto) {

        if (contexto == null || contexto.conteudo() == null || !aplicavel(contexto)) {
            return List.of();
        }

        try {
            List<Achado> achados = detectar(contexto);

            if (achados == null || achados.isEmpty()) {
                return List.of();
            }

            return List.copyOf(achados);

        } catch (RuntimeException e) {
            return List.of(aoFalhar(contexto, e));
        }
    }

    /**
     * Gancho opcional: decide se o teste faz sentido para este artefato.
     * O padrão é executar sempre.
     */
    protected boolean aplicavel(ContextoAnalise contexto) {
        return true;
    }

    /** Passo obrigatório: a detecção específica de cada teste. */
    protected abstract List<Achado> detectar(ContextoAnalise contexto);

    /**
     * Gancho opcional: como relatar uma falha do próprio teste. O padrão é
     * registrar um achado informativo, de modo que a lacuna de cobertura
     * apareça no relatório em vez de passar despercebida.
     */
    protected Achado aoFalhar(ContextoAnalise contexto, RuntimeException erro) {
        return new Achado(
                id(),
                categoria(),
                Severidade.BAIXA,
                null,
                "O teste '" + nome() + "' não pôde ser concluído; o arquivo não foi coberto por esta verificação.",
                erro.getClass().getSimpleName()
        );
    }

    /** Atalho para montar um achado já preenchido com a identidade do teste. */
    protected Achado achado(Severidade severidade, TecnicaMitre tecnica, String descricao, String evidencia) {
        return new Achado(id(), categoria(), severidade, tecnica, descricao, evidencia);
    }
}
