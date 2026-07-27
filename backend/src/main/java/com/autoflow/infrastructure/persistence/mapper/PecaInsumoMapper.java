package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.controller.pecainsumo.request.PecaInsumoRequest;
import com.autoflow.controller.pecainsumo.response.PecaInsumoResponse;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PecaInsumoMapper {
    PecaInsumoResponse toResponse(PecaInsumoEntity pecaInsumoEntity);

    List<PecaInsumoResponse> toResponseList(List<PecaInsumoEntity> pecaInsumos);

    PecaInsumoEntity mapToEntity(PecaInsumoRequest request);


    void updateEntity(PecaInsumoRequest request, @MappingTarget PecaInsumoEntity pecaInsumoEntity);
}
