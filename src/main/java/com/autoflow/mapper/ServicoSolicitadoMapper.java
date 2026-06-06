package com.autoflow.mapper;

import com.autoflow.controller.ordemservico.request.ServicoSolicitadoRequest;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServicoSolicitadoMapper {

    ServicoSolicitadoEntity mapToEntity(ServicoSolicitadoRequest servicoSolicitadoRequest);

    List<ServicoSolicitadoEntity> mapToEntities(List<ServicoSolicitadoRequest> servicosRequest);
}
