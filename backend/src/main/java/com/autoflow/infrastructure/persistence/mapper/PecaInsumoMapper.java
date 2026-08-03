package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.application.dto.pecainsumo.PecaInsumoInput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.presentation.pecainsumo.request.PecaInsumoRequest;
import com.autoflow.presentation.pecainsumo.response.PecaInsumoResponse;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PecaInsumoMapper {

    PecaInsumoInput mapToInput(@Valid PecaInsumoRequest request);

    void updateEntity(PecaInsumoInput request, @MappingTarget PecaInsumoOutput entity);
    void updateEntity(PecaInsumoInput request, @MappingTarget PecaInsumoEntity entity);


    PecaInsumoResponse toResponse(PecaInsumoOutput pecasInsumos);
    PecaInsumoResponse toResponse(PecaInsumoEntity pecaInsumoEntity);
    List<PecaInsumoResponse> toResponseList(List<PecaInsumoEntity> pecaInsumos);


    PecaInsumoOutput mapToOutput(PecaInsumoEntity entity);
    PecaInsumoOutput mapToOutput(PecaInsumoInput request);


    PecaInsumoEntity mapToEntity(PecaInsumoRequest request);
    PecaInsumoEntity mapToEntity(PecaInsumoOutput pecaInsumoEntity);


    PecaInsumoEntity mapToEntity(PecaInsumoInput request);
}
