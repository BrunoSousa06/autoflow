package com.autoflow.controller.ordemservico.acompanhamento.response;

import com.autoflow.domain.ordemservico.StatusServicoOs;

import java.math.BigDecimal;

public record ServicoAcompanhamentoResponse(
        Long id,
        String nome,
        BigDecimal valor,
        StatusServicoOs status
) {}