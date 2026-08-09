package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServicoPersistenceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    ServicoEntity mapToEntity(ServicoInput input);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    void updateEntity(ServicoInput input, @MappingTarget ServicoEntity entity);

    ServicoOutput mapToOutput(ServicoEntity entity);
}
