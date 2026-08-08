package com.autoflow.presentation.servico;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.dto.servico.TempoMedioServicoMetricaOutput;
import com.autoflow.presentation.servico.request.ServicoRequest;
import com.autoflow.presentation.servico.response.ServicoResponse;
import com.autoflow.presentation.servico.response.TempoMedioServicoResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServicoControllerMapper {

    ServicoInput toInput(ServicoRequest request);

    ServicoResponse toResponse(ServicoOutput output);

    TempoMedioServicoResponse toResponse(TempoMedioServicoMetricaOutput output);
}
