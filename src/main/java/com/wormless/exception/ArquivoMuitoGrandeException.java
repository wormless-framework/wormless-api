package com.wormless.exception;

/** Arquivo acima do tamanho máximo aceito para análise (HTTP 413). */
public class ArquivoMuitoGrandeException extends BusinessException {

    public ArquivoMuitoGrandeException(String message) {
        super(message);
    }
}
