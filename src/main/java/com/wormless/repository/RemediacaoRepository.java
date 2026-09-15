package com.wormless.repository;
import com.wormless.entities.Remediacao;
import org.springframework.data.jpa.repository.JpaRepository; 

public interface RemediacaoRepository extends JpaRepository<Remediacao, Long> { 
    
    boolean existsByAmeacaId(Long ameacaId); 

}