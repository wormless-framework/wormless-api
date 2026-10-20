package com.wormless.exception;

/** Extensão do arquivo enviado fora dos tipos aceitos pela sandbox (HTTP 415). */
public class TipoArquivoNaoSuportadoException extends BusinessException {

    public TipoArquivoNaoSuportadoException(String message) {
        super(message);
    }
}
