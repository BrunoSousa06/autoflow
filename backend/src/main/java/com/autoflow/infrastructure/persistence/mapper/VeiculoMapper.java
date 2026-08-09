package com.autoflow.infrastructure.persistence.mapper;


import com.autoflow.application.dto.veiculo.CadastrarVeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoClienteOutput;
import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VeiculoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", source = "cliente")
    VeiculoEntity mapToEntity(CadastrarVeiculoInput request, ClienteEntity cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    void updateEntity(VeiculoInput request, @MappingTarget VeiculoEntity veiculoEntity);

    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "cliente", source = "cliente")
    VeiculoOutput mapToOutput(VeiculoEntity veiculoEntity);

    VeiculoClienteOutput mapToClienteOutput(ClienteEntity clienteEntity);
}
