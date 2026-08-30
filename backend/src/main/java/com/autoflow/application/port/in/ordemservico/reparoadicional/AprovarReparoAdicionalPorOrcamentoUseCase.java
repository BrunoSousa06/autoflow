package com.autoflow.application.port.in.ordemservico.reparoadicional;

public interface AprovarReparoAdicionalPorOrcamentoUseCase {
    boolean executeSeExistir(Long orcamentoId);
    void executeObrigatorio(Long orcamentoId);
}
