package com.autoflow.application.dto.veiculo;


public record CadastrarVeiculoInput(

        String cpfCnpj,
        String placa,
        String marca,
        String modelo,
        Integer ano

) {
}
