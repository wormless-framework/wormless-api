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
public class UsuarioWeb extends Usuario {

    public AnaliseJob uploadArquivo(Arquivo arquivo) {
        return new AnaliseJob();
    }
}