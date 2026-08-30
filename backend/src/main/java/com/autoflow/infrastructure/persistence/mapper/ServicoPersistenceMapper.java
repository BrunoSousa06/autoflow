package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.domain.servico.Servico;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServicoPersistenceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    ServicoEntity mapToEntity(Servico servico);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    void updateEntity(Servico servico, @MappingTarget ServicoEntity entity);

    @Mapping(target = "id", source = "id")
    Servico mapToDomain(ServicoEntity entity);
}
