package com.autoflow.application.port.in.ordemservico.reparoadicional;

import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;

import java.util.Optional;

public interface ConsultarReparoAdicionalPorOrcamentoUseCase {
    Optional<ReparoAdicional> execute(Long orcamentoId);
}
