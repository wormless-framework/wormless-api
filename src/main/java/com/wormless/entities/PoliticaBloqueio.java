package com.wormless.entities;

import com.wormless.entities.enums.Severidade;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "politicas_bloqueio")
public class PoliticaBloqueio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Severidade limiteSeveridade;

    public boolean avaliar(RelatorioAmeaca relatorio) {
        if (relatorio == null
                || relatorio.getSeveridadeGeral() == null
                || limiteSeveridade == null) {
            return false;
        }

        return relatorio.getSeveridadeGeral().ordinal()
                >= limiteSeveridade.ordinal();
    }
}
