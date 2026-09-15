package com.wormless.repository;

import com.wormless.entities.IndicadorAmeaca;
import com.wormless.entities.enums.StatusIndicador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndicadorAmeacaRepository
        extends JpaRepository<IndicadorAmeaca, Long> {

    List<IndicadorAmeaca> findByStatus(StatusIndicador status);

    List<IndicadorAmeaca> findByRelatorioAmeacaId(Long relatorioAmeacaId);

    List<IndicadorAmeaca> findByAmeacaId(Long ameacaId);
}