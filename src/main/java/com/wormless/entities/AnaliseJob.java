package com.wormless.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.wormless.entities.enums.StatusJob;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "analises_job")
public class AnaliseJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private StatusJob status = StatusJob.PENDENTE;

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arquivo_id", nullable = false, unique = true)
    private Arquivo arquivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_cicd_id")
    private PipelineCICD pipelineCICD;

    @OneToOne(mappedBy = "analiseJob")
    private RelatorioAmeaca relatorioAmeaca;

    @OneToMany(mappedBy = "analiseJob")
    private List<DependenciaScan> dependencias = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String resultadoBruto;

    public StatusJob consultarStatus() {
        return status;
    }
}