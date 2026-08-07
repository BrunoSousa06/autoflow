package com.autoflow.presentation.cliente.mapper;

import com.autoflow.application.dto.cliente.ClienteInput;
import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.dto.cliente.ClienteVeiculoOutput;
import com.autoflow.presentation.cliente.request.ClienteRequest;
import com.autoflow.presentation.cliente.response.ClienteResponse;
import com.autoflow.presentation.veiculo.response.VeiculoClienteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteControllerMapper {

    @Mapping(target = "usuarioId", ignore = true)
    ClienteInput toInput(ClienteRequest request);

    ClienteResponse toResponse(ClienteOutput output);

    VeiculoClienteResponse toResponse(ClienteVeiculoOutput output);
}
