package com.autoflow.application.gateway;

import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;

import java.util.Optional;

public interface ReparoAdicionalGateway {

    ReparoAdicional save(ReparoAdicional reparoAdicional);

    Optional<ReparoAdicional> findById(Long id);

    Optional<ReparoAdicional> findByIdForUpdate(Long id);

    Optional<ReparoAdicional> findByOrcamentoId(Long orcamentoId);
}
