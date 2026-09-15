package com.wormless.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter
@Table(name = "usuarios_web")
public class UsuarioWeb extends Usuario {

    @OneToMany(mappedBy = "usuarioWeb")
    private List<Arquivo> arquivos = new ArrayList<>();
}