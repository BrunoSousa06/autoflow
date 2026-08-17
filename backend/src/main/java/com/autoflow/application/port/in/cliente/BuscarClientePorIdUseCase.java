package com.autoflow.application.port.in.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;

public interface BuscarClientePorIdUseCase {
    ClienteOutput execute(Long id);
}
