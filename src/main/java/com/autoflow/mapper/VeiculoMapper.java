package com.autoflow.mapper;


import com.autoflow.controller.veiculo.request.VeiculoRequest;
import com.autoflow.controller.veiculo.response.VeiculoResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VeiculoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", source = "cliente")
    VeiculoEntity mapToEntity(VeiculoRequest request, ClienteEntity cliente);

    VeiculoResponse mapToResponse(VeiculoEntity veiculoEntity);

    void updateEntity(VeiculoRequest request, @MappingTarget VeiculoEntity veiculoEntity);

    List<VeiculoResponse> mapToList(List<VeiculoEntity> veiculos);
}