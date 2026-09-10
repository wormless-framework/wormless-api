package com.wormless.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AnalistaSOC extends Usuario {

    public RelatorioAmeaca visualizarRelatorio(Long id) {
        return new RelatorioAmeaca();
    }

    public FalsoPositivo marcarFalsoPositivo(IndicadorAmeaca indicador, String justificativa) {
        return new FalsoPositivo();
    }
}
