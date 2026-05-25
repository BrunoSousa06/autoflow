package com.autoflow.repository.cliente;


import com.autoflow.controller.cliente.ClienteEntrada;
import com.autoflow.controller.cliente.ClienteSaida;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteEntity mapToEntity(ClienteEntrada clienteEntrada);
    void updateEntity(ClienteEntrada entrada, @MappingTarget ClienteEntity entity);

    ClienteSaida mapToSaida(ClienteEntity clienteEntity);

    List<ClienteSaida> mapToList(List<ClienteEntity> clientes);
}
