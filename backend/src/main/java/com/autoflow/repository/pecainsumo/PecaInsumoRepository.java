package com.autoflow.repository.pecainsumo;


import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PecaInsumoRepository extends JpaRepository<PecaInsumoEntity, Long>, JpaSpecificationExecutor<PecaInsumoEntity> {
    Optional<PecaInsumoEntity> findByNomeIgnoreCase(String nome);

    Iterable<Long> id(Long id);
}
