package com.wormless.services;

import com.wormless.entities.Arquivo;
import com.wormless.exception.ArquivoMuitoGrandeException;
import com.wormless.exception.BusinessException;
import com.wormless.exception.TipoArquivoNaoSuportadoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SandboxServiceValidacaoTest {

    @InjectMocks
    private SandboxService sandboxService;

    @Test
    void arquivoAusente() {
        assertThatThrownBy(() -> sandboxService.validarUpload(null))
                .isExactlyInstanceOf(BusinessException.class)
                .hasMessage("O arquivo para análise deve ser informado.");
    }

    @Test
    void arquivoSemNome() {
        MultipartFile arquivo = new MockMultipartFile("arquivo", "", null, new byte[] {1});

        assertThatThrownBy(() -> sandboxService.validarUpload(arquivo))
                .isExactlyInstanceOf(BusinessException.class)
                .hasMessage("O arquivo enviado não possui nome.");
    }

    @Test
    void arquivoVazio() {
        MultipartFile arquivo = new MockMultipartFile("arquivo", "doc.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> sandboxService.validarUpload(arquivo))
                .isExactlyInstanceOf(BusinessException.class)
                .hasMessage("O arquivo enviado está vazio.");
    }

    @Test
    void tipoNaoSuportado() {
        MultipartFile arquivo = new MockMultipartFile("arquivo", "notas.txt", "text/plain", new byte[] {1});

        assertThatThrownBy(() -> sandboxService.validarUpload(arquivo))
                .isInstanceOf(TipoArquivoNaoSuportadoException.class)
                .hasMessageContaining(".pdf, .doc, .exe, .zip");
    }

    @Test
    void arquivoAcimaDoLimite() {
        MultipartFile arquivo = mock(MultipartFile.class);
        when(arquivo.getOriginalFilename()).thenReturn("grande.zip");
        when(arquivo.isEmpty()).thenReturn(false);
        when(arquivo.getSize()).thenReturn(Arquivo.TAMANHO_MAXIMO_BYTES + 1);

        assertThatThrownBy(() -> sandboxService.validarUpload(arquivo))
                .isInstanceOf(ArquivoMuitoGrandeException.class);
    }

    @Test
    void arquivoValido() {
        MultipartFile arquivo = new MockMultipartFile("arquivo", "setup.EXE", null, new byte[] {'M', 'Z'});

        assertThatCode(() -> sandboxService.validarUpload(arquivo)).doesNotThrowAnyException();
    }
}
