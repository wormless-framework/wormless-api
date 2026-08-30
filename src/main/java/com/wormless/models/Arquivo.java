package com.wormless.models; 

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "arquivos_analisados")
public class Arquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String hashSha256; 

    private String nomeArquivo;
    
    private String ameacaDetectada;
    
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    private StatusAnalise status;

    private boolean bloqueadoPeloEnforcement;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String relatorioIa; 

    private String origem;
}

enum StatusAnalise {
    SEGURO,
    MALICIOSO,
    SUSPEITO,
    FALSO_POSITIVO_APROVADO
}