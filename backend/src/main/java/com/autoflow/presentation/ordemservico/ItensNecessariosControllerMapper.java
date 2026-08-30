package com.autoflow.presentation.ordemservico;

import com.autoflow.domain.ordemservico.ItemNecessario;
import com.autoflow.presentation.ordemservico.request.ItensNecessariosRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItensNecessariosControllerMapper {


    @Mapping(target = "nome", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "valorUnitario", ignore = true)
    @Mapping(target = "valorTotal", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "motivoPendencia", ignore = true)
    @Mapping(target = "quantidadeDisponivel", ignore = true)
    @Mapping(target = "mensagemStatus", ignore = true)
    ItemNecessario mapToEntity(ItensNecessariosRequest request);

    List<ItemNecessario> mapToEntities(List<ItensNecessariosRequest> requests);
}
