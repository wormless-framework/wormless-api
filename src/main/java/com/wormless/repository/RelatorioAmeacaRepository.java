package com.wormless.repository;

import com.wormless.entities.RelatorioAmeaca;
import com.wormless.entities.enums.Severidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelatorioAmeacaRepository
        extends JpaRepository<RelatorioAmeaca, Long> {

    List<RelatorioAmeaca> findBySeveridadeGeral(Severidade severidade);

    List<RelatorioAmeaca> findByAnaliseJobId(Long analiseJobId);
}