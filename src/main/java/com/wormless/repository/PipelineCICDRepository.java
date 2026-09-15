package com.wormless.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wormless.entities.PipelineCICD;

public interface PipelineCICDRepository extends JpaRepository<PipelineCICD, Long>{
    
    Optional<PipelineCICD> findByRepositorio(String repositorio);

    boolean existsByRepositorio(String repositorio);
}
