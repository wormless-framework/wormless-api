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

import com.wormless.entities.enums.StatusIndicador;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "falsos_positivos")
public class FalsoPositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String justificativaTecnica;

    private LocalDateTime dataMarcacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analista_soc_id", nullable = false)
    private AnalistaSOC analistaSOC;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicador_ameaca_id", nullable = false, unique = true)
    private IndicadorAmeaca indicadorAmeaca;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regra_deteccao_id", nullable = false, unique = true)
    private RegraDeteccao regraDeteccao;

    public void aplicar() {
        if (indicadorAmeaca != null) {
            indicadorAmeaca.setStatus(StatusIndicador.FALSO_POSITIVO);
        }

        if (regraDeteccao != null) {
            regraDeteccao.ignorarEmMetricasFuturas();
        }
    }
}