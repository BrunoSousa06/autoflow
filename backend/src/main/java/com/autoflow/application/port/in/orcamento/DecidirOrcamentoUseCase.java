package com.autoflow.application.port.in.orcamento;

import com.autoflow.domain.orcamento.Orcamento;

public interface DecidirOrcamentoUseCase {
    Orcamento aprovarComoUsuario(Long orcamentoId, String emailUsuario);
    Orcamento recusarComoUsuario(Long orcamentoId, String motivo, String emailUsuario);
    Orcamento aprovarComoToken(Long orcamentoId, String token, String nome);
    Orcamento recusarComoToken(Long orcamentoId, String token, String motivo, String nome);
    Orcamento aprovarDaOrdem(Long orcamentoId, String numeroOs);
}
