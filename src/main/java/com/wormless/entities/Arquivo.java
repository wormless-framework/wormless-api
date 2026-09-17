package com.wormless.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "arquivos")
public class Arquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeOriginal;

    private Long tamanho;

    private String tipoMime;

    private String caminhoTemporario;

    private LocalDateTime dataUpload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_web_id")
    private UsuarioWeb usuarioWeb;

    @OneToOne(mappedBy = "arquivo")
    private AnaliseJob analiseJob;

    public boolean validarFormato() {
        return tipoMime != null && !tipoMime.isBlank();
    }

    public boolean validarTamanho() {
        return tamanho != null && tamanho > 0 && tamanho <= 100L * 1024 * 1024; // 100 MB
    }
}
