package com.autoflow.infrastructure.persistence.mapper;


import com.autoflow.application.dto.veiculo.CadastrarVeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.presentation.veiculo.response.VeiculoResponse;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VeiculoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", source = "cliente")
    VeiculoEntity mapToEntity(CadastrarVeiculoInput request, ClienteEntity cliente);

    VeiculoResponse mapToResponse(VeiculoEntity veiculoEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    void updateEntity(VeiculoInput request, @MappingTarget VeiculoEntity veiculoEntity);

    List<VeiculoResponse> mapToList(List<VeiculoEntity> veiculos);

    VeiculoOutput mapToOutput(VeiculoEntity veiculoEntity);
}