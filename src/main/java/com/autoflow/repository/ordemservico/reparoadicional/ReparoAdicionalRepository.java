package com.autoflow.repository.ordemservico.reparoadicional;

import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.domain.ordemservico.reparoadicional.StatusReparoAdicional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReparoAdicionalRepository extends JpaRepository<ReparoAdicionalEntity, Long> {
    Optional<ReparoAdicionalEntity> findByOrcamentoId(Long orcamentoId);

    boolean existsByNumeroOsAndStatus(String numeroOs, StatusReparoAdicional statusReparoAdicional);
}
