package com.wormless.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        return resposta(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request) {

        return resposta(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(TipoArquivoNaoSuportadoException.class)
    public ResponseEntity<ErrorResponse> handleTipoArquivoNaoSuportado(
            TipoArquivoNaoSuportadoException exception,
            HttpServletRequest request) {

        return resposta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getMessage(), request);
    }

    @ExceptionHandler(ArquivoMuitoGrandeException.class)
    public ResponseEntity<ErrorResponse> handleArquivoMuitoGrande(
            ArquivoMuitoGrandeException exception,
            HttpServletRequest request) {

        return resposta(HttpStatus.CONTENT_TOO_LARGE, exception.getMessage(), request);
    }

    /** Barrado pelo limite do multipart antes de chegar ao service. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            HttpServletRequest request) {

        return resposta(
                HttpStatus.CONTENT_TOO_LARGE,
                "O arquivo excede o tamanho máximo de 100 MB.",
                request
        );
    }

    /** Campo de arquivo ausente ou requisição que não é multipart. */
    @ExceptionHandler({MissingServletRequestPartException.class, MultipartException.class})
    public ResponseEntity<ErrorResponse> handleMultipart(
            Exception exception,
            HttpServletRequest request) {

        return resposta(
                HttpStatus.BAD_REQUEST,
                "O arquivo para análise deve ser enviado como multipart/form-data.",
                request
        );
    }

    /** Falha de Bean Validation em @Valid (corpo JSON ou @ModelAttribute). */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleValidacao(
            BindException exception,
            HttpServletRequest request) {

        String mensagem = exception.getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .collect(Collectors.joining(" "));

        return resposta(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(ProcessamentoArquivoException.class)
    public ResponseEntity<ErrorResponse> handleProcessamentoArquivo(
            ProcessamentoArquivoException exception,
            HttpServletRequest request) {

        LOG.error("Falha ao processar arquivo:", exception);

        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception,
            HttpServletRequest request) {

        LOG.error("Erro interno no servidor capturado:", exception);

        return resposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno no servidor.",
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        return resposta(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    private static ResponseEntity<ErrorResponse> resposta(
            HttpStatus status,
            String mensagem,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }
}
