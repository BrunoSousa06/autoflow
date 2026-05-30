package com.autoflow.controller.veiculo.response;

import com.autoflow.controller.cliente.response.ClienteVeiculoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record VeiculoResponse(
         Long id,
         String marca,
         int ano,
         String placa,
         String modelo,
         ClienteVeiculoResponse cliente
         ) {


}

