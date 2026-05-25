package com.autoflow.controller.veiculo;

import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VeiculoEntrada {

    @NotNull
    Long idCliente;
    String marca;
    Long ano;
    String placa;
    String modelo;
}

