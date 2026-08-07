package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.HistoricoStatusOsOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.OrcamentoResumoOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.ItemNecessarioOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.ServicoSolicitadoOutput;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.presentation.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse;
import com.autoflow.presentation.ordemservico.acompanhamento.response.HistoricoStatusOsResponse;
import com.autoflow.presentation.ordemservico.acompanhamento.response.OrcamentoResumoResponse;
import com.autoflow.controller.ordemservico.response.ItemNecessarioResponse;
import com.autoflow.controller.ordemservico.response.ServicoOsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

    default AcompanhamentoOrdemServicoOutput mapToOutPut(
            OrdemServicoEntity ordemServico,
            OrcamentoEntity orcamentoAtual,
            List<HistoricoStatusOsEntity> historico) {

        return new AcompanhamentoOrdemServicoOutput(
                ordemServico.getNumeroOs(),
                ordemServico.getVeiculo().getPlaca(),
                ordemServico.getStatus(),
                ordemServico.getDataAbertura(),
                ordemServico.getUltimaAtualizacao(),
                ordemServico.getServicosSolicitados().stream()
                        .map(ServicoSolicitadoOutput::from)
                        .toList(),
                orcamentoAtual == null ? null : OrcamentoResumoOutput.from(orcamentoAtual),
                orcamentoAtual == null ? null : orcamentoAtual.getStatus(),
                mensagemParaCliente(ordemServico),
                historico.stream().map(HistoricoStatusOsOutput::from).toList());
    }

    private static String mensagemParaCliente(OrdemServicoEntity ordemServico) {
        return switch (ordemServico.getStatus()) {
            case RECEBIDA -> "Recebemos sua ordem de serviço. Em breve iniciaremos o diagnóstico.";
            case EM_DIAGNOSTICO -> "Seu veículo está em diagnóstico técnico.";
            case AGUARDANDO_APROVACAO -> "O orçamento está disponível e aguardando sua aprovação.";
            case EM_EXECUCAO -> "Os serviços aprovados estão em execução.";
            case FINALIZADA -> "Os serviços foram finalizados. Seu veículo está aguardando entrega.";
            case ENTREGUE -> "Seu veículo foi entregue. Obrigado por utilizar a AutoFlow.";
        };
    }
}
