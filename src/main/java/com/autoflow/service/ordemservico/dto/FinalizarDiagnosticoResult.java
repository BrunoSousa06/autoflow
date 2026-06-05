package com.autoflow.service.ordemservico.dto;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;

public record FinalizarDiagnosticoResult(
        OrdemServicoEntity ordemServico,
        Long orcamentoId,
        String publicUrl
) {}