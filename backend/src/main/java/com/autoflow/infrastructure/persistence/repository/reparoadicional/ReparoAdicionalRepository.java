package com.autoflow.infrastructure.persistence.repository.reparoadicional;

import com.autoflow.infrastructure.persistence.entity.ordemservico.reparoadicional.ReparoAdicionalEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReparoAdicionalRepository extends JpaRepository<ReparoAdicionalEntity, Long> {
    Optional<ReparoAdicionalEntity> findByOrcamentoId(Long orcamentoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReparoAdicionalEntity r where r.id = :id")
    Optional<ReparoAdicionalEntity> findByIdForUpdate(@Param("id") Long id);
}
