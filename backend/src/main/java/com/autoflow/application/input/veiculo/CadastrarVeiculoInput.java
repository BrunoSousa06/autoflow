package com.autoflow.application.input.veiculo;


public record CadastrarVeiculoInput(

        String cpfCnpj,
        String placa,
        String marca,
        String modelo,
        Integer ano

) {
}
