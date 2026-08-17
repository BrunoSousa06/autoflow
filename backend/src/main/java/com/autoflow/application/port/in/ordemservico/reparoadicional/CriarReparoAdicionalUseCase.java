package com.autoflow.application.port.in.ordemservico.reparoadicional;

import com.autoflow.application.input.ordemservico.reparoadicional.CriarReparoAdicionalCommand;
import com.autoflow.application.output.ordemservico.reparoadicional.CriarReparoAdicionalOutput;

public interface CriarReparoAdicionalUseCase {
    CriarReparoAdicionalOutput execute(CriarReparoAdicionalCommand command);
}
