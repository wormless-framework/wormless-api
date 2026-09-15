package com.wormless.repository;
import com.wormless.entities.RegraDeteccao; 
import org.springframework.data.jpa.repository.JpaRepository; 
import java.util.List; 

public interface RegraDeteccaoRepository extends JpaRepository<RegraDeteccao, Long> { 
    List<RegraDeteccao> findByAtivaTrue(); 
    List<RegraDeteccao> findByAtivaFalse(); boolean existsByPadraoAndAtivaTrue(String padrao); 
}