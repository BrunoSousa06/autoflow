package com.autoflow.application.mapper;

import com.autoflow.application.dto.pecainsumo.PecaInsumoInput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface PecaInsumoMapper {
    @Mapping(target = "id", ignore = true)
    void updateEntity(PecaInsumoInput request, @MappingTarget PecaInsumoEntity entity);

    PecaInsumoOutput mapToOutput(PecaInsumoEntity entity);

    @Mapping(target = "id", ignore = true)
    PecaInsumoEntity mapToEntity(PecaInsumoInput request);
}
