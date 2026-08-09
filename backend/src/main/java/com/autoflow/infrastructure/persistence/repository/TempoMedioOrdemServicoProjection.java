package com.autoflow.infrastructure.persistence.repository;

public interface TempoMedioOrdemServicoProjection {
    Long getQuantidadeOrdensFinalizadas();

    Double getTempoMedioSegundos();
}
