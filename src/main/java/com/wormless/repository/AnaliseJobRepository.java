package com.wormless.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wormless.entities.AnaliseJob;
import com.wormless.entities.enums.StatusJob;

public interface AnaliseJobRepository extends JpaRepository<AnaliseJob, Long> {
    
    List<AnaliseJob> findByStatus(StatusJob status);
    List<AnaliseJob> findbyArquivosId(Long arquivoId);
    List<AnaliseJob> findByPipelineCICD(Long pipelineCICDId);
}
