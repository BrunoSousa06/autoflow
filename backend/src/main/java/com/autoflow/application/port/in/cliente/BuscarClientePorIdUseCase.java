package com.autoflow.application.port.in.cliente;

import com.autoflow.application.output.cliente.ClienteOutput;

public interface BuscarClientePorIdUseCase {
    ClienteOutput execute(Long id);
}
