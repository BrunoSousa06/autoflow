package com.autoflow.mapper;

import com.autoflow.controller.pecaInsumo.request.PecaInsumoRequest;
import com.autoflow.controller.pecaInsumo.response.PecaInsumoResponse;
import com.autoflow.domain.pecaInsumo.PecaInsumoEntity;
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
