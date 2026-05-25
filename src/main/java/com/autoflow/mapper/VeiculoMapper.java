package com.autoflow.mapper;


import com.autoflow.controller.veiculo.VeiculoEntrada;
import com.autoflow.controller.veiculo.VeiculoSaida;
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
    VeiculoEntity mapToEntity(VeiculoEntrada entrada, ClienteEntity cliente);

    VeiculoSaida mapToSaida(VeiculoEntity veiculoEntity);

    void updateEntity(VeiculoEntrada entrada, @MappingTarget VeiculoEntity veiculoEntity);

    List<VeiculoSaida> mapToList(List<VeiculoEntity> veiculos);
}