package com.autoflow.application.port.in.ordemservico.reparoadicional;

import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;

public interface RecusarReparoAdicionalUseCase {
    ReparoAdicional execute(Long reparoAdicionalId, String motivo);
}
