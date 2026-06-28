package com.autoflow.repository.servico;

public interface TempoMedioServicoProjection {
    Long getServicoId();
    String getNomeServico();
    Long getQuantidadeExecucoes();
    Double getTempoMedioSegundos();
}
