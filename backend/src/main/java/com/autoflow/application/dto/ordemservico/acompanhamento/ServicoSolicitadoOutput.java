package com.autoflow.application.dto.ordemservico.acompanhamento;

import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.StatusServicoOs;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ServicoSolicitadoOutput(
        Long id,
        Long servicoId,
        String nome,
        BigDecimal valor,
        StatusServicoOs status,
        LocalDateTime iniciadoEm,
        LocalDateTime finalizadoEm,
        List<ItemNecessarioOutput> itensNecessarios
) {
    public static ServicoSolicitadoOutput from(ServicoSolicitadoEntity servico) {
        return new ServicoSolicitadoOutput(
                servico.getId(), servico.getServicoId(), servico.getNome(), servico.getValor(),
                servico.getStatus(), servico.getIniciadoEm(), servico.getFinalizadoEm(),
                servico.getItensNecessarios().stream().map(ItemNecessarioOutput::from).toList());
    }
}
