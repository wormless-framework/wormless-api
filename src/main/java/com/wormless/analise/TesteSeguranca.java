package com.wormless.analise;

import java.util.List;

/**
 * Contrato de um teste de segurança executável sobre um artefato.
 *
 * <p>Cada implementação é um bean do Spring; o {@link MotorAnaliseSeguranca}
 * recebe todas elas por injeção, de modo que incluir um novo teste na
 * plataforma significa apenas criar uma classe — nenhum ponto central precisa
 * ser alterado.</p>
 */
public interface TesteSeguranca {

    /** Identificador estável do teste, usado na serialização dos achados. */
    String id();

    /** Nome legível exibido em relatórios e na listagem de cobertura. */
    String nome();

    CategoriaTeste categoria();

    /** Executa o teste e devolve os achados encontrados. */
    List<Achado> executar(ContextoAnalise contexto);

    /** Posição na esteira de análise; valores menores executam antes. */
    default int ordem() {
        return 100;
    }
}
