package com.autoflow.infrastructure.persistence.repository;

public interface TempoMedioServicoProjection {
    Long getServicoId();

    String getNomeServico();

    Long getQuantidadeExecucoes();

    Double getTempoMedioSegundos();
}
