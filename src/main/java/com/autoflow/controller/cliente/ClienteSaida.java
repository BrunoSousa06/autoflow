package com.autoflow.controller.cliente;

import com.autoflow.controller.veiculo.VeiculoClienteSaida;
import lombok.*;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteSaida {


    Long id;
    String nome;
    String cpf;
    Long telefone;
    String email;
    List<VeiculoClienteSaida> veiculos;
}