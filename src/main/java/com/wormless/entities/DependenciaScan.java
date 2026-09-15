package com.wormless.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dependencias_scan")
public class DependenciaScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomePacote;

    private String versao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analise_job_id", nullable = false)
    private AnaliseJob analiseJob;

    @OneToMany(mappedBy = "dependenciaScan")
    private List<Vulnerabilidade> vulnerabilidades = new ArrayList<>();
}