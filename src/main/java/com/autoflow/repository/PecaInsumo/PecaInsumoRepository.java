package com.autoflow.repository.PecaInsumo;


import com.autoflow.domain.pecaInsumo.PecaInsumoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PecaInsumoRepository extends JpaRepository<PecaInsumoEntity, Long> {
    Optional<PecaInsumoEntity> findByNomeIgnoreCase(String nome);

}
