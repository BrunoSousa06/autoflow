package com.autoflow.application.port.in.cliente;

import com.autoflow.application.output.cliente.ClienteOutput;

public interface ListarClienteUseCase {
    ClienteOutput execute(Long documento);
}
