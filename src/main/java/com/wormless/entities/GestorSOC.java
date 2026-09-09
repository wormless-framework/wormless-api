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
public class GestorSOC extends Usuario {
    
    public PainelGovernanca acessarPainel() {
        return new PainelGovernanca();
    }
}