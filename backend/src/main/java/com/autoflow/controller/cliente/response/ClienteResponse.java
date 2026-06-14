package com.autoflow.controller.cliente.response;

import com.autoflow.controller.veiculo.response.VeiculoClienteResponse;

import java.util.List;

public record ClienteResponse(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        List<VeiculoClienteResponse> veiculos){
}