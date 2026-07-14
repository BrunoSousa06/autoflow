package com.autoflow.mapper;

import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.dto.servico.TempoMedioServicoMetricaOutput;
import com.autoflow.presentation.servico.request.ServicoRequest;
import com.autoflow.presentation.servico.response.ServicoResponse;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.presentation.servico.response.TempoMedioServicoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServicoMapper {

    ServicoResponse toResponse(ServicoEntity entity);

    List<ServicoResponse> toResponseList(List<ServicoEntity> entityList);

    ServicoEntity mapToEntity(ServicoRequest request);

    void updateEntity(ServicoRequest request, @MappingTarget ServicoEntity entity);

    ServicoOutput mapToOutput(ServicoEntity entity);

    List<ServicoOutput> mapToListOutput(List<ServicoEntity> entityList);

    ServicoResponse mapToResponse(ServicoOutput output);


    List<TempoMedioServicoResponse> mapToMetricResponse(List<TempoMedioServicoMetricaOutput> metricasOutput);
}
