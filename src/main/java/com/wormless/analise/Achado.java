package com.wormless.analise;

import com.wormless.analise.mitre.TecnicaMitre;
import com.wormless.entities.enums.Severidade;

/**
 * Uma detecção individual produzida por um teste de segurança.
 *
 * <p>O achado é serializado em uma linha TSV dentro de
 * {@code AnaliseJob.resultadoBruto}. O formato mantém o contrato anterior de
 * "uma detecção por linha" — consumido tanto pela API quanto pelo prompt da
 * análise por IA — e ainda assim permite reconstruir o achado completo na
 * consulta de um job já processado, sem alterar o modelo de dados.</p>
 *
 * @param idTeste    identificador do teste que gerou a detecção
 * @param categoria  família do teste
 * @param severidade severidade atribuída à detecção
 * @param tecnica    técnica MITRE ATT&CK associada, ou {@code null}
 * @param descricao  explicação em linguagem acessível
 * @param evidencia  trecho ou medida que embasou a detecção
 */
public record Achado(
        String idTeste,
        CategoriaTeste categoria,
        Severidade severidade,
        TecnicaMitre tecnica,
        String descricao,
        String evidencia) {

    private static final String SEPARADOR = "\t";
    private static final String SEM_TECNICA = "-";
    private static final int TOTAL_CAMPOS = 6;
    private static final int LIMITE_EVIDENCIA = 300;

    /** Identificador atribuído a linhas gravadas antes deste formato. */
    public static final String ID_LEGADO = "LEGADO";

    public Achado {
        descricao = normalizar(descricao);
        evidencia = truncar(normalizar(evidencia));
    }

    /** Serializa o achado em uma linha TSV. */
    public String paraLinha() {
        return String.join(
                SEPARADOR,
                idTeste,
                categoria.name(),
                severidade.name(),
                tecnica == null ? SEM_TECNICA : tecnica.name(),
                descricao,
                evidencia
        );
    }

    /**
     * Reconstrói um achado a partir de uma linha TSV.
     *
     * <p>Linhas fora do formato — como as gravadas pela versão anterior do
     * motor, que continham apenas a descrição — são preservadas como achados
     * legados em vez de descartadas.</p>
     */
    public static Achado deLinha(String linha) {

        String[] campos = linha.split(SEPARADOR, -1);

        if (campos.length < TOTAL_CAMPOS) {
            return new Achado(ID_LEGADO, CategoriaTeste.ESTATICA, Severidade.MEDIA, null, linha, "");
        }

        return new Achado(
                campos[0],
                converter(CategoriaTeste.class, campos[1], CategoriaTeste.ESTATICA),
                converter(Severidade.class, campos[2], Severidade.MEDIA),
                SEM_TECNICA.equals(campos[3]) ? null : converter(TecnicaMitre.class, campos[3], null),
                campos[4],
                campos[5]
        );
    }

    /** Texto legível exibido na API e usado no relatório enviado à IA. */
    public String paraTexto() {

        StringBuilder texto = new StringBuilder();

        if (tecnica != null) {
            texto.append('[').append(tecnica.getId()).append("] ");
        }

        texto.append(descricao);

        if (!evidencia.isBlank()) {
            texto.append(" (evidência: ").append(evidencia).append(')');
        }

        return texto.toString();
    }

    private static <E extends Enum<E>> E converter(Class<E> tipo, String valor, E padrao) {
        try {
            return Enum.valueOf(tipo, valor);
        } catch (IllegalArgumentException e) {
            return padrao;
        }
    }

    private static String normalizar(String valor) {

        if (valor == null) {
            return "";
        }

        return valor.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim();
    }

    private static String truncar(String valor) {

        if (valor.length() <= LIMITE_EVIDENCIA) {
            return valor;
        }

        return valor.substring(0, LIMITE_EVIDENCIA) + "...";
    }
}
