package com.autoflow.presentation.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoInput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.presentation.pecainsumo.request.PecaInsumoRequest;
import com.autoflow.presentation.pecainsumo.response.PecaInsumoResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PecaInsumoControllerMapper {
    PecaInsumoInput toInput(PecaInsumoRequest request);

    PecaInsumoResponse toResponse(PecaInsumoOutput output);
}
