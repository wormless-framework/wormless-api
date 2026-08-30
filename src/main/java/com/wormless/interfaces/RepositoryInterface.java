package com.wormless.interfaces;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface RepositoryInterface<T, ID> extends Repository<T, ID> {
    // Métodos comuns que você queira padronizar para todos os seus repositories
    List<T> findAll();
    <S extends T> S save(S entity);
    Optional<T> findById(ID id);
}