package com.wormless.repository;

import com.wormless.entities.FalsoPositivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FalsoPositivoRepository
        extends JpaRepository<FalsoPositivo, Long> {

    List<FalsoPositivo> findByAnalistaSOCId(Long analistaSOCId);

    List<FalsoPositivo> findByIndicadorAmeacaId(Long indicadorAmeacaId);

    List<FalsoPositivo> findByRegraDeteccaoId(Long regraDeteccaoId);
}