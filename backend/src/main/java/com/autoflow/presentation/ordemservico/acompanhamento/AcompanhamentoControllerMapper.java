package com.autoflow.presentation.ordemservico.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.*;
import com.autoflow.presentation.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse;
import com.autoflow.presentation.ordemservico.acompanhamento.response.HistoricoStatusOsResponse;
import com.autoflow.presentation.ordemservico.acompanhamento.response.OrcamentoResumoResponse;
import com.autoflow.presentation.ordemservico.response.ItemNecessarioResponse;
import com.autoflow.presentation.ordemservico.response.ServicoOsResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AcompanhamentoControllerMapper {

    List<AcompanhamentoOrdemServicoResponse> toResponse(List<AcompanhamentoOrdemServicoOutput> output);

    AcompanhamentoOrdemServicoResponse toResponse(AcompanhamentoOrdemServicoOutput output);

    OrcamentoResumoResponse toResponse(OrcamentoResumoOutput output);

    HistoricoStatusOsResponse toResponse(HistoricoStatusOsOutput output);

    ItemNecessarioResponse toResponse(ItemNecessarioOutput output);

    ServicoOsResponse toResponse(ServicoSolicitadoOutput output);
}
