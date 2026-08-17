package com.autoflow.application.port.in.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;

public interface BuscarClientePorEmailUseCase {
    ClienteOutput execute(String email);
}
