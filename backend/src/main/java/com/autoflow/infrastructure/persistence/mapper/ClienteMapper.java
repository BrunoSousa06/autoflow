package com.autoflow.infrastructure.persistence.mapper;


import com.autoflow.application.dto.cliente.ClienteInput;
import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.presentation.cliente.request.ClienteRequest;
import com.autoflow.presentation.cliente.response.ClienteResponse;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    void updateEntity(ClienteInput request, @MappingTarget ClienteEntity entity);

    ClienteResponse maptoResponse(ClienteOutput output);

    List<ClienteResponse> mapToList(List<ClienteEntity> clientes);

    ClienteEntity mapToEntity(ClienteInput request);

    ClienteOutput mapToOutput(ClienteEntity clienteEntity);

    List<ClienteOutput> mapToListOutput(List<ClienteEntity> clientes);

    ClienteInput mapToInput(ClienteRequest request);

    default Optional<ClienteOutput> mapToOutputOpt(Optional<ClienteEntity> byCpfCnpj) {
        return byCpfCnpj.map(this::mapToOutput);
    }
}