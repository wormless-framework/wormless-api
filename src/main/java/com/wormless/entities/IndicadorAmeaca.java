package com.wormless.entities;

import com.wormless.entities.enums.StatusIndicador;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "indicadores_ameaca")
public class IndicadorAmeaca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trechoArquivo;

    private int linhaOcorrencia;

    private String descricaoComportamento;

    @Enumerated(EnumType.STRING)
    private StatusIndicador status = StatusIndicador.ATIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relatorio_ameaca_id", nullable = false)
    private RelatorioAmeaca relatorioAmeaca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ameaca_id", nullable = false)
    private Ameaca ameaca;
}
