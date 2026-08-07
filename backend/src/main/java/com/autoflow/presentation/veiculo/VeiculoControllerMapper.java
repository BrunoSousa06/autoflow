package com.autoflow.presentation.veiculo;

import com.autoflow.application.dto.veiculo.CadastrarVeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoClienteOutput;
import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.presentation.cliente.response.ClienteVeiculoResponse;
import com.autoflow.presentation.veiculo.request.VeiculoRequest;
import com.autoflow.presentation.veiculo.request.VeiculoUpdateRequest;
import com.autoflow.presentation.veiculo.response.VeiculoResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VeiculoControllerMapper {

    CadastrarVeiculoInput toInput(VeiculoRequest request);

    VeiculoInput toInput(VeiculoUpdateRequest request);

    VeiculoResponse toResponse(VeiculoOutput output);

    ClienteVeiculoResponse toResponse(VeiculoClienteOutput output);
}
