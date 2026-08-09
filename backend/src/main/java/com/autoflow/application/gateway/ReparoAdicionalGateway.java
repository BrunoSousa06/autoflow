package com.autoflow.application.gateway;

import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;

import java.util.Optional;

public interface ReparoAdicionalGateway {

    ReparoAdicionalEntity save(ReparoAdicionalEntity reparoAdicional);

    Optional<ReparoAdicionalEntity> findById(Long id);

    Optional<ReparoAdicionalEntity> findByIdForUpdate(Long id);

    Optional<ReparoAdicionalEntity> findByOrcamentoId(Long orcamentoId);
}
