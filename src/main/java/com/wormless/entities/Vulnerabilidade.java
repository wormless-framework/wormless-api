package com.wormless.entities;

import jakarta.persistence.Entity;
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
@Table(name = "vulnerabilidades")
public class Vulnerabilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cve;

    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ameaca_id")
    private Ameaca ameaca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependencia_scan_id")
    private DependenciaScan dependenciaScan;
}