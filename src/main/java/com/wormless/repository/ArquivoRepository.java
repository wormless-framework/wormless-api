package com.wormless.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wormless.entities.Arquivo;

public interface ArquivoRepository extends JpaRepository <Arquivo, Long> {
    
    List<Arquivo> findByNomeOriginalContainingIgnoreCase(String nomeOrigina);
    List<Arquivo> findByUsuarioWebId(Long usuarioWebId);
}
