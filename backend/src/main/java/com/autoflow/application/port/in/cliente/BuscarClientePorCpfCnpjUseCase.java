package com.autoflow.application.port.in.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;

public interface BuscarClientePorCpfCnpjUseCase {
    ClienteOutput execute(String cpfCnpj);
}
