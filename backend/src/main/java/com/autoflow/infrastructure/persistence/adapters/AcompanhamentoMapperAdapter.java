package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;
import com.autoflow.application.gateway.AcompanhamentoMapperGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.infrastructure.persistence.mapper.AcompanhamentoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AcompanhamentoMapperAdapter implements AcompanhamentoMapperGateway {

    private final AcompanhamentoMapper mapper;

    @Override
    public AcompanhamentoOrdemServicoOutput mapToOutput(
            OrdemServicoEntity ordemServico,
            OrcamentoEntity orcamento,
            List<HistoricoStatusOsEntity> historico) {
        return mapper.mapToOutPut(ordemServico, orcamento, historico);
    }
}
