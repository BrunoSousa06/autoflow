package com.autoflow.mapper;


import com.autoflow.controller.cliente.ClienteEntrada;
import com.autoflow.controller.cliente.ClienteSaida;
import com.autoflow.domain.cliente.ClienteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteEntity mapToEntity(ClienteEntrada clienteEntrada);
    void updateEntity(ClienteEntrada entrada, @MappingTarget ClienteEntity entity);

    ClienteSaida mapToSaida(ClienteEntity clienteEntity);

    List<ClienteSaida> mapToList(List<ClienteEntity> clientes);
}
