package com.wormless.exception;

/** Falha do servidor ao gravar ou ler o arquivo recebido (HTTP 500). */
public class ProcessamentoArquivoException extends RuntimeException {

    public ProcessamentoArquivoException(String message, Throwable cause) {
        super(message, cause);
    }
}
