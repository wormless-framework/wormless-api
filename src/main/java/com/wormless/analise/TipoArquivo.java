package com.wormless.analise;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Tipos de arquivo aceitos pela sandbox.
 *
 * <p>O tipo é definido pela extensão do nome enviado. O MIME declarado pelo
 * navegador não é usado na validação: varia entre sistemas (um {@code .exe}
 * chega como {@code application/x-msdownload} ou
 * {@code application/octet-stream}) e é controlado pelo cliente.</p>
 *
 * <p>A assinatura (magic bytes) não é exigida no upload de propósito: um
 * arquivo cujo conteúdo não bate com a extensão é justamente o que os testes
 * de segurança devem relatar, em vez de ser recusado sem análise.</p>
 */
public enum TipoArquivo {

    PDF("pdf", "%PDF-"),

    /** Word 97-2003: contêiner OLE (Compound File Binary). */
    DOC("doc", "ÐÏ\u0011à¡±\u001Aá"),

    EXE("exe", "MZ"),

    ZIP("zip", "PK\u0003\u0004");

    private final String extensao;
    private final String assinatura;

    TipoArquivo(String extensao, String assinatura) {
        this.extensao = extensao;
        this.assinatura = assinatura;
    }

    public String getExtensao() {
        return extensao;
    }

    /** Se o conteúdo começa com a assinatura deste tipo. */
    public boolean assinaturaConfere(ContextoAnalise contexto) {
        return contexto.conteudo() != null && contexto.conteudo().startsWith(assinatura);
    }

    /** Tipo correspondente à extensão do nome, se for um tipo aceito. */
    public static Optional<TipoArquivo> porNome(String nomeArquivo) {

        if (nomeArquivo == null) {
            return Optional.empty();
        }

        int ponto = nomeArquivo.lastIndexOf('.');

        if (ponto < 0 || ponto == nomeArquivo.length() - 1) {
            return Optional.empty();
        }

        String extensao = nomeArquivo.substring(ponto + 1).toLowerCase(Locale.ROOT);

        return Arrays.stream(values())
                .filter(tipo -> tipo.extensao.equals(extensao))
                .findFirst();
    }

    /** Lista legível das extensões aceitas: {@code .pdf, .doc, .exe, .zip}. */
    public static String extensoesAceitas() {
        return Arrays.stream(values())
                .map(tipo -> "." + tipo.extensao)
                .collect(Collectors.joining(", "));
    }
}
