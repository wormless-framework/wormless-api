package com.wormless.analise;

import com.wormless.exception.BusinessException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Amostra imutável do artefato sob análise, compartilhada por todos os testes.
 *
 * <p>O conteúdo é exposto como texto decodificado em ISO-8859-1. Essa
 * codificação é transparente a bytes (cada caractere corresponde a exatamente
 * um byte de 0 a 255), então os testes conseguem procurar tanto por marcadores
 * textuais quanto por sequências binárias sem manipular {@code byte[]}
 * mutável.</p>
 *
 * @param nomeArquivo nome original enviado pelo usuário
 * @param tipoMime    tipo MIME declarado no upload
 * @param tamanho     tamanho em bytes
 * @param conteudo    conteúdo decodificado em ISO-8859-1
 * @param sha256      hash SHA-256 do conteúdo, em hexadecimal minúsculo
 */
public record ContextoAnalise(
        String nomeArquivo,
        String tipoMime,
        long tamanho,
        String conteudo,
        String sha256) {

    /** Quantidade de bytes iniciais inspecionados na verificação de cabeçalho. */
    public static final int TAMANHO_CABECALHO = 1024;

    public static ContextoAnalise de(String nomeArquivo, String tipoMime, byte[] bytes) {

        if (bytes == null) {
            throw new BusinessException("O conteúdo do arquivo deve ser informado.");
        }

        return new ContextoAnalise(
                nomeArquivo,
                tipoMime,
                bytes.length,
                new String(bytes, StandardCharsets.ISO_8859_1),
                calcularSha256(bytes)
        );
    }

    private static String calcularSha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(bytes)
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível na JVM.", e);
        }
    }

    /** Primeiros {@value #TAMANHO_CABECALHO} bytes do arquivo, como texto. */
    public String cabecalho() {
        return conteudo.substring(0, Math.min(conteudo.length(), TAMANHO_CABECALHO));
    }

    public boolean contem(String marcador) {
        return conteudo.contains(marcador);
    }

    /** Quantas vezes o marcador aparece no conteúdo. */
    public int contar(String marcador) {

        if (marcador == null || marcador.isEmpty()) {
            return 0;
        }

        int total = 0;
        int posicao = conteudo.indexOf(marcador);

        while (posicao >= 0) {
            total++;
            posicao = conteudo.indexOf(marcador, posicao + marcador.length());
        }

        return total;
    }
}
