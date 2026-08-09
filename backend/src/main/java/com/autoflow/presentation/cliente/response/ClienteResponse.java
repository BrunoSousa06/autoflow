package com.autoflow.presentation.cliente.response;

import com.autoflow.presentation.veiculo.response.VeiculoClienteResponse;

import java.util.List;

public record ClienteResponse(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        List<VeiculoClienteResponse> veiculos) {
}