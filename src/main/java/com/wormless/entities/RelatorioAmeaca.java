package com.wormless.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.wormless.entities.enums.Severidade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "relatorios_ameaca")
public class RelatorioAmeaca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataGeracao;

    @Enumerated(EnumType.STRING)
    private Severidade severidadeGeral;

    private String resumo;

    @OneToOne
    @JoinColumn(name = "analise_job_id", nullable = false, unique = true)
    private AnaliseJob analiseJob;

    @OneToMany(
        mappedBy = "relatorioAmeaca",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<IndicadorAmeaca> indicadores = new ArrayList<>();

    public String gerarResumo() {
        return resumo;
    }
}