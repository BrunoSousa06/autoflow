package com.autoflow.application.port.in.ordemservico.reparoadicional;

import com.autoflow.application.dto.ordemservico.reparoadicional.CriarReparoAdicionalCommand;
import com.autoflow.application.dto.ordemservico.reparoadicional.CriarReparoAdicionalOutput;

public interface CriarReparoAdicionalUseCase {
    CriarReparoAdicionalOutput execute(CriarReparoAdicionalCommand command);
}
