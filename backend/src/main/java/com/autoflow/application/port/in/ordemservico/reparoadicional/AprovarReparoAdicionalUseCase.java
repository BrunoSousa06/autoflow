package com.autoflow.application.port.in.ordemservico.reparoadicional;

import com.autoflow.domain.ordemservico.OrdemServico;

public interface AprovarReparoAdicionalUseCase {
    OrdemServico execute(Long reparoAdicionalId);
}
