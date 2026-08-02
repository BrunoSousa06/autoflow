package com.autoflow.application.gateway;



import com.autoflow.domain.ordemservico.acompanhamento.AcessoAcompanhamento;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AcompanhamentoPublicoGateway {

    void salvar(
            Long ordemServicoId,
            AcessoAcompanhamento acesso
    );

    Optional<DadosAcompanhamentoPublico> buscarPorTokenHash(
            String tokenHash
    );

    record DadosAcompanhamentoPublico(
            String numeroOs,
            com.autoflow.domain.ordemservico.StatusOrdemServico status,
            LocalDateTime dataAbertura,
            LocalDateTime execucaoIniciadaEm,
            LocalDateTime finalizadaEm,
            LocalDateTime entregueEm,
            Long orcamentoId,
            AcessoAcompanhamento acesso
    ) {
    }
}
