package com.wormless.repository;

import com.wormless.entities.DependenciaScan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DependenciaScanRepository
        extends JpaRepository<DependenciaScan, Long> {

    List<DependenciaScan> findByAnaliseJobId(Long analiseJobId);

    List<DependenciaScan> findByNomePacote(String nomePacote);

    List<DependenciaScan> findByNomePacoteAndVersao(
            String nomePacote,
            String versao);
}