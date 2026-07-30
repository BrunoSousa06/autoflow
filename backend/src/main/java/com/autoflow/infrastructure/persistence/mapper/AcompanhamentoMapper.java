package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.presentation.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AcompanhamentoMapper {

    List<AcompanhamentoOrdemServicoResponse> toResponse(List<AcompanhamentoOrdemServicoOutput> output);
    AcompanhamentoOrdemServicoResponse toResponse(AcompanhamentoOrdemServicoOutput output);

    default AcompanhamentoOrdemServicoOutput mapToOutPut(
            OrdemServicoEntity ordemServico,
            OrcamentoEntity orcamentoAtual,
            List<HistoricoStatusOsEntity> historico) {

        AcompanhamentoOrdemServicoResponse response =
                AcompanhamentoOrdemServicoResponse.from(
                        ordemServico,
                        orcamentoAtual,
                        historico);

        return new AcompanhamentoOrdemServicoOutput(
                response.numeroOs(),
                response.placa(),
                response.statusAtual(),
                response.dataAbertura(),
                response.ultimaAtualizacao(),
                response.servicosSolicitados(),
                response.orcamentoAtual(),
                response.situacaoAprovacao(),
                response.mensagemParaCliente(),
                response.historicoStatus());
    }
}
