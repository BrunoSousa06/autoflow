package com.autoflow.presentation.ordemservico;

import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
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
    @Mapping(target = "ordemServico", ignore = true)
    @Mapping(target = "itensNecessarios", ignore = true)
    @Mapping(target = "reparoAdicional", ignore = true)
    ServicoSolicitadoEntity mapToEntity(ServicoSolicitadoRequest servicoSolicitadoRequest);

    List<ServicoSolicitadoEntity> mapToEntities(List<ServicoSolicitadoRequest> servicosRequest);
}
