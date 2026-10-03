package com.autoflow.presentation.ordemservico.reparoadicional;

import com.autoflow.application.input.ordemservico.reparoadicional.CriarReparoAdicionalCommand;
import com.autoflow.application.input.ordemservico.reparoadicional.ItemReparoAdicionalCommand;
import com.autoflow.application.input.ordemservico.reparoadicional.ServicoReparoAdicionalCommand;
import com.autoflow.application.output.ordemservico.reparoadicional.CriarReparoAdicionalOutput;
import com.autoflow.presentation.ordemservico.reparoadicional.request.CriarReparoAdicionalRequest;
import com.autoflow.presentation.ordemservico.reparoadicional.request.ServicoReparoAdicionalRequest;
import com.autoflow.presentation.ordemservico.reparoadicional.response.CriarReparoAdicionalResponse;
import com.autoflow.presentation.ordemservico.request.ItensNecessariosRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReparoAdicionalRestMapper {

    @Mapping(target = "numeroOs", source = "numeroOs")
    @Mapping(target = "emailMecanico", source = "emailMecanico")
    @Mapping(target = "servicos", source = "request.servicos")
    CriarReparoAdicionalCommand toCommand(
            String numeroOs,
            String emailMecanico,
            CriarReparoAdicionalRequest request
    );

    CriarReparoAdicionalResponse toResponse(CriarReparoAdicionalOutput output);

    ServicoReparoAdicionalCommand toCommand(ServicoReparoAdicionalRequest request);

    ItemReparoAdicionalCommand toCommand(ItensNecessariosRequest request);
}
