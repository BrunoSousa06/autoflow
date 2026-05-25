package com.autoflow.controller.veiculo;

import com.autoflow.controller.cliente.ClienteSaidaVeiculo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VeiculoClienteSaida {

    private Long id;
    private String marca;
    private Long ano;
    private String placa;
    private String modelo;
}
