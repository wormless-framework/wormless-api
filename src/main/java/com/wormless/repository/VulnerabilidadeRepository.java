package com.wormless.repository;

import com.wormless.entities.Vulnerabilidade; 
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; 
import java.util.Optional; 

public interface VulnerabilidadeRepository extends JpaRepository<Vulnerabilidade, Long> { 
    
    Optional<Vulnerabilidade> findByCve(String cve); 
    List<Vulnerabilidade> findByAmeacaId(Long ameacaId); 
    List<Vulnerabilidade> findByDependenciaScanId(Long dependenciaScanId); 

}