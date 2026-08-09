package com.autoflow.presentation.ordemservico.response;

import com.autoflow.domain.ordemservico.DiagnosticoEntity;

import java.time.LocalDateTime;

public record DiagnosticoDetalheResponse(
        Long mecanicoId,
        String mecanicoNome,
        String mecanicoEmail,
        String laudo,
        LocalDateTime iniciadoEm,
        LocalDateTime concluidoEm
) {
    public static DiagnosticoDetalheResponse fromDomain(DiagnosticoEntity diagnostico) {
        if (diagnostico == null) return null;
        var mecanico = diagnostico.getMecanico();
        return new DiagnosticoDetalheResponse(
                mecanico != null ? mecanico.getId() : null,
                mecanico != null ? mecanico.getNome() : null,
                mecanico != null ? mecanico.getEmail() : null,
                diagnostico.getLaudo(),
                diagnostico.getIniciadoEm(),
                diagnostico.getConcluidoEm()
        );
    }
}
