package com.autoflow.mapper;

import com.autoflow.controller.ordemServico.request.ItensNecessariosRequest;
import com.autoflow.domain.ordemServico.ItemNecessarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItensNecessariosMapper {


    @Mapping(target = "nome", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "valorUnitario", ignore = true)
    @Mapping(target = "valorTotal", ignore = true)
    @Mapping(target = "status", ignore = true)
    ItemNecessarioEntity mapToEntity(ItensNecessariosRequest request);
    List<ItemNecessarioEntity> mapToEntities(List<ItensNecessariosRequest> requests);
}
