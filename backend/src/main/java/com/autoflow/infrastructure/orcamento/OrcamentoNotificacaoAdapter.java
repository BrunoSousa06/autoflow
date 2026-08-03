package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.gateway.OrcamentoNotificacaoGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.service.orcamento.OrcamentoNotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrcamentoNotificacaoAdapter implements OrcamentoNotificacaoGateway {

    private final OrcamentoNotificacaoService notificacaoService;

    @Override
    public void notificar(
            OrcamentoEntity orcamento,
            OrdemServicoEntity ordemServico,
            String urlPublica
    ) {
        notificacaoService.enviarLinkOrcamentoParaCliente(orcamento, ordemServico, urlPublica);
    }
}
