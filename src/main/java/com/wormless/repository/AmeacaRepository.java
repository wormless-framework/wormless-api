package com.wormless.repository; 
import com.wormless.entities.Ameaca; 
import com.wormless.entities.enums.Severidade; 
import org.springframework.data.jpa.repository.JpaRepository; 
import java.util.List; 
import java.util.Optional; 

public interface AmeacaRepository extends JpaRepository<Ameaca, Long> { 
    
    Optional<Ameaca> findByNome(String nome); 
    List<Ameaca> findByTipo(String tipo); 
    List<Ameaca> findBySeveridade(Severidade severidade); 
}