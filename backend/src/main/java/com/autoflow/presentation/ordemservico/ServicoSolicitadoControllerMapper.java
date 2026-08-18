package com.autoflow.presentation.ordemservico;

import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.presentation.ordemservico.request.ServicoSolicitadoRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServicoSolicitadoControllerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nome", ignore = true)
    @Mapping(target = "valor", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "iniciadoEm", ignore = true)
    @Mapping(target = "finalizadoEm", ignore = true)
    @Mapping(target = "itensNecessarios", ignore = true)
    ServicoSolicitado mapToEntity(ServicoSolicitadoRequest servicoSolicitadoRequest);

    List<ServicoSolicitado> mapToEntities(List<ServicoSolicitadoRequest> servicosRequest);
}
