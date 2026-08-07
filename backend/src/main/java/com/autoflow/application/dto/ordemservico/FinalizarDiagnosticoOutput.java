package com.autoflow.application.dto.ordemservico;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
public record FinalizarDiagnosticoOutput(OrdemServicoEntity ordemServico, Long orcamentoId, String publicUrl) {}
