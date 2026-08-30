package com.wormless.repositories;

import com.wormless.interfaces.RepositoryInterface;
import com.wormless.models.Arquivo;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArquivoRepository extends RepositoryInterface<Arquivo, Long> {

    // Método customizado para buscar pelo hash SHA-256 (essencial para o cache)
    Optional<Arquivo> findByHashSha256(String hashSha256);

}