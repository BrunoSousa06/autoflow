package com.autoflow.application.dto.cliente;

import com.autoflow.presentation.veiculo.response.VeiculoClienteResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
public record ClienteOutput(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        List<VeiculoClienteResponse> veiculos
) {
}
