package com.wormless.entities;

import jakarta.persistence.Entity;
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
@Table(name = "analistas_soc")
public class AnalistaSOC extends Usuario {

    @OneToMany(mappedBy = "analistaSOC")
    private List<FalsoPositivo> falsosPositivos = new ArrayList<>();
}