package com.autoflow.application.port.in.ordemservico.reparoadicional;

public interface RecusarReparoAdicionalPorOrcamentoUseCase {
    boolean executeSeExistir(Long orcamentoId, String motivo);
}
