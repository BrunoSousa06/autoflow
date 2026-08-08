package com.autoflow.infrastructure.persistence.repository;


import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PecaInsumoRepository extends JpaRepository<PecaInsumoEntity, Long>, JpaSpecificationExecutor<PecaInsumoEntity> {
    Optional<PecaInsumoEntity> findByNomeIgnoreCase(String nome);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PecaInsumoEntity p where p.id in :ids")
    List<PecaInsumoEntity> findAllByIdForUpdate(@Param("ids") List<Long> ids);

    Iterable<Long> id(Long id);
}
