package com.wormless.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "ameacas")
public class Ameaca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String tipo;

    private String comoAge;

    @Enumerated(EnumType.STRING)
    private Severidade severidade;

    @OneToMany(mappedBy = "ameaca")
    private List<IndicadorAmeaca> indicadores = new ArrayList<>();

    @OneToMany(mappedBy = "ameaca")
    private List<Vulnerabilidade> vulnerabilidades = new ArrayList<>();

    @OneToOne(mappedBy = "ameaca", fetch = FetchType.LAZY)
    private Remediacao remediacao;
}