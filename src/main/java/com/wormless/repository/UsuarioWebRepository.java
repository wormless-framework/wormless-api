package com.wormless.repository;

import com.wormless.entities.UsuarioWeb;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioWebRepository
        extends JpaRepository<UsuarioWeb, Long> {

    Optional<UsuarioWeb> findByEmail(String email);

    boolean existsByEmail(String email);
}
