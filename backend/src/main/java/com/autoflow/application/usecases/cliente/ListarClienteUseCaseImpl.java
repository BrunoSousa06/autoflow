package com.autoflow.application.usecases.cliente;

import com.autoflow.application.exception.ClienteNaoEncontradoException;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.output.cliente.ClienteOutput;
import com.autoflow.application.port.in.cliente.ListarClienteUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ListarClienteUseCaseImpl implements ListarClienteUseCase {

    private final ClienteGateway clienteGateway;

    @Override
    public ClienteOutput execute(Long documento) {
        String identificador = String.valueOf(documento).replaceAll("\\D", "");

        if (identificador.matches("\\d{11}|\\d{14}")) {
            return clienteGateway.findByCpfCnpj(identificador)
                    .orElseThrow(() -> new ClienteNaoEncontradoException(
                            "Cliente não encontrado com o CPF/CNPJ: " + identificador));
        }

        return clienteGateway.findById(documento)
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                        "Cliente não encontrado com o ID: " + documento));
    }
}
