package com.wormless.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wormless.entities.AnaliseJob;
import com.wormless.entities.enums.StatusJob;

public interface AnaliseJobRepository extends JpaRepository<AnaliseJob, Long> {
    
    List<AnaliseJob> findByStatus(StatusJob status);
    List<AnaliseJob> findByArquivoId(Long arquivoId);
    List<AnaliseJob> findByPipelineCICD(Long pipelineCICDId);
    Page<AnaliseJob> findByArquivoProprietarioId(Long proprietarioId, Pageable pageable);
    boolean existsByIdAndArquivoProprietarioId(Long id, Long proprietarioId);
}
