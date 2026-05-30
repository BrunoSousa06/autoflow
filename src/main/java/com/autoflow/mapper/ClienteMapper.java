package com.autoflow.mapper;


import com.autoflow.controller.cliente.request.ClienteRequest;
import com.autoflow.controller.cliente.response.ClienteResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.usuario.RoleEnum;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteEntity mapToEntity(ClienteRequest clienteRequest);

    void updateEntity(ClienteRequest request, @MappingTarget ClienteEntity entity);

    ClienteResponse maptoResponse(ClienteEntity clienteEntity);

    List<ClienteResponse> mapToList(List<ClienteEntity> clientes);

}
