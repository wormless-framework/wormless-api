package com.wormless.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sandbox/analises");

    @Test
    void cadaErroDeArquivoTemSeuStatus() {
        assertThat(handler.handleBusinessException(new BusinessException("x"), request).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(handler.handleTipoArquivoNaoSuportado(
                new TipoArquivoNaoSuportadoException("x"), request).getStatusCode())
                .isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(handler.handleArquivoMuitoGrande(
                new ArquivoMuitoGrandeException("x"), request).getStatusCode())
                .isEqualTo(HttpStatus.CONTENT_TOO_LARGE);
        assertThat(handler.handleProcessamentoArquivo(
                new ProcessamentoArquivoException("x", new java.io.IOException()), request).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void limiteDoMultipartRespondeComoArquivoMuitoGrande() {
        var resposta = handler.handleMaxUploadSizeExceeded(request);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE);
        assertThat(resposta.getBody().getMessage()).contains("100 MB");
        assertThat(resposta.getBody().getPath()).isEqualTo("/sandbox/analises");
    }

    @Test
    void erroDeProcessamentoMantemMensagemPropria() {
        var resposta = handler.handleProcessamentoArquivo(
                new ProcessamentoArquivoException("Falha ao ler.", new java.io.IOException()), request);

        assertThat(resposta.getBody().getMessage()).isEqualTo("Falha ao ler.");
    }
}
