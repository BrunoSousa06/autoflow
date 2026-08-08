package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.HistoricoStatusOsOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.OrcamentoResumoOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.ItemNecessarioOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.ServicoSolicitadoOutput;
import com.autoflow.presentation.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse;
import com.autoflow.presentation.ordemservico.acompanhamento.response.HistoricoStatusOsResponse;
import com.autoflow.presentation.ordemservico.acompanhamento.response.OrcamentoResumoResponse;
import com.autoflow.controller.ordemservico.response.ItemNecessarioResponse;
import com.autoflow.controller.ordemservico.response.ServicoOsResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AcompanhamentoMapper {

    List<AcompanhamentoOrdemServicoResponse> toResponse(List<AcompanhamentoOrdemServicoOutput> output);
    AcompanhamentoOrdemServicoResponse toResponse(AcompanhamentoOrdemServicoOutput output);

    default OrcamentoResumoResponse toResponse(OrcamentoResumoOutput output) {
        if (output == null) return null;
        return new OrcamentoResumoResponse(output.id(), output.tipo(), output.versao(), output.status(),
                output.totalServicos(), output.totalItens(), output.totalGeral(), output.criadoEm(),
                output.disponibilizadoEm(), output.aprovadoEm(), output.reprovadoEm(), output.mensagem());
    }

    default HistoricoStatusOsResponse toResponse(HistoricoStatusOsOutput output) {
        return new HistoricoStatusOsResponse(output.status(), output.mensagemCliente(), output.registradoEm());
    }

    default ItemNecessarioResponse toResponse(ItemNecessarioOutput output) {
        return new ItemNecessarioResponse(output.pecaInsumoId(), output.nome(), output.tipo(), output.valorUnitario(),
                output.quantidade(), output.valorTotal(), output.status(), output.motivoPendencia(),
                output.quantidadeDisponivel(), output.mensagemStatus());
    }

    default ServicoOsResponse toResponse(ServicoSolicitadoOutput output) {
        return new ServicoOsResponse(output.id(), output.servicoId(), output.nome(), output.valor(), output.status(),
                output.iniciadoEm(), output.finalizadoEm(), output.itensNecessarios().stream()
                .map(this::toResponse).toList());
    }

}
