package com.autoflow.application.input.veiculo;


public record CadastrarVeiculoCommand(

        String cpfCnpj,
        String placa,
        String marca,
        String modelo,
        Integer ano

) {
}
