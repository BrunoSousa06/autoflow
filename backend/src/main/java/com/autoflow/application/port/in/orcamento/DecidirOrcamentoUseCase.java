package com.autoflow.application.port.in.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;

public interface DecidirOrcamentoUseCase {
    OrcamentoEntity aprovarComoUsuario(Long orcamentoId, String emailUsuario);
    OrcamentoEntity recusarComoUsuario(Long orcamentoId, String motivo, String emailUsuario);
    OrcamentoEntity aprovarComoToken(Long orcamentoId, String token, String nome);
    OrcamentoEntity recusarComoToken(Long orcamentoId, String token, String motivo, String nome);
    OrcamentoEntity aprovarDaOrdem(Long orcamentoId, String numeroOs);
}
