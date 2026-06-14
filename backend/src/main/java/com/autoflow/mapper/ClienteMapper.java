package com.autoflow.mapper;


import com.autoflow.controller.cliente.request.ClienteRequest;
import com.autoflow.controller.cliente.response.ClienteResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    void updateEntity(ClienteRequest request, @MappingTarget ClienteEntity entity);

    ClienteResponse maptoResponse(ClienteEntity clienteEntity);

    List<ClienteResponse> mapToList(List<ClienteEntity> clientes);

    ClienteEntity mapToEntity(ClienteRequest request);
}
