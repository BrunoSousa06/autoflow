package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.presentation.servico.request.ServicoRequest;
import com.autoflow.presentation.servico.response.ServicoResponse;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServicoMapper {

    ServicoResponse toResponse(ServicoEntity entity);

    ServicoEntity mapToEntity(ServicoRequest request);

    void updateEntity(ServicoRequest request, @MappingTarget ServicoEntity entity);

    ServicoEntity mapToEntity(ServicoInput input);

    void updateEntity(ServicoInput input, @MappingTarget ServicoEntity entity);

    ServicoOutput mapToOutput(ServicoEntity entity);
}
