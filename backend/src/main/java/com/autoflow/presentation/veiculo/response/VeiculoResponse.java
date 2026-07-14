package com.autoflow.presentation.veiculo.response;

import com.autoflow.presentation.cliente.response.ClienteVeiculoResponse;

public record VeiculoResponse(
         Long id,
         String marca,
         int ano,
         String placa,
         String modelo,
         ClienteVeiculoResponse cliente
         ) {


}

