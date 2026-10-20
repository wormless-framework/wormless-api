package com.wormless.analise;

/**
 * Famílias de teste suportadas pelo motor de análise.
 *
 * <p>A categoria deixa explícito que a plataforma não depende apenas de
 * análise estática de marcadores: há verificação estrutural, heurística,
 * reputação de indicadores e inspeção de indicadores de rede.</p>
 */
public enum CategoriaTeste {

    ESTATICA("Análise estática dos marcadores declarados no documento"),

    ESTRUTURAL("Verificação da estrutura e da integridade do arquivo"),

    HEURISTICA("Heurística estatística e comportamental"),

    REPUTACAO("Reputação de indicadores de comprometimento conhecidos"),

    REDE("Indicadores de rede e de comando e controle");

    private final String descricao;

    CategoriaTeste(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
