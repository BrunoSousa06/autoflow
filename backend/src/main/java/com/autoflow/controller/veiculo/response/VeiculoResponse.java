package com.autoflow.controller.veiculo.response;

import com.autoflow.controller.cliente.response.ClienteVeiculoResponse;

public record VeiculoResponse(
         Long id,
         String marca,
         int ano,
         String placa,
         String modelo,
         ClienteVeiculoResponse cliente
         ) {


}

